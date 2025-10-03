package com.clinicmanager.gui;

import com.clinicmanager.app.ViewLoader;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Utility methods for loading and displaying JavaFX views in a consistent manner. */
public final class FxViewHelper {

  private static final Logger LOG = LoggerFactory.getLogger(FxViewHelper.class);

  private FxViewHelper() {}

  /**
   * Loads an FXML resource using the provided {@link ViewLoader} and returns the configured loader.
   *
   * @param viewLoader abstraction responsible for creating {@link FXMLLoader} instances
   * @param fxmlPath classpath-relative path to the FXML resource
   * @return the fully initialised {@link FXMLLoader}
   */
  public static FXMLLoader load(ViewLoader viewLoader, String fxmlPath) {
    try {
      FXMLLoader loader = viewLoader.loader(fxmlPath);
      loader.load();
      return loader;
    } catch (IOException ex) {
      LOG.error("Unable to load FXML view: {}", fxmlPath, ex);
      throw new IllegalStateException("Unable to load view: " + fxmlPath, ex);
    }
  }

  /**
   * Shows the content associated with the provided loader in a new stage.
   *
   * @param loader loader produced by {@link #load(ViewLoader, String)}
   * @param title window title
   * @return the created {@link Stage}
   */
  public static Stage showInNewStage(FXMLLoader loader, String title) {
    Parent root = loader.getRoot();
    Stage stage = new Stage();
    stage.setTitle(title);
    stage.setScene(new Scene(root));
    stage.show();
    return stage;
  }

  /**
   * Convenience method that loads an FXML resource and displays it in a new stage.
   *
   * @param viewLoader abstraction responsible for creating {@link FXMLLoader} instances
   * @param fxmlPath classpath-relative path to the FXML resource
   * @param title window title
   * @return the {@link FXMLLoader} used to build the scene
   */
  @SuppressWarnings("UnusedReturnValue")
  public static FXMLLoader showInNewStage(ViewLoader viewLoader, String fxmlPath, String title) {
    FXMLLoader loader = load(viewLoader, fxmlPath);
    showInNewStage(loader, title);
    return loader;
  }

  /**
   * Replaces the content of the supplied stage with the view defined by the given FXML.
   *
   * @param stage stage that should display the loaded view
   * @param viewLoader abstraction responsible for creating {@link FXMLLoader} instances
   * @param fxmlPath classpath-relative path to the FXML resource
   * @param title window title
   * @return the {@link FXMLLoader} used to build the new scene
   */
  @SuppressWarnings("UnusedReturnValue")
  public static FXMLLoader replaceScene(
      Stage stage, ViewLoader viewLoader, String fxmlPath, String title) {
    FXMLLoader loader = load(viewLoader, fxmlPath);
    stage.setScene(new Scene(loader.getRoot()));
    stage.setTitle(title);
    return loader;
  }
}
