package clothdryer.scenes;

import clothdryer.ProgramManager;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ProgramSelectionScene {

    private final Stage stage;
    private final ProgramManager programManager;

    public ProgramSelectionScene(Stage stage, ProgramManager programManager) {
        this.stage = stage;
        this.programManager = programManager;
    }

    public Scene getScene() {
        Label headline = new Label("Wähle ein Trockenprogramm:");

        Button cottonButton = new Button("Baumwolle");
        Button syntheticButton = new Button("Synthetik");
        Button woolButton = new Button("Wolle");

        cottonButton.setOnAction(e -> {
            String selectedProgram = "cotton";
            RunningScene runningScene = new RunningScene(stage, selectedProgram, programManager);
            stage.setScene(runningScene.getScene());
        });
        syntheticButton.setOnAction(e -> {
            String selectedProgram = "synthetic";
            RunningScene runningScene = new RunningScene(stage, selectedProgram, programManager);
            stage.setScene(runningScene.getScene());
        });
        woolButton.setOnAction(e -> {
            String selectedProgram = "wool";
            RunningScene runningScene = new RunningScene(stage, selectedProgram, programManager);
            stage.setScene(runningScene.getScene());
        });

        VBox layout = new VBox(15, headline, cottonButton, syntheticButton, woolButton);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center");
        return new Scene(layout, 400, 300);
    }
}
