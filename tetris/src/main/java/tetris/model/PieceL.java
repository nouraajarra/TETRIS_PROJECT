package tetris.model;

import java.awt.Color;

public class PieceL extends Piece {
    public PieceL() {
        couleur = new Color(240, 160, 0);
        rotationState = 0;
        formes = new int[][][] {
            {{0,0,1},{1,1,1},{0,0,0}},
            {{1,0},{1,0},{1,1}},
            {{0,0,0},{1,1,1},{1,0,0}},
            {{1,1},{0,1},{0,1}}
        };
        positionX = 3;
        positionY = 0;
    }
}
