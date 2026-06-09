
public class Species {

    public String name;
    public String food;  // Type de nourriture (herbivore, carnivore, etc.)
    public float speed;  // Vitesse de l'animal
    public boolean isCarnivore;  // Détermine si l'espèce est carnivore ou non
    public boolean isHerbivore;  // Détermine si l'espèce est herbivore ou non

    public Species(String name, String food, float speed) {
        this.name = name;
        this.food = food;
        this.speed = speed;
        // Définir si l'espèce est carnivore ou herbivore
        this.isCarnivore = food.equals("Herbivore");
        this.isHerbivore = food.equals("None");
    }

}
