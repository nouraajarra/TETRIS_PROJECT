package tetris.model;

import java.awt.Color;

public abstract class Piece {
    protected Color couleur;
    protected int positionX;
    protected int positionY;
    protected int rotationState;
    protected int[][][] formes;

    public int[][] getForme() {
        return formes[rotationState];
    }

    public void tourner() {
        rotationState = (rotationState + 1) % formes.length;
    }

    public void tournerInverse() {
        rotationState = (rotationState - 1 + formes.length) % formes.length;
    }

    public void deplacer(String direction) {
        switch (direction) {
            case "LEFT"  -> positionX--;
            case "RIGHT" -> positionX++;
            case "DOWN"  -> positionY++;
        }
    }

    public Piece copie() {
        try {
            Piece copy = this.getClass().getDeclaredConstructor().newInstance();
            copy.positionX = this.positionX;
            copy.positionY = this.positionY;
            copy.rotationState = this.rotationState;
            return copy;
        } catch (Exception e) {
            return null;
        }
    }

    public int getPositionX() { return positionX; }
    public int getPositionY() { return positionY; }
    public void setPositionX(int x) { this.positionX = x; }
    public void setPositionY(int y) { this.positionY = y; }
    public Color getCouleurObj() { return couleur; }
    public int getRotationState() { return rotationState; }
    public void setRotationState(int s) { this.rotationState = s % formes.length; }
    public int getNbRotations() { return formes.length; }
}
