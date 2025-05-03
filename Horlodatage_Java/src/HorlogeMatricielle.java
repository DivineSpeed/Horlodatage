import java.util.Arrays;

// Implémentation d'une horloge matricielle
public class HorlogeMatricielle implements Horloge {
    private int[][] matrice;
    private int idProcessus;

    public HorlogeMatricielle(int nbProcessus, int idProcessus) {
        matrice = new int[nbProcessus][nbProcessus];
        this.idProcessus = idProcessus;
    }

    @Override
    public void evenementLocal() {
        matrice[idProcessus][idProcessus]++;
    }

    @Override
    public void envoyerMessage(int destId) {
        matrice[idProcessus][idProcessus]++;
        if(idProcessus != destId){
            matrice[idProcessus][destId]++;
        }
    }

    @Override
    public void recevoirMessage(String message) {
        String[] lignes = message.split(";");
        int[][] matriceRecue = new int[matrice.length][matrice.length];
        
        // Conversion du message en matrice
        for (int i = 0; i < matrice.length; i++) {
            String[] valeurs = lignes[i].split(",");
            for (int j = 0; j < matrice[i].length; j++) {
                matriceRecue[i][j] = Integer.parseInt(valeurs[j].trim());
            }
        }
        
        // Mise à jour élément par élément avec le max
        for (int i = 0; i < matrice.length; i++) {
            for (int j = 0; j < matrice[i].length; j++) {
                matrice[i][j] = Math.max(matrice[i][j], matriceRecue[i][j]);
            }
        }
        
        // Incrémentation après réception
        matrice[idProcessus][idProcessus]++;
    }

    @Override
    public String getHorloge() {
        return Arrays.deepToString(matrice);
    }

    // Format la matrice pour l'envoi
    public String getHorlogePourEnvoi() {
        StringBuilder sb = new StringBuilder();
        for (int[] ligne : matrice) {
            for (int val : ligne) {
                sb.append(val).append(",");
            }
            sb.setLength(sb.length() - 1);
            sb.append(";");
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }
}
