package com.clinicmanager.gui;

import com.clinicmanager.controller.DoctorControlPanel;
import com.clinicmanager.model.actors.Doctor;
import com.clinicmanager.model.entities.Slot;
import com.clinicmanager.repository.RepositoryManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.clinicmanager.gui.AppContext;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class DoctorScheduleController {
    @FXML
    private TableView<SlotTableRow> slotsTable;
    @FXML
    private TableColumn<SlotTableRow, String> dateCol;
    @FXML
    private TableColumn<SlotTableRow, String> startCol;
    @FXML
    private TableColumn<SlotTableRow, String> endCol;
    @FXML
    private TableColumn<SlotTableRow, String> statusCol;
    @FXML
    private Button addSlotBtn;
    @FXML
    private Button removeSlotBtn;
    @FXML
    private Button closeSlotBtn;
    @FXML
    private Button openSlotBtn;
    @FXML
    private Button refreshBtn;
    @FXML
    private Button closeBtn;

    private SlotTableRow selectedSlotRow;

    @FXML
    private void initialize() {
        closeBtn.setOnAction(e -> ((Stage) closeBtn.getScene().getWindow()).close());
        setupTable();
        loadSlots();
        refreshBtn.setOnAction(e -> loadSlots());
        addSlotBtn.setOnAction(e -> handleAddSlot());
        removeSlotBtn.setOnAction(e -> handleRemoveSlot());
        closeSlotBtn.setOnAction(e -> handleCloseSlot());
        openSlotBtn.setOnAction(e -> handleOpenSlot());
        slotsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            selectedSlotRow = newSel;
            updateSlotButtons();
        });
        updateSlotButtons();
    }

    private void setupTable() {
        slotsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        startCol.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        endCol.setCellValueFactory(new PropertyValueFactory<>("endTime"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    // --- Публичный метод для обновления слотов (для глобального обновления UI) ---
    public void loadSlots() {
        loadSlotsImpl();
    }

    // Старая приватная реализация
    private void loadSlotsImpl() {
        var panel = AppContext.getPanel();
        if (!(panel instanceof DoctorControlPanel doctorPanel)) return;
        Doctor doctor = (Doctor) doctorPanel.currentPerson();
        RepositoryManager repos = AppContext.getRepositories();
        List<com.clinicmanager.model.entities.Slot> allSlots = repos.slots.findAll().stream()
                .filter(s -> s.scheduleId() == doctor.scheduleId())
                .toList();
        ObservableList<SlotTableRow> rows = FXCollections.observableArrayList();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (var slot : allSlots) {
            String status = slot.isAvailable() ? "wolny" : "zajęty";
            rows.add(new SlotTableRow(
                    slot.date().format(fmt),
                    slot.timeRange().start().toString(),
                    slot.timeRange().end().toString(),
                    status
            ));
        }
        slotsTable.setItems(rows);
    }

    private void updateSlotButtons() {
        if (selectedSlotRow == null) {
            removeSlotBtn.setDisable(true);
            closeSlotBtn.setDisable(true);
            openSlotBtn.setDisable(true);
            return;
        }
        var slot = findSlotByRow(selectedSlotRow);
        if (slot == null) {
            removeSlotBtn.setDisable(true);
            closeSlotBtn.setDisable(true);
            openSlotBtn.setDisable(true);
            return;
        }
        boolean hasRealAppointment = hasRealPatientAppointment(slot);
        boolean isClosedByDoctor = isClosedByDoctor(slot);
        // Удалять можно только если нет записей вообще
        removeSlotBtn.setDisable(hasRealAppointment || isClosedByDoctor);
        // Закрыть можно только если слот свободен и нет записей пациента, и он не закрыт вручную
        closeSlotBtn.setDisable(hasRealAppointment || isClosedByDoctor);
        // Открыть можно только если слот закрыт вручную (есть ENDED appointment с patientId=-1)
        openSlotBtn.setDisable(!isClosedByDoctor);
    }

    private boolean hasRealPatientAppointment(Slot slot) {
        return AppContext.getRepositories().appointments.findAll().stream()
            .anyMatch(a -> a.slotId() == slot.id() && a.patientId() != -1 && !a.status().name().equals("CANCELLED"));
    }

    private boolean isClosedByDoctor(Slot slot) {
        return AppContext.getRepositories().appointments.findAll().stream()
            .anyMatch(a -> a.slotId() == slot.id() && a.patientId() == -1 && a.status().name().equals("ENDED"));
    }

    private void handleCloseSlot() {
        if (selectedSlotRow == null) return;
        var slot = findSlotByRow(selectedSlotRow);
        if (slot == null) return;
        if (hasRealPatientAppointment(slot) || isClosedByDoctor(slot)) return;
        // Закрываем слот: создаём ENDED appointment с patientId = -1
        var doctor = (Doctor) ((DoctorControlPanel) AppContext.getPanel()).currentPerson();
        var app = new com.clinicmanager.model.entities.Appointment(-1, -1, doctor.id(), slot.id(), com.clinicmanager.model.enums.AppointmentStatus.ENDED);
        AppContext.getRepositories().appointments.save(app);
        loadSlots();
        updateSlotButtons();
    }

    private void handleOpenSlot() {
        if (selectedSlotRow == null) return;
        var slot = findSlotByRow(selectedSlotRow);
        if (slot == null) return;
        // Открываем слот: удаляем ENDED appointment с patientId = -1
        var repo = AppContext.getRepositories().appointments;
        var toDelete = repo.findAll().stream()
            .filter(a -> a.slotId() == slot.id() && a.patientId() == -1 && a.status().name().equals("ENDED"))
            .findFirst().orElse(null);
        if (toDelete != null) {
            repo.delete(toDelete);
            loadSlots();
            updateSlotButtons();
        }
    }

    private Slot findSlotByRow(SlotTableRow row) {
        var panel = AppContext.getPanel();
        if (!(panel instanceof DoctorControlPanel doctorPanel)) return null;
        Doctor doctor = (Doctor) doctorPanel.currentPerson();
        RepositoryManager repos = AppContext.getRepositories();
        return repos.slots.findAll().stream()
            .filter(s -> s.scheduleId() == doctor.scheduleId()
                && s.date().toString().equals(row.getDate())
                && s.timeRange().start().toString().equals(row.getStartTime())
                && s.timeRange().end().toString().equals(row.getEndTime()))
            .findFirst().orElse(null);
    }

    private void handleAddSlot() {
        var panel = AppContext.getPanel();
        if (!(panel instanceof DoctorControlPanel doctorPanel)) return;
        Doctor doctor = (Doctor) doctorPanel.currentPerson();
        Dialog<Slot> dialog = new Dialog<>();
        dialog.setTitle("Dodaj slot");
        DatePicker datePicker = new DatePicker();
        TextField startField = new TextField();
        startField.setPromptText("Godzina od (np. 09:00)");
        TextField endField = new TextField();
        endField.setPromptText("Godzina do (np. 10:00)");
        VBox vbox = new VBox(10, new Label("Data:"), datePicker, new Label("Godzina od:"), startField, new Label("Godzina do:"), endField);
        dialog.getDialogPane().setContent(vbox);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    var date = datePicker.getValue();
                    var start = java.time.LocalTime.parse(startField.getText());
                    var end = java.time.LocalTime.parse(endField.getText());
                    var range = new com.clinicmanager.model.entities.TimeRange(start, end);
                    return new com.clinicmanager.model.entities.Slot(doctor.scheduleId(), date, range);
                } catch (Exception ex) {
                    return null;
                }
            }
            return null;
        });
        dialog.showAndWait().ifPresent(slot -> {
            if (slot != null) {
                // Проверка на пересечение по времени и дате
                RepositoryManager repos = AppContext.getRepositories();
                boolean intersects = repos.slots.findAll().stream()
                    .filter(s -> s.scheduleId() == doctor.scheduleId() && s.date().equals(slot.date()))
                    .anyMatch(s -> timesOverlap(s.timeRange(), slot.timeRange()));
                if (intersects) {
                    new Alert(Alert.AlertType.ERROR, "Slot koliduje z istniejącym!", ButtonType.OK).showAndWait();
                } else {
                    repos.slots.save(slot);
                    loadSlots();
                }
            }
        });
    }

    private boolean timesOverlap(com.clinicmanager.model.entities.TimeRange t1, com.clinicmanager.model.entities.TimeRange t2) {
        return !t1.end().isBefore(t2.start()) && !t2.end().isBefore(t1.start());
    }

    private void handleRemoveSlot() {
        if (selectedSlotRow == null) return;
        var slot = findSlotByRow(selectedSlotRow);
        if (slot != null && slot.isAvailable()) {
            AppContext.getRepositories().slots.delete(slot);
            loadSlots();
        }
    }

    public static class SlotTableRow {
        private final String date;
        private final String startTime;
        private final String endTime;
        private final String status;

        public SlotTableRow(String date, String startTime, String endTime, String status) {
            this.date = date;
            this.startTime = startTime;
            this.endTime = endTime;
            this.status = status;
        }

        public String getDate() {
            return date;
        }

        public String getStartTime() {
            return startTime;
        }

        public String getEndTime() {
            return endTime;
        }

        public String getStatus() {
            return status;
        }
    }
}
