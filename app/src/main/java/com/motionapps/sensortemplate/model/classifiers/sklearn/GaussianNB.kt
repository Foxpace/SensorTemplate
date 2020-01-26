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

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            if (args.size == 4) { // Features:
                val features = DoubleArray(args.size)
                var i = 0
                val l = args.size
                while (i < l) {
                    features[i] = args[i].toDouble()
                    i++
                }
                // Parameters:
                val priors =
                    doubleArrayOf(0.3333333333333333, 0.3333333333333333, 0.3333333333333333)
                val sigmas = arrayOf(
                    doubleArrayOf(
                        0.12176400309550259,
                        0.14081600309550263,
                        0.029556003095502676,
                        0.010884003095502673
                    ),
                    doubleArrayOf(
                        0.2611040030955028,
                        0.09650000309550268,
                        0.21640000309550278,
                        0.03832400309550265
                    ),
                    doubleArrayOf(
                        0.39625600309550263,
                        0.10192400309550273,
                        0.2984960030955029,
                        0.07392400309550265
                    )
                )
                val thetas = arrayOf(
                    doubleArrayOf(
                        5.005999999999999,
                        3.428000000000001,
                        1.4620000000000002,
                        0.2459999999999999
                    ),
                    doubleArrayOf(5.936, 2.7700000000000005, 4.26, 1.3259999999999998),
                    doubleArrayOf(6.587999999999998, 2.9739999999999998, 5.552, 2.026)
                )
                // Prediction:
                val clf = GaussianNB(priors, sigmas, thetas)
                val estimation = clf.predict(features)
                println(estimation)
            }
        }
    }

}