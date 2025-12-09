package com.example.demo_lignedroite;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Assurez-vous que le chemin vers Scene.fxml est correct
        Parent root = FXMLLoader.load(getClass().getResource("scene.fxml"));

        // Configurer la scène et la fenêtre
        primaryStage.setTitle("Dessin Vectoriel JavaFX à Contrôle");
        primaryStage.setScene(new Scene(root, 900, 600));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
