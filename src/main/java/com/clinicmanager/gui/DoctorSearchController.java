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
    private boolean showingCustomList = false;

    private final RepositoryManager repos = AppContext.getRepositories();
    private final DoctorRepository doctorRepo = repos.doctors;
    private final ScheduleRepository scheduleRepo = repos.schedules;

    @FXML
    private void initialize() {
        List<Doctor> doctors = doctorRepo.findAll();
        doctorListView.setItems(FXCollections.observableArrayList(doctors));
        showingCustomList = false;
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
        removeFavoriteBtn.setDisable(true);
        removeFavoriteBtn.setOnAction(e -> handleRemoveFavorite());
        scheduleListView.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            if (val != null && selectedDoctor != null) {
                // Find the selected slot based on the rendered string
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
        addFavoriteBtn.setText(isFav ? "Already in favorites" : "Add to favorites");
        removeFavoriteBtn.setDisable(!isFav);
    }

    private void showDoctorInfo(Doctor doc) {
        if (doc == null) {
            doctorInfoLabel.setText("");
            scheduleListView.setItems(FXCollections.emptyObservableList());
            makeAppointmentBtn.setDisable(true);
            return;
        }
        doctorInfoLabel.setText("Name: " + doc.name() + "\nPhone: " + doc.phoneNumber());
        // Fetch the schedule (only free slots)
        List<Slot> slots = repos.slots.findAll();
        List<Slot> doctorSlots = slots.stream().filter(s -> s.scheduleId() == doc.scheduleId() && s.isAvailable())
                .toList();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        scheduleListView.setItems(FXCollections.observableArrayList(
                doctorSlots.stream()
                        .map(s -> s.date().toString() + " " + s.timeRange().start() + "-" + s.timeRange().end())
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
            new Alert(Alert.AlertType.INFORMATION, "Doctor added to favorites!", ButtonType.OK).showAndWait();
        }
    }

    // Allows selecting a doctor programmatically (e.g., from favorites)
    public void selectDoctor(Doctor doctor) {
        if (doctor == null)
            return;
        doctorListView.getSelectionModel().select(doctor);
        showDoctorInfo(doctor);
        updateFavoriteBtn();
    }

    // Allows setting any doctor list to display (e.g., favorites only)
    public void setDoctors(List<Doctor> doctors) {
        showingCustomList = true;
        doctorListView.setItems(FXCollections.observableArrayList(doctors));
        doctorListView.getSelectionModel().clearSelection();
        doctorInfoLabel.setText("");
        scheduleListView.setItems(FXCollections.emptyObservableList());
        makeAppointmentBtn.setDisable(true);
        addFavoriteBtn.setDisable(true);
        removeFavoriteBtn.setDisable(true);
    }

    // Allows removing the selected doctor from favorites (if applicable)
    public void removeFromFavoritesForCurrentPatient() {
        var panel = AppContext.getPanel();
        if (selectedDoctor == null || panel == null
                || !(panel.currentPerson() instanceof com.clinicmanager.model.actors.Patient patient)) {
            return;
        }
        repos.favoriteDoctors.deleteByPatientAndDoctor(patient.id(), selectedDoctor.id());
        updateFavoriteBtn();
    }

    private void handleRemoveFavorite() {
        var panel = AppContext.getPanel();
        if (selectedDoctor == null || panel == null
                || !(panel.currentPerson() instanceof com.clinicmanager.model.actors.Patient patient)) {
            return;
        }
        if (!repos.favoriteDoctors.isFavorite(patient.id(), selectedDoctor.id())) {
            removeFavoriteBtn.setDisable(true);
            addFavoriteBtn.setDisable(false);
            addFavoriteBtn.setText("Add to favorites");
            return;
        }
        repos.favoriteDoctors.deleteByPatientAndDoctor(patient.id(), selectedDoctor.id());
        new Alert(Alert.AlertType.INFORMATION, "Doctor removed from favorites!", ButtonType.OK).showAndWait();
        if (showingCustomList) {
            doctorListView.getItems().remove(selectedDoctor);
            selectedDoctor = null;
            selectedSlot = null;
            doctorInfoLabel.setText("");
            scheduleListView.setItems(FXCollections.emptyObservableList());
            makeAppointmentBtn.setDisable(true);
            addFavoriteBtn.setDisable(true);
            removeFavoriteBtn.setDisable(true);
            doctorListView.getSelectionModel().clearSelection();
        } else {
            updateFavoriteBtn();
        }
    }
}
