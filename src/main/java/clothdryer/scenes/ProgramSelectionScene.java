package clothdryer.scenes;

import clothdryer.DryerState;
import clothdryer.ProgramManager;
import clothdryer.SafetyModule;
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

/**
 * The ProgramSelectionScene class represents the main selection screen
 * for the dryer application. It allows the user to choose a drying program,
 * open or close the door, and load new laundry.
 * 
 * The scene updates its controls dynamically based on the current state
 * of the dryer (door open/closed/locked).
 */
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

    private Label statusLabel;
    private Label tempLabel;
    private Label humidityLabel;

    /**
     * Constructs a new ProgramSelectionScene.
     * 
     * @param stage the primary stage of the application
     * @param programManager the manager handling dryer state and logic
     */
    public ProgramSelectionScene(Stage stage, ProgramManager programManager) {
        this.stage = stage;
        this.programManager = programManager;
    }

    /**
     * Creates and returns the JavaFX Scene for program selection.
     * 
     * @return the program selection Scene
     */
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

        VBox infoBox = new VBox(5);
        statusLabel = new Label();
        tempLabel = new Label();
        humidityLabel = new Label();
        infoBox.getChildren().addAll(statusLabel, tempLabel, humidityLabel);

        HBox doorControls = new HBox(10, doorStatusLabel, doorButton);
        doorControls.setAlignment(Pos.CENTER);

        VBox layout = new VBox(15, headline, cottonButton, syntheticButton, woolButton, loadLaundryButton, doorControls, infoBox);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center");
        startUpdateTimeline();
        Scene scene = new Scene(layout, 400, 420); // Increased height from e.g. 300/350 to 420
        return scene;
    }

    /**
     * Starts a timeline that periodically updates the door controls and
     * program buttons based on the current state.
     */
    private void startUpdateTimeline() {
        updateTimeline = new Timeline(
                new KeyFrame(Duration.seconds(0.5), event -> {
                    updateDoorControls();
                })
        );
        updateTimeline.setCycleCount(Animation.INDEFINITE);
        updateTimeline.play();
    }

    /**
     * Updates the door status label, door button, and enables/disables
     * program and laundry buttons according to the current door state.
     */
    private void updateDoorControls() {
        doorStatusLabel.setText(getDoorStatusText());
        doorButton.setText(getDoorButtonText());
        
        if (programManager.getState().getTemperature() > SafetyModule.SAFE_DOOR_TEMPERATURE) {
            doorButton.setDisable(true);
            doorStatusLabel.setText("Am abkühlen, bitte warten ...");
        } else {
            doorButton.setDisable(programManager.isDoorLocked());
        }
        
        // Enable the load laundry button only when door is open
        boolean doorOpen = !programManager.isDoorClosed();
        loadLaundryButton.setDisable(!doorOpen);
        
        // Also update program buttons based on door state
        boolean doorClosed = programManager.isDoorClosed();
        cottonButton.setDisable(!doorClosed);
        syntheticButton.setDisable(!doorClosed);
        woolButton.setDisable(!doorClosed);

        DryerState state = programManager.getState();
        statusLabel.setText("Status: " + formatStatus(state.getStatus()));
        tempLabel.setText(String.format("Temperatur: %.1f °C", state.getTemperature()));
        humidityLabel.setText(String.format("Restfeuchte: %.1f%%", state.getHumidity()));
    }

    /**
     * Returns a user-friendly string describing the current door status.
     * 
     * @return the door status text
     */
    private String getDoorStatusText() {
        if (programManager.isDoorLocked()) {
            return "Tür: Verriegelt";
        } else if (programManager.isDoorClosed()) {
            return "Tür: Geschlossen";
        } else {
            return "Tür: Geöffnet";
        }
    }

    /**
     * Returns the appropriate text for the door toggle button.
     * 
     * @return the door button text
     */
    private String getDoorButtonText() {
        return programManager.isDoorClosed() ? "Tür öffnen" : "Tür schließen";
    }

    /**
     * Toggles the door state (open/close) and updates the controls.
     */
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

    private String formatStatus(DryerState.ProgramStatus status) {
        return switch (status) {
            case IDLE -> "Bereit";
            case RUNNING -> "Läuft";
            case COOLING -> "Kühlt ab...";
            case ERROR -> "Fehler";
            case DOOR_OPEN -> "Tür geöffnet";
            default -> "Unbekannt";
        };
    }
}
