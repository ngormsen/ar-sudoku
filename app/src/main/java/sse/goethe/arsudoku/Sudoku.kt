package sse.goethe.arsudoku

class Sudoku(private val sudoku: Array<IntArray>) {
    private val n = 9
    fun solve() {
        if (!backtrackSolve()) {
            println("This sudoku can't be solved.")
        }
        for (i in 0 until n) {
            for (j in 0 until n) {
                print(sudoku[i][j].toString() + " ")
            }
            println()
        }
    }

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
                if (backtrackSolve()) {
                    return true
                }
                sudoku[i][j] = 0 // We've failed.
            }
        }
        return false // Backtracking
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
