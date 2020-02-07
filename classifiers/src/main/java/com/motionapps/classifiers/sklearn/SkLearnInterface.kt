package com.motionapps.classifiers.sklearn

interface SkLearnInterface {
    fun predict(features: DoubleArray) : Int
}