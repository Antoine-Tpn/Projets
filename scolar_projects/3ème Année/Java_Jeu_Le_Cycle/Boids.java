
import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class Boids {

    protected double radius_Separation = 50.0;     // Zone d'espacement
    protected double radius_Cohesion = 80.0;       // Zone de rapprochement
    protected double radius_Alignment = 150.0;     // Zone d'alignement
    private int dx, dy;
    private final Random random = new Random();

    public Point getBoid(Element me, ArrayList<Element> others) {
        dx = me.getDx();
        dy = me.getDy();

        for (Element e : others) {
            if (e == me || !me.getSpecies().name.equals(e.getSpecies().name)) {
                continue;
            }

            int distX = e.getX() - me.getX();
            int distY = e.getY() - me.getY();
            double distance = Math.sqrt(distX * distX + distY * distY);

            if (distance < radius_Separation) {
                dx -= distX / 2;
                dy -= distY / 2;

            } else if (distance < radius_Cohesion) {
                dx += distX;//+ distX>0?random.nextInt(distX+1):-random.nextInt(-distX+1);
                dy += distY;//+ distY>0?random.nextInt(distY+1):-random.nextInt(-distY+1);
            } else if (distance < radius_Alignment) {
                dx += distX;//+ distX>0?random.nextInt(distX+1):-random.nextInt(-distX+1);
                dy += distY;//+ distY>0?random.nextInt(distY+1):-random.nextInt(-distY+1);
            }

        }
        return new Point((int) Math.signum(dx), (int) Math.signum(dy));
    }
}
