package gui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Slider;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;

// Application JavaFX pour la visualisation des horloges
public class SimulateurHorloge extends Application {
    
    // Instance statique pour accès singleton
    private static SimulateurHorloge instance;
    
    private int numProcesses = 4;
    private ComboBox<String> clockTypeComboBox;
    private Button startButton;
    private Button stopButton;
    private VBox processesContainer;
    private TextArea logArea;
    private List<VisualiseurProcessus> visualiseurProcessuses = new ArrayList<>();
    private boolean simulationRunning = false;
    
    @Override
    public void start(Stage primaryStage) {
        // Définir l'instance statique
        instance = this;
        
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f5f5f5;");
        
        // Panneau de contrôle supérieur
        VBox topSection = new VBox(10);
        topSection.setPadding(new Insets(15));
        topSection.setStyle("-fx-background-color: #e0e0e0; -fx-border-color: #cccccc; -fx-border-width: 0 0 1 0;");
        
        Label titleLabel = new Label("Simulateur d'Horloges Réparties");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.web("#2c3e50"));
        
        HBox controlPanel = createControlPanel();
        
        topSection.getChildren().addAll(titleLabel, controlPanel);
        root.setTop(topSection);
        
        // Vue centrale avec visualiseurs de processus
        ScrollPane visualizationScrollPane = new ScrollPane();
        visualizationScrollPane.setStyle("-fx-background: #f8f8f8; -fx-border-color: #ddd;");
        
        processesContainer = new VBox(15);
        processesContainer.setPadding(new Insets(25));
        processesContainer.setStyle("-fx-background-color: #f8f8f8;");
        
        visualizationScrollPane.setContent(processesContainer);
        visualizationScrollPane.setFitToWidth(true);
        
        root.setCenter(visualizationScrollPane);
        
        Scene scene = new Scene(root, 1000, 800);
        primaryStage.setTitle("Simulateur d'Algorithmes Répartis - Projet Horloges Logiques");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // Message de bienvenue dans la console
        System.out.println("Bienvenue au Simulateur d'Horloges Réparties");
        System.out.println("Sélectionnez le type d'horloge et cliquez sur Démarrer pour lancer la simulation");
        System.out.println("Cette interface lancera les processus de votre implémentation originale");
        
        // Ajouter des boutons supplémentaires
        addAdditionalButtons(controlPanel);
        
        // Gérer la fermeture de l'application
        primaryStage.setOnCloseRequest(e -> {
            System.out.println("Application en cours de fermeture - arrêt de tous les processus...");
            if (simulationRunning) {
                AdaptateurHorloge.stopAllProcesses();
            }
        });
        
        // Ajouter un hook d'arrêt système en backup
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("JVM en cours d'arrêt - assurant la terminaison de tous les processus");
            AdaptateurHorloge.stopAllProcesses();
            
