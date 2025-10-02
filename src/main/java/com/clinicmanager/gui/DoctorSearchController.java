package com.clinicmanager.gui;

import com.clinicmanager.model.actors.Doctor;
import com.clinicmanager.model.entities.Slot;
import com.clinicmanager.repository.DoctorRepository;
import com.clinicmanager.repository.RepositoryManager;
import com.clinicmanager.repository.ScheduleRepository;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class DoctorSearchController {
    @FXML
    private ListView<Doctor> doctorListView;
    @FXML
    private Label doctorInfoLabel;
    @FXML
    private ListView<String> scheduleListView;
    @FXML
    private Button makeAppointmentBtn;
    @FXML
    private Button addFavoriteBtn;
    @FXML
    private Button removeFavoriteBtn;

    private Doctor selectedDoctor;
    private Slot selectedSlot;
    private List<Slot> currentDoctorSlots = List.of();

    private final RepositoryManager repos = AppContext.getRepositories();
    private final DoctorRepository doctorRepo = repos.doctors;
    private final ScheduleRepository scheduleRepo = repos.schedules;

    @FXML
    private void initialize() {
        List<Doctor> doctors = doctorRepo.findAll();
        doctorListView.setItems(FXCollections.observableArrayList(doctors));
        doctorListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Doctor item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : item.name());
            }
        });
        doctorListView.getSelectionModel().selectedItemProperty().addListener((obs, old, doc) -> {
            selectedDoctor = doc;
            showDoctorInfo(doc);
            updateFavoriteBtn();
        });
        makeAppointmentBtn.setOnAction(e -> handleMakeAppointment());
        makeAppointmentBtn.setDisable(true);
        addFavoriteBtn.setDisable(true);
        addFavoriteBtn.setOnAction(e -> handleToggleFavorite());
        scheduleListView.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            if (val != null && selectedDoctor != null) {
                int idx = scheduleListView.getSelectionModel().getSelectedIndex();
                if (idx >= 0 && idx < currentDoctorSlots.size()) {
                    selectedSlot = currentDoctorSlots.get(idx);
                    makeAppointmentBtn.setDisable(false);
                    return;
                }
            }
            selectedSlot = null;
            makeAppointmentBtn.setDisable(true);
        });
    }


    private void updateFavoriteBtn() {
        var panel = AppContext.getPanel();
        if (selectedDoctor == null || panel == null
                || !(panel.currentPerson() instanceof com.clinicmanager.model.actors.Patient patient)) {
            addFavoriteBtn.setDisable(true);
            addFavoriteBtn.setText("Add to favorites");
            return;
        }
        boolean isFav = repos.favoriteDoctors.isFavorite(patient.id(), selectedDoctor.id());
        addFavoriteBtn.setDisable(false);
        addFavoriteBtn.setText(isFav ? "Remove from favorites" : "Add to favorites");
    }

    private void showDoctorInfo(Doctor doc) {
        if (doc == null) {
            doctorInfoLabel.setText("");
            scheduleListView.setItems(FXCollections.emptyObservableList());
            makeAppointmentBtn.setDisable(true);
            currentDoctorSlots = List.of();
            return;
        }
        doctorInfoLabel.setText("Name: " + doc.name() + "\nPhone: " + doc.phoneNumber());
        // Fetch the schedule (only free slots)
        List<Slot> slots = repos.slots.findAll();
        List<Slot> doctorSlots = slots.stream().filter(s -> s.scheduleId() == doc.scheduleId() && s.isAvailable())
                .toList();
        currentDoctorSlots = doctorSlots;
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        scheduleListView.setItems(FXCollections.observableArrayList(
                doctorSlots.stream()
                        .map(s -> dateFmt.format(s.date()) + " " + s.timeRange().start() + "-" + s.timeRange().end())
                        .toList()));
        // Save the slot list for selection
        scheduleListView.getSelectionModel().clearSelection();
        makeAppointmentBtn.setDisable(true);
    }

    private void handleMakeAppointment() {
        if (selectedDoctor == null || selectedSlot == null)
            return;
        // Pobierz aktualnego pacjenta
        var panel = AppContext.getPanel();
        if (panel == null || !(panel.currentPerson() instanceof com.clinicmanager.model.actors.Patient patient)) {
            new Alert(Alert.AlertType.ERROR, "Error: current patient not found", ButtonType.OK).showAndWait();
            return;
        }
        // Validation: a patient cannot book the same doctor more than once per day
        if (!repos.appointments.canPatientBookSlot(patient.id(), selectedDoctor.id(), selectedSlot.date())) {
            new Alert(Alert.AlertType.WARNING, "You are already booked with this doctor for the selected day!", ButtonType.OK)
                    .showAndWait();
            return;
        }
        // Ask the patient for a problem description
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Problem description");
        dialog.setHeaderText("Provide a short description of the problem or reason for the appointment:");
        dialog.setContentText("Description:");
        var result = dialog.showAndWait();
        if (result.isEmpty() || result.get().isBlank()) {
            new Alert(Alert.AlertType.WARNING, "You must provide a problem description!", ButtonType.OK).showAndWait();
            return;
        }
        String problemDescription = result.get();
        // Create the appointment
        var appointment = new com.clinicmanager.model.entities.Appointment(
                -1,
                patient.id(),
                selectedDoctor.id(),
                selectedSlot.id(),
                com.clinicmanager.model.enums.AppointmentStatus.PENDING,
                problemDescription);
        repos.appointments.save(appointment);
        // Refresh the slot list
        showDoctorInfo(selectedDoctor);
        new Alert(Alert.AlertType.INFORMATION, "Appointment booked!", ButtonType.OK).showAndWait();
    }

    private void handleToggleFavorite() {
        var panel = AppContext.getPanel();
        if (selectedDoctor == null || panel == null
                || !(panel.currentPerson() instanceof com.clinicmanager.model.actors.Patient patient)) {
            return;
        }
        boolean isFav = repos.favoriteDoctors.isFavorite(patient.id(), selectedDoctor.id());
        if (isFav) {
            repos.favoriteDoctors.deleteByPatientAndDoctor(patient.id(), selectedDoctor.id());
            new Alert(Alert.AlertType.INFORMATION, "Doctor removed from favorites!", ButtonType.OK).showAndWait();
        } else {
            repos.favoriteDoctors
                    .save(new com.clinicmanager.model.entities.FavoriteDoctor(-1, patient.id(), selectedDoctor.id()));
            new Alert(Alert.AlertType.INFORMATION, "Doctor added to favorites!", ButtonType.OK).showAndWait();
        }
        updateFavoriteBtn();
    }

    // Allows setting any doctor list to display (e.g., favorites only)
    public void setDoctors(List<Doctor> doctors) {
        doctorListView.setItems(FXCollections.observableArrayList(doctors));
        doctorListView.getSelectionModel().clearSelection();
        doctorInfoLabel.setText("");
        scheduleListView.setItems(FXCollections.emptyObservableList());
        makeAppointmentBtn.setDisable(true);
        addFavoriteBtn.setDisable(true);
        addFavoriteBtn.setText("Add to favorites");
        currentDoctorSlots = List.of();
    }
}
