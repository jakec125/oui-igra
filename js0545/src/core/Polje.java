package core;

public class Polje {
    public int x;
    public int y;
    public int gScore;
    public int fScore;
    public Polje predhodnik;

    public Polje(int x, int y, Polje predhodnik, int gScore, int fScore) {
        this.x = x;
        this.y = y;
        this.gScore = gScore;
        this.fScore = fScore;
        this.predhodnik = predhodnik;
    }
}
