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

    /**
     * Search for the field with the lowest number of possibilities regarding the three major
     * constraints (row, column & grid) in constant time (for fixed n). Returns the field's
     * indices as well as it's value or zero if all fields are filled.
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
        for (j in 0 until n) {
            for (i in 0 until n) {
                if (current[i][j] == 0) {
                    val localMin = getMinPossibilities(current, i, j)

                    if (localMin < min) {
                        // Early stopping if there is only one possible number
                        if (localMin == 1) {
                            return Triple(indexRow, indexColumn, sudoku[indexRow][indexColumn])
                        }
                        min = localMin
                        indexRow = i
                        indexColumn = j
                    }
                }
            }
        }
        val value: Int
        if (indexColumn >= 0 && indexRow >= 0) {
            value = sudoku[indexRow][indexColumn]
            print(min.toString() + " -> ") // FIXME: remove
        } else {
            value = 0
        }
        return Triple(indexRow, indexColumn, value)
    }

    /**
     * Returns the minimum number of possibilities across row, column and grid constraints for a
     * given sudoku state and column & row indices.
     *
     * @author Manuel Stoeckel
     * @param current The current sudoku state
     * @param i The row index
     * @param j The column index
     * @return The minimum number of possibilities
     */
    private fun getMinPossibilities(current: Array<IntArray>, i: Int, j: Int): Int {
        val rowPossibilities = getRowPossibilities(current, i)
        val columnPossibilities = getColumnPossibilities(current, j)
        val gridPossibilities = getGridPossibilities(current, i, j)

        val localMin = intArrayOf(
            rowPossibilities.size,
            columnPossibilities.size,
            gridPossibilities.size
        ).min()

        return localMin!!
    }

    fun getRowPossibilities(current: Array<IntArray>, row: Int): HashSet<Int> {
        val ret = HashSet<Int>()
        ret.addAll(current[row].asIterable())
        return getDiff(ret)
    }

    fun getColumnPossibilities(current: Array<IntArray>, col: Int): HashSet<Int> {
        val ret = HashSet<Int>()
        for (i in 0 until n) {
            ret.add(current[i][col])
        }
        return getDiff(ret)
    }

    fun getGridPossibilities(current: Array<IntArray>, row: Int, col: Int): HashSet<Int> {
        val gridOffsetRow = Math.floorMod(row, 3) * 3
        val gridOffsetColumn = Math.floorMod(col, 3) * 3

        val ret = HashSet<Int>()
        for (i in gridOffsetRow until gridOffsetRow + 3) {
            for (j in gridOffsetColumn until gridOffsetColumn + 3) {
                ret.add(current[i][j])
            }
        }
        return getDiff(ret)
    }

    fun getDiff(set: HashSet<Int>): HashSet<Int> {
        val numbers = HashSet<Int>(listOf(1, 2, 3, 4, 5, 6, 7, 8, 9))
        numbers.removeAll(set)
        return numbers
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
