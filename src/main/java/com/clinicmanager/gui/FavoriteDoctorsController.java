package com.clinicmanager.gui;

import com.clinicmanager.model.actors.Doctor;
import com.clinicmanager.model.actors.Patient;
import com.clinicmanager.model.entities.FavoriteDoctor;
import com.clinicmanager.repository.RepositoryManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.List;
import java.util.stream.Collectors;

public class FavoriteDoctorsController {
    @FXML private ListView<Doctor> favoriteDoctorsListView;
    @FXML private Button showDoctorBtn;
    @FXML private Button removeFavoriteBtn;
    @FXML private Button closeBtn;

    private final RepositoryManager repos = AppContext.getRepositories();
    private Patient currentPatient;
    private Doctor selectedDoctor;

    @FXML
    private void initialize() {
        var panel = AppContext.getPanel();
        if (panel == null || !(panel.currentPerson() instanceof Patient patient)) {
            showDoctorBtn.setDisable(true);
            removeFavoriteBtn.setDisable(true);
            return;
        }
        this.currentPatient = patient;
        loadFavoriteDoctors();
        favoriteDoctorsListView.getSelectionModel().selectedItemProperty().addListener((obs, old, doc) -> {
            selectedDoctor = doc;
            boolean hasSelection = doc != null;
            showDoctorBtn.setDisable(!hasSelection);
            removeFavoriteBtn.setDisable(!hasSelection);
        });
        showDoctorBtn.setDisable(true);
        removeFavoriteBtn.setDisable(true);
        showDoctorBtn.setOnAction(e -> handleShowDoctor());
        removeFavoriteBtn.setOnAction(e -> handleRemoveFavorite());
        closeBtn.setOnAction(e -> ((Stage) closeBtn.getScene().getWindow()).close());
    }

    private void loadFavoriteDoctors() {
        List<FavoriteDoctor> favs = repos.favoriteDoctors.findByPatientId(currentPatient.id());
        List<Doctor> doctors = favs.stream()
                .map(fav -> repos.doctors.findById(fav.doctorId()))
                .filter(d -> d != null)
                .collect(Collectors.toList());
        favoriteDoctorsListView.setItems(FXCollections.observableArrayList(doctors));
        favoriteDoctorsListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Doctor item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : item.name());
            }
        });
    }

    private void handleShowDoctor() {
        if (selectedDoctor == null) return;
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/gui/doctor_search.fxml"));
            javafx.scene.Parent root = loader.load();
            DoctorSearchController controller = loader.getController();
            // Передаём выбранного врача в контроллер поиска
            controller.selectDoctor(selectedDoctor);
            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Szczegóły lekarza");
            stage.setScene(scene);
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void handleRemoveFavorite() {
        if (selectedDoctor == null) return;
        repos.favoriteDoctors.deleteByPatientAndDoctor(currentPatient.id(), selectedDoctor.id());
        loadFavoriteDoctors();
        showDoctorBtn.setDisable(true);
        removeFavoriteBtn.setDisable(true);
    }
}

