package gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.application.Platform;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;


public class VisualiseurProcessus extends BorderPane {
    
    private int processId;
    private String clockType;
    private Label processLabel;
    private Circle processCircle;
    private VBox clockDisplay;
    private ScrollPane messageScrollPane;
    private TextFlow messageLog;
    private Label statusLabel;
    
    // Pour l'horloge scalaire
    private int scalarClock = 0;
    
    // Pour l'horloge vectorielle
    private int[] vectorClock;
    
    // Pour l'horloge matricielle
    private int[][] matrixClock;
    
    private static final double CIRCLE_RADIUS = 20;
    private static final Color DEFAULT_COLOR = Color.web("#3498db");  // Bleu
    private static final Color SENDING_COLOR = Color.web("#f39c12");  // Orange
    private static final Color RECEIVING_COLOR = Color.web("#2ecc71"); // Vert
    private static final Color LOCAL_EVENT_COLOR = Color.web("#9b59b6"); // Violet
    
    public VisualiseurProcessus(int processId, String clockType) {
        this.processId = processId;
        // Normaliser le type d'horloge pour éviter les caractères spéciaux
        if (clockType.contains("Vectorielle")) {
            this.clockType = "Horloge Vectorielle";
        } else if (clockType.contains("Matricielle")) {
            this.clockType = "Horloge Matricielle";
        } else {
            this.clockType = "Horloge Scalaire";
        }
        
        setPadding(new Insets(10));
        setStyle("-fx-background-color: white; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 1);");
        
        // Initialiser les horloges en fonction du type
        initializeClocks(4); // Par défaut à 4 processus
        
        // Créer les composants UI
        createVisualComponents();
    }
    
    private void initializeClocks(int numProcesses) {
        switch (clockType) {
            case "Horloge Vectorielle":
                vectorClock = new int[numProcesses];
                break;
            case "Horloge Matricielle":
                matrixClock = new int[numProcesses][numProcesses];
                break;
            default: // Horloge scalaire
                scalarClock = 0;
                break;
        }
    }
    
