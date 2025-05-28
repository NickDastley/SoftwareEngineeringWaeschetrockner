package clothdryer;

import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import static org.junit.jupiter.api.Assertions.*;

public class RunningSceneTest extends ApplicationTest {
    private Stage stage;

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        ProgramSelectionScene selectionScene = new ProgramSelectionScene(stage);
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
