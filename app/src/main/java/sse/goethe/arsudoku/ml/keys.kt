/**
 * @author: David Machajewski
 * date: 26.01.2020
 */
package sse.goethe.arsudoku.ml

object Keys {
    const val MODEL_PATH = "app/assets/mnist_model.tflite"
    const val LABEL_PATH = "app/assets/mnist_labels.txt"
    /* TODO: DEFINE REST OF CONSTANTS */

    /* TODO: IMG SIZE HAS TO BE DISCUSSED */
    const val DIM_IMG_SIZE_X = 28 /* 28 like mnist */
    const val DIM_IMG_SIZE_Y = 28
    const val DIM_PIXEL_SIZE = 1 /* 1 because greyscale, not rgb */
    const val DIM_BATCH_SIZE = 1
    const val INPUT_SIZE = 28 /* for ... */
}