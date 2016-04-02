package com.google.engedu.puzzle8;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.NavigableMap;


public class PuzzleBoard {

    private static final int NUM_TILES = 3;
    private static final int DEAD_TILE = ((NUM_TILES*NUM_TILES) -1);
    private static final int[][] NEIGHBOUR_COORDS = {
            { -1, 0 }, // left
            { 1, 0 },  // right
            { 0, -1 }, // below
            { 0, 1 }   // above
    };
    private ArrayList<PuzzleTile> tiles;
    private static int steps;
    private static PuzzleBoard previousBoard;

    PuzzleBoard(Bitmap bitmap, int parentWidth) {
        tiles = new ArrayList<>();

        int numtile = 0;
        Log.d("Puzzle init", ""+parentWidth);
        Bitmap fullPic = Bitmap.createScaledBitmap(bitmap, parentWidth, parentWidth, true);

        for (int i = 0; i < NUM_TILES; ++i) {
            for (int j = 0; j < NUM_TILES; ++j) {
                Bitmap newBitMap = Bitmap.createBitmap(fullPic,(parentWidth/NUM_TILES)*j,(parentWidth/NUM_TILES)*i, parentWidth/NUM_TILES, parentWidth/NUM_TILES);
                PuzzleTile tile = new PuzzleTile(newBitMap,numtile);
                tiles.add(tile);
                numtile++;
            }
        }
        tiles.set(DEAD_TILE,null);
    }
    PuzzleBoard(PuzzleBoard otherBoard) {
        tiles = (ArrayList<PuzzleTile>) otherBoard.tiles.clone();
        steps = otherBoard.steps + 1;
        previousBoard = otherBoard;
    }

    public void reset() {
        this.steps = 0;
        this.previousBoard = null;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        return tiles.equals(((PuzzleBoard) o).tiles);
    }

    public void setSteps(int x){
        this.steps = x;
    }
    public void setPrviousNull(){
        previousBoard = null;
    }

    public void draw(Canvas canvas) {
        if (tiles == null) {
            return;
        }
        for (int i = 0; i < NUM_TILES * NUM_TILES; i++) {
            PuzzleTile tile = tiles.get(i);
            if (tile != null) {
                tile.draw(canvas, i % NUM_TILES, i / NUM_TILES);
            }
        }
    }

    public boolean click(float x, float y) {
        for (int i = 0; i < NUM_TILES * NUM_TILES; i++) {
            PuzzleTile tile = tiles.get(i);
            if (tile != null) {
                if (tile.isClicked(x, y, i % NUM_TILES, i / NUM_TILES)) {
                    return tryMoving(i % NUM_TILES, i / NUM_TILES);
                }
            }
        }
        return false;
    }

    public boolean resolved() {
        for (int i = 0; i < NUM_TILES * NUM_TILES - 1; i++) {
            PuzzleTile tile = tiles.get(i);
            if (tile == null || tile.getNumber() != i)
                return false;
        }
        return true;
    }

    private int XYtoIndex(int x, int y) {
        return x + y * NUM_TILES;
    }

    protected void swapTiles(int i, int j) {
        PuzzleTile temp = tiles.get(i);
        tiles.set(i, tiles.get(j));
        tiles.set(j, temp);
    }

    private boolean tryMoving(int tileX, int tileY) {
        for (int[] delta : NEIGHBOUR_COORDS) {
            int nullX = tileX + delta[0];
            int nullY = tileY + delta[1];
            if (nullX >= 0 && nullX < NUM_TILES && nullY >= 0 && nullY < NUM_TILES &&
                    tiles.get(XYtoIndex(nullX, nullY)) == null) {
                swapTiles(XYtoIndex(nullX, nullY), XYtoIndex(tileX, tileY));
                return true;
            }

        }
        return false;
    }

    public ArrayList<PuzzleBoard> neighbours() {
        int i = 0;
        ArrayList<PuzzleBoard> validMoves = new ArrayList<PuzzleBoard>();
        // i should be the index of the null
        while(tiles.get(i)!=null){
            ++i;
        }
        for(int j = 0; j < 4; ++j){
            int move = XYtoIndex(NEIGHBOUR_COORDS[j][0],NEIGHBOUR_COORDS[j][1]) + i;
            if(move < 9 && move > -1){
                PuzzleBoard copy = new PuzzleBoard(this);
                copy.swapTiles(move,i);
                validMoves.add(copy);
            }
        }
        return validMoves;
    }

    public int getSteps(){return steps;}

    public int priority() {
        int manhattenValue = 0;
        int originalX, originalY, newX, newY;

        for(int i = 0; i < NUM_TILES*NUM_TILES; ++i) {
            if (tiles.get(i)!=null) {
                int h = tiles.get(i).getNumber();
                newX = i / NUM_TILES;
                newY = i % NUM_TILES;
                originalX = h / NUM_TILES;
                originalY = h % NUM_TILES;

                manhattenValue += Math.abs(newX - originalX);
                manhattenValue += Math.abs(newY - originalY);
            }
        }
        manhattenValue += steps;
        return manhattenValue;
    }

    public ArrayList<PuzzleBoard> getPreviousBoard() {

        ArrayList<PuzzleBoard> prev = new ArrayList<>();
        prev.add(this);
        int index = 0;

        while(prev.get(index).previousBoard != null){
            prev.add(prev.get(index).previousBoard);
            index++;
        }
        return prev;
    }
}
