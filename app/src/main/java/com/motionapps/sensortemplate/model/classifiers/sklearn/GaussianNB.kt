package com.motionapps.sensortemplate.model.classifiers.sklearn

import kotlin.math.ln
import kotlin.math.pow

class GaussianNB(
    private val priors: DoubleArray,
    private val sigmas: Array<DoubleArray>,
    private val thetas: Array<DoubleArray>
): SkLearnInterface {
    override fun predict(features: DoubleArray): Int {
        val likelihoods = DoubleArray(sigmas.size)
        run {
            var i = 0
            val il = this.sigmas.size
            while (i < il) {
                var sum = 0.0
                run {
                    var j = 0
                    val jl: Int = this.sigmas[0].size
                    while (j < jl) {
                        sum += ln(2.0 * Math.PI * this.sigmas[i][j])
                        j++
                    }
                }
                var nij = -0.5 * sum
                sum = 0.0
                var j = 0
                val jl: Int = this.sigmas[0].size
                while (j < jl) {
                    sum += (features[j] - this.thetas[i][j]).pow(2.0) / this.sigmas[i][j]
                    j++
                }
                nij -= 0.5 * sum
                likelihoods[i] = ln(this.priors[i]) + nij
                i++
            }
        }
        var classIdx = 0
        var i = 0
        val l = likelihoods.size
        while (i < l) {
            classIdx = if (likelihoods[i] > likelihoods[classIdx]) i else classIdx
            i++
        }
        return classIdx
    }
}