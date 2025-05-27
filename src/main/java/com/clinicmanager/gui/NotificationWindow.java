package com.clinicmanager.gui;

import com.clinicmanager.model.entities.Notification;
import com.clinicmanager.service.NotificationManager;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;

public class NotificationWindow {
    public static void showNotifications(Stage owner, NotificationManager notificationManager, int personId) {
        ListView<String> listView = new ListView<>();
        Runnable refreshList = () -> {
            List<Notification> notifications = notificationManager.getAllNotificationsByPersonId(personId);
            listView.setItems(FXCollections.observableArrayList(
                notifications.stream().map(n -> (n.isRead() ? "" : "[NOWE] ") + n.timestamp() + ": " + n.message()).toList()
            ));
        };
        refreshList.run();

        Button markReadBtn = new Button("Oznacz jako przeczytane");
        markReadBtn.setOnAction(e -> {
            List<Notification> notifications = notificationManager.getAllNotificationsByPersonId(personId);
            for (Notification n : notifications) {
                if (!n.isRead()) {
                    notificationManager.markAsRead(n);
                }
            }
            refreshList.run();
        });

        VBox vbox = new VBox(10, listView, markReadBtn);
        vbox.setPadding(new Insets(10));
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Powiadomienia");
        dialog.setScene(new Scene(vbox, 400, 300));
        dialog.showAndWait();
    }
}

