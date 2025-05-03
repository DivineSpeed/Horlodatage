public interface Horloge {
    // Incrémente l'horloge lors d'un événement local
    void evenementLocal();
    // Prépare l'horloge pour l'envoi d'un message
    void envoyerMessage(int destId);
    // Met à jour l'horloge lors de la réception d'un message
    void recevoirMessage(String message);
    // Renvoie la représentation de l'horloge
    String getHorloge();
}
