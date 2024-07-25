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
import kotlin.random.Random

class NewUsersActivity : AppCompatActivity() {
    var url = "http://192.168.1.15:12345"
    var page = "newUser"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_users)

        //Create button listener
        val createButton = findViewById<Button>(R.id.button_create_user)
        createButton.setOnClickListener {
            runOnUiThread{sendUserDataToServer()}
        }
    }

    fun sendUserDataToServer() {
        val email = findViewById<EditText>(R.id.editText_email)
        val firstName = findViewById<EditText>(R.id.editText_first_name)
        val lastName = findViewById<EditText>(R.id.editText_last_name)

        val jsonBody = JSONObject()
        jsonBody.put("method", "createAccount")
        jsonBody.put("last_name", lastName.text.toString())
        jsonBody.put("first_name", firstName.text.toString())
        jsonBody.put("email", email.text.toString())

        val jsonPostRequest = JsonObjectRequest(Request.Method.POST, url, jsonBody,
            { response ->
                val intent = Intent(this, CreatePasswordActivity::class.java).apply{
                    putExtra("email",email.text.toString())
                    putExtra("page", page)
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