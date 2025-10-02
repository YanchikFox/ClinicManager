package com.clinicmanager.gui.localization;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public final class LocalizationManager {
    public static final Locale ENGLISH = Locale.ENGLISH;
    public static final Locale RUSSIAN = new Locale("ru");
    public static final Locale POLISH = new Locale("pl");

    private static final LocalizationManager INSTANCE = new LocalizationManager();

    private final ObjectProperty<Locale> localeProperty;
    private ResourceBundle bundle;

    private LocalizationManager() {
        Locale defaultLocale = ENGLISH;
        this.bundle = ResourceBundle.getBundle("i18n.messages", defaultLocale);
        this.localeProperty = new SimpleObjectProperty<>(defaultLocale);
        this.localeProperty.addListener((obs, oldLocale, newLocale) ->
                bundle = ResourceBundle.getBundle("i18n.messages", newLocale));
    }

    public static LocalizationManager getInstance() {
        return INSTANCE;
    }

    public void setLocale(Locale locale) {
        if (locale != null) {
            localeProperty.set(locale);
        }
    }

    public Locale getLocale() {
        return localeProperty.get();
    }

    public ReadOnlyObjectProperty<Locale> localeProperty() {
        return localeProperty;
    }

    public String get(String key) {
        return bundle.getString(key);
    }

    public String format(String key, Object... args) {
        return MessageFormat.format(get(key), args);
    }
}