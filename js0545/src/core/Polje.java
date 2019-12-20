package core;

import java.util.HashMap;

public class Polje {
    public int x;
    public int y;
    public int gScore;
    public int fScore;
    public HashMap<Integer, Polje> predhodniki;

    public Polje(int x, int y, int gScore, int fScore) {
        this.x = x;
        this.y = y;
        this.gScore = gScore;
        this.fScore = fScore;
        this.predhodniki = new HashMap<>();
    }
}
