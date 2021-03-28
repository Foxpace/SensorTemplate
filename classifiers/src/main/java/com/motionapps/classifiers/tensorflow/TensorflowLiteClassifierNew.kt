package com.motionapps.classifiers.tensorflow

import android.content.Context
import com.motionapps.classifiers.ml.Placeholder
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.IOException

import java.nio.ByteBuffer
import java.nio.ByteOrder

class TensorflowLiteClassifierNew: TfInterface {

    private lateinit var model: Placeholder

    private val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, featureSize), DataType.FLOAT32)

    /** initialization of neural network - NN - this simple ! */
    override fun initModel(context: Context) {
        model = Placeholder.newInstance(context)
    }

    /**
     * @param features - number of features - placeholder has 5 inputs
     * @return - there are 2 outputs from 2 neurons, but only one is needed to return, the other one is 1 - returned value
     */
    override fun predict(features: ArrayList<Double>): Float {
        val inputData = ByteBuffer.allocateDirect(4 * featureSize) // input buffer with floats
        inputData.order(ByteOrder.nativeOrder())

        for (feature in features) {
            inputData.putFloat(feature.toFloat())
        }

        inputFeature0.loadBuffer(inputData)

        // Runs model inference and gets result.
        val outputs = model.process(inputFeature0)
        return outputs.outputFeature0AsTensorBuffer.floatArray[0]
    }

    override fun closeModel() {
        model.close()
    }

    /**
     * singleton access to NN
     */
    companion object{
        const val featureSize = 5
        private var classifier: TensorflowLiteClassifierNew? = null

        @Throws(IOException::class)
        fun create(context: Context): TensorflowLiteClassifierNew? {
            if (classifier == null) { // existing NN is returned
                classifier = TensorflowLiteClassifierNew()
                classifier?.initModel(context)
            }
            return classifier
        }

        fun destroy() {
            if (classifier != null) {
                classifier!!.closeModel()
                classifier = null
            }
        }
    }


}