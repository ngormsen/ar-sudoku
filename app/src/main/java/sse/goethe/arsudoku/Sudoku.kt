package sse.goethe.arsudoku

import java.util.*
import kotlin.collections.HashSet
import kotlin.concurrent.schedule

/**
 * Implements a Sudoku class that holds functions for solving the Sudoku and
 * producing a hint given the current state of the Sudoku.
 *
 * @param sudoku an array of numbers representing the scanned sudoku
 */

class Sudoku(private val sudoku: Array<IntArray>) {
    private val n = 9
    private var solvable = true;
    private var time = true;

    fun getSolvableState(): Boolean{
        return solvable;
    }

    fun getCurrentState(): Array<IntArray>{
        return sudoku
    }

    fun setNumber(row: Int, column: Int, value: Int){
        sudoku[row][column] = value
    }

    fun printCurrentState(){
        for (i in 0 until n) {
            for (j in 0 until n) {
                print(sudoku[i][j].toString())
                if (Math.floorMod(j, 3) == 2 && j < n - 1)
                    print(" ")
            }
            println()
            if (Math.floorMod(i, 3) == 2 && i < n - 1) println()
        }
    }

    /**
     * Solves the given Sudoku using a simple backtracking algorithm.
     * Credits to: TODO
     *
     * @author Nils Gormsen
     */
    fun solve() {
        Timer("stopSolver", false).schedule(100) {
//            println("Timer started")
            time = false;
        }
        if (!backtrackSolve()) {
            println("This sudoku can't be solved or execution time to long.")
            solvable = false;
        }
    }
    /**
     * Checks whether it is suitable to put the given number into the chosen field.
     * @param i row of the Sudoku
     * @param j column of the Sudoku
     * @param x number value
     */
    fun isSuitableToPutXThere(i: Int, j: Int, x: Int): Boolean {
        // Is 'x' used in row.
        for (jj in 0 until n) {
            if (sudoku[i][jj] == x) {
                return false
            }
        }
        // Is 'x' used in column.
        for (ii in 0 until n) {
            if (sudoku[ii][j] == x) {
                return false
            }
        }
        // Is 'x' used in sudoku 3x3 box.
        val boxRow = i - i % 3
        val boxColumn = j - j % 3
        for (ii in 0..2) {
            for (jj in 0..2) {
                if (sudoku[boxRow + ii][boxColumn + jj] == x) {
                    return false
                }
            }
        }
        // Everything looks good.
        return true
    }
    /**
     * Goes through all possible combinations to find the correct solution to the Sudoku.
     */
    fun backtrackSolve(): Boolean {
        var i = 0
        var j = 0
        var isThereEmptyCell = false
        var ii = 0
        while (ii < n && !isThereEmptyCell) {
            var jj = 0
            while (jj < n && !isThereEmptyCell) {
                if (sudoku[ii][jj] == 0) {
                    isThereEmptyCell = true
                    i = ii
                    j = jj
                }
                jj++
            }
            ii++
        }
        // We've done here.
        if (!isThereEmptyCell) {
            return true
        }
        for (x in 1..9) {
            if (isSuitableToPutXThere(i, j, x)) {
                sudoku[i][j] = x
                if (time && backtrackSolve()) {
                    return true
                }
                sudoku[i][j] = 0 // We've failed.
            }
        }
        return false // Backtracking
    }

    /**
     * Search for the field with the lowest number of possibilities regarding the three major
     * constraints (row, column & grid) in constant time (for fixed n). Returns the field's
     * indices and as it's value or zero if all fields are filled.
     *
     * The function traverses the Sudoku left-to-right & top-to-bottom (reading order) and returns
     * the first encountered minimum if there are multiple.
     *
     * @author Manuel Stoeckel
     * @param current The sudoku in its current state with empty fields.
     * @return A Triple(indexRow, indexColumn, value)
     */
    fun hint(current: Array<IntArray>): Triple<Int, Int, Int> {
        var min = 10
        var indexRow = -1
        var indexColumn = -1
        for (i in 0 until n) {
            for (j in 0 until n) {
                if (current[i][j] == 0) {
                    val localMin = getCellPossibilityCount(current, i, j)

                    if (localMin < min) {
                        min = localMin
                        indexRow = i
                        indexColumn = j

                        // Early stopping if there is only one possible number
                        if (localMin == 1) {
                            return Triple(indexRow, indexColumn, sudoku[indexRow][indexColumn])
                        }
                    }
                }
            }
        }
        val value = (if (indexColumn >= 0 && indexRow >= 0) sudoku[indexRow][indexColumn] else 0)
        return Triple(indexRow, indexColumn, value)
    }

