package com.clinicmanager.app;

import com.clinicmanager.controller.BaseControlPanel;

public interface PanelManager {
    void setCurrentPanel(BaseControlPanel panel);

    BaseControlPanel getCurrentPanel();

    void clear();
}
