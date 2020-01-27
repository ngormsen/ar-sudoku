/**
 * @author: David Machajewski
 * date: 26.01.2020
 *
 */
package sse.goethe.arsudoku.ml
import sse.goethe.arsudoku.ml.Keys.LABEL_PATH
import android.content.res.AssetManager
import org.tensorflow.lite.Interpreter
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.lang.RuntimeException
import java.util.*

/**
 *
 */
class DigitClassifier constructor(private val assetManager: AssetManager) {
    private var interpreter: Interpreter? = null
    val results = 0
    private val labels = Vector<String>()

    init {
        /*
        During an instance initialization, the initializer blocks
        are executed in the same order as they appear in the class body
        */
        try {
            val br = BufferedReader(InputStreamReader(assetManager.open(LABEL_PATH)))
            while (true) {
                val line = br.readLine() ?: break
                labels.add(line)
            }
            br.close()
        } catch (e: IOException) {
            throw RuntimeException("can not read the labels or label file!", e)
        }
        // TODO: Init ByteArray, get image data and load the Model
    }
}