    /**
     * Returns the number of possibilities across row, column and grid constraints for a
     * given sudoku state and cell.
     *
     * @author Manuel Stoeckel
     * @param current The current sudoku state
     * @param row The row index
     * @param col The column index
     * Default: true
     * @return The minimum number of possibilities
     */
    private fun getCellPossibilityCount(
        current: Array<IntArray>,
        row: Int,
        col: Int
    ): Int {
        val offsetRow = Math.floorDiv(row, 3) * 3
        val offsetColumn = Math.floorDiv(col, 3) * 3

        val occurringNumbers = HashSet<Int>()

        // Add numbers that occur in the current row and column
        occurringNumbers.addAll(getRowNumbers(current, row))
        occurringNumbers.addAll(getColumnNumbers(current, col))

        // Add numbers, that occur in the current minor grid
        for (i in offsetRow until offsetRow + 3) {
            for (j in offsetColumn until offsetColumn + 3) {
                occurringNumbers.add(current[i][j])
            }
        }

        val remainingPossible = getRemainingPossible(occurringNumbers)

        // Get the set of numbers that occur in all conjugate rows and columns.
        // If this set is not empty, filter the remaining possible numbers for them.
        if (remainingPossible.size > 1) {
            val conjugates = getConjugateNumbers(current, row, offsetRow, col, offsetColumn)

            if (conjugates.isNotEmpty() && remainingPossible.intersect(conjugates).isNotEmpty())
                remainingPossible.retainAll(conjugates)
        }

        return remainingPossible.size
    }

    private fun getRowNumbers(current: Array<IntArray>, row: Int): HashSet<Int> {
        val occurringNumbers = HashSet<Int>()
        occurringNumbers.addAll(current[row].asIterable())
        return occurringNumbers
    }

    private fun getColumnNumbers(current: Array<IntArray>, col: Int): HashSet<Int> {
        val occurringNumbers = HashSet<Int>()
        for (i in 0 until n) {
            occurringNumbers.add(current[i][col])
        }
        return occurringNumbers
    }

    private fun getConjugateNumbers(
        current: Array<IntArray>,
        row: Int,
        gridOffsetRow: Int,
        col: Int,
        gridOffsetColumn: Int
    ): HashSet<Int> {
        var c = true
        val conjugateOccurring = HashSet<Int>()
        for (i in gridOffsetRow until gridOffsetRow + 3) {
            if (i != row) {
                if (c) {
                    conjugateOccurring.addAll(getRowNumbers(current, i))
                    c = false
                } else {
                    conjugateOccurring.retainAll(getRowNumbers(current, i))
                }
            }
        }

        for (j in gridOffsetColumn until gridOffsetColumn + 3) {
            if (j != col) {
                conjugateOccurring.retainAll(getColumnNumbers(current, j))
            }
        }
        conjugateOccurring.remove(0)
        return conjugateOccurring
    }

    /**
     * Returns the remaining possible numbers for a given set of occurring numbers
     *
     * @param occurringNumbers The set of occurring numbers
     * @return The remaining possible numbers as a new HashSet(Int)
     */
    private fun getRemainingPossible(occurringNumbers: HashSet<Int>): HashSet<Int> {
        val remainingNumbers = HashSet<Int>(listOf(1, 2, 3, 4, 5, 6, 7, 8, 9))
        remainingNumbers.removeAll(occurringNumbers)
        return remainingNumbers
    }


    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            Sudoku(
                arrayOf(
                    intArrayOf(3, 0, 6, 5, 0, 8, 4, 0, 0),
                    intArrayOf(5, 2, 0, 0, 0, 0, 0, 0, 0),
                    intArrayOf(0, 8, 7, 0, 0, 0, 0, 3, 1),
                    intArrayOf(0, 0, 3, 0, 1, 0, 0, 8, 0),
                    intArrayOf(9, 0, 0, 8, 6, 3, 0, 0, 5),
                    intArrayOf(0, 5, 0, 0, 9, 0, 6, 0, 0),
                    intArrayOf(1, 3, 0, 0, 0, 0, 2, 5, 0),
                    intArrayOf(0, 0, 0, 0, 0, 0, 0, 7, 4),
                    intArrayOf(0, 0, 5, 2, 0, 6, 3, 0, 0)
                )
            ).solve()
        }
    }
}
