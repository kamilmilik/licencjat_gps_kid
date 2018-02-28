package kamilmilik.licencjat_gps_kid

import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import kamilmilik.licencjat_gps_kid.Login.LoginActivity
import kamilmilik.licencjat_gps_kid.Login.RegistrationActivity
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION




class MainActivity : AppCompatActivity() {
    private val TAG = MainActivity::class.java.simpleName
    private val MY_PERMISSION_REQUEST_CODE : Int = 7171

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        if (ContextCompat.checkSelfPermission(this,
//                android.Manifest.permission.ACCESS_FINE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED) {
//            Log.i(TAG, "Wszystko ok ?")
//            // Should we show an explanation?
//            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)&&
//                    ( ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
//                Toast.makeText(this,"You must accept it",Toast.LENGTH_SHORT).show()
//                // Show an explanation to the user *asynchronously* -- don't block
//                // this thread waiting for the user's response! After the user
//                // sees the explanation, try again to request the permission.
//
//            } else {
//
//                // No explanation needed, we can request the permission.
//                Log.i(TAG,"request permission")
//                ActivityCompat.requestPermissions(this,
//                        arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION,
//                                android.Manifest.permission.ACCESS_COARSE_LOCATION),
//                        MY_PERMISSION_REQUEST_CODE)
//
//                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
//                // app-defined int constant. The callback method gets the
//                // result of the request.
//            }
//        } else {
//            // Permission has already been granted
//            var firebaseAuth = FirebaseAuth.getInstance()
//            if (firebaseAuth.currentUser != null) {
//                finish()
//                val intent = Intent(this, ListOnline::class.java)
//                startActivity(intent)
//            }
//            registrationButton.setOnClickListener(View.OnClickListener {
//                Log.i(TAG, "registrationButton listener: registration button Action")
//                val intent = Intent(this@MainActivity, RegistrationActivity::class.java)
//                startActivity(intent)
//            })
//
//            loginButton.setOnClickListener(View.OnClickListener {
//                Log.i(TAG, "loginButton listener: login button Action")
//                val intent = Intent(this@MainActivity, LoginActivity::class.java)
//                startActivity(intent)
//            })
//        }


        var firebaseAuth = FirebaseAuth.getInstance()
        if (firebaseAuth.currentUser != null) {
            finish()
            val intent = Intent(this, ListOnline::class.java)
            startActivity(intent)
        }
        registrationButton.setOnClickListener(View.OnClickListener {
            Log.i(TAG, "registrationButton listener: registration button Action")
            val intent = Intent(this@MainActivity, RegistrationActivity::class.java)
            startActivity(intent)
        })

        loginButton.setOnClickListener(View.OnClickListener {
            Log.i(TAG, "loginButton listener: login button Action")
            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            startActivity(intent)
        })


    }


    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSION_REQUEST_CODE -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Log.i(TAG,"wszystko ok zaakceptowane permissions")
                    var firebaseAuth = FirebaseAuth.getInstance()
                    if (firebaseAuth.currentUser != null) {
                        finish()
                        val intent = Intent(this, ListOnline::class.java)
                        startActivity(intent)
                    }
                    registrationButton.setOnClickListener(View.OnClickListener {
                        Log.i(TAG, "registrationButton listener: registration button Action")
                        val intent = Intent(this@MainActivity, RegistrationActivity::class.java)
                        startActivity(intent)
                    })

                    loginButton.setOnClickListener(View.OnClickListener {
                        Log.i(TAG, "loginButton listener: login button Action")
                        val intent = Intent(this@MainActivity, LoginActivity::class.java)
                        startActivity(intent)
                    })


                } else {
                    Log.i(TAG,"permission denied, boo!")
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                }
                return
            }

        // Add other 'when' lines to check for other
        // permissions this app might request.
            else -> {
                Log.i(TAG,"Ignore all request")
                // Ignore all other requests.
            }
        }
    }
}
