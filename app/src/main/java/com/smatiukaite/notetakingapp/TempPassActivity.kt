package com.smatiukaite.notetakingapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class TempPassActivity : AppCompatActivity() {
    var url = "http://192.168.1.15:12345"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_temp_pass)
        val view = View(this)

        //Finish button listener
        val finishButton = findViewById<Button>(R.id.button_submit)
        finishButton.setOnClickListener {
            runOnUiThread{sendTempPasswordToServer()}
        }
    }

    fun sendTempPasswordToServer(){
        val warning = findViewById<TextView>(R.id.warning_message)
        val tempPassword = findViewById<EditText>(R.id.editText_TempPassword)

        val jsonBody = JSONObject()
        jsonBody.put("Temporary Password", tempPassword.text.toString())

        val jsonPostRequest = JsonObjectRequest(Request.Method.POST, url, jsonBody,
            { response ->
                // retrieve the temporary password from the server response
                val serverTempPassword = response.getString("temporary_password")

                // compare the temporary password entered by the user with the one on the server
                if (tempPassword.text.toString() == serverTempPassword) {
                    val intent = Intent(this@TempPassActivity, CreatePasswordActivity::class.java).apply {
                        intent.putExtra("Temporary Password", tempPassword.text.toString())
                    }
                    startActivity(intent)
                }else{
                    warning.isVisible = true
                }
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