package com.clinicmanager.gui;

import com.clinicmanager.controller.DoctorControlPanel;
import com.clinicmanager.model.actors.Doctor;
import com.clinicmanager.model.entities.Slot;
import com.clinicmanager.repository.RepositoryManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import com.clinicmanager.gui.AppContext;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class DoctorScheduleController {
    @FXML private TableView<SlotTableRow> slotsTable;
    @FXML private TableColumn<SlotTableRow, String> dateCol;
    @FXML private TableColumn<SlotTableRow, String> startCol;
    @FXML private TableColumn<SlotTableRow, String> endCol;
    @FXML private TableColumn<SlotTableRow, String> statusCol;
    @FXML private Button addSlotBtn;
    @FXML private Button removeSlotBtn;
    @FXML private Button closeSlotBtn;
    @FXML private Button openSlotBtn;
    @FXML private Button refreshBtn;
    @FXML private Button closeBtn;

    @FXML
    private void initialize() {
        closeBtn.setOnAction(e -> ((Stage) closeBtn.getScene().getWindow()).close());
        setupTable();
        loadSlots();
        refreshBtn.setOnAction(e -> loadSlots());
    }

    private void setupTable() {
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        startCol.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        endCol.setCellValueFactory(new PropertyValueFactory<>("endTime"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void loadSlots() {
        var panel = AppContext.getPanel();
        if (!(panel instanceof DoctorControlPanel doctorPanel)) return;
        Doctor doctor = (Doctor) doctorPanel.currentPerson();
        RepositoryManager repos = AppContext.getRepositories();
        List<com.clinicmanager.model.entities.Slot> allSlots = repos.slots.findAll().stream()
            .filter(s -> s.scheduleId() == doctor.scheduleId())
            .toList();
        ObservableList<SlotTableRow> rows = FXCollections.observableArrayList();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (var slot : allSlots) {
            String status = slot.isAvailable() ? "wolny" : "zajęty";
            rows.add(new SlotTableRow(
                slot.date().format(fmt),
                slot.timeRange().start().toString(),
                slot.timeRange().end().toString(),
                status
            ));
        }
        slotsTable.setItems(rows);
    }

    public static class SlotTableRow {
        private final String date;
        private final String startTime;
        private final String endTime;
        private final String status;
        public SlotTableRow(String date, String startTime, String endTime, String status) {
            this.date = date;
            this.startTime = startTime;
            this.endTime = endTime;
            this.status = status;
        }
        public String getDate() { return date; }
        public String getStartTime() { return startTime; }
        public String getEndTime() { return endTime; }
        public String getStatus() { return status; }
    }
}
