package clothdryer;

import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import static org.junit.jupiter.api.Assertions.*;

class ProgramSelectionTest extends ApplicationTest {

    private Stage stage;

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        ProgramSelectionScene selectionScene = new ProgramSelectionScene(stage);
        stage.setScene(selectionScene.getScene());
        stage.show();
    }

    @Test
    void testCottonButtonSwitchesToRunningScene() {
        clickOn("Baumwolle");
        Scene currentScene = stage.getScene();
        assertTrue(currentScene.getRoot().lookupAll(".label").stream()
                .anyMatch(node -> ((javafx.scene.control.Label) node).getText().toLowerCase().contains("cotton")));
    }

    @Test
    void testSyntheticsButtonSwitchesToRunningScene() {
        clickOn("Synthetik");
        Scene currentScene = stage.getScene();
        assertTrue(currentScene.getRoot().lookupAll(".label").stream()
                .anyMatch(node -> ((javafx.scene.control.Label) node).getText().toLowerCase().contains("synthetic")));
    }

    @Test
    void testWoolButtonSwitchesToRunningScene() {
        clickOn("Wolle");
        Scene currentScene = stage.getScene();
        assertTrue(currentScene.getRoot().lookupAll(".label").stream()
                .anyMatch(node -> ((javafx.scene.control.Label) node).getText().toLowerCase().contains("wool")));
    }
}