    private void createVisualComponents() {
        // Section d'identifiant de processus
        HBox header = new HBox(10);
        header.setPadding(new Insets(0, 0, 10, 0));
        header.setStyle("-fx-border-color: #eee; -fx-border-width: 0 0 1 0;");
        
        processCircle = new Circle(CIRCLE_RADIUS);
        processCircle.setFill(DEFAULT_COLOR);
        processCircle.setStroke(Color.web("#2980b9"));
        processCircle.setStrokeWidth(2);
        processCircle.setEffect(new javafx.scene.effect.InnerShadow(5, Color.web("#2980b9")));
        
        VBox idBox = new VBox(2);
        processLabel = new Label("Processus " + processId);
        processLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        statusLabel = new Label("Pret");
        statusLabel.setTextFill(Color.web("#27ae60"));
        statusLabel.setFont(Font.font(11));
        
        idBox.getChildren().addAll(processLabel, statusLabel);
        
        header.getChildren().addAll(processCircle, idBox);
        header.setAlignment(Pos.CENTER_LEFT);
        setTop(header);
        
        //HBox pour contenir l'horloge et le journal de messages
        HBox contentBox = new HBox(15);
        contentBox.setPadding(new Insets(10, 0, 10, 0));
        
        //Visualisation de l'horloge
        VBox clockVisualization = new VBox(10);
        clockVisualization.setMaxWidth(320);
        clockVisualization.setPrefWidth(320);
        
        Label clockTypeLabel = new Label(clockType + ":");
        clockTypeLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        
        //Créer un conteneur pour l'affichage de l'horloge
        clockDisplay = new VBox(5);
        clockDisplay.setStyle("-fx-background-color: #f8f8f8; -fx-padding: 10; -fx-border-color: #eee; -fx-border-radius: 3;");
        
        Label waitingLabel = new Label("En attente des donnees du processus...");
        waitingLabel.setTextFill(Color.web("#7f8c8d"));
        waitingLabel.setFont(Font.font("System", FontPosture.ITALIC, 12));
        
        clockDisplay.getChildren().add(waitingLabel);
        
        clockVisualization.getChildren().addAll(clockTypeLabel, clockDisplay);
        
        //Journal des messages
        VBox logBox = new VBox(5);
        HBox.setHgrow(logBox, javafx.scene.layout.Priority.ALWAYS);
        
        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        
        Label logLabel = new Label("Journal des messages:");
        logLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        
        Label logInfoLabel = new Label("  (messages envoyes et recus)");
        logInfoLabel.setFont(Font.font("System", FontPosture.ITALIC, 10));
        logInfoLabel.setTextFill(Color.web("#7f8c8d"));
        
        headerBox.getChildren().addAll(logLabel, logInfoLabel);
        
        messageLog = new TextFlow();
        messageLog.setPrefHeight(200);
        messageLog.setPrefWidth(400);  // Augmenté de 250
        messageLog.setStyle("-fx-background-color: #f9f9f9; -fx-padding: 8; -fx-font-family: 'System';");
        
        //Textes de bienvenue/explication
        Text welcomeText = new Text("Journal des communications pour Processus " + processId + ":\n");
        welcomeText.setStyle("-fx-font-weight: bold;");
        
        Text sendExplanation = new Text("→ ENVOI ");
        sendExplanation.setFill(Color.web("#e74c3c"));
        sendExplanation.setStyle("-fx-font-weight: bold;");
        
        Text receiveExplanation = new Text(" ← RECEPTION ");
        receiveExplanation.setFill(Color.web("#2ecc71"));
        receiveExplanation.setStyle("-fx-font-weight: bold;");
        
        Text localExplanation = new Text(" ● EVENEMENT\n");
        localExplanation.setFill(Color.web("#9b59b6"));
        localExplanation.setStyle("-fx-font-weight: bold;");
        
        messageLog.getChildren().addAll(
            welcomeText, sendExplanation, receiveExplanation, localExplanation
        );
        
        messageScrollPane = new ScrollPane(messageLog);
        messageScrollPane.setFitToWidth(true);
        messageScrollPane.setStyle("-fx-background: #f9f9f9; -fx-background-color: #f9f9f9; -fx-border-color: #ddd; -fx-border-radius: 2;");
        messageScrollPane.setPrefHeight(200);
        messageScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        messageScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        
        logBox.getChildren().addAll(headerBox, messageScrollPane);
        
        contentBox.getChildren().addAll(clockVisualization, logBox);
        setCenter(contentBox);
    }
    
