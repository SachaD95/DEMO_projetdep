package com.example.demo_lignedroite;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polyline;
import java.util.List;

/**
 * Méthode ultra-simple pour tracer des points sur un Pane JavaFX
 */
public class DrawingHelper {


    public static void draw(Pane pane, List<Point> points, Color color) {
        Polyline line = new Polyline();

        for (Point p : points) {
            line.getPoints().addAll(p.x(), p.y());
        }

        line.setStroke(color);
        line.setStrokeWidth(3);

        pane.getChildren().add(line);
    }
}


