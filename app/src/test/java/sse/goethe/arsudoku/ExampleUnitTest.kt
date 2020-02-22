package sse.goethe.arsudoku

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testSudokuHint() {
        val current = arrayOf(
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
        for (i in 0 until 9) {
            for (j in 0 until 9) {
                print(current[i][j].toString() + " ")
            }
            println()
        }
        println()
        val sudoku = Sudoku(
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
        )
        sudoku.solve()
        println()
        println(sudoku.hint(current))
    }

}
