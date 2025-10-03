package com.clinicmanager.app;

import com.clinicmanager.repository.Repositories;
import com.clinicmanager.service.AccountService;
import com.clinicmanager.service.NotificationService;
import com.clinicmanager.service.RegistrationUseCase;
import com.clinicmanager.service.SlotGenerationService;
import com.clinicmanager.time.TimeTickListener;
import javafx.util.Callback;

public interface ApplicationContainer extends AutoCloseable {
  Repositories repositories();

  AccountService accountService();

  RegistrationUseCase registrationUseCase();

  SlotGenerationService slotGenerationService();

  NotificationService notificationService();

  PanelManager panelManager();

  ClinicFacade clinic();

  TimeTickListener timeTickListener();

  ViewLoader viewLoader();

  Callback<Class<?>, Object> controllerFactory();

  @Override
  void close();
}
