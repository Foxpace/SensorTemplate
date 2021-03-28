package com.motionapps.classifiers.tensorflow

import android.content.Context


interface TfInterface {
    fun initModel(context: Context)
    fun predict(features: ArrayList<Double>): Float
    fun closeModel()

}