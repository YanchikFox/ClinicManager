package com.clinicmanager.gui.localization;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public final class LocalizationManager {
    public static final Locale ENGLISH = Locale.ENGLISH;
    public static final Locale RUSSIAN = new Locale("ru");
    public static final Locale POLISH = new Locale("pl");

    private static final LocalizationManager INSTANCE = new LocalizationManager();
    private static final ResourceBundle.Control UTF8_CONTROL = new Utf8Control();

    private final ObjectProperty<Locale> localeProperty;
    private ResourceBundle bundle;

    private LocalizationManager() {
        Locale defaultLocale = ENGLISH;
        this.bundle = ResourceBundle.getBundle("i18n.messages", defaultLocale, UTF8_CONTROL);
        this.localeProperty = new SimpleObjectProperty<>(defaultLocale);
        this.localeProperty.addListener((obs, oldLocale, newLocale) ->
                bundle = ResourceBundle.getBundle("i18n.messages", newLocale, UTF8_CONTROL));
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
    private static class Utf8Control extends ResourceBundle.Control {
        @Override
        public ResourceBundle newBundle(String baseName, Locale locale, String format,
                                        ClassLoader loader, boolean reload)
                throws IllegalAccessException, InstantiationException, IOException {
            String bundleName = toBundleName(baseName, locale);
            String resourceName = toResourceName(bundleName, "properties");
            ResourceBundle bundle = null;
            InputStream stream = null;
            if (reload) {
                URL url = loader.getResource(resourceName);
                if (url != null) {
                    URLConnection connection = url.openConnection();
                    if (connection != null) {
                        connection.setUseCaches(false);
                        stream = connection.getInputStream();
                    }
                }
            } else {
                stream = loader.getResourceAsStream(resourceName);
            }
            if (stream != null) {
                try (InputStream input = stream;
                     Reader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
                    bundle = new PropertyResourceBundle(reader);
                }
            }
            return bundle;
        }
    }
}
