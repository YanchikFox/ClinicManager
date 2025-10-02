package com.clinicmanager.gui;

import com.clinicmanager.model.entities.Notification;
import com.clinicmanager.service.NotificationService;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class NotificationWindow {

    public static void showNotifications(Stage owner, NotificationService notificationManager, int personId) {
        ListView<String> listView = new ListView<>();
        Runnable refreshList = () -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            // Always reload notifications from DB
            List<Notification> notifications = notificationManager.getAllNotificationsByPersonId(personId);
            listView.setItems(FXCollections.observableArrayList(
                    notifications.stream().map(n -> (n.isRead() ? "" : "[NEW] ") + n.timestamp().format(formatter) + ": " + n.message()).toList()
            ));
        };
        refreshList.run();

        Button markReadBtn = new Button("Mark as read");
        markReadBtn.setOnAction(e -> {
            // Always reload notifications from DB before marking as read
            List<Notification> notifications = notificationManager.getAllNotificationsByPersonId(personId);
            for (Notification n : notifications) {
                if (!n.isRead()) {
                    notificationManager.markAsRead(n);
                }
            }
            // After marking as read, reload notifications from DB again
            refreshList.run();
        });

        VBox vbox = new VBox(10, listView, markReadBtn);
        vbox.setPadding(new Insets(10));
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Notifications");
        dialog.setScene(new Scene(vbox, 700, 300));
        dialog.showAndWait();
    }
}

