package com.example.demo_lignedroite;


import java.util.List;

public class FrechetDistanceCalculator {

    /**
     * Calcule la distance euclidienne entre deux points.
     */
    private static double euclideanDistance(Point p1, Point p2) {
        double dx = p1.x() - p2.x();
        double dy = p1.y() - p2.y();
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Calcule la Distance de Fréchet discrète entre deux tracés (listes de points).
     * @param P La liste de points du tracé modèle.
     * @param Q La liste de points du tracé utilisateur.
     * @return La distance de Fréchet discrète.
     */
    public static double calculateDiscreteFrechet(List<Point> P, List<Point> Q) {
        if (P.isEmpty() || Q.isEmpty()) {
            return Double.POSITIVE_INFINITY;
        }

        int n = P.size(); // Taille du tracé P (modèle)
        int m = Q.size(); // Taille du tracé Q (utilisateur)

        // Matrice de programmation dynamique (n x m)
        double[][] CA = new double[n][m];

        // Remplissage de la matrice
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                // 1. Calculer la distance euclidienne entre P[i] et Q[j]
                double dist = euclideanDistance(P.get(i), Q.get(j));

                // 2. Déterminer la valeur de récurrence (le min des trois voisins)
                if (i == 0 && j == 0) {
                    // Coin supérieur gauche
                    CA[i][j] = dist;
                } else if (i == 0) {
                    // Première ligne (seulement depuis la gauche)
                    CA[i][j] = Math.max(CA[i][j - 1], dist);
                } else if (j == 0) {
                    // Première colonne (seulement depuis le haut)
                    CA[i][j] = Math.max(CA[i - 1][j], dist);
                } else {
                    // Cas général : max de la distance courante et du min des trois voisins
                    double min_prev = Math.min(CA[i - 1][j],       // Voisin du haut (i-1, j)
                            Math.min(CA[i][j - 1],   // Voisin de gauche (i, j-1)
                                    CA[i - 1][j - 1])); // Voisin diagonal (i-1, j-1)
                    CA[i][j] = Math.max(min_prev, dist);
                }
            }
        }

        // La Distance de Fréchet est la valeur dans le coin inférieur droit de la matrice
        return CA[n - 1][m - 1];
    }
}