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

class PasswordActivity : AppCompatActivity() {
    var url = "http://192.168.1.15:12345"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password)

        //Create button listener
        val setPasswordButton = findViewById<Button>(R.id.button_set_pass)
        setPasswordButton.setOnClickListener {
            runOnUiThread{sendPasswordToServer()}
        }
    }

    fun sendPasswordToServer() {
        val email = findViewById<EditText>(R.id.editText_email)
        val tempPass = findViewById<EditText>(R.id.editTextTempPassword)
        val password = findViewById<EditText>(R.id.editTextNewPassword)

        val jsonBody = JSONObject()
        jsonBody.put("method", "registerAccount")
        jsonBody.put("email", email.text.toString());
        jsonBody.put("temp_password", tempPass.text.toString())
        jsonBody.put("password", password.text.toString())

        val jsonPostRequest = JsonObjectRequest(Request.Method.POST, url, jsonBody,
            { response ->
                //Navigate to home page
                val intent = Intent(this, DocumentActivity::class.java)
                intent.putExtra("email", email.text.toString())
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