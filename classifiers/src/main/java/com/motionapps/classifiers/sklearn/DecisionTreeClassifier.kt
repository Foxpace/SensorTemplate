package com.motionapps.classifiers.sklearn

class DecisionTreeClassifier(
    private val lChilds: IntArray,
    private val rChilds: IntArray,
    private val thresholds: DoubleArray,
    private val indices: IntArray,
    private val classes: Array<IntArray>
): SkLearnInterface {

    override fun predict(features: DoubleArray): Int {
        return predict(features)
    }

    fun predict(features: DoubleArray, node: Int = 0): Int {
        return if (thresholds[node] != -2.0) {
            if (features[this.indices[node]] <= thresholds[node]) {
                predict(features, lChilds[node])
            } else {
                predict(features, rChilds[node])
            }
        } else findMax(classes[node])
    }

    private fun findMax(nums: IntArray): Int {
        var index = 0
        for (i in nums.indices) {
            index = if (nums[i] > nums[index]) i else index
        }
        return index
    }


}