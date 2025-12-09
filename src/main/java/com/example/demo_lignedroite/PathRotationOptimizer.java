
package com.example.demo_lignedroite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PathRotationOptimizer {

    private static final double ROTATION_STEP = 0.25;

    public record RotationResult(
            double bestAngle,
            double bestFrechetDistance,
            double NormalizedAngle,
            double scaleFactor,
            List<Point> rotatedPoints
    ) {}

    // Contient le meilleur résultat global trouvé après toutes les rotations
    private static class OptimizationState {
        double bestAngle = 0;
        double bestFrechetDistance = Double.MAX_VALUE;
        List<Point> bestRotatedPoints = new ArrayList<>();
    }

    /**
     * Applique uniquement la mise à l'échelle au chemin de l'utilisateur.
     * La translation (centrage) est gérée séparément.
     */
    private List<Point> normalizePathScale(List<Point> userPoints, double scaleFactor) {
        if (userPoints.isEmpty()) {
            return new ArrayList<>();
        }

        List<Point> normalizedPoints = new ArrayList<>(userPoints.size());

        for (Point p : userPoints) {
            normalizedPoints.add(new Point(p.x() * scaleFactor, p.y() * scaleFactor));
        }

        return normalizedPoints;
    }

    public RotationResult findOptimalRotation(List<Point> userPoints, List<Point> modelPoints) {
        if (userPoints.isEmpty() || modelPoints.isEmpty()) {
            return new RotationResult(0, Double.MAX_VALUE, 0, 1, new ArrayList<>());
        }

        // 1. Calculer le facteur d'échelle basé sur la distance début-fin
        double userLength = euclideanDistance(userPoints.get(0), userPoints.get(userPoints.size() - 1));
        double modelLength = euclideanDistance(modelPoints.get(0), modelPoints.get(modelPoints.size() - 1));
        double scaleFactor = (userLength > 1e-6) ? (modelLength / userLength) : 1.0;

        // 2. Mettre à l'échelle le tracé utilisateur
        List<Point> scaledUser = normalizePathScale(userPoints, scaleFactor);

        // 3. Centrer les deux tracés à l'origine (0, 0)

        Point userCenter = getCenter(scaledUser);
        Point modelCenter = getCenter(modelPoints);

        List<Point> centeredUser = translatePoints(scaledUser, userCenter, true); // Soustraire le centre utilisateur
        List<Point> centeredModel = translatePoints(modelPoints, modelCenter, true); // Soustraire le centre modèle

        // La rotation se fera autour de l'origine (0, 0)
        Point rotationCenter = new Point(0, 0);

        // 4. Préparer l'orientation inversée
        List<Point> reversedUser = new ArrayList<>(centeredUser);
        Collections.reverse(reversedUser);

        OptimizationState state = new OptimizationState();

        FrechetDistanceCalculator calculator = new FrechetDistanceCalculator();


        // 5. Tester l'orientation originale et inversée
        System.out.println("\n--- Testing ORIGINAL orientation ---");
        testRotationsOnOrientation(centeredUser, centeredModel, rotationCenter, calculator, state, false);

        System.out.println("\n--- Testing REVERSED orientation ---");
        testRotationsOnOrientation(reversedUser, centeredModel, rotationCenter, calculator, state, true);

        // 6. Finaliser les résultats
        double finalBestAngle = state.bestAngle;
        double finalBestFrechetDistance = state.bestFrechetDistance;

        // 7. Re-translater le meilleur tracé rotaté pour le placer au centre du modèle
        List<Point> finalRotatedPoints = translatePoints(state.bestRotatedPoints, modelCenter, false); // Additionner le centre modèle

        System.out.println("\nBest angle found: " + finalBestAngle + "°");
        System.out.println("Best Fréchet distance: " + finalBestFrechetDistance);



        double normalizedAngle = finalBestAngle;
        if (normalizedAngle > 180) {
            normalizedAngle = normalizedAngle - 360;
        }

        double normalizedAngleAbs = Math.abs(normalizedAngle);
        System.out.println("normalizedAngle: " + normalizedAngleAbs);

        return new RotationResult(normalizedAngle, finalBestFrechetDistance, normalizedAngleAbs, scaleFactor, finalRotatedPoints);
    }

    private void testRotationsOnOrientation(List<Point> pointsToRotate, List<Point> modelPoints,
                                            Point center, FrechetDistanceCalculator calculator,
                                            OptimizationState state, boolean isReversed) {

        for (double angle = 0; angle < 360; angle += ROTATION_STEP) {
            List<Point> rotatedPoints = rotatePoints(pointsToRotate, center, angle);

            // Note: Nous comparons centeredUser/reversedUser (rotaté) avec centeredModel.
            double frechetDistance = calculator.calculateDiscreteFrechet(rotatedPoints, modelPoints);

            if (angle % 45 == 0) {
                String orientation = isReversed ? "REVERSED" : "ORIGINAL";
                System.out.println("Angle " + angle + "° (" + orientation + ") -> Fréchet: " + frechetDistance);
            }

            if (frechetDistance < state.bestFrechetDistance) {
                state.bestFrechetDistance = frechetDistance;
                state.bestAngle = angle;
                state.bestRotatedPoints = rotatedPoints;
            }
        }
    }


    private List<Point> rotatePoints(List<Point> points, Point center, double angleDegrees) {
        double angleRadians = Math.toRadians(angleDegrees);
        double cos = Math.cos(angleRadians);
        double sin = Math.sin(angleRadians);

        List<Point> rotated = new ArrayList<>(points.size());

        for (Point p : points) {
            double dx = p.x() - center.x();
            double dy = p.y() - center.y();

            double rotatedX = dx * cos - dy * sin;
            double rotatedY = dx * sin + dy * cos;

            rotated.add(new Point(
                    rotatedX + center.x(),
                    rotatedY + center.y()
            ));
        }

        return rotated;
    }

    /**
     * Translates a list of points by the inverse or direct coordinates of a center.
     * @param subtract If true, subtracts the center coordinates (for centering to 0,0).
     * If false, adds the center coordinates (for re-translation).
     */
    private List<Point> translatePoints(List<Point> points, Point center, boolean subtract) {
        List<Point> translated = new ArrayList<>(points.size());
        double sign = subtract ? -1.0 : 1.0;

        for (Point p : points) {
            translated.add(new Point(
                    p.x() + sign * center.x(),
                    p.y() + sign * center.y()
            ));
        }
        return translated;
    }

    private Point getCenter(List<Point> points) {
        double sumX = 0, sumY = 0;
        for (Point p : points) {
            sumX += p.x();
            sumY += p.y();
        }
        return new Point(sumX / points.size(), sumY / points.size());
    }

    private double euclideanDistance(Point p1, Point p2) {
        double dx = p1.x() - p2.x();
        double dy = p1.y() - p2.y();
        return Math.sqrt(dx * dx + dy * dy);
    }
}