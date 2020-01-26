package com.motionapps.sensortemplate.model.classifiers.sklearn

interface SkLearnInterface {
    fun predict(features: DoubleArray) : Int
}