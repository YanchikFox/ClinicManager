package com.clinicmanager.gui;

import com.clinicmanager.app.PanelManager;
import com.clinicmanager.model.actors.Patient;
import com.clinicmanager.model.entities.MedicalCard;
import com.clinicmanager.model.entities.MedicalRecord;
import com.clinicmanager.repository.Repositories;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.util.List;

public class MedicalCardController {
    @FXML
    private Label cardInfoLabel;
    @FXML
    private ListView<String> recordsListView;

    private final PanelManager panelManager;
    private final Repositories repositories;
    private Patient patient;
    private boolean initialized = false;

    public MedicalCardController(PanelManager panelManager, Repositories repositories) {
        this.panelManager = panelManager;
        this.repositories = repositories;
    }

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
            // If the patient was not provided explicitly, attempt to retrieve it from the current panel
            Object person = panelManager.getCurrentPanel().currentPerson();
            if (person instanceof Patient p) {
                patient = p;
                loadCard();
            } else {
                // If no patient is available, leave the view empty
                cardInfoLabel.setText("Error: missing patient data");
                recordsListView.setItems(FXCollections.observableArrayList());
            }
        }
    }

    private void loadCard() {
        if (patient == null) {
            cardInfoLabel.setText("Error: missing patient data");
            recordsListView.setItems(FXCollections.observableArrayList());
            return;
        }
        MedicalCard card = repositories.medicalCards().findById(patient.medicalCardId());
        if (card == null) {
            cardInfoLabel.setText("No medical card for patient: " + patient.name());
            recordsListView.setItems(FXCollections.observableArrayList());
            return;
        }
        cardInfoLabel.setText("Card ID: " + card.id() + " | Patient: " + patient.name());
        List<MedicalRecord> records = card.getRecords(repositories.medicalRecords());
        recordsListView.setItems(FXCollections.observableArrayList(
                records.stream().map(r -> r.date() + ": " + r.description()).toList()));
    }
}
