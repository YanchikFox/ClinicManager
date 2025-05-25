package com.clinicmanager.controller;

import com.clinicmanager.service.AccountManager;
import com.clinicmanager.service.NotificationManager;

public class PatientControlPanel extends BaseControlPanel {
    public PatientControlPanel(String token, AccountManager accountManager, NotificationManager notificationManager) {
        super(token, accountManager, notificationManager);
    }

    // ...другие методы
}
