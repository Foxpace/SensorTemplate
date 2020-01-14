package com.motionapps.sensortemplate.activities.about

import com.motionapps.sensortemplate.R
// enumeration of licenses
enum class AboutEnum(val title: Int, val text: Int) {
    AUTOSTARTER(R.string.about_autostarter_title, R.string.about_autostarter_text),
    TENSORFLOW(R.string.about_tensorflow_title, R.string.about_tensorflow_text);

}