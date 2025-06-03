package com.clinicmanager.gui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.time.format.DateTimeFormatter;
import com.clinicmanager.time.TimeManager;
import java.time.LocalDateTime;

public class DoctorPanelController {
    @FXML private Button viewScheduleBtn;
    @FXML private Button viewAppointmentsBtn;
    @FXML private Button viewMedicalCardBtn;
    @FXML private Button logoutBtn;
    @FXML private Label virtualTimeLabel;
    @FXML private Button startTimeBtn;
    @FXML private Button stopTimeBtn;
    @FXML private Button setTimeBtn;

    private final TimeManager timeManager = TimeManager.getInstance();
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final com.clinicmanager.service.SlotAutoGeneratorService slotAutoGeneratorService = new com.clinicmanager.service.SlotAutoGeneratorService();

    @FXML
    private void initialize() {
        // Otwórz okno z grafikiem lekarza
        viewScheduleBtn.setOnAction(e -> {
            try {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/gui/doctor_schedule.fxml"));
                javafx.scene.Scene scene = new javafx.scene.Scene(loader.load());
                javafx.stage.Stage stage = new javafx.stage.Stage();
                stage.setTitle("Mój grafik");
                stage.setScene(scene);
                stage.show();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // Otwórz okno z wizytami lekarza
        viewAppointmentsBtn.setOnAction(e -> {
            try {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/gui/doctor_appointments.fxml"));
                javafx.scene.Scene scene = new javafx.scene.Scene(loader.load());
                javafx.stage.Stage stage = new javafx.stage.Stage();
                stage.setTitle("Moje wizyty");
                stage.setScene(scene);
                stage.show();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // Otwórz okno z listą pacjentów
        viewMedicalCardBtn.setOnAction(e -> {
            try {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/gui/doctor_patients.fxml"));
                javafx.scene.Scene scene = new javafx.scene.Scene(loader.load());
                javafx.stage.Stage stage = new javafx.stage.Stage();
                stage.setTitle("Moi pacjenci");
                stage.setScene(scene);
                stage.show();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // Wyloguj się
        logoutBtn.setOnAction(e -> {
            try {
                var panel = com.clinicmanager.gui.AppContext.getPanel();
                if (panel != null) {
                    panel.revokeToken();
                }
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/gui/start_menu.fxml"));
                javafx.scene.Scene scene = new javafx.scene.Scene(loader.load());
                javafx.stage.Stage stage = (javafx.stage.Stage) logoutBtn.getScene().getWindow();
                stage.setScene(scene);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // Виртуальное время
        updateTimeLabel(timeManager.getCurrentTime());
        timeManager.addListener(this::onTimeChanged);
        startTimeBtn.setOnAction(e -> timeManager.start());
        stopTimeBtn.setOnAction(e -> timeManager.stop());
        setTimeBtn.setOnAction(e -> handleSetTime());
    }

    private void updateTimeLabel(LocalDateTime time) {
        Platform.runLater(() -> virtualTimeLabel.setText(dtf.format(time)));
    }

    private void onTimeChanged(LocalDateTime time) {
        updateTimeLabel(time);
        // Глобальное обновление UI и данных
        var slotRepo = com.clinicmanager.gui.AppContext.getRepositories().slots;
        var appointmentRepo = com.clinicmanager.gui.AppContext.getRepositories().appointments;
        // Удаляем все свободные слоты, которые полностью в прошлом (конец < now)
        slotRepo.findAll().forEach(slot -> {
            LocalDateTime slotEnd = LocalDateTime.of(slot.date(), slot.timeRange().end());
            boolean isPast = slotEnd.isBefore(time);
            boolean hasActiveAppointment = appointmentRepo.findAll().stream()
                .anyMatch(a -> a.slotId() == slot.id() && !a.status().name().equals("CANCELLED"));
            if (isPast && !hasActiveAppointment) {
                slotRepo.delete(slot);
            }
        });
        // --- Новое: удаляем все свободные слоты, которые начинаются после текущего времени (будущее) ---
        slotRepo.findAll().forEach(slot -> {
            LocalDateTime slotStart = LocalDateTime.of(slot.date(), slot.timeRange().start());
            boolean isFuture = slotStart.isAfter(time);
            boolean hasActiveAppointment = appointmentRepo.findAll().stream()
                .anyMatch(a -> a.slotId() == slot.id() && !a.status().name().equals("CANCELLED"));
            if (isFuture && !hasActiveAppointment) {
                slotRepo.delete(slot);
            }
        });
        // --- Автогенерация слотов для всех докторов после смены времени ---
        slotAutoGeneratorService.ensureFutureSlotsForAllDoctors();
        // --- Обновить все открытые окна/контроллеры (например, списки визитов, расписание) ---
        javafx.application.Platform.runLater(() -> {
            for (javafx.stage.Window window : javafx.stage.Window.getWindows()) {
                if (window.isShowing() && window.getScene() != null && window.getScene().getRoot() instanceof javafx.scene.Parent) {
                    javafx.scene.Parent root = (javafx.scene.Parent) window.getScene().getRoot();
                    Object controller = root.getProperties().get("fx:controller");
                    if (controller instanceof com.clinicmanager.gui.DoctorAppointmentsController) {
                        ((com.clinicmanager.gui.DoctorAppointmentsController) controller).reloadAppointments();
                    }
                    if (controller instanceof com.clinicmanager.gui.DoctorScheduleController) {
                        ((com.clinicmanager.gui.DoctorScheduleController) controller).loadSlots();
                    }
                }
            }
        });
        // TODO: отправлять уведомления за 10 минут до приёма
    }

    private void handleSetTime() {
        TextInputDialog dialog = new TextInputDialog(dtf.format(timeManager.getCurrentTime()));
        dialog.setTitle("Ustaw czas systemowy");
        dialog.setHeaderText("Podaj nowy czas (yyyy-MM-dd HH:mm):");
        dialog.setContentText("Czas:");
        dialog.showAndWait().ifPresent(str -> {
            try {
                LocalDateTime newTime = LocalDateTime.parse(str, dtf);
                timeManager.setCurrentTime(newTime);
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, "Nieprawidłowy format czasu!", ButtonType.OK).showAndWait();
            }
        });
    }
}
