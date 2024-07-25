package com.smatiukaite.notetakingapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class ResetPasswordDOCActivity : AppCompatActivity() {
    var url = "http://192.168.1.15:12345"
    val page = "DOC"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password_doc)

        val token = intent.getStringExtra("token")!!
        Log.d("TOKEN at RESET DOC ", token)

        val resetPasswordButton = findViewById<Button>(R.id.buttonResetPassword)
        resetPasswordButton.setOnClickListener {
            sendEmailForPasswordRevoveryToServer(token)
        }
    }

    fun sendEmailForPasswordRevoveryToServer(token: String) {
        val email = findViewById<EditText>(R.id.editTextEmail).text.toString()

        val jsonBody = JSONObject()
        jsonBody.put("method", "forgotPassword")
        jsonBody.put("email", email);

        val jsonPostRequest = JsonObjectRequest(Request.Method.POST, url, jsonBody,
            { response ->
                //Navigate to Create Password Activity page
                val intent = Intent(this, CreatePasswordActivity::class.java).apply {
                    putExtra("email", email)
                    System.out.println("EMAIL " + email)

                    putExtra("token", token)

                    putExtra("page", page)
                    System.out.println("PAGE " + page)
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