    //Mettre à jour l'affichage de l'horloge avec la valeur du processus réel
    public void updateClockDisplay(String clockValue) {
        // Effacer l'affichage précédent
        clockDisplay.getChildren().clear();
        
        if (clockType.equals("Horloge Matricielle")) {
            try {
                // Extraire les données entre les crochets extérieurs
                String matrixContent = clockValue.substring(1, clockValue.length() - 1).trim();
                String[] rows = matrixContent.split("\\],\\s*\\[");
                
                // Nettoyer les chaînes de lignes
                for (int i = 0; i < rows.length; i++) {
                    rows[i] = rows[i].replace("[", "").replace("]", "");
                }
                
                // Créer une grille pour l'affichage matriciel
                GridPane matrixGrid = new GridPane();
                matrixGrid.setHgap(8); 
                matrixGrid.setVgap(8);  
                matrixGrid.setPadding(new Insets(8)); 
                matrixGrid.setStyle("-fx-background-color: white; -fx-border-color: #cccccc; -fx-border-radius: 3;");
                
                for (int i = 0; i < rows.length; i++) {
                    Label rowHeader = new Label("P" + i);
                    rowHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
                    
                    if (i == processId) {
                        rowHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: #e74c3c;");
                    }
                    
                    matrixGrid.add(rowHeader, 0, i + 1);
                }
                
                for (int i = 0; i < rows.length; i++) {
                    Label colHeader = new Label("P" + i);
                    colHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
                    matrixGrid.add(colHeader, i + 1, 0);
                }
                
                // Ajouter les valeurs de la matrice
                for (int i = 0; i < rows.length; i++) {
                    String[] values = rows[i].split(",");
                    for (int j = 0; j < values.length; j++) {
                        Label valueLabel = new Label(values[j].trim());
                        valueLabel.setPadding(new Insets(2, 4, 2, 4));  
                        valueLabel.setStyle("-fx-font-family: 'Consolas', monospace;");
                        
                        if (i == processId) {
                            valueLabel.setStyle("-fx-font-family: 'Consolas', monospace; -fx-border-color: #f7dc6f; -fx-border-width: 0 0 2 0;");
                        }
                        
                        if (i == j) {
                            valueLabel.setTextFill(Color.web("#e74c3c"));
                        }
                        
                        matrixGrid.add(valueLabel, j + 1, i + 1);
                    }
                }
                
                
                VBox explanationBox = new VBox(5);
                explanationBox.setStyle("-fx-padding: 5 0 0 0;");
                
                HBox highlightingExplanation = new HBox(10);
                highlightingExplanation.setAlignment(Pos.CENTER_LEFT);
                
                Label borderExplanation = new Label("Ligne courante");
                borderExplanation.setStyle("-fx-font-size: 10; -fx-border-color: #f7dc6f; -fx-border-width: 0 0 2 0;");
                
                Label valueExplanation = new Label("Valeur locale");
                valueExplanation.setStyle("-fx-font-size: 10; -fx-text-fill: #e74c3c;");
                
                highlightingExplanation.getChildren().addAll(
                    borderExplanation, valueExplanation
                );
                
                explanationBox.getChildren().addAll(highlightingExplanation);
                
                clockDisplay.getChildren().addAll(matrixGrid, explanationBox);
                
            } catch (Exception e) {
                // Si l'analyse échoue, afficher simplement la chaîne d'origine
                Label rawValueLabel = new Label(clockValue);
                rawValueLabel.setStyle("-fx-font-family: 'Consolas', monospace;");
                clockDisplay.getChildren().add(rawValueLabel);
            }
        } else if (clockType.equals("Horloge Vectorielle")) {
            try {
                // Extraire les données entre crochets
                String vectorContent = clockValue.substring(1, clockValue.length() - 1).trim();
                String[] values = vectorContent.split(",");
                
                VBox vectorDisplay = new VBox(5);
                
                // Créer une grille pour l'affichage vectoriel
                GridPane vectorGrid = new GridPane();
                vectorGrid.setHgap(12);
                vectorGrid.setVgap(8);  
                vectorGrid.setPadding(new Insets(8)); 
                vectorGrid.setStyle("-fx-background-color: white; -fx-border-color: #cccccc; -fx-border-radius: 3;");
                
                for (int i = 0; i < values.length; i++) {
                    Label header = new Label("P" + i);
                    header.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
                    
                    if (i == processId) {
                        header.setStyle("-fx-font-weight: bold; -fx-text-fill: #e74c3c;");
                    }
                    
                    vectorGrid.add(header, i, 0);
                }
                
                for (int i = 0; i < values.length; i++) {
                    Label valueLabel = new Label(values[i].trim());
                    valueLabel.setPadding(new Insets(2, 4, 2, 4));  // Réduit padding
                    valueLabel.setStyle("-fx-font-family: 'Consolas', monospace;");
                    
                    if (i == processId) {
                        valueLabel.setStyle("-fx-font-family: 'Consolas', monospace; -fx-border-color: #f7dc6f; -fx-border-width: 0 0 2 0;");
                        valueLabel.setTextFill(Color.web("#e74c3c"));
                    }
                    
                    vectorGrid.add(valueLabel, i, 1);
                }
            
                
                VBox explanationBox = new VBox(5);
                explanationBox.setStyle("-fx-padding: 5 0 0 0;");
                
                HBox highlightingExplanation = new HBox(10);
                highlightingExplanation.setAlignment(Pos.CENTER_LEFT);
                
                Label borderExplanation = new Label("Valeur courante");
                borderExplanation.setStyle("-fx-font-size: 10; -fx-border-color: #f7dc6f; -fx-border-width: 0 0 2 0; -fx-text-fill: #e74c3c;");
                
                highlightingExplanation.getChildren().add(borderExplanation);
                
                explanationBox.getChildren().addAll(highlightingExplanation);
                
                vectorDisplay.getChildren().addAll(vectorGrid, explanationBox);
                clockDisplay.getChildren().add(vectorDisplay);
                
            } catch (Exception e) {
                // Si l'analyse échoue, afficher simplement la chaîne d'origine
                Label rawValueLabel = new Label(clockValue);
                rawValueLabel.setStyle("-fx-font-family: 'Consolas', monospace;");
                clockDisplay.getChildren().add(rawValueLabel);
            }
        } else {
            // Pour l'horloge scalaire, afficher simplement la valeur
            VBox scalarBox = new VBox(5);
            scalarBox.setAlignment(Pos.CENTER);
            scalarBox.setPadding(new Insets(10));
            
            Label scalarValueLabel = new Label(clockValue);
            scalarValueLabel.setFont(Font.font("Monospace", FontWeight.BOLD, 20));
            scalarValueLabel.setTextFill(Color.web("#e74c3c"));
            
            Label explanationLabel = new Label("Valeur de l'horloge locale du processus " + processId);
            explanationLabel.setStyle("-fx-font-size: 11; -fx-font-style: italic; -fx-text-fill: #7f8c8d;");
            
            scalarBox.getChildren().addAll(scalarValueLabel, explanationLabel);
            clockDisplay.getChildren().add(scalarBox);
        }
    }
    
