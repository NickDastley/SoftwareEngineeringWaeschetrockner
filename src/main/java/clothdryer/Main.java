package clothdryer;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class Main extends Application {
    private ProgramManager programManager;

    private static final String TITLE = "Wäschetrockner";

    @Override
    public void start(Stage stage) {
        // Initialize DryerState and ProgramManager
        programManager = new ProgramManager();

        Thread managerThread = new Thread(programManager);
        managerThread.setDaemon(true); // Ensure the thread doesn't block application exit
        managerThread.start();

        // Initialize GUI
        Scene scene = new ProgramSelectionScene(stage, programManager).getScene();
        stage.setScene(scene);
        stage.setTitle(TITLE);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
