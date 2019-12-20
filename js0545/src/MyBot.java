import com.google.gson.Gson;
import core.*;
import core.api.*;
import core.api.commands.Direction;

import java.util.*;

/**
 * Example Java bot implementation for Planet Lia Bounce Evasion.
 */
public class MyBot implements Bot {

    InitialData data;
    LinkedList<LinkedList<Saw>> mojeZagice;
    int timerZagice;
    int timeoutTimer;
    int prejsnjeTocke;
    // Called only once before the match starts. It holds the
    // data that you may need before the game starts.
    @Override
    public void setup(InitialData data) {
//        System.out.println((new Gson()).toJson(data));
        this.data = data;
        this.mojeZagice = new LinkedList<>();
        this.timerZagice = 0;
        this.timeoutTimer = 0;
        this.prejsnjeTocke = 0;
        inicializacijaMojihZagic();
    }

    // Called repeatedly while the match is generating. Each
    // time you receive the current match state and can use
    // response object to issue your commands.
    @Override
    public void update(MatchState state, Response response) {
//        System.out.println(Arrays.toString(mojeZagice.get(0).toArray()));

//        if (prejsnjeTocke == state.yourUnit.points) {
//            timeoutTimer++;
//            if (timeoutTimer == 50) {
//                randomPremik(state);
//                timeoutTimer = 0;
//                return;
//            }
//        }
//        prejsnjeTocke = state.yourUnit.points;

        Unit jaz = state.yourUnit;
        Unit nasprotnik = state.opponentUnit;
        Coin[] kovanci = state.coins;

        int mojaRazdalja1 = razdalja(jaz.x, jaz.y, kovanci[0].x, kovanci[0].y);
        int nasprotnikRazdalja1 = razdalja(nasprotnik.x, nasprotnik.y, kovanci[0].x, kovanci[0].y);

        int mojaRazdalja2 = razdalja(jaz.x, jaz.y, kovanci[1].x, kovanci[1].y);
        int nasprotnikRazdalja2 = razdalja(nasprotnik.x, nasprotnik.y, kovanci[1].x, kovanci[1].y);

        Direction premik;
        if (mojaRazdalja1 <= mojaRazdalja2) {
            if (nasprotnikRazdalja1 >= mojaRazdalja1) {
                premik = (Direction) aStar(jaz.x, jaz.y, kovanci[0].x, kovanci[0].y, state, response)[1];
            }
            else {
                premik = (Direction) aStar(jaz.x, jaz.y, kovanci[1].x, kovanci[1].y, state, response)[1];
            }
        }
        else {
            if (nasprotnikRazdalja2 >= mojaRazdalja2) {
                premik = (Direction) aStar(jaz.x, jaz.y, kovanci[1].x, kovanci[1].y, state, response)[1];
            }
            else {
                premik = (Direction) aStar(jaz.x, jaz.y, kovanci[0].x, kovanci[0].y, state, response)[1];
            }
        }
        posodobiMojeZagice();
        response.moveUnit(premik);
    }

