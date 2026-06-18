package tetris.model;

public class Jeu {
    private String titre = "Tetris";
    private String version = "1.0";
    private int niveauMax = 10;

    private static Jeu instance;

    private Jeu() {}

    public static Jeu getInstance() {
        if (instance == null) instance = new Jeu();
        return instance;
    }

    public void demarrer() {}
    public void quitter() { System.exit(0); }

    public String getTitre() { return titre; }
    public String getVersion() { return version; }
    public int getNiveauMax() { return niveauMax; }

    
    public static int getIntervalle(int niveau) {
        return Math.max(100, 800 - (niveau * 70));
    }

    
    public static int calculerNiveau(int lignesEffacees) {
        
        return Math.min(10, 1 + lignesEffacees / 3);
    }

    
    public static int getStage(int lignesEffacees) {
        
        if (lignesEffacees >= 6) return 4;
        if (lignesEffacees >= 4) return 3;
        if (lignesEffacees >= 2) return 2;
        return 1;
    }

   
    public static int[] getSpawnPositions(int stage) {
        return switch (stage) {
            case 3 -> new int[]{1, 6};
            case 4 -> new int[]{0, 2, 5, 7};
            default -> new int[]{3};
        };
    }
}
