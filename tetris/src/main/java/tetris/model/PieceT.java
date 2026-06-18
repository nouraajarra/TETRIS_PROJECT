package tetris.model;

import java.awt.Color;

public class PieceT extends Piece {
    public PieceT() {
        couleur = new Color(160, 0, 240);
        rotationState = 0;
        formes = new int[][][] {
            {{0,1,0},{1,1,1},{0,0,0}},
            {{1,0},{1,1},{1,0}},
            {{0,0,0},{1,1,1},{0,1,0}},
            {{0,1},{1,1},{0,1}}
        };
        positionX = 3;
        positionY = 0;
    }
}
