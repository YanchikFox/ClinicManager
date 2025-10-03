package com.clinicmanager.gui;

import com.clinicmanager.app.ApplicationContainer;
import com.clinicmanager.app.DefaultApplicationContainer;
import com.clinicmanager.gui.localization.LocalizationManager;
import java.time.LocalDateTime;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainFX extends Application {
  private static Stage primaryStage;
  private static ApplicationContainer container;

  @Override
  public void start(Stage stage) throws Exception {
    container = new DefaultApplicationContainer();
    cleanupFutureSlots();

    FXMLLoader loader = container.viewLoader().loader("/gui/start_menu.fxml");
    Scene scene = new Scene(loader.load());
    primaryStage = stage;
    stage.setTitle(LocalizationManager.getInstance().get("start.title"));
    stage.setScene(scene);
    stage.show();
  }

  @Override
  public void stop() throws Exception {
    if (container != null) {
      container.close();
    }
    super.stop();
  }

  private void cleanupFutureSlots() {
    var repositories = container.repositories();
    LocalDateTime now = com.clinicmanager.time.TimeManager.getInstance().getCurrentTime();
    repositories
        .slots()
        .findAll()
        .forEach(
            slot -> {
              LocalDateTime slotStart = LocalDateTime.of(slot.date(), slot.timeRange().start());
              boolean isFuture = slotStart.isAfter(now);
              boolean hasActiveAppointment =
                  repositories.appointments().findAll().stream()
                      .anyMatch(
                          a -> a.slotId() == slot.id() && !a.status().name().equals("CANCELLED"));
              if (isFuture && !hasActiveAppointment) {
                repositories.slots().delete(slot);
              }
            });
  }

  public static Stage getPrimaryStage() {
    return primaryStage;
  }

  public static void main(String[] args) {
    launch(args);
  }
}