    private Object[] aStar(int xStart, int yStart, int xCilj, int yCilj, MatchState stanje, Response response) {
        int stevec = 0;

        PriorityQueue<Polje> vrsta = new PriorityQueue<Polje>(new PoljeComparator());
        Polje prvo = new Polje(xStart, yStart, 0, 0);
        prvo.fScore = hevristika(prvo, xCilj, yCilj, stanje);
        vrsta.add(prvo);
        Polje[][] open = new Polje[data.mapHeight][data.mapWidth];
        open[yStart][xStart] = prvo;
        while (!vrsta.isEmpty()) {
//            stevec++;
//            if (stevec == 700) {
//                System.out.println("random shit");
//                Object[] izhod = new Object[2];
//                izhod[0] = 69;
//                izhod[1] = randomPremik(stanje);
//                return izhod;
//            }
//            System.out.println(stevec);
            Polje trenutno = vrsta.poll();
            open[trenutno.y][trenutno.x] = null;
            if (!dovoljenPremik(trenutno.x, trenutno.y)) {
                continue;
            }
            if (trenutno.x == xCilj && trenutno.y == yCilj) {
//                System.out.println(trenutno.gScore);
                int dolzina = trenutno.gScore;
                Direction smer = smer(trenutno, xStart, yStart);
                Object[] izhod = new Object[2];
                izhod[0] = dolzina;
                izhod[1] = smer;
                return izhod;
            }

            Polje sosed;
            int trenutnaSkupnaDolzina = trenutno.gScore + 1;
            if (trenutno.x+1 < data.mapWidth) {
                if (open[trenutno.y][trenutno.x + 1] != null) {
                    sosed = open[trenutno.y][trenutno.x + 1];
                } else {
                    sosed = new Polje(trenutno.x + 1, trenutno.y, Integer.MAX_VALUE, Integer.MAX_VALUE);
                }
                obdelajSoseda(xCilj, yCilj, vrsta, open, trenutno, trenutnaSkupnaDolzina, sosed, stanje);
            }

            if (trenutno.x-1 >= 0) {
                if (open[trenutno.y][trenutno.x - 1] != null) {
                    sosed = open[trenutno.y][trenutno.x - 1];
                } else {
                    sosed = new Polje(trenutno.x - 1, trenutno.y, Integer.MAX_VALUE, Integer.MAX_VALUE);
                }
                obdelajSoseda(xCilj, yCilj, vrsta, open, trenutno, trenutnaSkupnaDolzina, sosed, stanje);
            }

            if (trenutno.y+1 < data.mapHeight) {
                if (open[trenutno.y + 1][trenutno.x] != null) {
                    sosed = open[trenutno.y + 1][trenutno.x];
                } else {
                    sosed = new Polje(trenutno.x, trenutno.y + 1, Integer.MAX_VALUE, Integer.MAX_VALUE);
                }
                obdelajSoseda(xCilj, yCilj, vrsta, open, trenutno, trenutnaSkupnaDolzina, sosed, stanje);
            }

            if (trenutno.y-1 >= 0) {
                if (open[trenutno.y - 1][trenutno.x] != null) {
                    sosed = open[trenutno.y - 1][trenutno.x];
                } else {
                    sosed = new Polje(trenutno.x, trenutno.y - 1, Integer.MAX_VALUE, Integer.MAX_VALUE);
                }
                obdelajSoseda(xCilj, yCilj, vrsta, open, trenutno, trenutnaSkupnaDolzina, sosed, stanje);
            }
        }

        return null;
    }

    private boolean dovoljenoGledeZag(int g, int x, int y) {
        boolean izhod = true;
        for (Saw zaga : mojeZagice.get(g)) {
            //                int[] novaLokacija = zagaNaslednjaLokacija(zaga);
            if (zaga.y == y && zaga.x == x) {
//                    System.out.printf("sosed: (%d, %d) cilj: (%d, %d)\n", x, y, novaLokacija[1], novaLokacija[0]);
                izhod = false;
                break;
            }
        }
        return izhod;
    }

    private int razdalja(int x1, int y1, int x2, int y2) {
        return Math.abs((x1-x2)) + Math.abs((y1-y2));
    }

    private void obdelajSoseda(int xCilj, int yCilj, PriorityQueue<Polje> vrsta, Polje[][] obiskano, Polje trenutno, int trenutnaSkupnaDolzina, Polje sosed, MatchState stanje) {
        vrsta.remove(sosed);
        sosed.gScore = trenutnaSkupnaDolzina;
        sosed.fScore = sosed.gScore + hevristika(sosed, xCilj, yCilj, stanje);
        sosed.predhodniki.put(sosed.gScore, trenutno);
        vrsta.add(sosed);
        obiskano[sosed.y][sosed.x] = sosed;
    }

    public int hevristika(Polje polje, int xCilj, int yCilj, MatchState stanje) {
        int hevristika = razdalja(polje.x, polje.y, xCilj, yCilj);
        if (polje.gScore <= 99 && !dovoljenoGledeZag(polje.gScore, polje.x, polje.y)) {
            if (polje.gScore == 1) {
//                System.out.printf("Izogibam se (%d, %d)\n", polje.x, polje.y);
            }
            hevristika += 10000;
//            if (polje.gScore == 1) {
//                hevristika += 5000;
//            }
        }
        return hevristika;
    }

    public Direction smer(Polje trenutno, int xStart, int yStart) {
        int stevecG = trenutno.gScore;
        while (stevecG != 1) {
            trenutno = trenutno.predhodniki.get(stevecG);
            stevecG--;
        }
        Polje naslednje = trenutno;
        trenutno = trenutno.predhodniki.get(1);

        if (trenutno.x < naslednje.x && trenutno.y == naslednje.y) {
            return Direction.RIGHT;
        }
        else if (trenutno.x > naslednje.x && trenutno.y == naslednje.y) {
            return Direction.LEFT;
        }
        else if (trenutno.x == naslednje.x && trenutno.y < naslednje.y) {
            return Direction.UP;
        }
        else {
            return Direction.DOWN;
        }
    }

