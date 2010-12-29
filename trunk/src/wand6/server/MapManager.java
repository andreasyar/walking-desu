package wand6.server;

import java.util.HashMap;
import java.util.Random;
import wand6.common.MapFragment;

class MapManager {

    private final static int debugLevel = 1;

    private static MapManager self = null;

    private final HashMap<Integer, HashMap<Integer, SMapFragment>> fragments = new HashMap<Integer, HashMap<Integer, SMapFragment>>();

    static MapManager getInstance() {
        if (self == null) {
            self = new MapManager();
        }

        return self;
    }

    private MapManager() {}

    SMapFragment getMapFragment(int idX, int idY) {
        if (fragments.containsKey(idY)) {
            if (!fragments.get(idY).containsKey(idX)) {
                generateMapFragment(idX, idY, MapFragment.width / MapFragment.cellWidth);
            }
        } else {
            fragments.put(idY, new HashMap<Integer, SMapFragment>());
            generateMapFragment(idX, idY, MapFragment.width / MapFragment.cellWidth);
        }

        return fragments.get(idY).get(idX);
    }

    private void generateMapFragment(int idX, int idY, int n) throws IllegalArgumentException {
        if (debugLevel > 0) {
            System.out.println("Generate map fragment idX=" + idX + " idY=" + idY + ".");
        }

        if (n <= 0) {
            throw new IllegalArgumentException("n must be greater than zero.");
        }

        if (idX < 0 || idX > MapFragment.maxIdX) {
            throw new IllegalArgumentException("idX must be in range 0..." + MapFragment.maxIdX);
        }

        if (idY < 0 || idY > MapFragment.maxIdY) {
            throw new IllegalArgumentException("idY must be in range 0..." + MapFragment.maxIdY);
        }

        int nextValidN = n;

        if ((n & (n - 1)) != 0) {
            System.err.println("n=" + n + " is not power of 2. We need scale.");
            nextValidN = getNextValidN(n);
            System.err.println("nextValidN=" + nextValidN + " new valid n value selected.");
            if (nextValidN == -1) {
                System.exit(1);
            }
        }

        int h = 300;

        int mask = nextValidN;
        int offset = h;
        int hmatr[][];

        hmatr = new int[nextValidN+1][nextValidN+1];
        for(int i=0; i<nextValidN+1; ++i){
            for(int j = 0; j<nextValidN+1; ++j){
                hmatr[i][j] = -1;
            }
        }
        hmatr[0][0] = offset;
        hmatr[0][nextValidN] = offset;
        hmatr[nextValidN][0] = offset;
        hmatr[nextValidN][nextValidN] = offset;

        Random rand = new Random();
        while (mask > 0) {
            int hmask = (int) (mask / 2.0);
            for ( int i = hmask; i < nextValidN && i >= 1.0;) {
                for ( int j = hmask; j < nextValidN && j >= 1.0;) {
                    //
                    if (hmatr[i][j] == -1) {
                        hmatr[i][j] = (int) ((  hmatr[i - hmask][j - hmask]
                                +hmatr[i - hmask][j + hmask] +hmatr[i + hmask][j - hmask]
                                +hmatr[i + hmask][j + hmask]) / 4.0
                                + (-1 * rand.nextFloat()) * offset);
                    }

                    if (hmatr[i][j] > h)
                        hmatr[i][j] = h ;

                    if (hmatr[i][j] < 0)
                        hmatr[i][j] = 0;
                    //
                    if (hmatr[i - hmask][j] == -1) {
                        hmatr[i - hmask][j] = (int) ((hmatr[i - hmask][j - hmask]
                                + hmatr[i - hmask][j + hmask]) / 2.0
                                + (-1 * rand.nextFloat()) * offset);
                    }

                    if (hmatr[i - hmask][j] > h)
                        hmatr[i - hmask][j] = h;

                    if (hmatr[i - hmask][j] <= 0)
                        hmatr[i - hmask][j] = 1;
                    //
                    if (hmatr[i][j - hmask] == -1) {
                        hmatr[i][j - hmask] = (int) ((hmatr[i - hmask][j - hmask]
                                + hmatr[i + hmask][j - hmask]) / 2.0
                                + (-1 * rand.nextFloat()) * offset);
                    }
                    if (hmatr[i][j - hmask] > h)
                        hmatr[i][j - hmask] = h;

                    if (hmatr[i][j - hmask] <= 0)
                        hmatr[i][j - hmask] = 1;

                    //
                    if (hmatr[i + hmask][j] == -1) {
                    hmatr[i + hmask][j] = (int) ((hmatr[i + hmask][j - hmask]
                            + hmatr[i + hmask][j + hmask]) / 2.0
                            + (-1 * rand.nextFloat()) * offset);
                    }
                    if (hmatr[i + hmask][j] > h)
                        hmatr[i + hmask][j] = h;

                    if (hmatr[i + hmask][j] <= 0)
                        hmatr[i + hmask][j] = 1;

                    //
                    if (hmatr[i][j + hmask] == -1) {
                        hmatr[i][j + hmask] = (int) ((hmatr[i - hmask][j + hmask]
                                + hmatr[i + hmask][j + hmask]) / 2.0
                                + (-1 * rand.nextFloat()) * offset);
                    }
                    if (hmatr[i][j + hmask] > h)
                        hmatr[i][j + hmask] = h;

                    if (hmatr[i][j + hmask] <= 0)
                        hmatr[i][j + hmask] = 1;

                    j += mask;
                }//~ for
                i += mask;
            }//~ for
            offset *= 0.46;
            mask = hmask;
        }//~ while()

        fragments.get(idY).put(idX, new SMapFragment(hmatr, idX, idY));
    }

    private int getNextValidN(int n) {
        for (int i = n; i < Integer.MAX_VALUE; i++) {
            if ((i & (i - 1)) == 0) {
                return i;
            }
        }

        return -1;
    }
}
