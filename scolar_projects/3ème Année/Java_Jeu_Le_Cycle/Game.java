
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import javax.swing.*;

public class Game extends JPanel {

    private final ArrayList<Element> elements = new ArrayList<>();
    ArrayList<Element> toRemove = new ArrayList<>();
    private final Random random = new Random();
    private int screenWidth;
    private int screenHeight;
    private long startTime;
    private JFrame frame;
    private JPanel simulationPanel;
    private JLabel timeLabel;
    private String mapName;
    private static int i, j, k, l = 0;
    private int nb_p,nb_car,nb_herb =5;

    public Game(String mapName, JFrame frame, long startTime) {
        this.frame = frame;
        this.mapName = mapName;
        this.startTime = System.currentTimeMillis();

        setLayout(new BorderLayout());

        // Charger l'image de fond
        String cheminImage;

        switch (this.mapName) {
            case "Map 1":
                cheminImage = "images/Foret.jpg";
                break;
            case "Map 2":
                cheminImage = "images/savane.png";
                break;
            default:
                cheminImage = "images/Froet.jpg";
                break;
        }

        ImageIcon fondIcon = new ImageIcon(cheminImage);
        Image fondImage = fondIcon.getImage();

        // Panel principal de simulation
        JPanel simulationPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Dessiner l'image de fond
                g.drawImage(fondImage, 0, 0, getWidth(), getHeight(), this);

                // Dessiner les éléments du jeu
                for (Element e : elements) {
                    e.show(g);
                }
            }
        };
        simulationPanel.setBackground(Color.WHITE);
        this.add(simulationPanel, BorderLayout.CENTER);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        screenWidth = screenSize.width;
        screenHeight = screenSize.height - 100;

        if ("Map 1".equals(mapName)) {
            nb_p=5;
            nb_car=5;
            nb_herb =5;
            genererMap1();
        } else {
            nb_p=5;
            nb_car=5;
            nb_herb =5;
            genererMap2();
        }

        // Barre de boutons en haut
        JPanel buttonPanel = new JPanel();
        JButton btnHerbe = new JButton("Ajouter Herbe");
        JButton btnHerbivore = new JButton("Ajouter Herbivore");
        JButton btnCarnivore = new JButton("Ajouter Carnivore");
        JButton btnAccueil = new JButton("Retour à l'accueil");

        btnHerbe.addActionListener(e -> addElement("Herbe"));
        btnHerbivore.addActionListener(e -> addElement("Herbivore"));
        btnCarnivore.addActionListener(e -> addElement("Carnivore"));
        btnAccueil.addActionListener(e -> {
            long elapsed = (System.currentTimeMillis() - startTime) / 1000;

            // Sauvegarde du meilleur temps dans fichier
            try {
                File file = new File("meilleur_temps.txt");
                long ancienTemps = 0;

                if (file.exists()) {
                    Scanner scanner = new Scanner(file);
                    if (scanner.hasNextLong()) {
                        ancienTemps = scanner.nextLong();
                    }
                    scanner.close();
                }

                if (elapsed > ancienTemps) {
                    PrintWriter writer = new PrintWriter(file);
                    writer.println(elapsed);
                    writer.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            // Retour à l'accueil
            frame.setContentPane(new Accueil(frame));
            frame.revalidate();
            frame.repaint();
        });

        buttonPanel.add(btnHerbe);
        buttonPanel.add(btnHerbivore);
        buttonPanel.add(btnCarnivore);
        buttonPanel.add(btnAccueil);

        timeLabel = new JLabel("Temps : 0 s");
        timeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        timeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        buttonPanel.add(timeLabel, BorderLayout.EAST);
        this.add(buttonPanel, BorderLayout.NORTH);

        // Timer qui met à jour la simulation et le temps
        Timer timer = new Timer(30, e -> {
            update();
            simulationPanel.repaint();

            long elapsed = (System.currentTimeMillis() - startTime) / 1000;
            timeLabel.setText("Temps : " + elapsed + " s");
        });
        timer.start();
    }

    public String getMapName() {
        return mapName;
    }

    public void genererMap1() {
        for (int i = 0; i < 25; i++) {
            addElement("Herbe");
        }
        for (int i = 0; i < 10; i++) {
            addElement("Herbivore");
        }
        for (int i = 0; i < 4; i++) {
            addElement("Carnivore");
        }
    }

    public void genererMap2() {
        for (int i = 0; i < 25; i++) {
            addElement("Herbe");
        }
        for (int i = 0; i < 10; i++) {
            addElement("Herbivore");
        }
        for (int i = 0; i < 4; i++) {
            addElement("Carnivore");
        }
    }

    public Game() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        screenWidth = screenSize.width;
        screenHeight = screenSize.height - 100;
        // Exemple d'éléments initiaux
        addElement("Herbe");
        addElement("Carnivore");
        addElement("Herbivore");
        Timer timer = new Timer(30, e -> {
            update();
        });
        timer.start();
    }

    public void addElement(String type) {
        int x = random.nextInt(screenWidth - 10);
        int y = random.nextInt(screenHeight - 10);
        Species species;
        switch (type) {
            case "Herbe":
                species = new Species("Herbe", "None", 0.0f);
                    break;
            case "Carnivore":
                species = new Species("Loup", "Lapin", 1.8f);
                break;
            case "Herbivore":
            default:
                species = new Species("Lapin", "Herbe", 1.2f);
                break;
        }
        // Choix aléatoire du sexe
        String sexe = random.nextBoolean() ? "F" : "M";
        int alpha = random.nextInt(2);
        if (type.equals("Herbe") && nb_p>=2){
            elements.add(new Element(x, y, sexe, 100, species, alpha, this.mapName));
        }
        if (type.equals("Carnivore") && nb_car>=2){
            elements.add(new Element(x, y, sexe, 100, species, alpha, this.mapName));
        }
        if (type.equals("Herbivore") && nb_herb>=2){
            elements.add(new Element(x, y, sexe, 100, species, alpha, this.mapName));
        }
    }

    public void update() {
        nb_p=0;
        nb_car=0;
        nb_herb=0;
        for (Element e : elements) {
            if (e.getSpecies().name.equals("Herbe")) {
                nb_p+=1;
            }
            if (e.getSpecies().name.equals("Loup")) {
                nb_car+=1;
            }
            if (e.getSpecies().name.equals("Lapin")) {
                nb_herb+=1;
            }
            e.updateFaim();
            e.move(elements);
            e.bounce(screenWidth, screenHeight);
            e.updatePv();
            if (e.pv <= 0) {
                toRemove.add(e);
            }
        }
        count();
        // Supprimer les éléments morts (herbivores mangés + l'herbe)
        elements.removeAll(toRemove);
        toRemove = new ArrayList<>();
        repaint();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (Element e : elements) {
            e.show(g);
        }
    }

    public Element randomDeath(ArrayList<Element> elements, String specie) {
        ArrayList<Element> candidats = new ArrayList<>();
        for (Element e : elements) {
            if (e.getSpecies().name.equals(specie)) {
                candidats.add(e);
            }
        }
        if (!candidats.isEmpty()) {
            Random rand = new Random();
            return candidats.get(rand.nextInt(candidats.size()));
        }
        return null;
    }

    public void count() {
        if (i == 60) {
            i = 0;
            addElement("Herbe");
        }
        if (j == 80) {
            j = 0;
            addElement("Herbivore");
        }
        if (k == 120) {
            k = 0;
            addElement("Carnivore");
        } else {
            i += 1;
            j += 1;
            k += 1;
        }
        if (l == 180) {
            l = 0;
            toRemove.add(randomDeath(elements, "Loup"));
        } else {
            l += 1;
        }
    }
}
