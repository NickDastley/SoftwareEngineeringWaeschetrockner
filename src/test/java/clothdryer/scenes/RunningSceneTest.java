package clothdryer.scenes;

import clothdryer.ProgramManager;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import static org.junit.jupiter.api.Assertions.*;

public class RunningSceneTest extends ApplicationTest {
    private Stage stage;
    private ProgramManager programManager;

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
        clickOn("Baumwolle");
    }

    @Test
    void testCancelButtonReturnsToProgramSelection() {
        clickOn("Abbrechen");
        Scene currentScene = stage.getScene();
        assertTrue(currentScene.getRoot().lookupAll(".label").stream()
                .anyMatch(node -> ((javafx.scene.control.Label) node).getText().toLowerCase().contains("wähle ein trockenprogramm")));
    }
}
