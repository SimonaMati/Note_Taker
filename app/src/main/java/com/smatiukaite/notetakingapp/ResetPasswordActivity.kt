package com.smatiukaite.notetakingapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class ResetPasswordActivity : AppCompatActivity() {
    var url = "http://192.168.1.15:12345"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        val resetPasswordButton = findViewById<Button>(R.id.buttonResetPassword)
        resetPasswordButton.setOnClickListener {
            sendEmailForPasswordRevoveryToServer()
        }
    }

    fun sendEmailForPasswordRevoveryToServer() {
        val email = findViewById<EditText>(R.id.editTextEmail).text.toString()

        val jsonBody = JSONObject()
        jsonBody.put("method", "forgotPassword")
        jsonBody.put("email", email);

        val jsonPostRequest = JsonObjectRequest(Request.Method.POST, url, jsonBody,
            { response ->
                val page = "firstPage"
                //Navigate to Create Password Activity
                val intent = Intent(this, CreatePasswordActivity::class.java).apply {
                    putExtra("page", page)
                    putExtra("email", email)
                }
                startActivity(intent)
            },
            { error ->
                // handle error response here
                val errorMessage = error.message
                if (errorMessage != null) {
                    Log.e("Volley Error", errorMessage)
                }
            })

        // add the request to the Volley request queue
        Volley.newRequestQueue(applicationContext).add(jsonPostRequest)
    }
}