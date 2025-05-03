package gui;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import javafx.application.Platform;


//Classe adaptateur pour connecter l'interface graphique avec les implémentations d'horloge existantes

public class AdaptateurHorloge {
    
    private static Map<Integer, Thread> runningProcesses = new HashMap<>();
    private static Map<Integer, Process> processHandles = new HashMap<>();
    private static Map<Integer, VisualiseurProcessus> visualizers = new HashMap<>();
    
    // Valeurs de délai par défaut
    private static long eventDelay = 1000; // ms
    private static long messageDelay = 3000; // ms
    
    
    //Définir les paramètres de vitesse de simulation
    public static void setDelays(long newEventDelay, long newMessageDelay) {
        eventDelay = newEventDelay;
        messageDelay = newMessageDelay;
    }
    
    
    //Enregistre un visualiseur de processus pour recevoir les mises à jour
    public static void registerVisualizer(int id, VisualiseurProcessus visualizer) {
        visualizers.put(id, visualizer);
    }
    
   
    public static boolean startProcess(int id, int clockType) {
        try {
            // Construire la commande de processus
            ProcessBuilder builder = new ProcessBuilder(
                "java", 
                "-cp", 
                System.getProperty("java.class.path"),
                "-Dfile.encoding=UTF-8",
                "Main",
                String.valueOf(id),  // Passer id 
                String.valueOf(clockType),  // Passer clockType 
                String.valueOf(eventDelay),  // Passer event delay 
                String.valueOf(messageDelay)  // Passer message delay 
            );
            
            
            // Démarrer le processus
            Process process = builder.start();
            processHandles.put(id, process);
            
            // Créer un thread pour gérer les E/S du processus
            Thread ioThread = new Thread(() -> {
                try {
                    // Lire et transmettre la sortie pour la journalisation et les mises à jour de l'interface
                    Scanner scanner = new Scanner(
                        new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8)
                    ).useDelimiter("\\A");
                    
                    while (scanner.hasNextLine()) {
                        String line = scanner.nextLine();
                        final String outputLine = line;
                        System.out.println("[Process " + id + "] " + line);
                        
                        // Mettre à jour l'interface si le visualiseur existe
                        Platform.runLater(() -> {
                            VisualiseurProcessus visualizer = visualizers.get(id);
                            if (visualizer != null) {
                                // Enregistrer le message
                                visualizer.addLogMessage(outputLine);
                                
                                // Extraire et mettre à jour la valeur de l'horloge si présente
                                if (outputLine.contains("Horloge:")) {
                                    String clockValue = outputLine.substring(outputLine.indexOf("Horloge:") + 9).trim();
                                    visualizer.updateClockDisplay(clockValue);
                                }
                                
                                // Détecter les types de messages pour la visualisation d'activité (distinct des mises à jour d'horloge)
                                if (outputLine.contains("Message envoye") || outputLine.contains("envoy")) {
                                    visualizer.flashActivity(VisualiseurProcessus.ActivityType.SENDING);
                                    
                                
                                } 
                                else if (outputLine.contains("Message recu")) {
                                    visualizer.flashActivity(VisualiseurProcessus.ActivityType.RECEIVING);
                                } 
                                else if (outputLine.contains("Instruction locale")) {
                                    visualizer.flashActivity(VisualiseurProcessus.ActivityType.LOCAL);
                                }
                            }
                        });
                    }
                    
                    // Vérifier si le processus s'est terminé normalement
                    int exitCode = process.waitFor();
                    if (exitCode == 0) {
                        Platform.runLater(() -> {
                            VisualiseurProcessus visualizer = visualizers.get(id);
                            if (visualizer != null) {
                                visualizer.setStatus("Completed", javafx.scene.paint.Color.BLUE);
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            
            ioThread.setDaemon(true);
            ioThread.start();
            
            runningProcesses.put(id, ioThread);
            
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    
    //Démarre plusieurs processus dans la séquence appropriée
    public static void startAllProcesses(int numProcesses, int clockType) {
        // Démarrer tous les processus avec un petit délai entre eux
        for (int i = 0; i < numProcesses; i++) {
            final int id = i;
            new Thread(() -> {
                try {
                    Thread.sleep(id * 1000); // Démarrage échelonné pour éviter les conditions de course
                    startProcess(id, clockType);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
    
    
    //Arrête tous les processus en cours d'exécution
    public static void stopAllProcesses() {
        for (Integer id : processHandles.keySet()) {
            Process process = processHandles.get(id);
            if (process != null) {
                try {
                    // Terminer gracieusement si possible
                    process.destroy();
                    
                    // Attendre brièvement que le processus se termine
                    if (!process.waitFor(1000, java.util.concurrent.TimeUnit.MILLISECONDS)) {
                        // Si toujours en cours d'exécution, forcer la terminaison
                        process.destroyForcibly();
                        System.out.println("Processus " + id + " terminé de force");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        
        //Attendre un moment pour s'assurer que les sockets sont libérés
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // Vider les collections
        processHandles.clear();
        runningProcesses.clear();
        
        System.out.println("Tous les processus sont arrêtés et les ressources libérées");
    }
    
    
    //Simule une défaillance de processus
    public static void crashProcess(int id) {
        Process process = processHandles.get(id);
        if (process != null) {
            process.destroy();
            processHandles.remove(id);
            runningProcesses.remove(id);
            
            System.out.println("Processus " + id + " a subi une panne simulée");
        }
    }
    
    //Récupère un processus défaillant
    public static void recoverProcess(int id, int clockType) {
        // Redémarrer le processus
        boolean success = startProcess(id, clockType);
        
        if (success) {
            System.out.println("Processus " + id + " a été récupéré avec succès");
        } else {
            System.err.println("Échec de la récupération du processus " + id);
        }
    }
    
    //Convertit la chaîne de type d'horloge de l'interface en valeur numérique utilisée dans Main
    public static int getClockTypeValue(String clockTypeString) {
        switch (clockTypeString) {
            case "Horloge Scalaire":
                return 1;
            case "Horloge Vectorielle":
                return 2;
            case "Horloge Matricielle":
                return 3;
            default:
                return 1; // Par défaut à scalaire
        }
    }
    
    //Convertit le type d'horloge numérique en chaîne d'interface
    public static String getClockTypeString(int clockType) {
        switch (clockType) {
            case 1:
                return "Horloge Scalaire";
            case 2:
                return "Horloge Vectorielle";
            case 3:
                return "Horloge Matricielle";
            default:
                return "Horloge Scalaire";
        }
    }
} 