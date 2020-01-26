package com.motionapps.sensortemplate.model.classifiers.sklearn

import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.tanh

class SVC (
    private val  nClasses: Int,
    private val  nRows: Int,
    private val  vectors: Array<DoubleArray>,
    private val  coefficients: Array<DoubleArray>,
    private val  intercepts: DoubleArray,
    private val  weights: IntArray,
    private val  kernel: Kernel,
    private val  gamma: Double,
    private val  coef0: Double,
    private val  degree: Double
): SkLearnInterface {

    private val classes: IntArray = IntArray(nClasses) {0}


    init {
        for(i in 0 until nClasses){
            classes[i] = i
        }
    }

    enum class Kernel {
        LINEAR, POLY, RBF, SIGMOID
    }


    override fun predict(features: DoubleArray): Int {
        val kernels = DoubleArray(vectors.size)
        var kernel: Double

        when (this.kernel) {
            Kernel.LINEAR ->  // <x,x'>
            {
                var i = 0
                while (i < vectors.size) {
                    kernel = 0.0
                    var j = 0
                    while (j < vectors[i].size) {
                        kernel += vectors[i][j] * features[j]
                        j++
                    }
                    kernels[i] = kernel
                    i++
                }
            }
            Kernel.POLY ->  // (y<x,x'>+r)^d
            {
                var i = 0
                while (i < vectors.size) {
                    kernel = 0.0
                    var j = 0
                    while (j < vectors[i].size) {
                        kernel += vectors[i][j] * features[j]
                        j++
                    }
                    kernels[i] =
                        (gamma * kernel + coef0).pow(degree)
                    i++
                }
            }
            Kernel.RBF ->  // exp(-y|x-x'|^2)
            {
                var i = 0
                while (i < vectors.size) {
                    kernel = 0.0
                    var j = 0
                    while (j < vectors[i].size) {
                        kernel += (vectors[i][j] - features[j]).pow(2.0)
                        j++
                    }
                    kernels[i] = exp(-gamma * kernel)
                    i++
                }
            }
            Kernel.SIGMOID ->  // tanh(y<x,x'>+r)
            {
                var i = 0
                while (i < vectors.size) {
                    kernel = 0.0
                    var j = 0
                    while (j < vectors[i].size) {
                        kernel += vectors[i][j] * features[j]
                        j++
                    }
                    kernels[i] = tanh(gamma * kernel + coef0)
                    i++
                }
            }
        }
        val starts = IntArray(nRows)
        for (i in 0 until nRows) {
            if (i != 0) {
                var start = 0
                for (j in 0 until i) {
                    start += weights[j]
                }
                starts[i] = start
            } else {
                starts[0] = 0
            }
        }
        val ends = IntArray(nRows)
        for (i in 0 until nRows) {
            ends[i] = weights[i] + starts[i]
        }
        if (nClasses == 2) {
            for (i in kernels.indices) {
                kernels[i] = -kernels[i]
            }
            var decision = 0.0
            for (k in starts[1] until ends[1]) {
                decision += kernels[k] * coefficients[0][k]
            }
            for (k in starts[0] until ends[0]) {
                decision += kernels[k] * coefficients[0][k]
            }
            decision += intercepts[0]
            return if (decision > 0) {
                0
            } else 1
        }
        val decisions = DoubleArray(intercepts.size)
        run {
            var i = 0
            var d = 0
            val l = this.nRows
            while (i < l) {
                for (j in i + 1 until l) {
                    var tmp = 0.0
                    for (k in starts[j] until ends[j]) {
                        tmp += this.coefficients[i][k] * kernels[k]
                    }
                    for (k in starts[i] until ends[i]) {
                        tmp += this.coefficients[j - 1][k] * kernels[k]
                    }
                    decisions[d] = tmp + this.intercepts[d]
                    d++
                }
                i++
            }
        }
        val votes = IntArray(intercepts.size)
        run {
            var i = 0
            var d = 0
            val l = this.nRows
            while (i < l) {
                for (j in i + 1 until l) {
                    votes[d] = if (decisions[d] > 0) i else j
                    d++
                }
                i++
            }
        }
        val amounts = IntArray(nClasses)
        run {
            var i = 0
            val l = votes.size
            while (i < l) {
                amounts[votes[i]] += 1
                i++
            }
        }
        var classVal = -1
        var classIdx = -1
        var i = 0
        val l = amounts.size
        while (i < l) {
            if (amounts[i] > classVal) {
                classVal = amounts[i]
                classIdx = i
            }
            i++
        }
        return classes[classIdx]
    }
}