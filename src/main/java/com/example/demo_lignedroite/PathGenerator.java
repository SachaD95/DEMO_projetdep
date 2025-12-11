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
    /**
     * Génère un arc de cercle entre start et end.
     * @param steps Nombre de points.
     * @param curvature Amplitude de la courbe (distance du sommet de l'arc par rapport au milieu du segment).
     */
    public List<Point> generateArc(Point start, Point end, int steps, double curvature) {
        List<Point> path = new ArrayList<>(steps);
        double midX = (start.x() + end.x()) / 2;
        double midY = (start.y() + end.y()) / 2;

        // Vecteur perpendiculaire pour le bombement
        double dx = end.x() - start.x();
        double dy = end.y() - start.y();
        double len = Math.sqrt(dx * dx + dy * dy);
        double perpX = -(dy / len) * curvature;
        double perpY = (dx / len) * curvature;

        for (int i = 0; i < steps; i++) {
            double t = i / (double) (steps - 1);
            // Interpolation quadratique (Bézier simple) pour simuler l'arc
            double x = (1 - t) * (1 - t) * start.x() + 2 * (1 - t) * t * (midX + perpX) + t * t * end.x();
            double y = (1 - t) * (1 - t) * start.y() + 2 * (1 - t) * t * (midY + perpY) + t * t * end.y();
            path.add(new Point(x, y));
        }
        return path;
    }
    /**
     * Génère une ligne avec une oscillation sinusoïdale.
     * @param frequency Nombre de vagues sur la longueur.
     * @param amplitude Hauteur des vagues.
     */
    public List<Point> generateSinusoid(Point start, Point end, int steps, double frequency, double amplitude) {
        List<Point> path = new ArrayList<>(steps);
        double dx = end.x() - start.x();
        double dy = end.y() - start.y();
        double len = Math.sqrt(dx * dx + dy * dy);
        double perpX = -dy / len;
        double perpY = dx / len;

        for (int i = 0; i < steps; i++) {
            double t = i / (double) (steps - 1);
            double lineX = start.x() + t * dx;
            double lineY = start.y() + t * dy;

            // Ajout de l'oscillation perpendiculaire
            double offset = Math.sin(t * Math.PI * 2 * frequency) * amplitude;
            path.add(new Point(lineX + perpX * offset, lineY + perpY * offset));
        }
        return path;
    }
    /**
     * Génère une ligne avec des pics pointus réguliers.
     */
    public List<Point> generateSpikyLine(Point start, Point end, int steps, int numSpikes, double spikeHeight) {
        List<Point> path = new ArrayList<>(steps);
        double dx = end.x() - start.x();
        double dy = end.y() - start.y();
        double len = Math.sqrt(dx * dx + dy * dy);
        double perpX = -dy / len;
        double perpY = dx / len;

        for (int i = 0; i < steps; i++) {
            double t = i / (double) (steps - 1);
            double lineX = start.x() + t * dx;
            double lineY = start.y() + t * dy;

            // Crée un signal en dents de scie pour les pics
            double spikeFactor = (t * numSpikes) % 1.0;
            double offset = (spikeFactor > 0.9) ? spikeHeight : 0; // Pic seulement sur 10% de l'intervalle

            path.add(new Point(lineX + perpX * offset, lineY + perpY * offset));
        }
        return path;
    }
    /**
     * Génère une ligne qui revient en arrière au milieu.
     * @param backFactor Pourcentage de la ligne où le retour s'effectue (ex: 0.2 pour reculer de 20%).
     */
    public List<Point> generateOverlappingLine(Point start, Point end, int steps, double backFactor) {
        List<Point> path = new ArrayList<>(steps);
        // On divise le trajet en 3 phases : avance (jusqu'à 70%), retour (de 70% vers 50%), avance finale (50% vers 100%)

        for (int i = 0; i < steps; i++) {
            double t = i / (double) (steps - 1);
            double adjustedT;

            if (t < 0.5) {
                adjustedT = t * 1.4; // Avance rapide jusqu'à 0.7
            } else if (t < 0.75) {
                adjustedT = 0.7 - (t - 0.5) * 0.8; // Recule de 0.7 vers 0.5
            } else {
                adjustedT = 0.5 + (t - 0.75) * 2.0; // Repart de 0.5 vers 1.0
            }

            double x = start.x() + (end.x() - start.x()) * adjustedT;
            double y = start.y() + (end.y() - start.y()) * adjustedT;
            path.add(new Point(x, y));
        }
        return path;
    }
}