    private boolean dovoljenPremik(int x, int y) {
        if (x < 0 || y < 0 || x > data.mapWidth-1 || y > data.mapHeight-1) {
            return false;
        }
        else {
            return data.map[y][x];
        }
    }

    private void inicializacijaMojihZagic() {
        mojeZagice.addFirst(new LinkedList<>());
        mojeZagice.getLast().add(new Saw(5, 0, SawDirection.UP_RIGHT));
        mojeZagice.getLast().add(new Saw(14, 10, SawDirection.DOWN_LEFT));
        timerZagice++;
        for (int i = 0; i < 99; i++) {
            LinkedList<Saw> prejsni = mojeZagice.getLast();
            mojeZagice.addLast(new LinkedList<>());
            LinkedList<Saw> novi = mojeZagice.getLast();
            for (Saw zagica : prejsni) {
                novi.add(new Saw(zagica.x, zagica.y, zagica.direction));
            }
            for (Saw saw : mojeZagice.getLast()) {
                mojeZagiceNalednjaLokacija(saw);
            }
//            System.out.println(mojeZagice.getLast().getFirst().toString());
            if (timerZagice%22 == 0) {
                mojeZagice.getLast().add(new Saw(5, 0, SawDirection.UP_RIGHT));
                mojeZagice.getLast().add(new Saw(14, 10, SawDirection.DOWN_LEFT));
            }
            timerZagice++;
        }
//        timerZagice += 20;
    }

    private void posodobiMojeZagice() {
        mojeZagice.removeFirst();
        LinkedList<Saw> prejsni = mojeZagice.getLast();
        mojeZagice.addLast(new LinkedList<>());
        LinkedList<Saw> novi = mojeZagice.getLast();
        for (Saw zagica : prejsni) {
            novi.add(new Saw(zagica.x, zagica.y, zagica.direction));
        }
        for (Saw saw : mojeZagice.getLast()) {
            mojeZagiceNalednjaLokacija(saw);
        }
        if (timerZagice%22 == 0) {
            mojeZagice.getLast().add(new Saw(5, 0, SawDirection.UP_RIGHT));
            mojeZagice.getLast().add(new Saw(14, 10, SawDirection.DOWN_LEFT));
        }
        timerZagice++;
    }

