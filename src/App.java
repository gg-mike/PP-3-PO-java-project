import data.Database;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("gui/MainWindow.fxml"));
            AnchorPane root = loader.load();
            Scene scene = new Scene(root);

            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        try {
            launch(args);
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Got Exception: " + e.getMessage());
        }
        Database.endThreads();

    }
}
