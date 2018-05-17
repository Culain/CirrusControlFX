package CirrusControl.Main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception{
        Position pos = new Position(100, 200, 300, -120, -180, -600);
        System.out.println(pos.toString());
        Offset offset = new Offset(11, 2222, 333, -120, -180, -300);
        pos.add(offset);
        System.out.println(pos.toString());

        Parent root = FXMLLoader.load(getClass().getResource("MainWindow.fxml"));
        primaryStage.setTitle("CirrusControl by Visio Nerf GmbH");
        primaryStage.setScene(new Scene(root));  //old: Scene(root,600,800)
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
