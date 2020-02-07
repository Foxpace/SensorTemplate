package com.motionapps.sensortemplate.activities.welcome.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat

import com.motionapps.sensortemplate.R


class FragmentPermission : Fragment(), CheckFragment {

    private val map: HashMap<Int, PermissionObject> = HashMap() // request code + custom object for permission

    override fun getWarningString(): String {
        return getString(R.string.permissions_needed)
    }

    override fun isOk(): Boolean {
        for(id: Int in map.keys){ // checks all permissions
            if(!map[id]!!.granted){
                return false
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val o: PermissionObject = map[requestCode]!!
        if (ContextCompat.checkSelfPermission(activity!!, o.permission) == PackageManager.PERMISSION_GRANTED){
            o.setPermissionGranted() // permission has been given
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_permission, container, false)
    }

    override fun onResume() {
        super.onResume()
        // arrays of basic info for required permissions - creation of views
        val permissions: Array<String> = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        val permissionsID: Array<Int> = arrayOf(GPS_PERMISSION)
        val permissionsViews: Array<Int> = arrayOf(R.id.permission_gps)
        val permissionsText: Array<Int> = arrayOf(R.string.permission_gps_text)

        for(i in permissions.indices){
            val v: View = view!!.findViewById<View>(permissionsViews[i])
            map[permissionsID[i]] = PermissionObject(this, v, permissionsID[i], permissions[i], permissionsText[i])
        }
    }


    companion object {
        @JvmStatic
        fun newInstance() = FragmentPermission()

        private const val GPS_PERMISSION = 1589
    }

    /**
     * custom object to store permission and its view
     */
    internal class PermissionObject(
        private val fragment: FragmentPermission, private val view: View,
        internal val id: Int,
        internal val permission: String,
        permissionsText: Int
    ){

        internal var granted: Boolean = false

        init {
            val textView = view.findViewById<TextView>(R.id.permission_line_text)
            textView.setText(permissionsText)
            //
            if (ContextCompat.checkSelfPermission(fragment.activity!!, permission) == PackageManager.PERMISSION_GRANTED) {
                granted = true
                setPermissionGranted()
            } else {
                val b = view.findViewById<Button>(R.id.permission_line_button)
                b.setOnClickListener {
                    fragment.requestPermissions(arrayOf(permission), id)
                }
            }
        }

        fun setPermissionGranted() {
            val b = view.findViewById<Button>(R.id.permission_line_button)
            b.isClickable = false
            b.alpha = 0.5f

            val image = view.findViewById<ImageView>(R.id.permission_line_image)
            image.setImageResource(R.drawable.ic_check_black)
        }
    }
}