    //Ajouter un message au journal
    public void addLogMessage(String message) {
        String timestamp = java.time.LocalTime.now().toString().substring(0, 8);
        
        // Créer des composants de texte stylisés pour différents types de messages
        final Text timestampText = new Text("[" + timestamp + "] ");
        timestampText.setStyle("-fx-font-size: 10px; -fx-font-family: monospace;");
        timestampText.setFill(Color.GRAY);
        
        final Text messageText;
        final Text iconText;
        
        // Appliquer le style en fonction du type de message
        if (message.contains("Message envoy") || message.contains("envoy")) {
            iconText = new Text("→ ");
            iconText.setFill(Color.web("#e74c3c"));
            iconText.setStyle("-fx-font-weight: bold;");
            
            // Extraire l'ID de destination si possible
            String destIdStr = "?";
            try {
                if (message.contains("a ")) {
                    destIdStr = message.substring(message.lastIndexOf("a ") + 2).trim();
                }
            } catch (Exception e) {
                destIdStr = "?";
            }
            
            String title = "ENVOI: ";
            String content = "Message vers P" + destIdStr;
            
            Text titleText = new Text(title);
            titleText.setFill(Color.web("#e74c3c"));
            titleText.setStyle("-fx-font-weight: bold;");
            
            Text contentText = new Text(content);
            contentText.setFill(Color.web("#e74c3c", 0.8));
            
            messageText = new Text(""); 
            
            // Mettre à jour TextFlow sur le thread JavaFX
            if (messageLog != null) {
                Platform.runLater(() -> {
                    messageLog.getChildren().addAll(timestampText, iconText, titleText, contentText, new Text("\n"));
                    messageScrollPane.setVvalue(1.0);
                });
            }
            return;
        } 
        else if (message.contains("Message recu") || message.contains("recu de")) {
            // Créer une icône de réception et un texte coloré pour les messages de réception
            iconText = new Text("← ");
            iconText.setFill(Color.web("#2ecc71"));
            iconText.setStyle("-fx-font-weight: bold;");
            
            // Extraire l'ID source
            String sourceIdStr = "?";
            try {
                if (message.contains("de ")) {
                    sourceIdStr = message.substring(message.lastIndexOf("de ") + 3).trim();
                }
            } catch (Exception e) {
                sourceIdStr = "?";
            }
            
            String title = "RECEPTION: ";
            String content = "Message de P" + sourceIdStr;
            
            Text titleText = new Text(title);
            titleText.setFill(Color.web("#2ecc71"));
            titleText.setStyle("-fx-font-weight: bold;");
            
            Text contentText = new Text(content);
            contentText.setFill(Color.web("#2ecc71", 0.8));
            
            messageText = new Text("");  
            
            if (messageLog != null) {
                Platform.runLater(() -> {
                    messageLog.getChildren().addAll(timestampText, iconText, titleText, contentText, new Text("\n"));
                    messageScrollPane.setVvalue(1.0);
                });
            }
            return;
        }
        else if (message.contains("Horloge apres envoi")) {
            // Extraire et formater la valeur de l'horloge
            int index = message.indexOf("Horloge:");
            if (index < 0) {
                index = message.indexOf("apres envoi:");
                if (index < 0) {
                    index = message.indexOf("apres envoi");
                    if (index >= 0) {
                        index += 11;  
                    }
                }
            }
            
            iconText = new Text("⟳ ");
            iconText.setFill(Color.web("#3498db"));
            
            String clockValue = "";
            if (index >= 0) {
                if (message.contains("Horloge:")) {
                    clockValue = message.substring(index + 8).trim();
                } else {
                    clockValue = message.substring(index).trim();
                }
            }
            
            String title = "HORLOGE: ";
            String content = "Apres envoi: " + clockValue;
            
            Text titleText = new Text(title);
            titleText.setFill(Color.web("#3498db"));
            titleText.setStyle("-fx-font-weight: bold;");
            
            Text contentText = new Text(content);
            contentText.setFill(Color.web("#3498db", 0.8));
            
            messageText = new Text("");  
            
            // Mettre à jour TextFlow sur le thread JavaFX
            if (messageLog != null) {
                Platform.runLater(() -> {
                    messageLog.getChildren().addAll(timestampText, iconText, titleText, contentText, new Text("\n"));
                    // Défilement automatique vers le bas
                    messageScrollPane.setVvalue(1.0);
                });
            }
            return;
        }
        else if (message.contains("Mise a jour horloge apres reception")) {
            int index = message.indexOf("Horloge:");
            if (index < 0) {
                index = message.indexOf("apres reception");
                if (index >= 0) {
                    index += 15;  
                }
            }
            
            iconText = new Text("⟳ ");
            iconText.setFill(Color.web("#3498db"));
            
            String clockValue = "";
            if (index >= 0) {
                if (message.contains("Horloge:")) {
                    clockValue = message.substring(index + 8).trim();
                } else {
                    clockValue = message.substring(index).trim();
                }
            }
            
            String title = "HORLOGE: ";
            String content = "Apres reception: " + clockValue;
            
            Text titleText = new Text(title);
            titleText.setFill(Color.web("#3498db"));
            titleText.setStyle("-fx-font-weight: bold;");
            
            Text contentText = new Text(content);
            contentText.setFill(Color.web("#3498db", 0.8));
            
            messageText = new Text("");  
            
            // Mettre à jour TextFlow sur le thread JavaFX
            if (messageLog != null) {
                Platform.runLater(() -> {
                    messageLog.getChildren().addAll(timestampText, iconText, titleText, contentText, new Text("\n"));
                    messageScrollPane.setVvalue(1.0);
                });
            }
            return;
        }
        else if (message.contains("Instruction locale") || message.contains("LOCAL")) {
            iconText = new Text("● ");
            iconText.setFill(Color.web("#9b59b6"));
            iconText.setStyle("-fx-font-weight: bold;");
            
            String eventText = message;
            if (message.contains("[" + processId + "]")) {
                eventText = message.replace("[" + processId + "] ", "");
            }
            
            String title = "EVENEMENT LOCAL: ";
            String content = eventText;
            
            Text titleText = new Text(title);
            titleText.setFill(Color.web("#9b59b6"));
            titleText.setStyle("-fx-font-weight: bold;");
            
            Text contentText = new Text(content);
            contentText.setFill(Color.web("#9b59b6", 0.8));
            
            messageText = new Text("");  
            
            // Mettre à jour TextFlow sur le thread JavaFX
            if (messageLog != null) {
                Platform.runLater(() -> {
                    messageLog.getChildren().addAll(timestampText, iconText, titleText, contentText, new Text("\n"));
                    messageScrollPane.setVvalue(1.0);
                });
            }
            return;
        }
        else if (message.contains("Mise a jour horloge apres instruction locale")) {
            // Extraire et formater la valeur de l'horloge
            int index = message.indexOf("Horloge:");
            if (index < 0) {
                index = message.indexOf("apres instruction locale");
                if (index >= 0) {
                    index += 24; 
                }
            }
            
            iconText = new Text("⟳ ");
            iconText.setFill(Color.web("#3498db"));
            
            String clockValue = "";
            if (index >= 0) {
                if (message.contains("Horloge:")) {
                    clockValue = message.substring(index + 8).trim();
                } else {
                    clockValue = message.substring(index).trim();
                }
            }
            
            String title = "HORLOGE: ";
            String content = "Apres evenement: " + clockValue;
            
            Text titleText = new Text(title);
            titleText.setFill(Color.web("#3498db"));
            titleText.setStyle("-fx-font-weight: bold;");
            
            Text contentText = new Text(content);
            contentText.setFill(Color.web("#3498db", 0.8));
            
            messageText = new Text("");  
            
            // Mettre à jour TextFlow sur le thread JavaFX
            if (messageLog != null) {
                Platform.runLater(() -> {
                    messageLog.getChildren().addAll(timestampText, iconText, titleText, contentText, new Text("\n"));
                    messageScrollPane.setVvalue(1.0);
                });
            }
            return;
        }
        else {
            iconText = new Text("");
            messageText = new Text(message);
            messageText.setFill(Color.BLACK);
        }
        
        final Text newLine = new Text("\n");
        
        // Mettre à jour TextFlow sur le thread JavaFX
        if (messageLog != null) {
            Platform.runLater(() -> {
                messageLog.getChildren().addAll(timestampText, iconText, messageText, newLine);
                messageScrollPane.setVvalue(1.0);
            });
        }
    }
    
