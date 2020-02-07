package com.motionapps.sensortemplate.activities.welcome.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.motionapps.sensortemplate.R


class FragmentWelcome : Fragment(),
    CheckFragment {

    override fun getWarningString(): String {
        return ""
    }

    override fun isOk(): Boolean {
        return true
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_welcome, container, false)
    }


    companion object {
        @JvmStatic
        fun newInstance() =
            FragmentWelcome()
    }
}
