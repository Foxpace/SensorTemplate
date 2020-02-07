package com.motionapps.sensortemplate.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.motionapps.sensortemplate.activities.components.Permissions
import com.motionapps.sensortemplate.R

class Options : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment : PreferenceFragmentCompat() {


        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            // custom click preference
            val preference: Preference = findPreference(STARTER)!!
            preference.setOnPreferenceClickListener {
                context?.let { it1 -> Permissions.openWhiteList(context = it1) }
                true
            }
        }
    }


    companion object{
    // keys for preferences
        const val STARTER = "preference_background"
        const val FIRST_START = "preference_first_start"
    }
}