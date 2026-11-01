package wand6.server;

import wand6.common.MapFragment;

public class SMapFragment extends MapFragment {

    private static int[][] alignHmap(int[][] hmap) {
        int cellCount = hmap[0].length;
        int expectedCellCount = width / cellWidth;
        int[][] tmpHmap;

        if (cellCount > expectedCellCount) {    // drop cells
            tmpHmap = new int[expectedCellCount][expectedCellCount];
            for (int idY = 0; idY < expectedCellCount; idY++) {
                for (int idX = 0; idX < expectedCellCount; idX++) {
                    tmpHmap[idY][idX] = hmap[idY][idX];
                }
            }
        } else if (cellCount < expectedCellCount) { // clone cells
            tmpHmap = new int[expectedCellCount][expectedCellCount];
            for (int idY = 0; idY < expectedCellCount; idY++) {
                for (int idX = 0; idX < expectedCellCount; idX++) {
                    tmpHmap[idY][idX] = hmap[idY < hmap[0].length ? idY : hmap[0].length - 1][idX < hmap[0].length ? idX : hmap[0].length - 1];
                }
            }
        } else {
            tmpHmap = hmap;
        }

        return tmpHmap;
    }

    public SMapFragment(int[][] hmap, int idX, int idY) {
        super(alignHmap(hmap), idX, idY);
    }

    public SMapFragment() {
        super();
    }
}
