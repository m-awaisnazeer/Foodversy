package com.communisolve.foodversy

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.communisolve.foodversy.common.Common
import com.communisolve.foodversy.databinding.ActivityMainBinding
import com.communisolve.foodversy.model.UserModel
import com.communisolve.foodversy.remote.ApiService
import com.communisolve.foodversy.remote.RetrofitCloudClient
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import dmax.dialog.SpotsDialog
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.*
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity() {

    companion object {
        val APP_REQUEST_CODE = 7171
    }

    private var compositDisposable:CompositeDisposable = CompositeDisposable()
    private lateinit var apiService:ApiService
    lateinit var binding: ActivityMainBinding

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var listner: FirebaseAuth.AuthStateListener
    lateinit var dialog: AlertDialog
    private var providers: List<AuthUI.IdpConfig>? = null


    private lateinit var userRef: DatabaseReference
    override fun onStart() {
        super.onStart()
        firebaseAuth.addAuthStateListener(listner)
    }

    override fun onStop() {
        if (listner != null) {
            firebaseAuth.removeAuthStateListener(listner)

        }
        super.onStop()

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        apiService = RetrofitCloudClient.getInstance().create(ApiService::class.java)
        init()
    }

    private fun init() {
        providers = Arrays.asList(
            AuthUI.IdpConfig.PhoneBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        userRef = FirebaseDatabase.getInstance().getReference(Common.USER_REFERENCE)
        firebaseAuth = FirebaseAuth.getInstance()
        dialog = SpotsDialog.Builder()
            .setContext(this).setCancelable(false).build()
        listner = FirebaseAuth.AuthStateListener { firebaseAuth ->
           Dexter.withActivity(this)
               .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
               .withListener(object : PermissionListener {
                   override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                       val user = firebaseAuth.currentUser
                       if (user != null) {
                           //Already logged
                           Toast.makeText(this@MainActivity, "logged in", Toast.LENGTH_SHORT).show()
                           //
                           checkUserFromFirebaseDatabase(user.uid)
                       } else {
                           phonelogIn()
                       }
                   }

                   override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                       Toast.makeText(
                           this@MainActivity,
                           "You must accept this permission to use app",
                           Toast.LENGTH_SHORT
                       ).show()
                   }

                   override fun onPermissionRationaleShouldBeShown(
                       p0: PermissionRequest?,
                       p1: PermissionToken?
                   ) {

                   }

               }).check()



        }
    }

    private fun phonelogIn() {
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setTheme(R.style.LogInTheme)
                .setLogo(R.drawable.foodversy_iconpng)
                //.setAuthMethodPickerLayout(authMethodPickerLayout)
                .setAvailableProviders(providers!!)
                .setIsSmartLockEnabled(false)
                .build(), APP_REQUEST_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == APP_REQUEST_CODE) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                val user = FirebaseAuth.getInstance().currentUser
            } else {
                Toast.makeText(this, "${response!!.error}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkUserFromFirebaseDatabase(uid: String) {
        dialog.show()
        userRef.child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {

                        FirebaseAuth.getInstance()
                            .currentUser!!.getIdToken(true)
                            .addOnCompleteListener { task->
                                Common.authorizeToken = task.result!!.token
                                val headers = HashMap<String,String>()
                                headers.put("Authorization",Common.buildToken(Common.authorizeToken))

                                compositDisposable.add(
                                    apiService.getToken(headers)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe({ braintree ->
                                            dialog.dismiss()
                                            /*
                                             val userModel = snapshot.getValue(UserModel::class.java)
                                gotoHomeActivity(userModel)
                                             */

                                        }, {
                                            Toast.makeText(
                                                this@MainActivity,
                                                "${it.message}",
                                                Toast.LENGTH_SHORT
                                            )
                                                .show()
                                        })
                                )

                            }.addOnFailureListener {

                            }
                        dialog.dismiss()
                        val userModel = snapshot.getValue(UserModel::class.java)
                        gotoHomeActivity(userModel,"")
                    } else {
                        dialog.dismiss()
                        showRegisterDialog(uid)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@MainActivity, "${error.message}", Toast.LENGTH_SHORT).show()
                }

            })

    }

    private fun showRegisterDialog(uid: String) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("REGISTER")
        builder.setMessage("Please fill Information")

        val itemView = LayoutInflater.from(this)
            .inflate(R.layout.layout_register, null)

        val edt_name = itemView.findViewById<EditText>(R.id.edt_name)
        val edt_address = itemView.findViewById<EditText>(R.id.edt_address)
        val edt_phone = itemView.findViewById<EditText>(R.id.edt_phone)

        edt_phone.setText(FirebaseAuth.getInstance().currentUser!!.phoneNumber)

        builder.setView(itemView)
        builder.setNegativeButton("CANCEL") { dialog, i ->
            dialog.dismiss()
        }
        builder.setPositiveButton("Register") { dialog, i ->
            if (TextUtils.isEmpty(edt_name.text.toString())) {
                Toast.makeText(this, "Please Enter your name", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            } else if (TextUtils.isEmpty(edt_address.text.toString())) {
                Toast.makeText(this, "Please Enter your name", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            val userModel = UserModel(
                uid, edt_name.text.toString(),
                edt_address.text.toString(), edt_phone.text.toString()
            )


            userRef.child(uid)
                .setValue(userModel)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        FirebaseAuth.getInstance().currentUser!!
                            .getIdToken(true)
                            .addOnCompleteListener {
                                Common.authorizeToken = it.result.token

                                val headers = HashMap<String,String>()
                                headers.put("Authorization",Common.buildToken(Common.authorizeToken))
                                compositDisposable.add(
                                    apiService.getToken(headers)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe({ braintree ->
                                            dialog.dismiss()
                                            /*
                                            dialog.dismiss()
                                Toast.makeText(this, "Registeration Success", Toast.LENGTH_SHORT).show()
                                gotoHomeActivity(userModel, "")
                                             */

                                        }, {
                                            Toast.makeText(
                                                this@MainActivity,
                                                "${it.message}",
                                                Toast.LENGTH_SHORT
                                            )
                                                .show()
                                        }))
                            }.addOnFailureListener {
                                Toast.makeText(this, "${it.message}", Toast.LENGTH_SHORT).show()
                            }
                        dialog.dismiss()
                        Toast.makeText(this, "Registeration Success", Toast.LENGTH_SHORT).show()
                        gotoHomeActivity(userModel, "")
                    } else {

                    }
                }
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun gotoHomeActivity(userModel: UserModel?, token: String) {
        Common.currentUser = userModel
        Common.currentToken = token
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}