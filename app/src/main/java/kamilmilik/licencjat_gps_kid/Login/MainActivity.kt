package kamilmilik.licencjat_gps_kid.Login

import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import kamilmilik.licencjat_gps_kid.ApplicationActivity
import kotlinx.android.synthetic.main.activity_main.*
import kamilmilik.licencjat_gps_kid.ListOnline
import kamilmilik.licencjat_gps_kid.R
import kamilmilik.licencjat_gps_kid.Utils.CheckValidDataInEditText


class MainActivity : ApplicationActivity() {
    private val TAG = MainActivity::class.java.simpleName
    private val MY_PERMISSION_REQUEST_CODE : Int = 7171

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        super.setContentView(R.layout.activity_main)
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
//        if (firebaseAuth.currentUser != null && firebaseAuth.currentUser!!.isEmailVerified) {
//            finish()
//            val intent = Intent(this, ListOnline::class.java)
//            startActivity(intent)
//        }
        //TODO to wyzej odkomentowac a to nizej zakomentowac, to nizej tylko do testow
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
//            val intent = Intent(this@MainActivity, LoginActivity::class.java)
//            startActivity(intent)
            var email = emailLoginEditText!!.text.toString()
            var password = passwordLoginEditText!!.text.toString()
            if(CheckValidDataInEditText(this).checkIfUserEnterValidData(email,password)) {
                    val progressDialog = ProgressDialog.show(this, "Please wait...", "Proccessing...", true)
                    firebaseAuth!!.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                progressDialog.dismiss()
                                if (task.isSuccessful) {
                                    //TODO do testow zakomentowane , normalnie odkomentowac tego ifa
//                                    if (firebaseAuth.currentUser!!.isEmailVerified) {
                                        Toast.makeText(this, "Login successful", Toast.LENGTH_LONG).show()

                                        var userDatabase = FirebaseDatabase.getInstance().reference.child("user_account_settings")
                                        var currentUserId = firebaseAuth!!.currentUser!!.uid
                                        var deviceTokenId = FirebaseInstanceId.getInstance().token
                                        userDatabase!!.child(currentUserId).child("device_token").setValue(deviceTokenId).addOnSuccessListener {
                                            val intent = Intent(this, ListOnline::class.java)
                                            intent.putExtra("Email", firebaseAuth!!.currentUser!!.email)
                                            intent.flags = (Intent.FLAG_ACTIVITY_NEW_TASK) or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                            startActivity(intent)
                                            finish()
                                        }
//                                    }
//                                    else{
//                                        Log.i(TAG, "onCreate() not verified")
//                                         Toast.makeText(this, "Email is not verified check your email box", Toast.LENGTH_LONG).show()
//                                    }
                                } else {
                                    Log.e("ERROR", task.exception!!.toString())
                                    Toast.makeText(this, task.exception!!.message, Toast.LENGTH_LONG).show()
                                }
                }
            }
        })

        resetPasswordButton.setOnClickListener(View.OnClickListener {
            startActivity(Intent(this, ChangePasswordActivity::class.java))
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
//                        val intent = Intent(this@MainActivity, LoginActivity::class.java)
//                        startActivity(intent)
                        Log.i(TAG,"btnUserLogin_Click: sign In with currentUserId and password")
                        var email = emailLoginEditText!!.text.toString()
                        var password = passwordLoginEditText!!.text.toString()
                        if(CheckValidDataInEditText(this).checkIfUserEnterValidData(email,password)){
                            val progressDialog = ProgressDialog.show(this, "Please wait...", "Proccessing...", true)
                            firebaseAuth!!.signInWithEmailAndPassword(email, password)
                                    .addOnCompleteListener { task ->
                                        progressDialog.dismiss()
                                        if (task.isSuccessful) {
                                            Toast.makeText(this, "Login successful", Toast.LENGTH_LONG).show()

                                            var userDatabase = FirebaseDatabase.getInstance().reference.child("user_account_settings")
                                            var currentUserId = firebaseAuth!!.currentUser!!.uid
                                            var deviceTokenId = FirebaseInstanceId.getInstance().token
                                            userDatabase!!.child(currentUserId).child("device_token").setValue(deviceTokenId).addOnSuccessListener {
                                                val intent = Intent(this, ListOnline::class.java)
                                                intent.putExtra("Email", firebaseAuth!!.currentUser!!.email)
                                                intent.flags = (Intent.FLAG_ACTIVITY_NEW_TASK ) or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                startActivity(intent)
                                                finish()
                                            }

                                        } else {
                                            Log.e("ERROR", task.exception!!.toString())
                                            Toast.makeText(this, task.exception!!.message, Toast.LENGTH_LONG).show()
                                        }
                                    }
                        }
                    })

                    resetPasswordButton.setOnClickListener(View.OnClickListener {
                        Toast.makeText(this, "Action for remember password", Toast.LENGTH_SHORT).show()
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
