/**
 * @author: David Machajewski
 * date: 26.01.2020
 *
 */
package sse.goethe.arsudoku.ml
import sse.goethe.arsudoku.ml.Keys.LABEL_PATH
import sse.goethe.arsudoku.ml.Keys.DIM_IMG_SIZE_X
import sse.goethe.arsudoku.ml.Keys.DIM_IMG_SIZE_Y
import sse.goethe.arsudoku.ml.Keys.DIM_BATCH_SIZE
import sse.goethe.arsudoku.ml.Keys.DIM_PIXEL_SIZE
import sse.goethe.arsudoku.ml.Keys.INPUT_SIZE
import android.content.res.AssetManager
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import sse.goethe.arsudoku.ml.Keys.MODEL_PATH
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader
import io.reactivex.Single
import java.lang.Exception
import java.lang.RuntimeException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.*

/**
 *
 */
class DigitClassifier constructor(private val assetManager: AssetManager) {
    private var interpreter: Interpreter? = null
    val results = 0
    private var labelProb: Array<ByteArray>
    private val labels = Vector<String>()
    private var imgData: ByteBuffer
    private val intValues by lazy { IntArray(INPUT_SIZE) }
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
        labelProb = Array(1) {ByteArray(labels.size)}
        imgData = ByteBuffer.allocateDirect(DIM_BATCH_SIZE * DIM_PIXEL_SIZE *
                DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y)
        imgData.order(ByteOrder.nativeOrder())
        try {
            interpreter = Interpreter(loadModelFile(assetManager, MODEL_PATH))
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    private fun loadModelFile(assets: AssetManager, modelFilename: String): MappedByteBuffer {
        val fileDescriptor = assets.openFd(modelFilename)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)

        /* TODO: Test if it is working properly with some prints ! */
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap) {
        imgData.rewind()
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        var pixel = 0
        for(i in 0 until DIM_IMG_SIZE_X)
            for(j in 0 until DIM_IMG_SIZE_Y) {
                val value = intValues[pixel++]
                imgData.put((value shr 16 and 0xFF).toByte())
                imgData.put((value shr 8 and 0xFF).toByte())
                imgData.put((value and 0xFF).toByte())
                /* TODO: explain short */
            }
    }

    private fun recognizeImage(bitmap: Bitmap){
        /* TODO: interpreter will take the imgData as bytebuffer ... */
        /* use Single from reactiveX */
    }
}



