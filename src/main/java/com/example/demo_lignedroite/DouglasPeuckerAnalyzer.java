package com.example.demo_lignedroite;


import java.util.ArrayList;
import java.util.List;

public class DouglasPeuckerAnalyzer {

    // --- ENREGISTREUR DE RÉSULTAT ---
    // Cette classe est utilisée pour renvoyer à la fois la liste des points simplifiés
    // et la pénalité calculée.
    public record AnalysisResult(List<Point> simplifiedPoints, double totalPenalty) {}


    // --- 1. FONCTIONS GÉOMÉTRIQUES DE BASE ---

    /**
     * Calcule le carré de la distance euclidienne entre deux points.
     */
    private static double distanceSq(Point p1, Point p2) {
        double dx = p1.x() - p2.x();
        double dy = p1.y() - p2.y();
        return dx * dx + dy * dy;
    }

    /**
     * Calcule la distance orthogonale d'un point P à la DROITE infinie définie par les points A et B.
     * C'est la métrique clé pour l'algorithme Douglas-Peucker.
     * @param p Le point.
     * @param a Point de début de la ligne.
     * @param b Point de fin de la ligne.
     * @return La distance.
     */
    private static double distancePointToLine(Point p, Point a, Point b) {
        double area = Math.abs((a.x() * b.y() + b.x() * p.y() + p.x() * a.y()) -
                (a.y() * b.x() + b.y() * p.x() + p.y() * a.x()));
        double base = Math.sqrt(distanceSq(a, b));

        // Formule pour l'aire d'un triangle : 0.5 * base * hauteur (hauteur = distance)
        // Distance = 2 * Aire / Base
        return (base == 0) ? Math.sqrt(distanceSq(p, a)) : area / base;
    }

    /**
     * Calcule la distance minimale d'un point P à un SEGMENT de ligne [A, B].
     * (Nécessaire pour le calcul final de la pénalité)
     */
    private static double distancePointToSegment(Point p, Point a, Point b) {
        double segmentLengthSq = distanceSq(a, b);

        if (segmentLengthSq == 0.0) {
            return Math.sqrt(distanceSq(p, a));
        }

        // Calcule le facteur de projection t (0 <= t <= 1 si la projection est sur le segment)
        double t = ((p.x() - a.x()) * (b.x() - a.x()) + (p.y() - a.y()) * (b.y() - a.y())) / segmentLengthSq;

        // Limite t à [0, 1] pour rester sur le segment
        t = Math.max(0, Math.min(1, t));

        // Point projeté sur le segment
        double h_x = a.x() + t * (b.x() - a.x());
        double h_y = a.y() + t * (b.y() - a.y());

        Point h = new Point(h_x, h_y);

        return Math.sqrt(distanceSq(p, h));
    }


    // --- 2. ALGORITHME DE SIMPLIFICATION DOUGLAS-PEUCKER (Récursif) ---

    /**
     * Coeur de l'algorithme Douglas-Peucker.
     */
    private static void dpSimplify(List<Point> points, int first, int last, double epsilon, List<Point> resultList) {
        double maxDistance = 0;
        int maxIndex = 0;

        Point start = points.get(first);
        Point end = points.get(last);

        // 1. Trouver le point le plus éloigné
        for (int i = first + 1; i < last; i++) {
            double distance = distancePointToLine(points.get(i), start, end);

            if (distance > maxDistance) {
                maxDistance = distance;
                maxIndex = i;
            }
        }

        // 2. Simplification récursive
        if (maxDistance > epsilon) {
            // Le point maxIndex est significatif : récursion sur les deux sous-parties
            dpSimplify(points, first, maxIndex, epsilon, resultList);
            dpSimplify(points, maxIndex, last, epsilon, resultList);
        } else {
            // Le bruit est dans la tolérance : on garde seulement le point de début (le point de fin
            // est géré par l'appel récursif suivant ou par l'ajout initial).
            resultList.add(start);
        }
    }


    // --- 3. CALCUL DE LA PÉNALITÉ DE BRUIT ---

    /**
     * Calcule la pénalité totale : la somme des distances de chaque point
     * original au segment le plus proche de la courbe simplifiée.
     * @param original Le tracé utilisateur original (Q).
     * @param simplified Le tracé simplifié (Q_simple).
     * @return La pénalité totale de bruit.
     */
    private static double calculatePenalty(List<Point> original, List<Point> simplified) {
        double totalPenalty = 0.0;

        // Le tracé simplifié doit contenir au moins 2 points pour former un segment
        if (simplified.size() < 2) return 0.0;

        // Pour chaque point du tracé original
        for (Point p : original) {
            double minDistance = Double.POSITIVE_INFINITY;

            // Trouver le segment du tracé SIMPLIFIÉ le plus proche
            for (int i = 0; i < simplified.size() - 1; i++) {
                Point a = simplified.get(i);
                Point b = simplified.get(i + 1);

                // Calculer la distance de P au segment [a, b]
                double dist = distancePointToSegment(p, a, b);

                minDistance = Math.min(minDistance, dist);
            }

            // Si la distance n'est pas infinie (tracé non vide), on ajoute
            if (minDistance != Double.POSITIVE_INFINITY) {
                totalPenalty += minDistance;
            }
        }

        return totalPenalty;
    }

    // --- 4. MÉTHODE PUBLIQUE D'ANALYSE ---

    /**
     * Exécute l'analyse Douglas-Peucker, calcule la pénalité et renvoie les deux résultats.
     * @param traceOriginal Le tracé utilisateur (Q)
     * @param epsilon Le seuil de tolérance (mesure du bruit/simplification)
     * @return Un objet AnalysisResult contenant la liste simplifiée et la pénalité.
     */
    public static AnalysisResult analyze(List<Point> traceOriginal, double epsilon) {
        if (traceOriginal == null || traceOriginal.size() < 2) {
            return new AnalysisResult(new ArrayList<>(), 0.0);
        }

        List<Point> simplified = new ArrayList<>();

        // Lancer la simplification. Le point final doit être ajouté manuellement.
        dpSimplify(traceOriginal, 0, traceOriginal.size() - 1, epsilon, simplified);
        simplified.add(traceOriginal.get(traceOriginal.size() - 1));

        // Calculer la pénalité
        double penalty = calculatePenalty(traceOriginal, simplified);

        return new AnalysisResult(simplified, penalty);
    }
}
