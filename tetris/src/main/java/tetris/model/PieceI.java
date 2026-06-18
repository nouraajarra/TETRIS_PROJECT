package tetris.model;

import java.awt.Color;

public class PieceI extends Piece {
    public PieceI() {
        couleur = new Color(0, 240, 240);
        rotationState = 0;
        formes = new int[][][] {
            {{0,0,0,0},{1,1,1,1},{0,0,0,0},{0,0,0,0}},
            {{0,0,1,0},{0,0,1,0},{0,0,1,0},{0,0,1,0}},
            {{0,0,0,0},{0,0,0,0},{1,1,1,1},{0,0,0,0}},
            {{0,1,0,0},{0,1,0,0},{0,1,0,0},{0,1,0,0}}
        };
        positionX = 3;
        positionY = -1;
    }
}
