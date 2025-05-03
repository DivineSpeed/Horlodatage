import java.util.concurrent.atomic.AtomicInteger;

// Implémentation d'une horloge logique scalaire de Lamport
public class HorlogeScalaire implements Horloge {
    private AtomicInteger horloge = new AtomicInteger(0);

    @Override
    public void evenementLocal() {
        horloge.incrementAndGet();
    }

    @Override
    public void envoyerMessage(int destId) {
        horloge.incrementAndGet();
    }

    @Override
    public void recevoirMessage(String message) {
        // Mise à jour selon la règle de Lamport: max(local, reçu) + 1
        int horlogeRecue = Integer.parseInt(message.trim());
        horloge.set(Math.max(horloge.get(), horlogeRecue) + 1);
    }

    @Override
    public String getHorloge() {
        return String.valueOf(horloge.get());
    }
}
