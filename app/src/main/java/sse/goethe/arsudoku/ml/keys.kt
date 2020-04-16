/**
 * @author: David Machajewski
 * date: 26.01.2020
 */
package sse.goethe.arsudoku.ml

object Keys {
    const val MODEL_PATH = "cnn_full_finetuning.tflite"
    const val LABEL_PATH = "mnistLabels.txt"
    const val MAX_RESULTS = 3
    const val DIM_IMG_SIZE_X = 28 /* 28 like mnist */
    const val DIM_IMG_SIZE_Y = 28
    const val DIM_PIXEL_SIZE = 1 /* 1 because greyscale, not rgb */
    const val DIM_BATCH_SIZE = 1
    const val INPUT_SIZE = 28 /* for ... */
}