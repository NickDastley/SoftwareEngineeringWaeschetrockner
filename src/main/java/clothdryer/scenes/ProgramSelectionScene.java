package clothdryer.scenes;

import clothdryer.ProgramManager;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class ProgramSelectionScene {

    private final Stage stage;
    private final ProgramManager programManager;

    private Label doorStatusLabel;
    private Button doorButton;
    private Button cottonButton;
    private Button syntheticButton;
    private Button woolButton;
    private Button loadLaundryButton;
    private Timeline updateTimeline;

    public ProgramSelectionScene(Stage stage, ProgramManager programManager) {
        this.stage = stage;
        this.programManager = programManager;
    }

    public Scene getScene() {
        Label headline = new Label("Wähle ein Trockenprogramm:");

        cottonButton = new Button("Baumwolle");
        syntheticButton = new Button("Synthetik");
        woolButton = new Button("Wolle");

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

        doorStatusLabel = new Label(getDoorStatusText());
        doorButton = new Button(getDoorButtonText());
        doorButton.setOnAction(e -> toggleDoorState());
        
        // Add new laundry load button
        loadLaundryButton = new Button("Neue Wäsche einlegen");
        loadLaundryButton.setOnAction(e -> {
            if (programManager.loadNewLaundry()) {
                // Show confirmation message
                loadLaundryButton.setText("✓ Wäsche eingelegt");
                // Reset text after 2 seconds
                new javafx.animation.Timeline(
                    new javafx.animation.KeyFrame(javafx.util.Duration.seconds(2), 
                    event -> loadLaundryButton.setText("Neue Wäsche einlegen"))
                ).play();
            }
        });
        // Initially disable button, will be updated in updateDoorControls()
        loadLaundryButton.setDisable(true);

        HBox doorControls = new HBox(10, doorStatusLabel, doorButton);
        doorControls.setAlignment(Pos.CENTER);

        VBox layout = new VBox(15, headline, cottonButton, syntheticButton, woolButton, loadLaundryButton, doorControls);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center");
        startUpdateTimeline();
        return new Scene(layout, 400, 300);
    }

    private void startUpdateTimeline() {
        updateTimeline = new Timeline(
                new KeyFrame(Duration.seconds(0.5), event -> {
                    updateDoorControls();
                })
        );
        updateTimeline.setCycleCount(Animation.INDEFINITE);
        updateTimeline.play();
    }


    private void updateDoorControls() {
        doorStatusLabel.setText(getDoorStatusText());
        doorButton.setText(getDoorButtonText());
        doorButton.setDisable(programManager.isDoorLocked());
        
        // Enable the load laundry button only when door is open
        boolean doorOpen = !programManager.isDoorClosed();
        loadLaundryButton.setDisable(!doorOpen);
        
        // Also update program buttons based on door state
        boolean doorClosed = programManager.isDoorClosed();
        cottonButton.setDisable(!doorClosed);
        syntheticButton.setDisable(!doorClosed);
        woolButton.setDisable(!doorClosed);
    }

    private String getDoorStatusText() {
        if (programManager.isDoorLocked()) {
            return "Tür: Verriegelt";
        } else if (programManager.isDoorClosed()) {
            return "Tür: Geschlossen";
        } else {
            return "Tür: Geöffnet";
        }
    }

    private String getDoorButtonText() {
        return programManager.isDoorClosed() ? "Tür öffnen" : "Tür schließen";
    }

    private void toggleDoorState() {
        if (programManager.isDoorClosed()) {
            programManager.tryOpenDoor();
        } else {
            programManager.closeDoor();
        }

        boolean doorClosed = programManager.isDoorClosed();
        cottonButton.setDisable(!doorClosed);
        syntheticButton.setDisable(!doorClosed);
        woolButton.setDisable(!doorClosed);

        updateDoorControls();
    }
}
