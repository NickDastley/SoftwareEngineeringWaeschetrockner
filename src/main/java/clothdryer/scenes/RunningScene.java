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

public class RunningScene {

    private final Stage stage;
    private final String programName;
    private final ProgramManager programManager;
    private Timeline updateTimeline;

    public RunningScene(Stage stage, String programName, ProgramManager programManager) {
        this.stage = stage;
        this.programName = programName;
        this.programManager = programManager;

        // Starte das Programm in der Simulation
        programManager.startProgram(programName);
    }

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

        // Regelmäßige Aktualisierung der UI mit Daten aus der Simulation
        updateTimeline = new Timeline(
                new KeyFrame(Duration.seconds(0.5), event -> {
                    updateLabels(title, status, timeRemaining, humidity, temperature);
                })
        );
        updateTimeline.setCycleCount(Animation.INDEFINITE);
        updateTimeline.play();

        return scene;
    }

    private void updateLabels(Label title, Label status, Label timeRemaining,
                              Label humidity, Label temperature) {
        DryerState state = programManager.getState();

        title.setText("Programm: " + formatProgramName(state.getProgramName()));
        status.setText("Status: " + formatStatus(state.getStatus()));
        timeRemaining.setText("Restlaufzeit: " + formatTime(state.getRemainingSeconds()));
        humidity.setText("Restfeuchte: " + String.format("%.1f%%", state.getHumidity()));
        temperature.setText("Temperatur: " + String.format("%.1f °C", state.getTemperature()));

        // Wenn das Programm fertig oder fehlerhaft ist, zur Auswahlszene zurückkehren
        if (state.getStatus() == DryerState.ProgramStatus.IDLE ||
                state.getStatus() == DryerState.ProgramStatus.ERROR) {

            if (updateTimeline != null) {
                updateTimeline.stop();
            }

            // Verzögert zur Auswahlszene zurückkehren, damit der Benutzer den Status sieht
            new Timeline(new KeyFrame(Duration.seconds(3), event -> {
                ProgramSelectionScene programSelectionScene = new ProgramSelectionScene(stage, programManager);
                stage.setScene(programSelectionScene.getScene());
            })).play();
        }
    }

    private String formatProgramName(String name) {
        return switch (name) {
            case "cotton" -> "Baumwolle";
            case "synthetic" -> "Synthetik";
            case "wool" -> "Wolle";
            default -> name;
        };
    }

    private String formatStatus(DryerState.ProgramStatus status) {
        return switch (status) {
            case IDLE -> "Bereit";
            case RUNNING -> "Läuft";
            case ERROR -> "Fehler";
            default -> "Unbekannt";
        };
    }

    private String formatTime(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }
}