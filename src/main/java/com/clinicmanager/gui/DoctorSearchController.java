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
    @FXML private ListView<Doctor> doctorListView;
    @FXML private Label doctorInfoLabel;
    @FXML private ListView<String> scheduleListView;
    @FXML private Button makeAppointmentBtn;

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
        });
        makeAppointmentBtn.setOnAction(e -> handleMakeAppointment());
        makeAppointmentBtn.setDisable(true);
        scheduleListView.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            makeAppointmentBtn.setDisable(val == null);
        });
    }

    private void showDoctorInfo(Doctor doc) {
        if (doc == null) {
            doctorInfoLabel.setText("");
            scheduleListView.setItems(FXCollections.emptyObservableList());
            makeAppointmentBtn.setDisable(true);
            return;
        }
        doctorInfoLabel.setText("Имя: " + doc.name() + "\nТелефон: " + doc.phoneNumber());
        // Получаем расписание (все слоты)
        List<Slot> slots = repos.slots.findAll();
        List<Slot> doctorSlots = slots.stream().filter(s -> s.scheduleId() == doc.scheduleId()).toList();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        scheduleListView.setItems(FXCollections.observableArrayList(
            doctorSlots.stream().map(s -> s.date().toString() + " " + s.timeRange().start() + "-" + s.timeRange().end()).toList()
        ));
        makeAppointmentBtn.setDisable(true);
    }

    private void handleMakeAppointment() {
        // TODO: Реализовать создание Appointment для выбранного слота и пациента
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Запись на приём оформлена!", ButtonType.OK);
        alert.showAndWait();
    }
}

