/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package server.javatestserver;

import java.util.Random;

/**
 *
 * @author mikhail
 */
public class MapGenerator {

int h = 200; // начальная высота измеений. Коэф-т изменения
int n = 64; // размер картинки

int mask = n; //
int offset = h; // начальная высота изменений 
int hmatr[][];

    public MapGenerator() {
        hmatr = new int[n+1][n+1];
        for(int i=0; i<n+1; ++i){
            for(int j = 0; j<n+1; ++j){
                hmatr[i][j] = -1;
            }
        }
        hmatr[0][0] = offset;
        hmatr[0][n] = offset;
        hmatr[n][0] = offset;
        hmatr[n][n] = offset;
    }

    /**
     *
     * @param hmatr
     * @param n
     * @param h
     * @param mask
     * @param offset
     */
    public void hmap2D( int[][] hmatr, int n, int h,int mask,int  offset) {
        Random rand = new Random();
        while (mask > 0) {
            int hmask = (int) (mask / 2.0);
            for ( int i = hmask; i < n && i >= 1.0;) {
                for ( int j = hmask; j < n && j >= 1.0;) {
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
    }

//    public static void main( String args[]){
//        MapGenerator mg = new MapGenerator();
//        mg.hmap2D(mg.hmatr, mg.n, mg.h, mg.mask, mg.offset);
//
//         for(int i=0; i<mg.n+1; ++i){
//            for(int j = 0; j<mg.n+1; ++j){
//                System.out.print(mg.hmatr[i][j]+" ");
//            }
//             System.out.print("\n");
//        }
//    }
}
