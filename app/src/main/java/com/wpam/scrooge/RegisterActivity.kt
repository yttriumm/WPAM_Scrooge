package com.wpam.scrooge

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        val registerButton: Button = findViewById(R.id.registerButton)
        val emailText: EditText = findViewById(R.id.registerEditTextEmail)
        val passwordText: EditText = findViewById(R.id.registerEditTextPassword)
        val nameText: EditText = findViewById(R.id.registerEditTextName)

        registerButton.setOnClickListener {
            when {
                TextUtils.isEmpty(emailText.text.toString().trim {it <= ' '}) -> {
                    Toast.makeText(
                        this@RegisterActivity,
                        "Podaj adres e-mail.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                TextUtils.isEmpty(passwordText.text.toString().trim {it <= ' '}) -> {
                    Toast.makeText(
                        this@RegisterActivity,
                        "Podaj hasło.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                TextUtils.isEmpty(nameText.text.toString()) -> {
                    Toast.makeText(
                        this@RegisterActivity,
                        "Podaj imię.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                else -> {
                    val email: String = emailText.text.toString().trim({it <= ' '})
                    val password: String = passwordText.text.toString().trim({it <= ' '})
                    val name: String = nameText.text.toString()

                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if(task.isSuccessful) {
                                val firebaseUser: FirebaseUser = task.result!!.user!!
                                val db = FirebaseFirestore.getInstance()
                                val user: MutableMap<String, Any> = HashMap()
                                val uid = firebaseUser.uid.toString()
                                user["name"] = name
                                user["uid"] = uid

                                try {
                                    db.collection("Users").document(uid).set(user)
                                }
                                catch(e: Exception) {
                                    Toast.makeText(this@RegisterActivity,
                                        e.message.toString(),
                                        Toast.LENGTH_SHORT)
                                }

                                Toast.makeText(
                                    this@RegisterActivity,
                                    "Zarejestrowano.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                val goToDashboard: Intent =
                                    Intent(this@RegisterActivity, DashboardActivity::class.java)
                                goToDashboard.putExtra("uid", uid)
                                goToDashboard.putExtra("name", name)
                                startActivity(goToDashboard)
                                finish()
                            }
                            else {
                                    Toast.makeText(
                                        this@RegisterActivity,
                                        task.exception!!.message.toString(),
                                        Toast.LENGTH_SHORT
                                    ).show()

                                }
                            }
                        }



                }

                }
            }
        }


