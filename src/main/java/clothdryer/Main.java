package clothdryer;

import clothdryer.scenes.ProgramSelectionScene;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main class for the dryer simulation application.
 * <p>
 * This class initializes the application, creates the ProgramManager and its background thread,
 * and sets up the initial JavaFX scene for program selection.
 */
public class Main extends Application {
    private ProgramManager programManager;

    private static final String TITLE = "Wäschetrockner";

    /**
     * Entry point for the JavaFX application.
     * Initializes the ProgramManager, starts its thread, and shows the main window.
     *
     * @param stage the primary stage for this application
     */
    @Override
    public void start(Stage stage) {
        // Initialize DryerState and ProgramManager
        programManager = new ProgramManager();

        // Start the ProgramManager in a background thread
        Thread managerThread = new Thread(programManager);
        managerThread.setDaemon(true); // Ensure the thread doesn't block application exit
        managerThread.start();

        // Initialize GUI
        Scene scene = new ProgramSelectionScene(stage, programManager).getScene();
        stage.setScene(scene);
        stage.setTitle(TITLE);
        stage.show();
    }

    /**
     * Main method. Launches the JavaFX application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        launch();
    }
}
