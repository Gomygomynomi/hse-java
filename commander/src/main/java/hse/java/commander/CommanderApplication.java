package hse.java.commander;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class CommanderApplication extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("commander-ui.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setTitle("Two-Panel File Commander");
        stage.setScene(scene);
        stage.show();
    }
}