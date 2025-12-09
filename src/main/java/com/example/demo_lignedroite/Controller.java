package com.example.demo_lignedroite;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polyline;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

// Définition de la classe simple pour stocker les coordonnées (Utilisation d'un Record si Java 16+)
record Point(double x, double y) {}

public class Controller implements Initializable {

    // --- Éléments FXML ---
    @FXML private Pane drawingPane;    // Canevas de l'utilisateur (Courbe Q)
    @FXML private Pane modelPane;      // Canevas du modèle idéal (Courbe P)
    @FXML private Button activateButton;
    @FXML private Button ScoringButton;
    @FXML private Label statusLabel;
    @FXML private Label scoreLabel;
    @FXML private CheckBox checkboxAngle;

    // --- Logique de Dessin et Modèle ---
    private boolean isDrawingActive = false;
    private Polyline currentStroke;
    private List<Point> idealPoints; // Courbe P (Modèle)
    private double ratio;
    double startX = 200;
    double startY = 50;
    double endX = 50; // Basé sur prefWidth=300 du FXML
    double endY = 50;
    // ==========================================================
    // INITIALISATION
    // ==========================================================

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialiser le modèle et l'afficher
        createAndDisplayModel();
        updateStatus();
    }

    // ==========================================================
    // CRÉATION ET AFFICHAGE DU MODÈLE VECTORIEL (COURBE P)
    // ==========================================================

    private void createAndDisplayModel() {

        // 2. Générer les points d'échantillonnage pour le calcul de Fréchet (P)
        final int POINT_COUNT = 50;
        idealPoints = generateLinePoints(startX, startY, endX, endY, POINT_COUNT);

        // 3. Créer et configurer la Polyline pour l'affichage
        Polyline modelLine = new Polyline();
        modelLine.setStroke(Color.DARKGREEN);
        modelLine.setStrokeWidth(3);
        modelLine.getStrokeDashArray().addAll(10d, 5d); // Ligne pointillée

        for (Point p : idealPoints) {
            modelLine.getPoints().addAll(p.x(), p.y());
        }

        // 4. Afficher le modèle
        modelPane.getChildren().add(modelLine);
    }

    public void draw(List<Point> ToDraw){

    }

    /**
     * Génère une liste de points uniformément espacés pour définir la ligne droite idéale.
     */
    private List<Point> generateLinePoints(double x1, double y1, double x2, double y2, int count) {
        List<Point> points = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            double ratio = (double) i / (count - 1);
            // Interpolation linéaire
            double x = x1 + (x2 - x1) * ratio;
            double y = y1 + (y2 - y1) * ratio;

            points.add(new Point(x, y));
        }
        return points;
    }

    // ==========================================================
    // GESTION DU MODE DESSIN (Boutons)
    // ==========================================================

    @FXML
    private void handleActivateDrawing() {
        currentStroke = null;
        isDrawingActive = true;
        updateStatus();

        // Attacher les gestionnaires d'événements de la souris au Pane
        drawingPane.setOnMousePressed(this::handleMousePressed);
        drawingPane.setOnMouseDragged(this::handleMouseDragged);
        drawingPane.setOnMouseReleased(this::handleMouseReleased);

        // Effacer les tracés précédents et réinitialiser le score
        drawingPane.getChildren().clear();
        scoreLabel.setText("N/A");
    }

    @FXML
    private void Scoring() {
        isDrawingActive = false;
        updateStatus();

        // Détacher les gestionnaires d'événements de la souris du Pane
        drawingPane.setOnMousePressed(null);
        drawingPane.setOnMouseDragged(null);
        drawingPane.setOnMouseReleased(null);

        // Lancer le calcul du score si un tracé a été fait
        if (currentStroke != null && currentStroke.getPoints().size() >= 4) { // Au moins 2 points
            calculateScore();
        } else {
            scoreLabel.setText("Tracé trop court (min. 2 points).");
        }

    }

    /**
     * Met à jour l'état de l'interface utilisateur (boutons et statut).
     */
    private void updateStatus() {
        if (isDrawingActive) {
            statusLabel.setText("Statut: Dessin ACTIF (Tracez votre ligne)");
            activateButton.setDisable(true);
            ScoringButton.setDisable(false);
        } else {
            statusLabel.setText("Statut: Dessin Désactivé");
            activateButton.setDisable(false);
            ScoringButton.setDisable(true);
        }
    }

    // ==========================================================
    // LOGIQUE DE DESSIN À MAIN LEVÉE (Polyline - Courbe Q)
    // ==========================================================

    private void handleMousePressed(MouseEvent event) {
        if (!isDrawingActive) return;

        // Créer un nouvel objet Polyline (le tracé vectoriel)
        currentStroke = new Polyline();
        currentStroke.setStroke(Color.DARKRED);
        currentStroke.setStrokeWidth(4);

        // Ajouter le point initial
        currentStroke.getPoints().addAll(event.getX(), event.getY());

        // Ajouter le trait au canevas
        drawingPane.getChildren().add(currentStroke);
    }

    private void handleMouseDragged(MouseEvent event) {
        if (currentStroke != null) {
            // Ajouter les coordonnées X et Y à la liste des points de la Polyline
            currentStroke.getPoints().addAll(event.getX(), event.getY());
        }
    }

    private void handleMouseReleased(MouseEvent event) {
        // Le tracé est fini.
    }




    // ==========================================================
    // CALCUL ET AFFICHAGE DU SCORE (Distance de Fréchet)
    // ==========================================================

    @FXML
    private void calculateScore() {
        // 1. Préparer les points du tracé utilisateur (Q)
        List<Point> userPoints = new ArrayList<>();
        List<Double> rawPoints = currentStroke.getPoints();
        for (int i = 0; i < rawPoints.size(); i += 2) {
            userPoints.add(new Point(rawPoints.get(i), rawPoints.get(i + 1)));
        }

        int NEW_POINT_COUNT = rawPoints.size();
        idealPoints = generateLinePoints(startX, startY, endX, endY, NEW_POINT_COUNT);



        // 1bis on lance l'optimisation (UserPoints n'est pas normalisé)
        PathRotationOptimizer PRO = new PathRotationOptimizer();
        PathRotationOptimizer.RotationResult AfterRotation = PRO.findOptimalRotation(userPoints,idealPoints);

        // 2. Calculer la Distance de Fréchet
        double frechetDistance = AfterRotation.bestFrechetDistance();
        System.out.println(frechetDistance);
        System.out.println(AfterRotation.bestAngle());

        DrawingHelper DH = new DrawingHelper();
        DH.draw(drawingPane, AfterRotation.rotatedPoints(),Color.RED);


        // 3. Normaliser la distance pour obtenir un score (0 à 100)

        // Calculer la longueur de la diagonale idéale pour définir d_max
        Point pStart = idealPoints.get(0);
        Point pEnd = idealPoints.get(idealPoints.size() - 1);
        double diagonalLength = euclideanDistance(pStart, pEnd);

        // On fixe une distance maximale d_max comme étant 25% de la longueur idéale
        double d_max = diagonalLength/4;
        System.out.println("dmax="+d_max);

        // Normalisation et calcul du score
        double normalizedDistance = Math.min(frechetDistance, d_max);
        double score = 100.0 * (1.0 - (normalizedDistance / d_max));

        // S'assurer que le score est entre 0 et 100
        score = Math.max(0, Math.min(100, score));

        if (checkboxAngle.isSelected()){
            if (AfterRotation.NormalizedAngle()>10){
                score=score-10;
            }

        }










        // 4. Afficher les résultats
        scoreLabel.setText(String.format("%.1f/100 (Fréchet: %.2f px)", score, frechetDistance));
    }

    /**
     * Calcule la distance euclidienne entre deux points (utilitaire pour la normalisation).
     */
    private double euclideanDistance(Point p1, Point p2) {
        double dx = p1.x() - p2.x();
        double dy = p1.y() - p2.y();
        return Math.sqrt(dx * dx + dy * dy);
    }

    @FXML
    private void Test() {
        DrawingHelper DH = new DrawingHelper();

        PathGenerator generator = new PathGenerator();
        PathRotationOptimizer optimizer = new PathRotationOptimizer();

        // 1. Définir le Modèle (Ligne de 0 à 100 sur l'axe X)
        Point modelStart = new Point(10, 50);
        Point modelEnd = new Point(110, 50);
        int numSteps = 20;
        List<Point> modelPath = generator.generatePerfectLine(modelStart, modelEnd, numSteps);

        System.out.println("--- SCÉNARIO 1 : Ligne parfaite mais tournée à 90° ---");
        // L'utilisateur a tourné son trait de 90 degrés
        List<Point> userPath_Rotated90 = generator.generateRotatedLine(
                new Point(5, 50), new Point(55, 50), // Petit trait de 50 de long
                numSteps, 90.0
        );
        //DH.draw(drawingPane, userPath_Rotated90,Color.BLUE);

        PathRotationOptimizer.RotationResult result1 = optimizer.findOptimalRotation(userPath_Rotated90, modelPath);
        System.out.println("Résultat 1: Angle optimal trouvé: " + result1.bestAngle() + "°");
        System.out.println("Distance Fréchet minimale: " + result1.bestFrechetDistance());
        //DH.draw(drawingPane, result1.rotatedPoints(),Color.VIOLET);

        System.out.println("\n--- SCÉNARIO 2 : Ligne de même taille mais inversée et bruité ---");
        // Crée une ligne inversée (180°) avec du bruit léger
        List<Point> userPath_ReversedNoisy = generator.generateNoisyLine(
                new Point(110, 50), new Point(10, 50), // Sens inversé
                numSteps, 5 // Bruit de 0.5 unité
        );
        DH.draw(drawingPane, userPath_ReversedNoisy,Color.YELLOW);

        PathRotationOptimizer.RotationResult result2 = optimizer.findOptimalRotation(userPath_ReversedNoisy, modelPath);
        System.out.println("Résultat 2: Angle optimal trouvé: " + result2.bestAngle() + "°");
        System.out.println("Distance Fréchet minimale: " + result2.bestFrechetDistance());
        DH.draw(drawingPane, result2.rotatedPoints(),Color.GREEN);
    }

}