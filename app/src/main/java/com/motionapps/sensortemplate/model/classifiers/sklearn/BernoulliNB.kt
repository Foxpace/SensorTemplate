package com.motionapps.sensortemplate.model.classifiers.sklearn

class BernoulliNB(
    private val priors: DoubleArray,
    private val negProbs: Array<DoubleArray>,
    private val delProbs: Array<DoubleArray>
) : SkLearnInterface {

    override fun predict(features: DoubleArray): Int {
        val nClasses = priors.size
        val nFeatures = delProbs.size
        val jll = DoubleArray(nClasses)
        for (i in 0 until nClasses) {
            var sum = 0.0
            for (delProb in delProbs) {
                sum += features[i] * delProb[i]
            }
            jll[i] = sum
        }
        for (i in 0 until nClasses) {
            var sum = 0.0
            for (j in 0 until nFeatures) {
                sum += negProbs[i][j]
            }
            jll[i] += priors[i] + sum
        }
        var classIndex = 0
        for (i in 0 until nClasses) {
            classIndex = if (jll[i] > jll[classIndex]) i else classIndex
        }
        return classIndex
    }

}