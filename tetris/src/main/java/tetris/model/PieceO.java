package tetris.model;

import java.awt.Color;

public class PieceO extends Piece {
    public PieceO() {
        couleur = new Color(240, 240, 0);
        rotationState = 0;
        formes = new int[][][] {
            {{1,1},{1,1}}
        };
        positionX = 4;
        positionY = 0;
    }
}