    private void mojeZagiceNalednjaLokacija(Saw zaga) {
        if (zaga.x-1 < 0 && zaga.y-1 < 0 && zaga.direction == SawDirection.DOWN_LEFT) {
            zaga.y = zaga.y + 1;
            zaga.x = zaga.x + 1;
            zaga.direction = SawDirection.UP_RIGHT;
//            System.out.println("spodnji levi kot");
//            System.out.printf("(%d, %d)\n", zaga.x, zaga.y);
        }
        else if (zaga.x-1 < 0 && zaga.y+1 > data.mapHeight-1 && zaga.direction == SawDirection.UP_LEFT) {
            zaga.y = zaga.y - 1;
            zaga.x = zaga.x + 1;
            zaga.direction = SawDirection.DOWN_RIGHT;
//            System.out.println("zgornji levi kot");
//            System.out.printf("(%d, %d)\n", zaga.x, zaga.y);
        }
        else if (zaga.x+1 > data.mapWidth-1 && zaga.y+1 > data.mapHeight-1 && zaga.direction == SawDirection.UP_RIGHT) {
            zaga.y = zaga.y - 1;
            zaga.x = zaga.x - 1;
            zaga.direction = SawDirection.DOWN_LEFT;
//            System.out.println("zgornji desni kot");
//            System.out.printf("(%d, %d)\n", zaga.x, zaga.y);
        }
        else if (zaga.x+1 > data.mapWidth-1 && zaga.y-1 < 0 && zaga.direction == SawDirection.DOWN_RIGHT) {
            zaga.y = zaga.y + 1;
            zaga.x = zaga.x - 1;
            zaga.direction = SawDirection.UP_LEFT;
//            System.out.println("spodnji desni kot");
//            System.out.printf("(%d, %d)\n", zaga.x, zaga.y);
        }
        else if (zaga.x-1 < 0 && (zaga.direction == SawDirection.DOWN_LEFT || zaga.direction == SawDirection.UP_LEFT)) {
            if (zaga.direction == SawDirection.DOWN_LEFT) {
                zaga.y = zaga.y - 1;
                zaga.x = zaga.x + 1;
                zaga.direction = SawDirection.DOWN_RIGHT;
            }
            else {
                zaga.y = zaga.y + 1;
                zaga.x = zaga.x + 1;
                zaga.direction = SawDirection.UP_RIGHT;
            }
        }
        else if (zaga.x+1 > data.mapWidth-1 && (zaga.direction == SawDirection.DOWN_RIGHT || zaga.direction == SawDirection.UP_RIGHT)) {
            if (zaga.direction == SawDirection.DOWN_RIGHT) {
                zaga.y = zaga.y - 1;
                zaga.x = zaga.x - 1;
                zaga.direction = SawDirection.DOWN_LEFT;
            }
            else {
                zaga.y = zaga.y + 1;
                zaga.x = zaga.x - 1;
                zaga.direction = SawDirection.UP_LEFT;
            }
        }
        else if (zaga.y-1 < 0 && (zaga.direction == SawDirection.DOWN_RIGHT || zaga.direction == SawDirection.DOWN_LEFT)) {
            if (zaga.direction == SawDirection.DOWN_RIGHT) {
                zaga.y = zaga.y + 1;
                zaga.x = zaga.x + 1;
                zaga.direction = SawDirection.UP_RIGHT;
            }
            else {
                zaga.y = zaga.y + 1;
                zaga.x = zaga.x - 1;
                zaga.direction = SawDirection.UP_LEFT;
            }
        }
        else if (zaga.y+1 > data.mapHeight-1 && (zaga.direction == SawDirection.UP_RIGHT || zaga.direction == SawDirection.UP_LEFT)) {
            if (zaga.direction == SawDirection.UP_RIGHT) {
                zaga.y = zaga.y - 1;
                zaga.x = zaga.x + 1;
                zaga.direction = SawDirection.DOWN_RIGHT;
            }
            else {
                zaga.y = zaga.y - 1;
                zaga.x = zaga.x - 1;
                zaga.direction = SawDirection.DOWN_LEFT;
            }
        }
        else {
            if (zaga.direction == SawDirection.DOWN_LEFT) {
                zaga.y = zaga.y - 1;
                zaga.x = zaga.x - 1;
//                System.out.println("dol levo grem");
            }
            else if (zaga.direction == SawDirection.DOWN_RIGHT) {
                zaga.y = zaga.y - 1;
                zaga.x = zaga.x + 1;
            }
            else if (zaga.direction == SawDirection.UP_LEFT) {
                zaga.y = zaga.y + 1;
                zaga.x = zaga.x - 1;
            }
            else {
                zaga.y = zaga.y + 1;
                zaga.x = zaga.x + 1;
//                System.out.println("gor desno grem");
            }
        }
    }

    private boolean izvenPolja(int x, int y) {
        return x >= 0 && y >= 0 && x <= data.mapWidth - 1 && y <= data.mapHeight - 1;
    }

    private void izpisiVrsto(PriorityQueue<Polje> vrsta) {
        Object[] array = vrsta.toArray();
        for (Object polje : array) {
            System.out.printf("(%d, %d)|", ((Polje)polje).x, ((Polje)polje).y);
        }
        System.out.println();
    }

    private Direction randomPremik(MatchState state) {
        while (true) {
            double rand = Math.random();

            Direction direction;
            if (rand < 0.25) direction = Direction.LEFT;
            else if (rand < 0.5) direction = Direction.RIGHT;
            else if (rand < 0.75) direction = Direction.UP;
            else direction = Direction.DOWN;

            int newX, newY;

            if (direction == Direction.LEFT) newX = state.yourUnit.x - 1;
            else if (direction == Direction.RIGHT) newX = state.yourUnit.x + 1;
            else newX = state.yourUnit.x;

            if (direction == Direction.UP) newY = state.yourUnit.y + 1;
            else if (direction == Direction.DOWN) newY = state.yourUnit.y - 1;
            else newY = state.yourUnit.y;

            if (newX >= 0 && newY >= 0 && newX < data.mapWidth && newY < data.mapHeight && data.map[newY][newX] && dovoljenoGledeZag(1, newX, newY)) {
//                posodobiMojeZagice();
                return direction;
            }
        }
    }

    // Connects your bot to match generator, don't change it.
    public static void main(String[] args) throws Exception {
        NetworkingClient.connectNew(args, new MyBot());
    }
}
