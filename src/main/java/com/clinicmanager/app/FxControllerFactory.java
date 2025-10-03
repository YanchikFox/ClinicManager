package com.clinicmanager.app;

import com.clinicmanager.gui.DoctorAppointmentsController;
import com.clinicmanager.gui.DoctorPanelController;
import com.clinicmanager.gui.DoctorPatientsController;
import com.clinicmanager.gui.DoctorScheduleController;
import com.clinicmanager.gui.DoctorSearchController;
import com.clinicmanager.gui.LoginController;
import com.clinicmanager.gui.MedicalCardController;
import com.clinicmanager.gui.PatientAppointmentsController;
import com.clinicmanager.gui.PatientPanelController;
import com.clinicmanager.gui.RegisterController;
import com.clinicmanager.gui.StartMenuController;
import com.clinicmanager.repository.Repositories;
import com.clinicmanager.service.NotificationService;
import com.clinicmanager.service.RegistrationUseCase;
import com.clinicmanager.service.SlotGenerationService;
import java.util.function.Supplier;
import javafx.util.Callback;

public class FxControllerFactory implements Callback<Class<?>, Object> {
  private final ClinicFacade clinic;
  private final PanelManager panelManager;
  private final Repositories repositories;
  private final RegistrationUseCase registrationUseCase;
  private final SlotGenerationService slotGenerationService;
  private final NotificationService notificationService;
  private final Supplier<ViewLoader> viewLoaderSupplier;

  public FxControllerFactory(
      ClinicFacade clinic,
      PanelManager panelManager,
      Repositories repositories,
      RegistrationUseCase registrationUseCase,
      SlotGenerationService slotGenerationService,
      NotificationService notificationService,
      Supplier<ViewLoader> viewLoaderSupplier) {
    this.clinic = clinic;
    this.panelManager = panelManager;
    this.repositories = repositories;
    this.registrationUseCase = registrationUseCase;
    this.slotGenerationService = slotGenerationService;
    this.notificationService = notificationService;
    this.viewLoaderSupplier = viewLoaderSupplier;
  }

  private ViewLoader viewLoader() {
    return viewLoaderSupplier.get();
  }

  @Override
  public Object call(Class<?> type) {
    if (type == StartMenuController.class) {
      return new StartMenuController(viewLoader());
    }
    if (type == LoginController.class) {
      return new LoginController(clinic, panelManager, slotGenerationService, viewLoader());
    }
    if (type == RegisterController.class) {
      return new RegisterController(registrationUseCase, viewLoader());
    }
    if (type == DoctorPanelController.class) {
      return new DoctorPanelController(
          panelManager, repositories, slotGenerationService, viewLoader());
    }
    if (type == DoctorScheduleController.class) {
      return new DoctorScheduleController(panelManager, repositories);
    }
    if (type == DoctorAppointmentsController.class) {
      return new DoctorAppointmentsController(panelManager, repositories, viewLoader());
    }
    if (type == DoctorPatientsController.class) {
      return new DoctorPatientsController(panelManager, repositories, viewLoader());
    }
    if (type == DoctorSearchController.class) {
      return new DoctorSearchController(panelManager, repositories);
    }
    if (type == PatientPanelController.class) {
      return new PatientPanelController(
          panelManager, repositories, notificationService, viewLoader());
    }
    if (type == PatientAppointmentsController.class) {
      return new PatientAppointmentsController(panelManager, repositories, notificationService);
    }
    if (type == MedicalCardController.class) {
      return new MedicalCardController(panelManager, repositories);
    }
    try {
      return type.getDeclaredConstructor().newInstance();
    } catch (Exception ex) {
      throw new IllegalStateException("Unable to instantiate controller: " + type, ex);
    }
  }
}
