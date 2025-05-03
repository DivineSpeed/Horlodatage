import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

public class Processus {
    private int id;
    private int port;
    private Horloge horloge;
    private int nbProcessus;
    private long delayBetweenEvents = 500; // délai par défaut entre événements (ms)
    private long delayBetweenMessages = 2000; // délai par défaut entre messages (ms)
    private AtomicBoolean running = new AtomicBoolean(true);
    private ServerSocket serverSocket;

    public Processus(int id, int port, Horloge horloge, int nbProcessus) {
        this(id, port, horloge, nbProcessus, 500, 2000);
    }

    public Processus(int id, int port, Horloge horloge, int nbProcessus, long delayBetweenEvents, long delayBetweenMessages) {
        this.id = id;
        this.port = port;
        this.horloge = horloge;
        this.nbProcessus = nbProcessus;
        this.delayBetweenEvents = delayBetweenEvents;
        this.delayBetweenMessages = delayBetweenMessages;
        
        // Enregistre un hook pour fermer les ressources
        Runtime.getRuntime().addShutdownHook(new Thread(this::arreter));
    }

    public void demarrer() {
        Thread serverThread = new Thread(this::demarrerServeur);
        serverThread.setDaemon(true); // Thread daemon pour permettre l'arrêt du JVM
        serverThread.start();
        
        try {
            Thread.sleep(2000); // délai pour lancer le serveur

            // Instructions locales
            for (int i = 0; i < 5 && running.get(); i++) {
                instructionLocale("Instruction locale " + (i + 1));
                // Mise à jour de l'horloge
                horloge.evenementLocal();
                System.out.println("[" + id + "] Mise a jour horloge apres instruction locale " + (i + 1) + " | Horloge: " + horloge.getHorloge());
                
                // Délai entre événements
                Thread.sleep(delayBetweenEvents);
            }

            // Emission de messages
            for (int i = 0; i < 4 && running.get(); i++) {
                int dest = (id + i + 1) % nbProcessus;
                envoyerMessage(dest);
                
                // Délai entre messages
                Thread.sleep(delayBetweenMessages);
            }

            System.out.println("[" + id + "] Processus termine normalement.");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("[" + id + "] Processus interrompu.");
        } finally {
            // Libération des ressources
            arreter();
        }
    }
    
    public void arreter() {
        running.set(false);
        
        // Fermeture du socket serveur
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
                System.out.println("[" + id + "] Serveur socket ferme.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void instructionLocale(String action) {
        System.out.println("[" + id + "] " + action);
    }

    private void envoyerMessage(int destId) {
        if (!running.get()) return;
        
        int destPort = 5000 + destId;
        int tentative = 0;
        int maxTentatives = 15;

        while (tentative < maxTentatives && running.get()) {
            try (Socket socket = new Socket("localhost", destPort)) {
                PrintWriter out = new PrintWriter(
                    new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8),
                    true
                );

                // Mise à jour de l'horloge avant envoi
                horloge.envoyerMessage(destId);

                // Préparation du message
                String horlogeMessage;
                if (horloge instanceof HorlogeVectorielle) {
                    horlogeMessage = ((HorlogeVectorielle) horloge).getHorlogePourEnvoi();
                } else if (horloge instanceof HorlogeMatricielle) {
                    horlogeMessage = ((HorlogeMatricielle) horloge).getHorlogePourEnvoi();
                } else {
                    horlogeMessage = horloge.getHorloge();
                }

                // Envoi du message
                String fullMessage = id + "|" + horlogeMessage;
                out.println(fullMessage);

                System.out.println("[" + id + "] Message envoye a " + destId);
                System.out.println("[" + id + "] Horloge apres envoi: " + horloge.getHorloge());
                break; // Sortie si succès

            } catch (IOException e) {
                tentative++;
                if (tentative >= maxTentatives) {
                    System.err.println("[" + id + "] Erreur : Impossible de se connecter a " + destId + " après " + tentative + " tentatives.");
                } else {
                    System.out.println("[" + id + "] Tentative " + tentative + " echouee vers " + destId + ". Reessai...");
                    try {
                        Thread.sleep(500); // Attente avant réessai
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
    }

    private void demarrerServeur() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("[" + id + "] Serveur demarre sur le port " + port);
            
            while (running.get()) {
                try {
                    // Timeout pour vérifier périodiquement le flag running
                    serverSocket.setSoTimeout(1000);
                    Socket clientSocket = serverSocket.accept();
                    
                    // Traitement dans un nouveau thread
                    new Thread(() -> traiterReception(clientSocket)).start();
                } catch (SocketTimeoutException e) {
                    // Attendu en raison du timeout
                    continue;
                } catch (IOException e) {
                    if (running.get()) {
                        // Log seulement si pas en cours d'arrêt
                        e.printStackTrace();
                    }
                    break;
                }
            }
        } catch (IOException e) {
            if (running.get()) {
                // Log seulement si pas en cours d'arrêt
                System.err.println("[" + id + "] Erreur lors du demarrage du serveur: " + e.getMessage());
                e.printStackTrace();
            }
        } finally {
            if (serverSocket != null && !serverSocket.isClosed()) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void traiterReception(Socket socket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {
            String fullMessage = in.readLine();
            if (fullMessage != null) {
                // Extraction de l'expéditeur et du contenu
                String[] parts = fullMessage.split("\\|", 2);
                int senderId = Integer.parseInt(parts[0]);
                String horlogeMessage = parts[1];

                System.out.println("[" + id + "] Message recu de " + senderId);
                
                // Mise à jour de l'horloge
                horloge.recevoirMessage(horlogeMessage);
                System.out.println("[" + id + "] Mise a jour horloge apres reception | Horloge: " + horloge.getHorloge());
            }
        } catch (IOException e) {
            if (running.get()) {
                // Log seulement si pas en cours d'arrêt
                e.printStackTrace();
            }
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                // Ignorer lors de l'arrêt
                if (running.get()) {
                    e.printStackTrace();
                }
            }
        }
    }
}