    //Définir le statut du processus
    public void setStatus(String status, Color color) {
        String frenchStatus = status;
        if (status.equals("Ready")) frenchStatus = "Pret";
        if (status.equals("Running")) frenchStatus = "En cours";
        if (status.equals("Completed")) frenchStatus = "Termine";
        if (status.equals("Failed")) frenchStatus = "En panne";
        
        statusLabel.setText(frenchStatus);
        statusLabel.setTextFill(color);
    }
    
    //Faire clignoter le cercle de processus pour indiquer l'activité
    public void flashActivity(ActivityType type) {
        Color color;
        switch (type) {
            case SENDING:
                color = SENDING_COLOR;
                break;
            case RECEIVING:
                color = RECEIVING_COLOR;
                break;
            case LOCAL:
                color = LOCAL_EVENT_COLOR;
                break;
            default:
                color = DEFAULT_COLOR;
                return; // Ne rien faire pour le cas par défaut
        }
        
        // Changer la couleur pour indiquer l'activité
        final Color finalColor = color;
        Platform.runLater(() -> {
            processCircle.setFill(finalColor);
            
            // Planifier le retour à la couleur par défaut après un délai
            new Thread(() -> {
                try {
                    Thread.sleep(800);
                    Platform.runLater(() -> {
                        processCircle.setFill(DEFAULT_COLOR);
                    });
                } catch (InterruptedException e) {
                    // Ignorer l'interruption
                }
            }).start();
        });
    }
    
    public int getProcessId() {
        return processId;
    }
    
    //Types d'activité de processus pour la visualisation
    public enum ActivityType {
        LOCAL,
        SENDING,
        RECEIVING
    }
} 