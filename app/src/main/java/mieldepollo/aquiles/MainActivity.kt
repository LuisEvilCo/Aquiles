package mieldepollo.aquiles

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.widget.TextView
import com.google.android.gms.common.api.GoogleApiClient
import mieldepollo.aquiles.Core.buildFitnessClient
import mieldepollo.aquiles.Core.checkPermissions
import mieldepollo.aquiles.Core.requestPermissions
import com.google.android.gms.fitness.request.OnDataPointListener
import kotlinx.android.synthetic.main.activity_main.*



class MainActivity : AppCompatActivity() {

    private var mTextMessage: TextView? = null

    // [START auth_variable_references]
    var mClient: GoogleApiClient? = null
    // [END auth_variable_references]

    // [START mListener_variable_reference]
    // Need to hold a reference to this listener, as it's passed into the "unregister"
    // method in order to stop all sensors from sending data to this listener.
    var mListener: OnDataPointListener? = null
    // [END mListener_variable_reference]


    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                message.setText(R.string.title_home)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_dashboard -> {
                message.setText(R.string.title_dashboard)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_notifications -> {
                message.setText(R.string.title_notifications)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        //this should prompt the user to give us control of his/her life :D
        if(!checkPermissions(this)){
            requestPermissions(this)
        }
    }

    override fun onResume() {
        super.onResume()
        mClient ?: checkPermissions(this).let {
            mClient = buildFitnessClient(mainActivity = this)
        }
    }
}
