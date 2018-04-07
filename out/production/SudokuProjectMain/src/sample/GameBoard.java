
package sample;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;


/***
 * The GameBoard class contains three arrays, one for the sudoku solution,
 * one for the initial numbers displayed from it, and one that stores player's solution
 *
 * @author Captain Coder
 * @version 1
 */
public class GameBoard {

    /* Array that will contain the complete solution to the board */
    private static int[] solution;
    /* Array that will contain ONLY the numbers initially drawn on the board and that the player can't change */
    public static int[] initial;
    /* Array that will contain player's numbers */
    public static int[] player;

    /***
     * @see Arrays
     */
    public GameBoard() {

        generateGrid();
        player = new int[81];
    }
    public static void resetPlaygrid(){
        player = new int[81];
    }
    private static void generateGrid() {
        ArrayList<Integer> arr = new ArrayList<Integer>(9);
         solution = new int[81];
        for (int i = 1; i <= 9; i++) arr.add(i);

        //loads all boxes with numbers 1 through 9
        for (int i = 0; i < 81; i++) {
            if (i % 9 == 0) Collections.shuffle(arr);
            int perBox = ((i / 3) % 3) * 9 + ((i % 27) / 9) * 3 + (i / 27) * 27 + (i % 3);
            solution[perBox] = arr.get(i % 9);
        }

        //tracks rows and columns that have been sorted
        boolean[] sorted = new boolean[81];

        for (int i = 0; i < 9; i++) {
            boolean backtrack = false;
            //0 is row, 1 is column
            for (int a = 0; a < 2; a++) {
                //every number 1-9 that is encountered is registered
                boolean[] registered = new boolean[10]; //index 0 will intentionally be left empty since there are only number 1-9.
                int rowOrigin = i * 9;
                int colOrigin = i;

                ROW_COL:
                for (int j = 0; j < 9; j++) {
                    //row/column stepping - making sure numbers are only registered once and marking which cells have been sorted
                    int step = (a % 2 == 0 ? rowOrigin + j : colOrigin + j * 9);
                    int num = solution[step];

                    if (!registered[num]) registered[num] = true;
                    else //if duplicate in row/column
                    {
                        //box and adjacent-cell swap (BAS method)
                        //checks for either unregistered and unsorted candidates in same box,
                        //or unregistered and sorted candidates in the adjacent cells
                        for (int y = j; y >= 0; y--) {
                            int scan = (a % 2 == 0 ? i * 9 + y : i + 9 * y);
                            if (solution[scan] == num) {
                                //box stepping
                                for (int z = (a % 2 == 0 ? (i % 3 + 1) * 3 : 0); z < 9; z++) {
                                    if (a % 2 == 1 && z % 3 <= i % 3)
                                        continue;
                                    int boxOrigin = ((scan % 9) / 3) * 3 + (scan / 27) * 27;
                                    int boxStep = boxOrigin + (z / 3) * 9 + (z % 3);
                                    int boxNum = solution[boxStep];
                                    if ((!sorted[scan] && !sorted[boxStep] && !registered[boxNum])
                                            || (sorted[scan] && !registered[boxNum] && (a % 2 == 0 ? boxStep % 9 == scan % 9 : boxStep / 9 == scan / 9))) {
                                        solution[scan] = boxNum;
                                        solution[boxStep] = num;
                                        registered[boxNum] = true;
                                        continue ROW_COL;
                                    } else if (z == 8) //if z == 8, then break statement not reached: no candidates available
                                    {
                                        //Preferred adjacent swap (PAS)
                                        //Swaps x for y (preference on unregistered numbers), finds occurence of y
                                        //and swaps with z, etc. until an unregistered number has been found
                                        int searchingNo = num;

                                        //noting the location for the blindSwaps to prevent infinite loops.
                                        boolean[] blindSwapIndex = new boolean[81];

                                        //loop of size 18 to prevent infinite loops as well. Max of 18 swaps are possible.
                                        //at the end of this loop, if continue or break statements are not reached, then
                                        //fail-safe is executed called Advance and Backtrack Sort (ABS) which allows the
                                        //algorithm to continue sorting the next row and column before coming back.
                                        //Somehow, this fail-safe ensures success.
                                        for (int q = 0; q < 18; q++) {
                                            SWAP:
                                            for (int b = 0; b <= j; b++) {
                                                int pacing = (a % 2 == 0 ? rowOrigin + b : colOrigin + b * 9);
                                                if (solution[pacing] == searchingNo) {
                                                    int adjacentCell = -1;
                                                    int adjacentNo = -1;
                                                    int decrement = (a % 2 == 0 ? 9 : 1);

                                                    for (int c = 1; c < 3 - (i % 3); c++) {
                                                        adjacentCell = pacing + (a % 2 == 0 ? (c + 1) * 9 : c + 1);

                                                        //this creates the preference for swapping with unregistered numbers
                                                        if ((a % 2 == 0 && adjacentCell >= 81)
                                                                || (a % 2 == 1 && adjacentCell % 9 == 0))
                                                            adjacentCell -= decrement;
                                                        else {
                                                            adjacentNo = solution[adjacentCell];
                                                            if (i % 3 != 0
                                                                    || c != 1
                                                                    || blindSwapIndex[adjacentCell]
                                                                    || registered[adjacentNo])
                                                                adjacentCell -= decrement;
                                                        }
                                                        adjacentNo = solution[adjacentCell];

                                                        //as long as it hasn't been swapped before, swap it
                                                        if (!blindSwapIndex[adjacentCell]) {
                                                            blindSwapIndex[adjacentCell] = true;
                                                            solution[pacing] = adjacentNo;
                                                            solution[adjacentCell] = searchingNo;
                                                            searchingNo = adjacentNo;

                                                            if (!registered[adjacentNo]) {
                                                                registered[adjacentNo] = true;
                                                                continue ROW_COL;
                                                            }
                                                            break SWAP;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        //begin Advance and Backtrack Sort (ABS)
                                        backtrack = true;
                                        break ROW_COL;
                                    }
                                }
                            }
                        }
                    }
                }

                if (a % 2 == 0)
                    for (int j = 0; j < 9; j++) sorted[i * 9 + j] = true; //setting row as sorted
                else if (!backtrack)
                    for (int j = 0; j < 9; j++) sorted[i + j * 9] = true; //setting column as sorted
                else //reseting sorted cells through to the last iteration
                {
                    backtrack = false;
                    for (int j = 0; j < 9; j++) sorted[i * 9 + j] = false;
                    for (int j = 0; j < 9; j++) sorted[(i - 1) * 9 + j] = false;
                    for (int j = 0; j < 9; j++) sorted[i - 1 + j * 9] = false;
                    i -= 2;
                }
            }
        }

        //Creates the unsolved grid
        initial = new int[solution.length];
        System.arraycopy(solution, 0, initial, 0, solution.length);
        for (int i = 0; i < 3 * 20; i++) {
            initial[(int )(Math.random() * 81 + 0)] = 0;
        }

    }

    /***
     *
     * @return the solution array
     */
    public int[] getSolution() {
        return solution;
    }

    /***
     *
     * @return the initial filled-in numbers array
     */
    public int[] getInitial() {
        return initial;
    }

    /***
     *
     * @return the player array
     */
    public int[] getPlayer() {
        return player;
    }

    /***
     *
     * @param val the integer to insert in the player array
     * @param row location in array x
     * @param col location in array y
     */
    public void modifyPlayer(int val, int row, int col) {
        // check if the initial array has a zero (treated as empty square)
        // in the position we want to put in a number in the player array
        // this way we avoid intersections between the two
        if (initial[row*9 + col] == 0) {

            if(val >=0 && val <= 9) // only values from 0 to 9 inclusive are permitted
                player[row*9 + col] = val;
            else // print out an error message
                System.out.println("Value passed to player falls out of range");
        }

    }

    /***
     *
     * @return true if player solution matches original solution, false if not
     */
    public boolean checkForSuccess() {
        for(int index = 0; index<initial.length; index++) {
            // if the value in the initial array is zero, which means
            // the player has to input a value in the square
            if(initial[index] == 0) {

                // check if the player value corresponds to the solution value
                // and if it doesn't:
                if(player[index] != solution[index]) {

                    // return false, which will tell us there has been a mistake
                    // and that is enough for us to know the player hasn't solved
                    // the puzzle
                    return false;
                }
            }


        }
        // otherwise, if everything is correct, return true
        return true;
    }

    /***
     *
     * @return true if player solution is a correct one according to sudoku rules
     */
    public boolean checkForSuccessGeneral() {
        // combine the initial and player arrays
        // instantiate a 9x9 array filled with 0's;
        int[] combined = new int[81];
        // fill it up with the combination of initial number and player answers
        for(int index = 0; index < 81; index++){
            if(initial[index] != 0) {
                // add it at the same position in the combined one
                combined[index] = initial[index];
                // if there isn't
            } else {
                // add from the same position in the player array
                combined[index] = player[index];
            }
        }
        // check if the sum of the numbers in each row is
        // equal to 45 (the sum of numbers from 1 to 9)
        for(int row = 0; row<9; row++) {
            //for that row, create a sum variable
            int sum = 0;
            // add all the numbers from a row
            for(int col = 0; col<9; col++) {
                sum = sum + combined[row*9 + col];
            }
            // if the sum isn't 45, then the row is invalid, invalidating the whole solution
            if(sum!=45) {
                return false;
            }
        }


        // check if the sum of the numbers in each column is
        // equal to 45 (the sum of numbers from 1 to 9)
        for(int col = 0; col<9; col++) { // note that the for loops are switched around
            //for that column, create a sum variable
            int sum = 0;
            // add all the numbers from a column
            for(int row = 0; row<9; row++) {
                sum = sum + combined[row*9 + col];
            }
            // if the sum isn't 45, then the column is invalid, invalidating the whole solution
            if(sum!=45) {
                return false;
            }
        }

        // check if the sum of the numbers in each 3x3 unique square
        // on the 9x9 board sums to 45 (the sum of num)
        // we are going to create an offset of 3 squares for each check

        // increment the row offset with 3 each time
        int starter = 0;
        for(int ij = 0; ij < 9; ij++){
            //Tracks sum, must be 45 to pass
            int sumblock = 0;
            //Iterates through row of 3x3
            if(ij % 3 ==0 && ij != 0){
                starter+=18;
            }
            for(int i = 0; i < 3; i++){
                //Iterates through column of 3x3
                for(int j = 0; j < 3; j++){
                    sumblock += solution[(i*9+j)+ starter];
                }
            }
            starter+=3;
            if(sumblock != 45){
                return false;
            }

        }
        // if none of the checks have triggered a return false statement,
        // fly the all-clear and return true
        return true;
    }

}