            try {
                if (System.getProperty("os.name").toLowerCase().contains("win")) {
                    new ProcessBuilder("taskkill", "/F", "/IM", "java.exe", "/T")
                        .inheritIO()
                        .start()
                        .waitFor(2, java.util.concurrent.TimeUnit.SECONDS);
                }
            } catch (Exception ex) {

            }
        }));
    }
    
    private HBox createControlPanel() {
        HBox panel = new HBox(15);
        panel.setPadding(new Insets(10));
        panel.setAlignment(Pos.CENTER_LEFT);
        panel.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc; -fx-border-radius: 5;");
        
        // Sélection du type d'horloge
        VBox clockTypeBox = new VBox(5);
        Label clockTypeLabel = new Label("Type d'Horloge:");
        clockTypeLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        
        clockTypeComboBox = new ComboBox<>();
        clockTypeComboBox.getItems().addAll("Horloge Scalaire", "Horloge Vectorielle", "Horloge Matricielle");
        clockTypeComboBox.setValue("Horloge Scalaire");
        clockTypeComboBox.setStyle("-fx-font-size: 12px;");
        
        clockTypeBox.getChildren().addAll(clockTypeLabel, clockTypeComboBox);
        
        // Contrôle du nombre de processus
        VBox processCountBox = new VBox(5);
        Label processCountLabel = new Label("Nombre de Processus:");
        processCountLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        
        Spinner<Integer> processCountSpinner = new Spinner<>(2, 8, 4);
        processCountSpinner.setStyle("-fx-font-size: 12px;");
        processCountSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            numProcesses = newVal;
        });
        
        processCountBox.getChildren().addAll(processCountLabel, processCountSpinner);
        
        // Contrôle de vitesse
        VBox speedBox = new VBox(5);
        Label speedLabel = new Label("Vitesse de Simulation:");
        speedLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        
        HBox speedControlBox = new HBox(10);
        speedControlBox.setAlignment(Pos.CENTER_LEFT);
        
        Slider speedSlider = new Slider(0.1, 2.0, 1.0);
        speedSlider.setShowTickLabels(true);
        speedSlider.setShowTickMarks(true);
        speedSlider.setMajorTickUnit(0.5);
        speedSlider.setBlockIncrement(0.1);
        speedSlider.setPrefWidth(150);
        
        Label speedValueLabel = new Label("1.0x");
        speedValueLabel.setMinWidth(40);
        
        speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double speedFactor = newVal.doubleValue();
            speedValueLabel.setText(String.format("%.1fx", speedFactor));
            
            // Calculer les délais en fonction du facteur de vitesse
            long eventDelay = Math.round(1000 / speedFactor);
            long messageDelay = Math.round(3000 / speedFactor);
            
            // Mettre à jour l'adaptateur avec de nouvelles valeurs de délai
            AdaptateurHorloge.setDelays(eventDelay, messageDelay);
            
            System.out.println("Vitesse de simulation définie à " + String.format("%.1fx", speedFactor) + 
                " (événement: " + eventDelay + "ms, message: " + messageDelay + "ms)");
        });
        
        // Définir les délais initiaux
        AdaptateurHorloge.setDelays(1000, 3000);
        
        speedControlBox.getChildren().addAll(speedSlider, speedValueLabel);
        speedBox.getChildren().addAll(speedLabel, speedControlBox);
        
        // Boutons de contrôle
        VBox buttonBox = new VBox(5);
        Label controlLabel = new Label("Contrôles:");
        controlLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        
        HBox buttonsRow = new HBox(10);
        
        // Bouton démarrer
        startButton = new Button("Démarrer");
        startButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold;");
        startButton.setPrefWidth(100);
        startButton.setOnAction(e -> startSimulation());
        
        // Bouton arrêter
        stopButton = new Button("Arrêter");
        stopButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;");
        stopButton.setPrefWidth(100);
        stopButton.setDisable(true);
        stopButton.setOnAction(e -> stopSimulation());
        
        buttonsRow.getChildren().addAll(startButton, stopButton);
        buttonBox.getChildren().addAll(controlLabel, buttonsRow);
        
        // Ajouter tous les groupes de contrôle au panneau avec des séparateurs
        panel.getChildren().addAll(
            clockTypeBox,
            new Separator(javafx.geometry.Orientation.VERTICAL),
            processCountBox,
            new Separator(javafx.geometry.Orientation.VERTICAL),
            speedBox,
            new Separator(javafx.geometry.Orientation.VERTICAL),
            buttonBox
        );
        
        return panel;
    }
    
    private void addAdditionalButtons(HBox panel) {
        
    }
    
    private void startSimulation() {
        // Effacer la simulation précédente
        processesContainer.getChildren().clear();
        visualiseurProcessuses.clear();
        
        String clockTypeText = clockTypeComboBox.getValue();
        int clockTypeValue = AdaptateurHorloge.getClockTypeValue(clockTypeText);
        
        System.out.println("Démarrage de la simulation avec " + numProcesses + " processus utilisant " + clockTypeText);
        
        // Ajouter un en-tête pour la visualisation des processus
        Label processesHeader = new Label("Visualisation des Processus");
        processesHeader.setFont(Font.font("System", FontWeight.BOLD, 14));
        processesHeader.setStyle("-fx-padding: 0 0 10 0;");
        processesContainer.getChildren().add(processesHeader);
        
        // Créer d'abord des visualiseurs de processus
        for (int i = 0; i < numProcesses; i++) {
            VisualiseurProcessus visualizer = new VisualiseurProcessus(i, clockTypeText);
            visualiseurProcessuses.add(visualizer);
            
            VBox processBox = new VBox(5);
            processBox.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-padding: 10;");
            
            // Ajouter le visualiseur
            processBox.getChildren().add(visualizer);
            
            // Ajouter des contrôles de panne/récupération pour chaque processus
            final int procId = i;
            
            HBox controls = new HBox(10);
            controls.setAlignment(Pos.CENTER_RIGHT);
            controls.setPadding(new Insets(5, 0, 0, 0));
            
            Button failButton = new Button("Simuler Panne");
            failButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
            failButton.setOnAction(e -> simulateProcessFailure(procId, visualizer, failButton));
            
            controls.getChildren().add(failButton);
            processBox.getChildren().add(controls);
            
            processesContainer.getChildren().add(processBox);
            
            // Enregistrer le visualiseur auprès de l'adaptateur pour recevoir les mises à jour
            AdaptateurHorloge.registerVisualizer(i, visualizer);
            
            // Enregistrer la création du processus
            System.out.println("Processus " + i + " préparé pour la visualisation");
        }
        
        // Démarrer les processus réels en utilisant votre implémentation originale
        simulationRunning = true;
        startButton.setDisable(true);
        stopButton.setDisable(false);
        
        // Lancer les processus
        new Thread(() -> {
            AdaptateurHorloge.startAllProcesses(numProcesses, clockTypeValue);
        }).start();
        
        System.out.println("Processus démarrés avec votre implémentation originale (Main.java)");
        System.out.println("Chaque processus exécutera 5 événements locaux et enverra 4 messages selon votre code original");
    }
    
    private void stopSimulation() {
        if (simulationRunning) {
            AdaptateurHorloge.stopAllProcesses();
            simulationRunning = false;
            startButton.setDisable(false);
            stopButton.setDisable(true);
            System.out.println("Simulation arrêtée. Tous les processus sont terminés.");
        }
    }
    
    private void simulateProcessFailure(int processId, VisualiseurProcessus visualizer, Button failButton) {
        System.out.println("Simulation d'une panne du Processus " + processId);
        
        // Indication visuelle de la panne
        visualizer.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2; -fx-border-radius: 5;");
        visualizer.setOpacity(0.7);
        failButton.setDisable(true);
        
        // Arrêter réellement le processus
        AdaptateurHorloge.crashProcess(processId);
        
        // Ajouter un bouton de récupération
        Button recoverButton = new Button("Récupérer");
        recoverButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");
        recoverButton.setOnAction(e -> {
            // Récupération visuelle
            visualizer.setStyle("");
            visualizer.setOpacity(1.0);
            failButton.setDisable(false);
            
            // Récupérer réellement le processus
            String clockTypeText = clockTypeComboBox.getValue();
            int clockTypeValue = AdaptateurHorloge.getClockTypeValue(clockTypeText);
            AdaptateurHorloge.recoverProcess(processId, clockTypeValue);
            
            System.out.println("Processus " + processId + " récupéré");
            
            // Supprimer le bouton de récupération
            HBox parent = (HBox) failButton.getParent();
            parent.getChildren().remove(recoverButton);
        });
        
        // Ajouter à la même ligne que le bouton de panne
        HBox parent = (HBox) failButton.getParent();
        parent.getChildren().add(recoverButton);
    }
    
    //Méthode de journalisation simple redirigeant vers la console
    public void log(String message) {
        System.out.println(message);
    }
    
    //Obtenir l'instance singleton
    public static SimulateurHorloge getInstance() {
        return instance;
    }
    
    public static void main(String[] args) {
        launch(args);
    }
} 