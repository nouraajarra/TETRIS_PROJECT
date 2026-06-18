package tetris.model;

import java.awt.Color;

public class PieceS extends Piece {
    public PieceS() {
        couleur = new Color(0, 240, 0);
        rotationState = 0;
        formes = new int[][][] {
            {{0,1,1},{1,1,0},{0,0,0}},
            {{1,0},{1,1},{0,1}}
        };
        positionX = 3;
        positionY = 0;
    }
}
