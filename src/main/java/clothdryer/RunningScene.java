package clothdryer;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class RunningScene {

    private final Stage stage;
    private final String programName;

    private final ProgramManager programManager;

    public RunningScene(Stage stage, String programName, ProgramManager programManager) {
        this.stage = stage;
        this.programName = programName;

        this.programManager = programManager;
    }

    public Scene getScene() {
        Label title = new Label("Programm: " + programName);
        Label status = new Label("Status: Läuft");
        Label timeRemaining = new Label("Restlaufzeit: 00:45:00");
        Label humidity = new Label("Restfeuchte: 55%");
        Label temperature = new Label("Temperatur: 62 °C");

        Button cancel = new Button("Abbrechen");
        cancel.setOnAction(e -> {
            ProgramSelectionScene programSelectionScene = new ProgramSelectionScene(stage, programManager);
            stage.setScene(programSelectionScene.getScene());
        });

        VBox layout = new VBox(10, title, status, timeRemaining, humidity, temperature, cancel);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center");
        return new Scene(layout, 400, 300);
    }
}
