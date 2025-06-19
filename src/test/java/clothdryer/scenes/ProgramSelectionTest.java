package clothdryer.scenes;

import clothdryer.DryerState;
import clothdryer.ProgramManager;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testklasse für die Programmauswahl-Szene
 *
 * Traceability:
 * - TC-001: Programmauswahl
 */
class ProgramSelectionTest extends ApplicationTest {

    private Stage stage;
    private ProgramManager programManager;
    private DryerState dryerState;

    @Override
    public void start(Stage stage) {
        this.stage = stage;

        programManager = new ProgramManager();
        Thread managerThread = new Thread(programManager);
        managerThread.setDaemon(true); // Ensure the thread doesn't block application exit
        managerThread.start();

        ProgramSelectionScene selectionScene = new ProgramSelectionScene(stage, programManager);
        stage.setScene(selectionScene.getScene());
        stage.show();
    }

    @Test // TC-001
    void testCottonButtonSwitchesToRunningScene() {
        clickOn("Baumwolle");
        Scene currentScene = stage.getScene();
        assertTrue(currentScene.getRoot().lookupAll(".label").stream()
                .anyMatch(node -> ((javafx.scene.control.Label) node).getText().toLowerCase().contains("cotton")));
    }

    @Test // TC-001
    void testSyntheticsButtonSwitchesToRunningScene() {
        clickOn("Synthetik");
        Scene currentScene = stage.getScene();
        assertTrue(currentScene.getRoot().lookupAll(".label").stream()
                .anyMatch(node -> ((javafx.scene.control.Label) node).getText().toLowerCase().contains("synthetic")));
    }

    @Test // TC-001
    void testWoolButtonSwitchesToRunningScene() {
        clickOn("Wolle");
        Scene currentScene = stage.getScene();
        assertTrue(currentScene.getRoot().lookupAll(".label").stream()
                .anyMatch(node -> ((javafx.scene.control.Label) node).getText().toLowerCase().contains("wool")));
    }
}