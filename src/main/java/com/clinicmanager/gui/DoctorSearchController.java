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

// Kontroler obsługujący wyszukiwanie lekarzy i rezerwację wizyt
public class DoctorSearchController {
    @FXML
    private ListView<Doctor> doctorListView;
    @FXML
    private Label doctorInfoLabel;
    @FXML
    private ListView<String> scheduleListView;
    @FXML
    private Button makeAppointmentBtn;

    private Doctor selectedDoctor;
    private Slot selectedSlot;

    // Dostęp do repozytoriów przez AppContext (wzorzec singleton)
    private final RepositoryManager repos = AppContext.getRepositories();
    private final DoctorRepository doctorRepo = repos.doctors;
    private final ScheduleRepository scheduleRepo = repos.schedules;

    @FXML
    private void initialize() {
        // Pobieranie wszystkich lekarzy i wyświetlanie na liście
        List<Doctor> doctors = doctorRepo.findAll();
        doctorListView.setItems(FXCollections.observableArrayList(doctors));
        doctorListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Doctor item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : item.name());
            }
        });
        // Obsługa wyboru lekarza z listy
        doctorListView.getSelectionModel().selectedItemProperty().addListener((obs, old, doc) -> {
            selectedDoctor = doc;
            showDoctorInfo(doc);
        });
        // Obsługa przycisku rezerwacji wizyty
        makeAppointmentBtn.setOnAction(e -> handleMakeAppointment());
        makeAppointmentBtn.setDisable(true);
        // Obsługa wyboru slotu w grafiku lekarza
        scheduleListView.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            if (val != null && selectedDoctor != null) {
                // Wyszukiwanie dostępnych slotów dla wybranego lekarza
                List<Slot> slots = repos.slots.findAll();
                List<Slot> doctorSlots = slots.stream().filter(s -> s.scheduleId() == selectedDoctor.scheduleId() && s.isAvailable()).toList();
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

    // Wyświetlanie informacji o wybranym lekarzu oraz jego dostępnych terminach
    private void showDoctorInfo(Doctor doc) {
        if (doc == null) {
            doctorInfoLabel.setText("");
            scheduleListView.setItems(FXCollections.emptyObservableList());
            makeAppointmentBtn.setDisable(true);
            return;
        }
        doctorInfoLabel.setText("Imię: " + doc.name() + "\nTelefon: " + doc.phoneNumber());
        // Pobieranie dostępnych slotów dla lekarza
        List<Slot> slots = repos.slots.findAll();
        List<Slot> doctorSlots = slots.stream().filter(s -> s.scheduleId() == doc.scheduleId() && s.isAvailable()).toList();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        scheduleListView.setItems(FXCollections.observableArrayList(
                doctorSlots.stream().map(s -> s.date().toString() + " " + s.timeRange().start() + "-" + s.timeRange().end()).toList()
        ));
        // Czyszczenie wyboru slotu i blokowanie przycisku rezerwacji
        scheduleListView.getSelectionModel().clearSelection();
        makeAppointmentBtn.setDisable(true);
    }

    // Obsługa procesu rezerwacji wizyty
    private void handleMakeAppointment() {
        if (selectedDoctor == null || selectedSlot == null) return;
        // Pobranie aktualnie zalogowanego pacjenta
        var panel = AppContext.getPanel();
        if (panel == null || !(panel.currentPerson() instanceof com.clinicmanager.model.actors.Patient patient)) {
            // Wyświetlenie błędu, jeśli nie znaleziono pacjenta
            new Alert(Alert.AlertType.ERROR, "Błąd: nie znaleziono bieżącego pacjenta", ButtonType.OK).showAndWait();
            return;
        }
        // Sprawdzenie, czy pacjent nie jest już zapisany do tego lekarza na ten dzień
        if (!repos.appointments.canPatientBookSlot(patient.id(), selectedDoctor.id(), selectedSlot.date())) {
            new Alert(Alert.AlertType.WARNING, "Już jesteś zapisany do tego lekarza na wybrany dzień!", ButtonType.OK).showAndWait();
            return;
        }
        // Tworzenie nowej wizyty i zapis do bazy
        var appointment = new com.clinicmanager.model.entities.Appointment(
                -1,
                patient.id(),
                selectedDoctor.id(),
                selectedSlot.id(),
                com.clinicmanager.model.enums.AppointmentStatus.PENDING
        );
        repos.appointments.save(appointment);
        // Odświeżenie listy dostępnych slotów
        showDoctorInfo(selectedDoctor);
        new Alert(Alert.AlertType.INFORMATION, "Wizyta została umówiona!", ButtonType.OK).showAndWait();
    }
}

