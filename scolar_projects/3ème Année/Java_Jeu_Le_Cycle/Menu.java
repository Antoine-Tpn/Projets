
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import javax.swing.*;

public class Menu extends JPanel {

    private JButton boutonJouer;
    private JButton boutonOptions;
    private JButton boutonQuitter;
    private JLabel labelTemps;

    private long meilleurTemps = 0;

    public Menu(JFrame frame) {
        // Lecture du fichier pour récupérer le meilleur temps
        try {
            File file = new File("meilleur_temps.txt");
            if (file.exists()) {
                Scanner scanner = new Scanner(file);
                if (scanner.hasNextLong()) {
                    meilleurTemps = scanner.nextLong();
                }
                scanner.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Mise en page
        setLayout(new BorderLayout());
        setBackground(Color.LIGHT_GRAY);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Création des boutons
        boutonJouer = new JButton("Jouer");
        boutonOptions = new JButton("Options");
        boutonQuitter = new JButton("Quitter");

        // Ajout des boutons à un panneau vertical
        JPanel panelBoutons = new JPanel(new GridLayout(3, 1, 10, 10));
        panelBoutons.add(boutonJouer);
        panelBoutons.add(boutonOptions);
        panelBoutons.add(boutonQuitter);

        // Ajout du panneau des boutons à gauche
        add(panelBoutons, BorderLayout.WEST);

        // Création et configuration du label de temps
        long minutes = meilleurTemps / 60;
        long secondes = meilleurTemps % 60;
        labelTemps = new JLabel("⏱ Meilleur temps : " + minutes + " min " + secondes + " sec");
        labelTemps.setFont(new Font("Arial", Font.PLAIN, 14));
        labelTemps.setHorizontalAlignment(SwingConstants.RIGHT);

        // Ajout du label à droite
        add(labelTemps, BorderLayout.EAST);

        // Action du bouton Jouer
        boutonJouer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                long startTime = System.currentTimeMillis();

                Game gamePanel = new Game();  // passe le startTime
                frame.setContentPane(gamePanel);
                frame.revalidate();
                frame.repaint();
            }
        });

        // Action du bouton Quitter
        boutonQuitter.addActionListener(e -> System.exit(0));
    }
}
