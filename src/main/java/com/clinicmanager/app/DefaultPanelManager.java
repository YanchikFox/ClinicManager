package com.clinicmanager.app;

import com.clinicmanager.controller.BaseControlPanel;
import java.util.Optional;

public class DefaultPanelManager implements PanelManager {
  private BaseControlPanel currentPanel;

  @Override
  public void setCurrentPanel(BaseControlPanel panel) {
    this.currentPanel = panel;
  }

  @Override
  public BaseControlPanel getCurrentPanel() {
    return Optional.ofNullable(currentPanel)
        .orElseThrow(() -> new IllegalStateException("Control panel not initialised"));
  }

  @Override
  public void clear() {
    this.currentPanel = null;
  }
}
