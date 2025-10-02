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
import java.util.MissingResourceException;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public final class LocalizationManager {
    public static final Locale ENGLISH = Locale.ENGLISH;
    public static final Locale RUSSIAN = new Locale("ru");
    public static final Locale POLISH = new Locale("pl");

    private static final String BASE_BUNDLE_NAME = "i18n.messages";
    private static final Locale DEFAULT_LOCALE = ENGLISH;
    private static final LocalizationManager INSTANCE = new LocalizationManager();
    private static final ResourceBundle.Control UTF8_CONTROL = new Utf8Control();

    private final ObjectProperty<Locale> localeProperty;
    private ResourceBundle bundle;

    private LocalizationManager() {
        this.localeProperty = new SimpleObjectProperty<>(DEFAULT_LOCALE);
        this.bundle = loadBundle(DEFAULT_LOCALE);
        this.localeProperty.addListener((obs, oldLocale, newLocale) ->
                bundle = loadBundle(newLocale));
    }

    public static LocalizationManager getInstance() {
        return INSTANCE;
    }
    private ResourceBundle loadBundle(Locale locale) {
        Locale target = locale != null ? locale : DEFAULT_LOCALE;
        ResourceBundle resource = tryLoadBundle(target);
        if (resource != null) {
            return resource;
        }
        if (!DEFAULT_LOCALE.equals(target)) {
            resource = tryLoadBundle(DEFAULT_LOCALE);
            if (resource != null) {
                return resource;
            }
        }
        resource = tryLoadBundle(Locale.ROOT);
        if (resource != null) {
            return resource;
        }
        throw new IllegalStateException("Missing localization resources for " + BASE_BUNDLE_NAME);
    }

    private ResourceBundle tryLoadBundle(Locale locale) {
        try {
            return ResourceBundle.getBundle(BASE_BUNDLE_NAME, locale, UTF8_CONTROL);
        } catch (MissingResourceException ex) {
            return null;
        }
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
