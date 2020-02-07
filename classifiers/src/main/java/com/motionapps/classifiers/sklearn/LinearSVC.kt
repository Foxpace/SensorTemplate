package com.motionapps.classifiers.sklearn

class LinearSVC (
    private val coefficients: Array<DoubleArray>,
    private val intercepts: DoubleArray
): SkLearnInterface {

    override fun predict(features: DoubleArray): Int {
        var classIdx = 0
        var classVal = Double.NEGATIVE_INFINITY
        var i = 0
        val il = intercepts.size
        while (i < il) {
            var prob = 0.0
            var j = 0
            val jl: Int = coefficients[0].size
            while (j < jl) {
                prob += coefficients[i][j] * features[j]
                j++
            }
            if (prob + intercepts[i] > classVal) {
                classVal = prob + intercepts[i]
                classIdx = i
            }
            i++
        }
        return classIdx
    }
}