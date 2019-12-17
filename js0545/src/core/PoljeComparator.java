package core;

import java.util.Comparator;

public class PoljeComparator implements Comparator<Polje> {
    @Override
    public int compare(Polje p1, Polje p2) {
        if (p1.fScore < p2.fScore) {
            return -1;
        }
        else if (p1.fScore > p2.fScore) {
            return 1;
        }
        else {
            return 0;
        }
    }
}
