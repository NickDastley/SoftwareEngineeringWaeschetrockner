package clothdryer;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) {
        Label label = new Label("JavaFX läuft!");
        Scene scene = new ProgramSelectionScene(stage).getScene();
        stage.setScene(scene);
        stage.setTitle("Test");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
