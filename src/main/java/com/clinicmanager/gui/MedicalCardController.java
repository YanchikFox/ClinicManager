package com.clinicmanager.gui;

import com.clinicmanager.model.actors.Patient;
import com.clinicmanager.model.entities.MedicalCard;
import com.clinicmanager.model.entities.MedicalRecord;
import com.clinicmanager.repository.RepositoryManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.util.List;

public class MedicalCardController {
    @FXML private Label cardInfoLabel;
    @FXML private ListView<String> recordsListView;

    private final RepositoryManager repos = AppContext.getRepositories();
    private Patient patient;
    private boolean initialized = false;

    public void setPatient(Patient patient) {
        this.patient = patient;
        if (initialized) {
            loadCard();
        }
    }

    @FXML
    private void initialize() {
        initialized = true;
        if (patient != null) {
            loadCard();
        } else {
            // Если пациент не был передан явно, пробуем получить из AppContext
            Object person = AppContext.getPanel().currentPerson();
            if (person instanceof Patient p) {
                patient = p;
                loadCard();
            } else {
                // Если не пациент — ничего не делаем
                cardInfoLabel.setText("Błąd: brak danych pacjenta");
                recordsListView.setItems(FXCollections.observableArrayList());
            }
        }
    }

    private void loadCard() {
        if (patient == null) {
            cardInfoLabel.setText("Błąd: brak данных pacjenta");
            recordsListView.setItems(FXCollections.observableArrayList());
            return;
        }
        MedicalCard card = repos.cards.findById(patient.medicalCardId());
        if (card == null) {
            cardInfoLabel.setText("Brak karty medycznej dla pacjenta: " + patient.name());
            recordsListView.setItems(FXCollections.observableArrayList());
            return;
        }
        cardInfoLabel.setText("ID karty: " + card.id() + " | Pacjent: " + patient.name());
        List<MedicalRecord> records = card.getRecords();
        recordsListView.setItems(FXCollections.observableArrayList(
                records.stream().map(r -> r.date() + ": " + r.description()).toList()
        ));
    }
}
