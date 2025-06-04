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

    private Doctor selectedDoctor;
    private Slot selectedSlot;

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
        addFavoriteBtn.setOnAction(e -> handleAddFavorite());
        scheduleListView.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            if (val != null && selectedDoctor != null) {
                // Znajdź wybrany slot po stringu
                List<Slot> slots = repos.slots.findAll();
                List<Slot> doctorSlots = slots.stream()
                        .filter(s -> s.scheduleId() == selectedDoctor.scheduleId() && s.isAvailable()).toList();
                int idx = scheduleListView.getSelectionModel().getSelectedIndex();
                if (idx >= 0 && idx < doctorSlots.size()) {
                    selectedSlot = doctorSlots.get(idx);
                    makeAppointmentBtn.setDisable(false);
                } else {
                    selectedSlot = null;
                    makeAppointmentBtn.setDisable(true);
                }
            } else {
                selectedSlot = null;
                makeAppointmentBtn.setDisable(true);
            }
        });
    }

    private void updateFavoriteBtn() {
        var panel = AppContext.getPanel();
        if (selectedDoctor == null || panel == null
                || !(panel.currentPerson() instanceof com.clinicmanager.model.actors.Patient patient)) {
            addFavoriteBtn.setDisable(true);
            return;
        }
        boolean isFav = repos.favoriteDoctors.isFavorite(patient.id(), selectedDoctor.id());
        addFavoriteBtn.setDisable(isFav);
        addFavoriteBtn.setText(isFav ? "Już w ulubionych" : "Dodaj do ulubionych");
    }

    private void showDoctorInfo(Doctor doc) {
        if (doc == null) {
            doctorInfoLabel.setText("");
            scheduleListView.setItems(FXCollections.emptyObservableList());
            makeAppointmentBtn.setDisable(true);
            return;
        }
        doctorInfoLabel.setText("Imię: " + doc.name() + "\nTelefon: " + doc.phoneNumber());
        // Pobierz grafik (tylko wolne sloty)
        List<Slot> slots = repos.slots.findAll();
        List<Slot> doctorSlots = slots.stream().filter(s -> s.scheduleId() == doc.scheduleId() && s.isAvailable())
                .toList();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        scheduleListView.setItems(FXCollections.observableArrayList(
                doctorSlots.stream()
                        .map(s -> s.date().toString() + " " + s.timeRange().start() + "-" + s.timeRange().end())
                        .toList()));
        // Zapisz listę slotów do wyboru
        scheduleListView.getSelectionModel().clearSelection();
        makeAppointmentBtn.setDisable(true);
    }

    private void handleMakeAppointment() {
        if (selectedDoctor == null || selectedSlot == null)
            return;
        // Pobierz aktualnego pacjenta
        var panel = AppContext.getPanel();
        if (panel == null || !(panel.currentPerson() instanceof com.clinicmanager.model.actors.Patient patient)) {
            new Alert(Alert.AlertType.ERROR, "Błąd: nie znaleziono bieżącego pacjenta", ButtonType.OK).showAndWait();
            return;
        }
        // Sprawdzenie: nie można umówić się do tego samego lekarza więcej niż raz
        // dziennie
        if (!repos.appointments.canPatientBookSlot(patient.id(), selectedDoctor.id(), selectedSlot.date())) {
            new Alert(Alert.AlertType.WARNING, "Już jesteś zapisany do tego lekarza na wybrany dzień!", ButtonType.OK)
                    .showAndWait();
            return;
        }
        // Poproś pacjenta o opis problemu
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Opis problemu");
        dialog.setHeaderText("Podaj krótki opis problemu lub powodu wizyty:");
        dialog.setContentText("Opis:");
        var result = dialog.showAndWait();
        if (result.isEmpty() || result.get().isBlank()) {
            new Alert(Alert.AlertType.WARNING, "Musisz podać opis problemu!", ButtonType.OK).showAndWait();
            return;
        }
        String problemDescription = result.get();
        // Utwórz Appointment
        var appointment = new com.clinicmanager.model.entities.Appointment(
                -1,
                patient.id(),
                selectedDoctor.id(),
                selectedSlot.id(),
                com.clinicmanager.model.enums.AppointmentStatus.PENDING,
                problemDescription);
        repos.appointments.save(appointment);
        // Odśwież listę slotów
        showDoctorInfo(selectedDoctor);
        new Alert(Alert.AlertType.INFORMATION, "Wizyta została umówiona!", ButtonType.OK).showAndWait();
    }

    private void handleAddFavorite() {
        var panel = AppContext.getPanel();
        if (selectedDoctor == null || panel == null
                || !(panel.currentPerson() instanceof com.clinicmanager.model.actors.Patient patient)) {
            return;
        }
        if (!repos.favoriteDoctors.isFavorite(patient.id(), selectedDoctor.id())) {
            repos.favoriteDoctors
                    .save(new com.clinicmanager.model.entities.FavoriteDoctor(-1, patient.id(), selectedDoctor.id()));
            updateFavoriteBtn();
            new Alert(Alert.AlertType.INFORMATION, "Lekarz dodany do ulubionych!", ButtonType.OK).showAndWait();
        }
    }

    // Pozwala wybrać lekarza programowo (np. z ulubionych)
    public void selectDoctor(Doctor doctor) {
        if (doctor == null)
            return;
        doctorListView.getSelectionModel().select(doctor);
        showDoctorInfo(doctor);
        updateFavoriteBtn();
    }

    // Pozwala ustawić dowolną listę lekarzy do wyświetlenia (np. tylko ulubionych)
    public void setDoctors(List<Doctor> doctors) {
        doctorListView.setItems(FXCollections.observableArrayList(doctors));
        doctorListView.getSelectionModel().clearSelection();
        doctorInfoLabel.setText("");
        scheduleListView.setItems(FXCollections.emptyObservableList());
        makeAppointmentBtn.setDisable(true);
        addFavoriteBtn.setDisable(true);
    }

    // Pozwala usunąć wybranego lekarza z ulubionych (jeśli jest)
    public void removeFromFavoritesForCurrentPatient() {
        var panel = AppContext.getPanel();
        if (selectedDoctor == null || panel == null
                || !(panel.currentPerson() instanceof com.clinicmanager.model.actors.Patient patient)) {
            return;
        }
        repos.favoriteDoctors.deleteByPatientAndDoctor(patient.id(), selectedDoctor.id());
        updateFavoriteBtn();
    }
}
