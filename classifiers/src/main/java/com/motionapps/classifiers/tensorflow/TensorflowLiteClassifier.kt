package com.motionapps.classifiers.tensorflow

import android.content.Context
import android.os.SystemClock
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.collections.ArrayList


class TensorflowLiteClassifier(context: Context) {
    private var tflite: Interpreter?
    private val inputData: ByteBuffer
    private val labelProbArray: Array<FloatArray>
    private val labels: ArrayList<String> = ArrayList()


    /** initialization of neural network - NN */
    init {
        val options = Interpreter.Options()
        tflite = Interpreter(loadModelFile(context), options) // main interpreter of NN
        inputData = ByteBuffer.allocateDirect(4 * FEATURES_SIZE) // input buffer with floats
        inputData.order(ByteOrder.nativeOrder())
        labelProbArray = Array(1) { FloatArray(2) } // probe for outputs - array
        readLabels(context)
        Log.d(TAG, "Created a Tensorflow Lite  Classifier.")
    }

    private fun readLabels(context: Context){

        val br: BufferedReader?
        br = BufferedReader(InputStreamReader(context.assets.open(LABEL_PATH))) // reads labels from txt file - strings to show
        var line: String?
        while (br.readLine().also { line = it } != null) {
            labels.add(line!!)
        }
        br.close()
    }

    /**
     * @param features - number of features - placeholder has 5 inputs
     * @return - there are 2 outputs from 2 neurons, but only one is needed to return, the other one is 1 - returned value
     */
    fun classifyEvent(features: ArrayList<Double>?): Float {
        if (features == null) {
            return -1.0f
        }

        if (tflite == null) {
            Log.e(TAG, "Classifier has not been initialized; Skipped.")
            return -1.0f
        }
        if (features.size != FEATURES_SIZE) {
            Log.e(TAG, "Wrong size of features; Skipped.")
            return -1.0f
        }
        for (i in 0 until FEATURES_SIZE) {
            inputData.putFloat(features[i].toFloat())
        }

        // NN calculation
        val startTime = SystemClock.uptimeMillis()
        tflite!!.run(inputData, labelProbArray)
        val endTime = SystemClock.uptimeMillis()

        Log.d(TAG, "Timecost to run model inference: " + (endTime - startTime))
        inputData.clear()
        return labelProbArray[0][0] // picked first value
    }

    private fun close() {
        if (tflite != null) {
            tflite!!.close()
            tflite = null
        }
    }

    /**
     * @param context
     * @return - mapped file of NN
     * @throws IOException - NN is absent
     */
    @Throws(IOException::class)
    private fun loadModelFile(context: Context): MappedByteBuffer {

        val fileDescriptor = context.assets.openFd(MODEL_PATH)
        val fileChannel = FileInputStream(fileDescriptor.fileDescriptor).channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength

        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    /**
     * singleton access to NN
     */
    companion object {

        //basic paths to files in resources
        private const val TAG = "TfLiteFallDetection"
        private const val MODEL_PATH = "tensorflow/placeholder.tflite"
        private const val LABEL_PATH = "tensorflow/labels.txt"
        private const val FEATURES_SIZE = 5

        private var fallClassifierLite: TensorflowLiteClassifier? = null

        @Throws(IOException::class)
        fun create(context: Context): TensorflowLiteClassifier? {
            if (fallClassifierLite == null) { // existing NN is returned
                fallClassifierLite =
                    TensorflowLiteClassifier(
                        context
                    )
            }
            return fallClassifierLite
        }

        fun destroy() {
            if (fallClassifierLite != null) {
                fallClassifierLite!!.close()
                fallClassifierLite = null
            }
        }


    }


}