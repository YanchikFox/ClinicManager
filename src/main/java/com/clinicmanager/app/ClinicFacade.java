package com.clinicmanager.app;

import com.clinicmanager.controller.BaseControlPanel;

public interface ClinicFacade {
  BaseControlPanel login(String email, String password);
}
