package com.motionapps.sensortemplate.activities.welcome

import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.motionapps.sensortemplate.R
import com.motionapps.sensortemplate.activities.Main
import com.motionapps.sensortemplate.activities.Options
import com.motionapps.sensortemplate.activities.welcome.fragments.CheckFragment
import com.motionapps.sensortemplate.activities.welcome.fragments.FragmentPermission
import com.motionapps.sensortemplate.activities.welcome.fragments.FragmentWelcome

class Welcome : AppCompatActivity() {

    private var page: Int = 0
    private var currentFragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        // to move fragment
        val b = findViewById<Button>(R.id.welcome_next)
        b.setOnClickListener {
            changeFragment(false)
        }

        // set first fragment
        currentFragment = FragmentWelcome.newInstance()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.fragment_container, currentFragment!!)
        transaction.addToBackStack(null)
        transaction.commit()
        supportFragmentManager.executePendingTransactions()
    }

    private fun changeFragment(backPress: Boolean) {

        // check if fragments conditions are met. If not - show toast with message
        if(currentFragment != null && !(currentFragment as CheckFragment).isOk() && !backPress){
            Toast.makeText(this@Welcome,
                (currentFragment as CheckFragment).getWarningString(), Toast.LENGTH_LONG).show()
            return
        }else if (!backPress){ // if not back button - next page
            page += 1
        }

        currentFragment = getFragment(backPress)

        if (currentFragment != null){
            val transaction = supportFragmentManager.beginTransaction()
            if (backPress) { // custom animation to swipe left and right
                transaction.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right)
            } else {
                transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left)
            }
            transaction.replace(R.id.fragment_container, currentFragment!!)
            transaction.addToBackStack(null)
            transaction.commit()
        }else{ // all fragments passed - show main activity
            val prefs : SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
            val editor: SharedPreferences.Editor = prefs.edit()
            editor.putBoolean(Options.FIRST_START, true)
            editor.apply()

            startActivity(Intent(applicationContext, Main::class.java))
            finish()
        }

    }

    private fun getFragment(backPress: Boolean): Fragment? {
        //picks required fragment by conditions
        when(page){
            WELCOME -> return FragmentWelcome.newInstance()
            PERMISSION -> {
                return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    FragmentPermission.newInstance()
                }else{
                    if(backPress){
                        page -= 1
                    }else{
                        page += 1
                    }
                    getFragment(backPress)
                }
            }
            EXIT -> return null

        }
        return null
    }

    override fun onBackPressed() {

        if(page == WELCOME){ // if the first page is shown  - go back
            super.onBackPressed()
            return
        }

        page -= 1
        changeFragment(true)
    }

    companion object {
        private const val WELCOME: Int = 0
        private const val PERMISSION: Int = 1
        private const val EXIT = 2
    }


}
