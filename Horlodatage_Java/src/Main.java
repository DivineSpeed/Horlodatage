import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // Configuration pour l'encodage UTF-8
        System.setProperty("file.encoding", "UTF-8");
        
        Scanner sc = new Scanner(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        int id;
        int choix;
        long eventDelay = 500;  // Délai entre événements par défaut
        long msgDelay = 2000;   // Délai entre messages par défaut
        
        // Vérification des arguments (pour interface graphique)
        if (args.length >= 2) {
            id = Integer.parseInt(args[0]);
            choix = Integer.parseInt(args[1]);
            
            // Vérification des délais personnalisés
            if (args.length >= 4) {
                eventDelay = Long.parseLong(args[2]);
                msgDelay = Long.parseLong(args[3]);
            }
            
            System.out.println("Demarrage du processus " + id + " avec type d'horloge " + choix + 
                " (delai evenement: " + eventDelay + "ms, delai message: " + msgDelay + "ms)");
        } else {
            // Mode interactif
            System.out.print("Entrez ID du processus (0 à 3): ");
            id = sc.nextInt();
            
            System.out.println("Choisissez l'horloge:");
            System.out.println("1 - Scalaire");
            System.out.println("2 - Vectorielle");
            System.out.println("3 - Matricielle");
            
            choix = sc.nextInt();
            
            System.out.print("Delai entre evenements (ms): ");
            eventDelay = sc.nextLong();
            
            System.out.print("Delai entre messages (ms): ");
            msgDelay = sc.nextLong();
        }
        
        int port = 5000 + id;
        Horloge horloge;
        int nbProcessus = 4;

        // Création de l'horloge selon le choix
        if (choix == 1) {
            horloge = new HorlogeScalaire();
            System.out.println("Utilisation d'horloge scalaire pour processus " + id);
        } else if (choix == 2) {
            horloge = new HorlogeVectorielle(nbProcessus, id);
            System.out.println("Utilisation d'horloge vectorielle pour processus " + id);
        } else {
            horloge = new HorlogeMatricielle(nbProcessus, id);
            System.out.println("Utilisation d'horloge matricielle pour processus " + id);
        }

        // Démarrage du processus
        Processus p = new Processus(id, port, horloge, nbProcessus, eventDelay, msgDelay);
        p.demarrer();
    }
}
