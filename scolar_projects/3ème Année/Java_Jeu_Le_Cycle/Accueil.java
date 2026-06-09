
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import javax.imageio.ImageIO;
import javax.swing.*;

public class Accueil extends JPanel {

    private String selectedMap = null;
    private Font gameFont;
    private BufferedImage backgroundImage;

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }

    public Accueil(JFrame frame) {
        try {
            backgroundImage = ImageIO.read(new File("images/background.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("aaaaaaaaaaaaa");
        }

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        Font gameFont;
        try {
            gameFont = Font.createFont(Font.TRUETYPE_FONT, new File("Metal_Mania/MetalMania-Regular.ttf"))
                    .deriveFont(Font.BOLD, 90f);
        } catch (Exception e) {
            gameFont = new Font("SansSerif", Font.BOLD, 30);
        }

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        ge.registerFont(gameFont);

        JLabel titre = new JLabel("Bienvenue dans Le Cycle");
        titre.setFont(gameFont.deriveFont(90f));
        //titre.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(-150, 0, 0, 0);
        add(titre, gbc);

        JButton map1Button = new JButton("Forêt");
        JButton map2Button = new JButton("Savane");
        JButton jouerButton = new JButton("Jouer");
        jouerButton.setEnabled(false);
        map1Button.setFont(gameFont.deriveFont(50f));
        map2Button.setFont(gameFont.deriveFont(50f));
        jouerButton.setFont(gameFont.deriveFont(50f));
        map1Button.setForeground(Color.WHITE);
        map2Button.setForeground(Color.WHITE);
        jouerButton.setForeground(Color.WHITE);
        map1Button.setBackground(Color.BLACK);
        map2Button.setBackground(Color.BLACK);
        jouerButton.setBackground(Color.BLACK);

        map1Button.addActionListener(e -> {
            selectedMap = "Map 1";
            jouerButton.setEnabled(true);
            map1Button.setBackground(Color.GREEN);
            map2Button.setBackground(Color.BLACK);
        });

        map2Button.addActionListener(e -> {
            selectedMap = "Map 2";
            jouerButton.setEnabled(true);
            map2Button.setBackground(Color.GREEN);
            map1Button.setBackground(Color.BLACK);
        });

        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(60, 150, 10, 10);
        add(map1Button, gbc);

        gbc.gridx = 1;
        add(map2Button, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 0, 0);
        add(jouerButton, gbc);
        // --- Nouveau : label meilleur temps ---
        JLabel meilleurTempsLabel = new JLabel();
        meilleurTempsLabel.setFont(gameFont.deriveFont(50f));
        meilleurTempsLabel.setForeground(Color.WHITE);

        long meilleurTemps = lireMeilleurTemps();
        if (meilleurTemps > 0) {
            meilleurTempsLabel.setText("Meilleur temps : " + meilleurTemps + " secondes");
        } else {
            meilleurTempsLabel.setText("Aucun meilleur temps enregistré");
        }
        gbc.gridy = 3;
        gbc.insets = new Insets(100, 0, 0, 0);
        add(meilleurTempsLabel, gbc);

        jouerButton.addActionListener(e -> {
            if (selectedMap != null) {
                frame.setContentPane(new Game(selectedMap, frame, System.currentTimeMillis()));
                frame.revalidate();
                frame.repaint();
            }
        });
    }

    private long lireMeilleurTemps() {
        File file = new File("meilleur_temps.txt");
        if (file.exists()) {
            try (Scanner scanner = new Scanner(file)) {
                if (scanner.hasNextLong()) {
                    return scanner.nextLong();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }
}
