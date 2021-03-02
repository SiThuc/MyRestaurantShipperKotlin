
package com.example.myrestaurantshipperv2kotlin

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.example.myrestaurantshipperv2kotlin.common.Common
import com.example.myrestaurantshipperv2kotlin.databinding.ActivityMainBinding
import com.example.myrestaurantshipperv2kotlin.databinding.LayoutRegisterBinding
import com.example.myrestaurantshipperv2kotlin.model.ShipperUserModel
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import dmax.dialog.SpotsDialog
import io.paperdb.Paper

class MainActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var listener: FirebaseAuth.AuthStateListener
    private lateinit var dialog: AlertDialog
    private lateinit var serverRef: DatabaseReference
    private lateinit var binding: ActivityMainBinding

    companion object{
        const val APP_REQUEST_CODE = 2018
    }

    override fun onStart() {
        super.onStart()
        firebaseAuth.addAuthStateListener(listener)
    }

    override fun onStop() {
        firebaseAuth.removeAuthStateListener(listener)
        super.onStop()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()

        //Delete data
        Paper.init(this)
        Paper.book().delete(Common.TRIP_START)
        Paper.book().delete(Common.SHIPPING_DATA)
    }

    private fun init() {
        firebaseAuth = FirebaseAuth.getInstance()
        serverRef = FirebaseDatabase.getInstance().getReference(Common.SHIPPER_REF)
        dialog = SpotsDialog.Builder().setContext(this).setCancelable(false).build()

        listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if(user != null){
                //If user is already logged
                Toast.makeText(this, "Already Login", Toast.LENGTH_SHORT).show()
                checkUserFromFirebase(user)
            }else{
                //If user is not logged
                phoneLogin()
            }
        }
    }

    private fun phoneLogin() {
        val providers = arrayListOf(AuthUI.IdpConfig.PhoneBuilder().build())
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                APP_REQUEST_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == APP_REQUEST_CODE) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                val user = FirebaseAuth.getInstance().currentUser
            } else {
                Toast.makeText(this, "Failed to sign in", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkUserFromFirebase(shipper: FirebaseUser) {
        dialog.show()
        serverRef.child(shipper.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val shipperUser = snapshot.getValue(ShipperUserModel::class.java)
                            if (shipperUser!!.isActive)
                                goToHomeActivity(shipperUser)
                            else {
                                dialog.dismiss()
                                Toast.makeText(this@MainActivity, "You must be allowed from Admin to access this app", Toast.LENGTH_SHORT).show()
                            }

                        } else{
                            dialog.dismiss()
                            showRegisterDialog(shipper)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@MainActivity, error.message, Toast.LENGTH_SHORT).show()
                    }
                })
        dialog.dismiss()
    }

    private fun showRegisterDialog(user: FirebaseUser) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Register User")
        builder.setMessage("Please fill information for registering")

        val dialogBinding = LayoutRegisterBinding.inflate(layoutInflater)
        dialogBinding.edtPhone.setText(user.phoneNumber.toString())

        builder.setView(dialogBinding.root)
        builder.setNegativeButton("CANCEL"){dialogInterface,i -> dialogInterface.dismiss()}
                .setPositiveButton("REGISTER"){dialogInterface,i ->
                    if (TextUtils.isDigitsOnly(dialogBinding.edtName.text.toString())) {
                        Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }

                    val shipperUserModel = ShipperUserModel()
                    shipperUserModel.uid = user.uid
                    shipperUserModel.name = dialogBinding.edtName.text.toString()
                    shipperUserModel.phone = dialogBinding.edtPhone.text.toString()
                    shipperUserModel.isActive = false //By default, we set isActive is false

                    dialog.show()

                    serverRef.child(shipperUserModel.uid!!)
                            .setValue(shipperUserModel)
                            .addOnFailureListener(object : OnFailureListener {
                                override fun onFailure(p0: Exception) {
                                    dialog.dismiss()
                                    Toast.makeText(this@MainActivity, p0.message.toString(), Toast.LENGTH_SHORT).show()
                                }
                            }).addOnCompleteListener{task ->
                                dialog.dismiss()
                                Toast.makeText(this, "Congratulation! Register success! Admin wil check and active you soon", Toast.LENGTH_SHORT).show()
                                goToHomeActivity(shipperUserModel)
                            }
                }

        val registerDialog = builder.create()
        registerDialog.show()

    }

    private fun goToHomeActivity(shipper: ShipperUserModel?) {
        dialog.dismiss()
        Common.currentShipper = shipper
        startActivity(Intent(this@MainActivity, HomeActivity::class.java))
        finish()
    }
}