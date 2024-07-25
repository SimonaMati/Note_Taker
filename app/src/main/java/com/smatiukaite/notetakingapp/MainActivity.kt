package com.smatiukaite.notetakingapp

/**************************
Class: CSC244
Student: Simona Matiukaite
Project: Note Taking App
 ***************************/

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    var url = "http://192.168.1.15:12345"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val view = View(this)

        //Sign in button listener
        val signInButton = findViewById<Button>(R.id.button_signing)
        signInButton.setOnClickListener {
            runOnUiThread{signUserInToServer()}
        }

        //Forgot password listener
        val forgotPasswordButton = findViewById<Button>(R.id.button_forgot_password)
        forgotPasswordButton.setOnClickListener {
          goToForgotenPasswordActivity(view)
        }

        //New User listener
        val newUserButton = findViewById<Button>(R.id.button_new_user)
        newUserButton.setOnClickListener {
            goToNewUserActivity(view)
        }
    }

    fun signUserInToServer(){
        val email = findViewById<EditText>(R.id.editText_email)
        val password = findViewById<EditText>(R.id.editText_Password)

        val jsonBody = JSONObject()
        jsonBody.put("method", "authenticate")
        jsonBody.put("email", email.text.toString())
        jsonBody.put("password", password.text.toString())
        jsonBody.put("time_span", 1000000)
        jsonBody.put("time_unit", "SECONDS")

        val jsonPostRequest = JsonObjectRequest(Request.Method.POST, url, jsonBody,
            { response ->
                val token = response.getString("token")

                val intent = Intent(this, DocumentActivity::class.java)
                intent.putExtra("email",email.text.toString())
                intent.putExtra("token", token)
                startActivity(intent)
            },
            { error ->
                // handle error response here
                val errorMessage = error.message
                Toast.makeText(this, "Incorrect email or password", Toast.LENGTH_SHORT).show()
                if (errorMessage != null) {
                    Log.e("Volley Error", errorMessage)
                }
            })

        // add the request to the Volley request queue
        Volley.newRequestQueue(applicationContext).add(jsonPostRequest)
    }

    fun goToForgotenPasswordActivity(view: View){
        val intent = Intent(this, ResetPasswordActivity::class.java).apply {
        }
        startActivity(intent)
    }

    fun goToNewUserActivity(view: View){
        val intent = Intent(this, NewUsersActivity::class.java).apply {
        }
        startActivity(intent)
    }

}