import java.util.Arrays;

// Implémentation d'une horloge vectorielle
public class HorlogeVectorielle implements Horloge {
    private int[] vecteur;
    private int idProcessus;

    public HorlogeVectorielle(int nbProcessus, int idProcessus) {
        vecteur = new int[nbProcessus];
        this.idProcessus = idProcessus;
    }

    @Override
    public void evenementLocal() {
        vecteur[idProcessus]++;
    }

    @Override
    public void envoyerMessage(int destId) {
        vecteur[idProcessus]++;
    }

    @Override
    public void recevoirMessage(String message) {
        String[] parts = message.split(",");
        if (parts.length != vecteur.length) {
            throw new IllegalArgumentException("Message vectoriel invalide : tailles différentes.");
        }
        // Mise à jour selon la règle max(local[i], reçu[i])
        for (int i = 0; i < vecteur.length; i++) {
            vecteur[i] = Math.max(vecteur[i], Integer.parseInt(parts[i].trim()));
        }
        vecteur[idProcessus]++;
    }

    @Override
    public String getHorloge() {
        return Arrays.toString(vecteur);
    }

    // Format le vecteur pour l'envoi sans crochets
    public String getHorlogePourEnvoi() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < vecteur.length; i++) {
            sb.append(vecteur[i]);
            if (i < vecteur.length - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }
}
