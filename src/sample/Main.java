package sample;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("welcomePage.fxml"));
        primaryStage.setTitle("Lobitos Fishery Marketplace");
        primaryStage.setScene(new Scene(root, 800, 800));
        primaryStage.show();
    }




    public static void main(String[] args) {
        launch(args);
    }
}
