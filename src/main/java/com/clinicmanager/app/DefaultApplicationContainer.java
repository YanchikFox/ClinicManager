package com.clinicmanager.app;

import com.clinicmanager.repository.RepositoryManager;
import com.clinicmanager.repository.Repositories;
import com.clinicmanager.repository.DatabaseInitializer;
import com.clinicmanager.security.TokenService;
import com.clinicmanager.service.AccountManager;
import com.clinicmanager.service.AccountService;
import com.clinicmanager.service.NotificationManager;
import com.clinicmanager.service.NotificationService;
import com.clinicmanager.service.RegistrationService;
import com.clinicmanager.service.RegistrationUseCase;
import com.clinicmanager.service.SlotAutoGeneratorService;
import com.clinicmanager.service.SlotGenerationService;
import com.clinicmanager.time.TimeManager;
import com.clinicmanager.time.TimeTickHandler;
import com.clinicmanager.time.TimeTickListener;

import javafx.util.Callback;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class DefaultApplicationContainer implements ApplicationContainer {
    private final RepositoryManager repositoryManager;
    private final AccountService accountService;
    private final RegistrationUseCase registrationService;
    private final SlotGenerationService slotGenerationService;
    private final NotificationService notificationService;
    private final PanelManager panelManager;
    private final ClinicFacade clinic;
    private final TimeTickListener timeTickHandler;
    private final FxControllerFactory controllerFactory;
    private final ViewLoader viewLoader;

    public DefaultApplicationContainer(String dbUrl) {
        Objects.requireNonNull(dbUrl, "Database URL must not be null");
        DatabaseInitializer.initialize(dbUrl);

        this.repositoryManager = new RepositoryManager(dbUrl);
        TokenService tokenService = new TokenService();
        this.accountService = new AccountManager(repositoryManager.accounts(), tokenService);
        this.registrationService = new RegistrationService(
                repositoryManager.accounts(),
                repositoryManager.doctors(),
                repositoryManager.patients(),
                repositoryManager.slots(),
                repositoryManager.medicalCards(),
                repositoryManager.schedules());
        this.slotGenerationService = new SlotAutoGeneratorService(
                repositoryManager.doctors(), repositoryManager.slots());
        this.notificationService = new NotificationManager(repositoryManager.notifications());
        this.panelManager = new DefaultPanelManager();
        this.clinic = new Clinic(accountService,
                repositoryManager.doctors(),
                repositoryManager.patients());
        this.timeTickHandler = new TimeTickHandler(
                repositoryManager.slots(),
                repositoryManager.appointments(),
                notificationService);
        TimeManager.getInstance().setTickListener(timeTickHandler);

        AtomicReference<ViewLoader> viewLoaderRef = new AtomicReference<>();

        this.controllerFactory = new FxControllerFactory(
                clinic,
                panelManager,
                repositoryManager,
                registrationService,
                slotGenerationService,
                notificationService,
                viewLoaderRef::get);
        this.viewLoader = new FxViewLoader(controllerFactory);
        viewLoaderRef.set(this.viewLoader);
    }

    public DefaultApplicationContainer() {
        this("jdbc:sqlite:clinic.db");
    }

    @Override
    public Repositories repositories() {
        return repositoryManager;
    }

    @Override
    public AccountService accountService() {
        return accountService;
    }

    @Override
    public RegistrationUseCase registrationUseCase() {
        return registrationService;
    }

    @Override
    public SlotGenerationService slotGenerationService() {
        return slotGenerationService;
    }

    @Override
    public NotificationService notificationService() {
        return notificationService;
    }

    @Override
    public PanelManager panelManager() {
        return panelManager;
    }

    @Override
    public ClinicFacade clinic() {
        return clinic;
    }

    @Override
    public TimeTickListener timeTickListener() {
        return timeTickHandler;
    }

    @Override
    public ViewLoader viewLoader() {
        return viewLoader;
    }

    @Override
    public Callback<Class<?>, Object> controllerFactory() {
        return controllerFactory;
    }

    @Override
    public void close() {
        repositoryManager.closeAll();
    }
}
