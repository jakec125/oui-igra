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

    // Called only once before the match starts. It holds the
    // data that you may need before the game starts.
    @Override
    public void setup(InitialData data) {
        System.out.println((new Gson()).toJson(data));
        this.data = data;

        // Print out the map
//        for (int y = data.mapHeight - 1; y >= 0; y--) {
//            for (int x = 0; x < data.mapWidth; x++) {
//                System.out.print((data.map[y][x]) ? "_" : "#");
//            }
//            System.out.println();
//        }
    }

    // Called repeatedly while the match is generating. Each
    // time you receive the current match state and can use
    // response object to issue your commands.
    @Override
    public void update(MatchState state, Response response) {
        // Find and send your unit to a random direction that
        // moves it to a valid field on the map
        // TODO: Remove this code and implement a proper path finding!
        Unit jaz = state.yourUnit;
        Unit nasprotnik = state.opponentUnit;
        Coin kovanci[] = state.coins;

        int mojaRazdalja1 = razdalja(jaz.x, jaz.y, kovanci[0].x, kovanci[0].y);
        int nasprotnikRazdalja1 = razdalja(nasprotnik.x, nasprotnik.y, kovanci[0].x, kovanci[0].y);

        int mojaRazdalja2 = razdalja(jaz.x, jaz.y, kovanci[1].x, kovanci[1].y);
        int nasprotnikRazdalja2 = razdalja(nasprotnik.x, nasprotnik.y, kovanci[1].x, kovanci[1].y);

        Direction premik;
//        if (mojaRazdalja1 < mojaRazdalja2 && nasprotnikRazdalja1 > mojaRazdalja1) {
//            premik = (Direction) aStar(jaz.x, jaz.y, kovanci[0].x, kovanci[0].y, state)[1];
//        }
//        else {
//            premik = (Direction) aStar(jaz.x, jaz.y, kovanci[1].x, kovanci[1].y, state)[1];
//        }

        if (mojaRazdalja1 <= mojaRazdalja2) {
            if (nasprotnikRazdalja1 >= mojaRazdalja1) {
                premik = (Direction) aStar(jaz.x, jaz.y, kovanci[0].x, kovanci[0].y, state)[1];
            }
            else {
                premik = (Direction) aStar(jaz.x, jaz.y, kovanci[1].x, kovanci[1].y, state)[1];
            }
        }
        else {
            if (nasprotnikRazdalja2 >= mojaRazdalja2) {
                premik = (Direction) aStar(jaz.x, jaz.y, kovanci[1].x, kovanci[1].y, state)[1];
            }
            else {
                premik = (Direction) aStar(jaz.x, jaz.y, kovanci[0].x, kovanci[0].y, state)[1];
            }
        }
//        if (mojaRazdalja1 < mojaRazdalja2) {
//            premik = aStar(jaz.x, jaz.y, kovanci[0].x, kovanci[0].y, state);
//        }
//        else {
//            premik = aStar(jaz.x, jaz.y, kovanci[1].x, kovanci[1].y, state);
//        }



        response.moveUnit(premik);
    }

    private Object[] aStar(int xStart, int yStart, int xCilj, int yCilj, MatchState stanje) {

        PriorityQueue<Polje> vrsta = new PriorityQueue<Polje>(new PoljeComparator());
        Polje prvo = new Polje(xStart, yStart, null, 0, 0);
        prvo.fScore = hevristika(prvo, xCilj, yCilj, stanje);
        vrsta.add(prvo);
        Polje[][] obiskano = new Polje[data.mapHeight][data.mapWidth];
        Polje[][] zaprto = new Polje[data.mapHeight][data.mapWidth];
        obiskano[yStart][xStart] = prvo;
        while (!vrsta.isEmpty()) {

            Polje trenutno = vrsta.poll();
            obiskano[trenutno.y][trenutno.x] = null;
            zaprto[trenutno.y][trenutno.x] = trenutno;
            if (!dovoljenPremik(trenutno.x, trenutno.y)) {
                continue;
            }
            if (trenutno.x == xCilj && trenutno.y == yCilj) {
//                if (razdalja(xStart, yStart, xCilj, yCilj) == 0) {
//                    System.out.printf("jaz: (%d, %d), cilj: (%d, %d)\n", xStart, yStart, xCilj, yCilj);
//                    izpisiVrsto(vrsta);
//                }
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
                if (zaprto[trenutno.y][trenutno.x + 1] == null) {
                    if (obiskano[trenutno.y][trenutno.x + 1] != null) {
                        sosed = obiskano[trenutno.y][trenutno.x + 1];
                    } else {
                        sosed = new Polje(trenutno.x + 1, trenutno.y, trenutno, Integer.MAX_VALUE, Integer.MAX_VALUE);
                    }
                    obdelajSoseda(xCilj, yCilj, vrsta, obiskano, trenutno, trenutnaSkupnaDolzina, sosed, stanje);
                }
            }

            if (trenutno.x-1 >= 0) {
                if (zaprto[trenutno.y][trenutno.x - 1] == null) {
                    if (obiskano[trenutno.y][trenutno.x - 1] != null) {
                        sosed = obiskano[trenutno.y][trenutno.x - 1];
                    } else {
                        sosed = new Polje(trenutno.x - 1, trenutno.y, trenutno, Integer.MAX_VALUE, Integer.MAX_VALUE);
                    }
                    obdelajSoseda(xCilj, yCilj, vrsta, obiskano, trenutno, trenutnaSkupnaDolzina, sosed, stanje);
                }
            }

            if (trenutno.y+1 < data.mapHeight) {
                if (zaprto[trenutno.y + 1][trenutno.x] == null) {
                    if (obiskano[trenutno.y + 1][trenutno.x] != null) {
                        sosed = obiskano[trenutno.y + 1][trenutno.x];
                    } else {
                        sosed = new Polje(trenutno.x, trenutno.y + 1, trenutno, Integer.MAX_VALUE, Integer.MAX_VALUE);
                    }
                    obdelajSoseda(xCilj, yCilj, vrsta, obiskano, trenutno, trenutnaSkupnaDolzina, sosed, stanje);
                }
            }

            if (trenutno.y-1 >= 0) {
                if (zaprto[trenutno.y - 1][trenutno.x] == null) {
                    if (obiskano[trenutno.y - 1][trenutno.x] != null) {
                        sosed = obiskano[trenutno.y - 1][trenutno.x];
                    } else {
                        sosed = new Polje(trenutno.x, trenutno.y - 1, trenutno, Integer.MAX_VALUE, Integer.MAX_VALUE);
                    }
                    obdelajSoseda(xCilj, yCilj, vrsta, obiskano, trenutno, trenutnaSkupnaDolzina, sosed, stanje);
                }
            }
        }

        return null;
    }

    private boolean dovoljenoGledeZag(MatchState stanje, int x, int y) {
        boolean izhod = true;
        for (Saw zaga : stanje.saws) {
            if (true) {
                int[] novaLokacija = zagaNaslednjaLokacija(zaga);
                if (novaLokacija[0] == y && novaLokacija[1] == x) {
//                    System.out.printf("sosed: (%d, %d) cilj: (%d, %d)\n", x, y, novaLokacija[1], novaLokacija[0]);
                    izhod = false;
                    break;
                }
            }
        }
        return izhod;
    }

    private int razdalja(int x1, int y1, int x2, int y2) {
        return Math.abs((x1-x2)) + Math.abs((y1-y2));
    }

    private int[] zagaNaslednjaLokacija(Saw zaga) {
        int[] novaLokacija = new int[2];
        if (zaga.x-1 < 0 && (zaga.direction == SawDirection.DOWN_LEFT || zaga.direction == SawDirection.UP_LEFT)) {
            if (zaga.direction == SawDirection.DOWN_LEFT) {
                novaLokacija[0] = zaga.y - 1;
                novaLokacija[1] = zaga.x + 1;
            }
            else {
                novaLokacija[0] = zaga.y + 1;
                novaLokacija[1] = zaga.x + 1;
            }
        }
        else if (zaga.x+1 > data.mapWidth-1 && (zaga.direction == SawDirection.DOWN_RIGHT || zaga.direction == SawDirection.UP_RIGHT)) {
            if (zaga.direction == SawDirection.DOWN_RIGHT) {
                novaLokacija[0] = zaga.y - 1;
                novaLokacija[1] = zaga.x - 1;
            }
            else {
                novaLokacija[0] = zaga.y + 1;
                novaLokacija[1] = zaga.x - 1;
            }
        }
        else if (zaga.y-1 < 0 && (zaga.direction == SawDirection.DOWN_RIGHT || zaga.direction == SawDirection.DOWN_LEFT)) {
            if (zaga.direction == SawDirection.DOWN_RIGHT) {
                novaLokacija[0] = zaga.y + 1;
                novaLokacija[1] = zaga.x + 1;
            }
            else {
                novaLokacija[0] = zaga.y + 1;
                novaLokacija[1] = zaga.x - 1;
            }
        }
        else if (zaga.y+1 > data.mapHeight-1 && (zaga.direction == SawDirection.UP_RIGHT || zaga.direction == SawDirection.UP_LEFT)) {
            if (zaga.direction == SawDirection.UP_RIGHT) {
                novaLokacija[0] = zaga.y - 1;
                novaLokacija[1] = zaga.x + 1;
            }
            else {
                novaLokacija[0] = zaga.y - 1;
                novaLokacija[1] = zaga.x - 1;
            }
        }
        else {
            if (zaga.direction == SawDirection.DOWN_LEFT) {
                novaLokacija[0] = zaga.y - 1;
                novaLokacija[1] = zaga.x - 1;
            }
            else if (zaga.direction == SawDirection.DOWN_RIGHT) {
                novaLokacija[0] = zaga.y - 1;
                novaLokacija[1] = zaga.x + 1;
            }
            else if (zaga.direction == SawDirection.UP_LEFT) {
                novaLokacija[0] = zaga.y + 1;
                novaLokacija[1] = zaga.x - 1;
            }
            else {
                novaLokacija[0] = zaga.y + 1;
                novaLokacija[1] = zaga.x + 1;
            }
        }
        return novaLokacija;
    }

    private void obdelajSoseda(int xCilj, int yCilj, PriorityQueue<Polje> vrsta, Polje[][] obiskano, Polje trenutno, int trenutnaSkupnaDolzina, Polje sosed, MatchState stanje) {
//        if (trenutnaSkupnaDolzina < sosed.gScore) {
//            if (obiskano[sosed.y][sosed.x] != null) {
//                vrsta.remove(sosed);
//                sosed.predhodnik = trenutno;
//                sosed.gScore = trenutnaSkupnaDolzina;
//                sosed.fScore = sosed.gScore + hevristika(sosed, xCilj, yCilj, stanje);
//                vrsta.add(sosed);
//            }
//            else {
//                sosed.predhodnik = trenutno;
//                sosed.gScore = trenutnaSkupnaDolzina;
//                sosed.fScore = sosed.gScore + hevristika(sosed, xCilj, yCilj, stanje);
//                obiskano[sosed.y][sosed.x] = sosed;
//                vrsta.add(sosed);
//            }
//        }
        if (obiskano[sosed.y][sosed.x] != null) {
            if (sosed.gScore > trenutno.gScore) {
                return;
            }
        }
        vrsta.remove(sosed);
        sosed.predhodnik = trenutno;
        sosed.gScore = trenutnaSkupnaDolzina;
        sosed.fScore = sosed.gScore + hevristika(sosed, xCilj, yCilj, stanje);
        vrsta.add(sosed);
        obiskano[sosed.y][sosed.x] = sosed;
    }

    public int hevristika(Polje polje, int xCilj, int yCilj, MatchState stanje) {
        int hevristika = razdalja(polje.x, polje.y, xCilj, yCilj);
        if (!dovoljenoGledeZag(stanje, polje.x, polje.y) && polje.gScore == 1) {
//            System.out.printf("Izogibam se (%d, %d)\n", polje.x, polje.y);
            hevristika += 1000;
        }
        return hevristika;
    }

    public Direction smer(Polje trenutno, int xStart, int yStart) {
        while (trenutno.predhodnik.x != xStart || trenutno.predhodnik.y != yStart) {
            trenutno = trenutno.predhodnik;
        }
        Polje naslednje = trenutno;
        trenutno = trenutno.predhodnik;

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

    // Connects your bot to match generator, don't change it.
    public static void main(String[] args) throws Exception {
        NetworkingClient.connectNew(args, new MyBot());
    }
}
