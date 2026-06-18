package tetris.model;

import java.awt.Color;

public class PieceJ extends Piece {
    public PieceJ() {
        couleur = new Color(0, 0, 240);
        rotationState = 0;
        formes = new int[][][] {
            {{1,0,0},{1,1,1},{0,0,0}},
            {{1,1},{1,0},{1,0}},
            {{0,0,0},{1,1,1},{0,0,1}},
            {{0,1},{0,1},{1,1}}
        };
        positionX = 3;
        positionY = 0;
    }
}
