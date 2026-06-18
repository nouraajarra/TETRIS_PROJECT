package tetris.model;

import java.awt.Color;

public class PieceZ extends Piece {
    public PieceZ() {
        couleur = new Color(240, 0, 0);
        rotationState = 0;
        formes = new int[][][] {
            {{1,1,0},{0,1,1},{0,0,0}},
            {{0,1},{1,1},{1,0}}
        };
        positionX = 3;
        positionY = 0;
    }
}
