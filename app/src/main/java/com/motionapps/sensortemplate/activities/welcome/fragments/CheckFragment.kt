package com.motionapps.sensortemplate.activities.welcome.fragments

/**
 * basic inteface for welcome fragments to check conditions or get message
 */

interface CheckFragment {
    fun isOk(): Boolean
    fun getWarningString(): String
}