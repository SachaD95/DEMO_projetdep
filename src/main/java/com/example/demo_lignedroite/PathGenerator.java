package com.example.demo_lignedroite;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PathGenerator {

    private final Random random = new Random();

    /**
     * Génère une ligne droite parfaite entre deux points avec un nombre donné d'étapes.
     * @param start Point de départ.
     * @param end Point d'arrivée.
     * @param steps Nombre de points à générer (y compris le début et la fin).
     * @return Liste de points formant une ligne droite.
     */
    public List<Point> generatePerfectLine(Point start, Point end, int steps) {
        if (steps < 2) {
            steps = 2;
        }

        List<Point> path = new ArrayList<>(steps);
        double totalSteps = steps - 1;

        for (int i = 0; i < steps; i++) {
            double ratio = i / totalSteps;
            double x = start.x() + (end.x() - start.x()) * ratio;
            double y = start.y() + (end.y() - start.y()) * ratio;
            path.add(new Point(x, y));
        }

        return path;
    }

    /**
     * Génère une ligne droite avec du bruit aléatoire (simule une main tremblante).
     * @param start Point de départ.
     * @param end Point d'arrivée.
     * @param steps Nombre de points.
     * @param maxNoise Amplitude maximale du bruit à ajouter (en unités de coordonnées).
     * @return Liste de points formant une ligne droite bruité.
     */
    public List<Point> generateNoisyLine(Point start, Point end, int steps, double maxNoise) {
        List<Point> perfectPath = generatePerfectLine(start, end, steps);
        List<Point> noisyPath = new ArrayList<>(steps);

        for (Point p : perfectPath) {
            // Génère un bruit aléatoire entre -maxNoise et +maxNoise
            double noiseX = (random.nextDouble() * 2 * maxNoise) - maxNoise;
            double noiseY = (random.nextDouble() * 2 * maxNoise) - maxNoise;

            noisyPath.add(new Point(p.x() + noiseX, p.y() + noiseY));
        }

        // Assurer que le point de début et de fin restent précis pour la normalisation
        // C'est souvent important pour l'optimisation qui se base sur les extrémités.
        if (!noisyPath.isEmpty()) {
            noisyPath.set(0, start);
            noisyPath.set(steps - 1, end);
        }

        return noisyPath;
    }

    /**
     * Génère une ligne droite parfaite mais avec une rotation appliquée,
     * simulant un tracé utilisateur désorienté.
     * @param start Point de départ.
     * @param end Point d'arrivée.
     * @param steps Nombre de points.
     * @param angleDegrees Angle de rotation à appliquer (e.g., 90 pour un quart de tour).
     * @return Liste de points rotatés.
     */
    public List<Point> generateRotatedLine(Point start, Point end, int steps, double angleDegrees) {
        List<Point> perfectPath = generatePerfectLine(start, end, steps);

        // Le centre de rotation est le centre de gravité de la ligne parfaite
        Point center = getCenter(perfectPath);

        return rotatePoints(perfectPath, center, angleDegrees);
    }

    // --- Fonctions utilitaires réutilisées de PathRotationOptimizer ---

    private Point getCenter(List<Point> points) {
        double sumX = 0, sumY = 0;
        for (Point p : points) {
            sumX += p.x();
            sumY += p.y();
        }
        return new Point(sumX / points.size(), sumY / points.size());
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
}