
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.log4j.BasicConfigurator;

import java.io.File;
import java.net.URL;

public class Main extends Application {
    public static Stage primaryStage;
    public static Scene mainScene;
    @Override
    public void start(Stage primaryStage) throws Exception{
        URL url = new File("src/main/java/sample.fxml").toURI().toURL();
        FXMLLoader loader= new FXMLLoader();
        loader.setLocation(url);
        Parent root = loader.load();
        Main.primaryStage = primaryStage;
        mainScene = new Scene(root, 800   , 600);
        primaryStage.setScene(mainScene);
        primaryStage.setTitle("Hello World");
        primaryStage.show();


    }

    public static void main(String[] args) {
        BasicConfigurator.configure();
        launch(args);

    }
}
