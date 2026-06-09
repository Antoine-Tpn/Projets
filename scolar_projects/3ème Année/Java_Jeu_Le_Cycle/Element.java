
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;

public class Element extends Boids {

    private Species species;
    private String sexe;
    private int x, y;
    private int dx;
    private int dy;
    private boolean faim;  // Indicateur de faim
    public int pv;  // Points de vie
    private BufferedImage image;
    private int alpha;
    private int tickCounter = 0;
    private int cond_faim = 80;
    private double dist_chasse = 200;
    private double dist_fuite = 150.0;
    private double dist_eat = 10.0;

    public Element(int x, int y, String sexe, int pv, Species species, int alpha, String mapName) {
        super();
        this.x = x;
        this.y = y;
        this.sexe = sexe;
        this.species = species;
        this.faim = false;
        this.pv = pv;
        this.alpha = alpha;
        this.dx = 1;
        this.dy = 1;
        this.alpha = alpha;
        // Chargement de l’image selon l’espèce
        try {
            String imagePath = "";

            if (species.name.equals("Herbe")) {
                switch (mapName) {
                    case "Map 1":
                        imagePath = "/images/buisson1.png";
                        break;
                    case "Map 2":
                        imagePath = "/images/buisson_savane.png";
                        break;
                    default:
                        imagePath = "/images/buisson1.png";
                }
            } else if (species.name.equals("Lapin")) {
                switch (mapName) {
                    case "Map 1":
                        imagePath = "/images/lapin.png";
                        break;
                    case "Map 2":
                        imagePath = "/images/antilope.png";
                        break;
                    default:
                        imagePath = "/images/lapin.png";
                }
            } else if (species.name.equals("Loup")) {
                switch (mapName) {
                    case "Map 1":
                        imagePath = "/images/loup.png";
                        break;
                    case "Map 2":
                        imagePath = "/images/lion.png";
                        break;
                    default:
                        imagePath = "/images/loup.png";
                }
            }

            image = ImageIO.read(getClass().getResource(imagePath));
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Erreur chargement image : " + e.getMessage());
            image = null;
        }
    }

    public String getSexe() {
        return this.sexe;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public boolean isFaim() {
        return this.faim;  // Getter pour faim
    }

    public void setFaim(boolean faim) {
        this.faim = faim;  // Setter pour faim
    }

    public Species getSpecies() {
        return this.species;  // Getter pour species
    }

    public void setSpecies(Species species) {
        this.species = species;  // Setter pour species
    }

    public boolean peutInteragir(Element autre) {
        // Même espèce
        if (this.species.name.equals(autre.species.name)) {
            return true;
        } else {
            return false;
        }
    }

    public int getAlpha() {
        return this.alpha;
    }

    public int getDx() {
        return this.dx;
    }

    public int getDy() {
        return this.dy;
    }

    public void move(ArrayList<Element> others) {
        // 1. FUITE : si un prédateur est proche alors je fuie
        for (Element other : others) {
            if (other == this) {
                continue;
            }
            if (other.species.food.equals(this.species.name)) {  // L'autre est un prédateur
                double distance = getDistance(this, other);
                if (distance < dist_fuite) {
                    this.dx = Integer.signum(this.x - other.x);
                    this.dy = Integer.signum(this.y - other.y);
                    this.x += this.dx * species.speed;
                    this.y += this.dy * species.speed;
                    return;  // La fuite est prioritaire
                }
            }
        }

        // 2. CHASSE & MANGER : si une proie est proche et que j'ai faim alors je mange
        if (this.isFaim() == true) {
            //System.out.println(this.species+" "+this.faim);
            Element proie = null;
            double distanceMin = Double.MAX_VALUE;

            // Trouver la proie la plus proche
            for (Element other : others) {
                if (other == this) {
                    continue;
                }
                if (this.species.food.equals(other.species.name)) {
                    double distance = getDistance(this, other);
                    if (distance < distanceMin && distance < dist_chasse) {
                        distanceMin = distance;
                        proie = other;
                    }
                }
            }
            // Chasser la proie la plus proche
            if (proie != null) {
                double distance = getDistance(this, proie);
                this.dx = Integer.signum(proie.x - this.x);
                this.dy = Integer.signum(proie.y - this.y);

                if (distance < dist_eat) {
                    this.manger();
                    proie.pv = 0;
                    this.x += this.dx * species.speed;
                    this.y += this.dy * species.speed;
                    return;
                } else {
                    this.x += this.dx * species.speed * 2;
                    this.y += this.dy * species.speed * 2;
                    return;
                }
            }
        }

        // 3. BOID : Sinon déplacement en boids, en troupeau
        if (alpha == 0) {
            this.dx = this.getDx();
            this.dy = this.getDy();
        } else {
            Point movement = getBoid(this, others);
            if (movement.x == 0 && movement.y == 0) {
                if (Math.random() < 0.3) {
                    movement.x = (int) (Math.random() * 3) - 1;
                    movement.y = (int) (Math.random() * 3) - 1;
                }
            }
            if (dx == 0 && dy == 0) {
                System.out.println("erreur de move");
            }
            this.dx = movement.x;
            this.dy = movement.y;
        }
        this.x += this.dx * species.speed;
        this.y += this.dy * species.speed;
    }

    protected double getDistance(Element a, Element b) {
        int dx = a.getX() - b.getX();
        int dy = a.getY() - b.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    public void updateFaim() {
        // L'animal a faim si ses PV sont inférieurs à un certain seuil
        if (this.pv < cond_faim) {
            this.setFaim(true);
        } else {
            setFaim(false);
        }
    }

    public void manger() {
        if (species.name.equals("Lapin")) {
            this.pv = Math.min(this.pv + 20, 100);
            updateFaim();
        } else {
            this.pv = Math.min(this.pv + 30, 100);
            updateFaim();
        }
    }

    public void bounce(int screenWidth, int screenHeight) {
        if (this.x < 0 || this.x > screenWidth - 10) {
            this.dx *= -1;
            this.x = Math.max(0, Math.min(this.x, screenWidth - 10));
        }
        if (this.y < 0 || this.y > screenHeight - 10) {
            this.dy *= -1;
            this.y = Math.max(0, Math.min(this.y, screenHeight - 10));
        }
    }

    public void show(Graphics g) {
        if (image != null) {
            g.drawImage(image, this.x, this.y, 40, 40, null);
        } else {
            g.setColor(Color.GRAY);
            g.fillOval(this.x, this.y, 40, 40);
        }
    }

    public void updatePv() {  // produit une réduction de PV en fonction du temps (produit la faim)
        if (species.name.equals("Lapin")) {
            tickCounter++;
            if (tickCounter % 20 == 0) {
                this.pv--;
            }
        }
        if (species.name.equals("Loup")) {
            tickCounter++;
            if (tickCounter % 15 == 0) {
                this.pv--;
            }
        }
    }
}
