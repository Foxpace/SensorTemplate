package com.motionapps.sensortemplate.activities.welcome.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
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
        if (ContextCompat.checkSelfPermission(requireActivity(), o.permission[0]) == PackageManager.PERMISSION_GRANTED){
            o.setPermissionGranted() // permission has been given
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_permission, container, false)
        // arrays of basic info for required permissions - creation of views
        val linearLayout: LinearLayout = view!!.findViewById(R.id.permission_container)

        val permissions = ArrayList<Array<String>>()
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            permissions.add(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION))
        }else{
            permissions.add(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
        }
        val permissionsID: ArrayList<Int> = arrayListOf(GPS_PERMISSION)
        val permissionsText: ArrayList<Int> = arrayListOf(R.string.permission_gps_text)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            permissions.add(arrayOf(Manifest.permission.ACTIVITY_RECOGNITION))
            permissionsID.add(ACTIVITY_RECOGNITION)
            permissionsText.add(R.string.permission_activity_text)
        }

        for(i in permissions.indices){
            val v = layoutInflater.inflate(R.layout.permission_line, null)
            if(v != null){
                linearLayout.addView(v)
                map[permissionsID[i]] = PermissionObject(this, v,
                    permissionsID[i], permissions[i], permissionsText[i])
            }

        }
        return view
    }




    companion object {
        @JvmStatic
        fun newInstance() = FragmentPermission()

        private const val GPS_PERMISSION = 1589
        private const val ACTIVITY_RECOGNITION = 1685
    }

    /**
     * custom object to store permission and its view
     */
    internal class PermissionObject(
        private val fragment: FragmentPermission, private val view: View,
        internal val id: Int,
        internal val permission: Array<String>,
        permissionsText: Int
    ){

        internal var granted: Boolean = false

        init {
            val textView = view.findViewById<TextView>(R.id.permission_line_text)
            textView.setText(permissionsText)
            //
            if (ContextCompat.checkSelfPermission(fragment.requireActivity(), permission[0]) == PackageManager.PERMISSION_GRANTED) {
                granted = true
                setPermissionGranted()
            } else {
                val b = view.findViewById<Button>(R.id.permission_line_button)
                b.setOnClickListener {
                    fragment.requestPermissions(permission, id)
                }
            }
        }

        fun setPermissionGranted() {
            val b = view.findViewById<Button>(R.id.permission_line_button)
            b.isClickable = false
            b.alpha = 0.5f

            val image = view.findViewById<ImageView>(R.id.permission_line_image)
            image.setImageResource(R.drawable.ic_check_black)
            granted = true
        }
    }
}
