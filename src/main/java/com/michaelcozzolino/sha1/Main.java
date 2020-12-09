package com.michaelcozzolino.sha1;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("sample.fxml"));
        primaryStage.setTitle("SHA1 hasher");
        primaryStage.setResizable(false);
            primaryStage.setScene(new Scene(root, 637, 593));
        primaryStage.show();
    }


    public static void main(String[] args) {
        Sha1 sha1 = new Sha1();

        //System.out.println(sha1.compute("abc"));

//        System.exit(0);
        launch(args);
    }
}
