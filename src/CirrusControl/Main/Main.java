package CirrusControl.Main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.InetAddress;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception{
        // Testbereich
//        CirrusScanner scanner = new CirrusScanner();

//        scanner.setIpAddress("127.0.0.1");
//        scanner.sendCommand("LOCG 1");
        // Testbereich ende

        Parent root = FXMLLoader.load(getClass().getResource("MainWindow.fxml"));
        primaryStage.setTitle("CirrusControl by Visio Nerf GmbH");
        primaryStage.setScene(new Scene(root));  //old: Scene(root,600,800)
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
