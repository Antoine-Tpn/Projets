
import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Le Cycle");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // Plein écran
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.setUndecorated(false); // mettre true si tu veux vraiment en full sans bordures

            Accueil accueil = new Accueil(frame);
            frame.setContentPane(accueil);
            frame.setVisible(true);
        });
    }
}
