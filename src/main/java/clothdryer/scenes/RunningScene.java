package clothdryer.scenes;

import clothdryer.DryerState;
import clothdryer.ProgramManager;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * The RunningScene class represents the user interface shown while a drying
 * program is running. It displays the current program, status, remaining time,
 * humidity, and temperature, and allows the user to cancel the program.
 * <p>
 * The scene updates its display periodically with live data from the simulation.
 * When the program finishes or an error occurs, it automatically returns to the
 * program selection scene after a short delay.
 */
public class RunningScene {

    private final Stage stage;
    private final String programName;
    private final ProgramManager programManager;
    private Timeline updateTimeline;

    /**
     * Constructs a new RunningScene and starts the selected program in the simulation.
     *
     * @param stage         the primary stage of the application
     * @param programName   the name of the selected drying program
     * @param programManager the manager handling dryer state and logic
     */
    public RunningScene(Stage stage, String programName, ProgramManager programManager) {
        this.stage = stage;
        this.programName = programName;
        this.programManager = programManager;

        // Start the selected program in the simulation
        programManager.startProgram(programName);
    }

    /**
     * Creates and returns the JavaFX Scene for the running program.
     *
     * @return the running program Scene
     */
    public Scene getScene() {
        Label title = new Label("Programm: " + programName);
        Label status = new Label("Status: Läuft");
        Label timeRemaining = new Label("Restlaufzeit: --:--:--");
        Label humidity = new Label("Restfeuchte: ---%");
        Label temperature = new Label("Temperatur: -- °C");

        Button cancel = new Button("Abbrechen");
        cancel.setOnAction(e -> {
            if (updateTimeline != null) {
                updateTimeline.stop();
            }

            programManager.stopProgram();
            ProgramSelectionScene programSelectionScene = new ProgramSelectionScene(stage, programManager);
            stage.setScene(programSelectionScene.getScene());
        });

        VBox layout = new VBox(10, title, status, timeRemaining, humidity, temperature, cancel);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center");

        Scene scene = new Scene(layout, 400, 300);

        // Periodically update the UI with data from the simulation
        updateTimeline = new Timeline(
                new KeyFrame(Duration.seconds(0.5), event -> {
                    updateLabels(title, status, timeRemaining, humidity, temperature);
                })
        );
        updateTimeline.setCycleCount(Animation.INDEFINITE);
        updateTimeline.play();

        return scene;
    }

    /**
     * Updates the UI labels with the current simulation state.
     * If the program finishes or an error occurs, returns to the selection scene after a delay.
     *
     * @param title         the label for the program name
     * @param status        the label for the program status
     * @param timeRemaining the label for the remaining time
     * @param humidity      the label for the humidity
     * @param temperature   the label for the temperature
     */
    private void updateLabels(Label title, Label status, Label timeRemaining,
                              Label humidity, Label temperature) {
        DryerState state = programManager.getState();

        title.setText("Programm: " + formatProgramName(state.getProgramName()));
        status.setText("Status: " + formatStatus(state.getStatus()));
        timeRemaining.setText("Restlaufzeit: " + formatTime(state.getRemainingSeconds()));
        humidity.setText("Restfeuchte: " + String.format("%.1f%%", state.getHumidity()));
        temperature.setText("Temperatur: " + String.format("%.1f °C", state.getTemperature()));

        // If the program is finished or an error occurred, return to the selection scene after a short delay
        if (state.getStatus() == DryerState.ProgramStatus.IDLE ||
                state.getStatus() == DryerState.ProgramStatus.ERROR) {

            if (updateTimeline != null) {
                updateTimeline.stop();
            }

            // Delay returning to the selection scene so the user can see the status
            new Timeline(new KeyFrame(Duration.seconds(3), event -> {
                ProgramSelectionScene programSelectionScene = new ProgramSelectionScene(stage, programManager);
                stage.setScene(programSelectionScene.getScene());
            })).play();
        }
    }

    /**
     * Formats the program name for display.
     *
     * @param name the internal program name
     * @return the localized display name
     */
    private String formatProgramName(String name) {
        return switch (name) {
            case "cotton" -> "Baumwolle";
            case "synthetic" -> "Synthetik";
            case "wool" -> "Wolle";
            default -> name;
        };
    }

    /**
     * Formats the program status for display.
     *
     * @param status the program status
     * @return the localized status string
     */
    private String formatStatus(DryerState.ProgramStatus status) {
        return switch (status) {
            case IDLE -> "Bereit";
            case RUNNING -> "Läuft";
            case ERROR -> "Fehler";
            default -> "Unbekannt";
        };
    }

    /**
     * Formats the remaining time in seconds as HH:mm:ss.
     *
     * @param seconds the remaining time in seconds
     * @return the formatted time string
     */
    private String formatTime(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }
}