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

    @FXML
    private void initialize() {
        // Получаем текущего пациента
        Patient patient = (Patient) AppContext.getPanel().currentPerson();
        MedicalCard card = repos.cards.findById(patient.medicalCardId());
        cardInfoLabel.setText("ID karty: " + card.id() + " | Pacjent: " + patient.name());
        List<MedicalRecord> records = repos.records.findAll().stream()
                .filter(r -> r.medicalCardId() == card.id())
                .toList();
        recordsListView.setItems(FXCollections.observableArrayList(
                records.stream().map(r -> r.date() + ": " + r.description()).toList()
        ));
    }
}

