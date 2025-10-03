package com.clinicmanager.gui;

import com.clinicmanager.app.PanelManager;
import com.clinicmanager.model.actors.Doctor;
import com.clinicmanager.model.actors.Patient;
import com.clinicmanager.model.entities.Appointment;
import com.clinicmanager.model.entities.Slot;
import com.clinicmanager.repository.Repositories;
import com.clinicmanager.service.NotificationService;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class PatientAppointmentsController {
  @FXML private Label infoLabel;
  @FXML private ListView<String> appointmentsListView;
  @FXML private Button cancelBtn;
  @FXML private Button rescheduleBtn;
  @FXML private Button confirmBtn;

  private final PanelManager panelManager;
  private final Repositories repositories;
  private final NotificationService notificationManager;
  private List<Appointment> myAppointments;
  private Appointment selectedAppointment;

  public PatientAppointmentsController(
      PanelManager panelManager,
      Repositories repositories,
      NotificationService notificationManager) {
    this.panelManager = panelManager;
    this.repositories = repositories;
    this.notificationManager = notificationManager;
  }

  @FXML
  private void initialize() {
    reloadAppointments();
    appointmentsListView
        .getSelectionModel()
        .selectedIndexProperty()
        .addListener(
            (obs, old, idx) -> {
              if (idx != null && idx.intValue() >= 0 && idx.intValue() < myAppointments.size()) {
                selectedAppointment = myAppointments.get(idx.intValue());
                cancelBtn.setDisable(false);
                rescheduleBtn.setDisable(false);
                confirmBtn.setDisable(false);
              } else {
                selectedAppointment = null;
                cancelBtn.setDisable(true);
                rescheduleBtn.setDisable(true);
                confirmBtn.setDisable(true);
              }
            });
    cancelBtn.setOnAction(e -> handleCancel());
    rescheduleBtn.setOnAction(e -> handleReschedule());
    confirmBtn.setOnAction(e -> handleConfirm());
    cancelBtn.setDisable(true);
    rescheduleBtn.setDisable(true);
    confirmBtn.setDisable(true);
    javafx.application.Platform.runLater(
        () -> {
          if (appointmentsListView.getScene() != null
              && appointmentsListView.getScene().getRoot() != null) {
            appointmentsListView.getScene().getRoot().getProperties().put("fx:controller", this);
          }
        });
  }

  public void reloadAppointments() {
    Patient patient = (Patient) panelManager.getCurrentPanel().currentPerson();
    List<Appointment> all = repositories.appointments().findAll();
    myAppointments =
        all.stream()
            .filter(
                a ->
                    a.patientId() == patient.id()
                        && (a.status().name().equals("CONFIRMED")
                            || a.status().name().equals("PENDING")))
            .toList();
    updateList();
  }

  private void updateList() {
    List<String> display =
        myAppointments.stream()
            .map(
                a -> {
                  Doctor doc = a.getDoctor(repositories.doctors());
                  Slot slot = a.getSlot(repositories.slots());
                  String doctorName = (doc != null) ? doc.name() : "?";
                  String date = (slot != null) ? slot.date().toString() : "?";
                  String timeStart = (slot != null) ? slot.timeRange().start().toString() : "?";
                  String timeEnd = (slot != null) ? slot.timeRange().end().toString() : "?";
                  return String.format(
                      "Doctor: %s | Date: %s | Time: %s-%s | Status: %s",
                      doctorName, date, timeStart, timeEnd, a.status().name());
                })
            .toList();
    infoLabel.setText("Your active appointments:");
    appointmentsListView.setItems(FXCollections.observableArrayList(display));
  }

  private void handleCancel() {
    if (selectedAppointment != null) {
      selectedAppointment.cancel(repositories.appointments());
      notificationManager.createNotification(
          selectedAppointment.patientId(), "Your appointment has been cancelled by the user.");
      reloadAppointments();
    }
  }

  private void handleReschedule() {
    if (selectedAppointment != null) {
      List<Slot> freeSlots =
          repositories.slots().findAll().stream()
              .filter(
                  s ->
                      s.scheduleId()
                              == selectedAppointment.getDoctor(repositories.doctors()).scheduleId()
                          && s.isAvailable(repositories.appointments()))
              .toList();
      if (freeSlots.isEmpty()) {
        new Alert(Alert.AlertType.WARNING, "No available slots to reschedule.", ButtonType.OK)
            .showAndWait();
        return;
      }
      ChoiceDialog<Slot> dialog = new ChoiceDialog<>(freeSlots.get(0), freeSlots);
      dialog.setTitle("Reschedule appointment");
      dialog.setHeaderText("Select a new slot to reschedule:");
      dialog.setContentText("Slot:");
      dialog.setGraphic(null);
      ListView<Slot> slotListView = new ListView<>();
      slotListView.getItems().addAll(freeSlots);
      slotListView.setCellFactory(
          lv ->
              new ListCell<>() {
                @Override
                protected void updateItem(Slot s, boolean empty) {
                  super.updateItem(s, empty);
                  setText(
                      (empty || s == null)
                          ? null
                          : s.date() + " " + s.timeRange().start() + "-" + s.timeRange().end());
                }
              });
      slotListView.getSelectionModel().selectFirst();
      VBox vbox = new VBox(new Label("Choose a slot to reschedule the appointment."), slotListView);
      dialog.getDialogPane().setContent(vbox);
      dialog.setResultConverter(
          buttonType ->
              buttonType == ButtonType.OK
                  ? slotListView.getSelectionModel().getSelectedItem()
                  : null);
      dialog
          .showAndWait()
          .ifPresent(
              newSlot -> {
                selectedAppointment.reschedule(newSlot.id(), repositories.appointments());
                notificationManager.createNotification(
                    selectedAppointment.patientId(),
                    "Your appointment has been rescheduled to: "
                        + newSlot.date()
                        + " "
                        + newSlot.timeRange().start()
                        + "-"
                        + newSlot.timeRange().end());
                reloadAppointments();
              });
    }
  }

  private void handleConfirm() {
    if (selectedAppointment != null && !selectedAppointment.status().name().equals("CONFIRMED")) {
      selectedAppointment.confirm(repositories.appointments());
      notificationManager.createNotification(
          selectedAppointment.patientId(), "Your appointment has been confirmed.");
      reloadAppointments();
    }
  }
}
