package com.motionapps.classifiers.sklearn

import java.util.*
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class KNeighborsClassifier(
    private val nNeighbors: Int,
    private val nClasses: Int,
    private val power: Double,
    private val X: Array<DoubleArray>,
    private val y: IntArray
): SkLearnInterface {
    private val nTemplates: Int = y.size

    private class Neighbor internal constructor(var clazz: Int, var dist: Double)

    override fun predict(features: DoubleArray): Int {
        var classIdx = 0
        if (nNeighbors == 1) {
            var minDist = Double.POSITIVE_INFINITY
            var curDist: Double
            for (i in 0 until nTemplates) {
                curDist =
                    compute(X[i], features, power)
                if (curDist <= minDist) {
                    minDist = curDist
                    classIdx = y[i]
                }
            }
        } else {
            val classes = IntArray(nClasses)
            val dists = ArrayList<Neighbor>()
            for (i in 0 until nTemplates) {
                dists.add(
                    Neighbor(
                        y[i],
                        compute(X[i], features, power)
                    )
                )
            }
            dists.sortWith(Comparator { n1, n2 -> n1.dist.compareTo(n2.dist) })
            for (neighbor in dists.subList(0, nNeighbors)) {
                classes[neighbor.clazz]++
            }
            for (i in 0 until nClasses) {
                classIdx = if (classes[i] > classes[classIdx]) i else classIdx
            }
        }
        return classIdx
    }

    companion object {
        private fun compute(
            temp: DoubleArray,
            cand: DoubleArray,
            q: Double
        ): Double {
            var dist = 0.0
            var diff: Double
            var i = 0
            val l = temp.size
            while (i < l) {
                diff = abs(temp[i] - cand[i])
                if (q == 1.0) {
                    dist += diff
                } else if (q == 2.0) {
                    dist += diff * diff
                } else if (q == Double.POSITIVE_INFINITY) {
                    if (diff > dist) {
                        dist = diff
                    }
                } else {
                    dist += diff.pow(q)
                }
                i++
            }
            return if (q == 1.0 || q == Double.POSITIVE_INFINITY) {
                dist
            } else if (q == 2.0) {
                sqrt(dist)
            } else {
                dist.pow(1.0 / q)
            }
        }
    }


}