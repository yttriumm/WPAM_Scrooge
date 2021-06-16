package com.wpam.scrooge

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val emailText: EditText = findViewById(R.id.loginEditTextEmail)
        val passwordText: EditText = findViewById(R.id.loginEditTextPassword)
        val loginButton: Button = findViewById(R.id.loginButton)
        val registerLink: TextView = findViewById(R.id.registerLinkText)

        loginButton.setOnClickListener {
            when {
                TextUtils.isEmpty(emailText.text.toString().trim { it <= ' ' }) -> {
                    Toast.makeText(
                        this@LoginActivity,
                        "Podaj adres e-mail.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                TextUtils.isEmpty(passwordText.text.toString().trim { it <= ' ' }) -> {
                    Toast.makeText(
                        this@LoginActivity,
                        "Podaj hasło.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                else -> {
                    val email: String = emailText.text.toString().trim({ it <= ' ' })
                    val password: String = passwordText.text.toString().trim({ it <= ' ' })
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(
                                    this@LoginActivity,
                                    "Zalogowano.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                val firebaseUser: FirebaseUser = task.result!!.user!!
                                val intent: Intent =
                                    Intent(this@LoginActivity, DashboardActivity::class.java)
                                val uid = firebaseUser.uid.toString()
                                intent.putExtra("uid", firebaseUser.uid.toString())
                                val db = FirebaseFirestore.getInstance()

                                startActivity(intent)

                            }
                        }
                }
            }
        }
            registerLink.setOnClickListener {
                val goToRegisterActivityIntent: Intent = Intent(this, RegisterActivity::class.java)
                startActivity(goToRegisterActivityIntent)
            }
        }
    }
