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
import java.sql.SQLOutput

class CreatePasswordActivity : AppCompatActivity() {
    var url = "http://192.168.1.15:12345"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_password)

        //Create button listener
        val finishButton = findViewById<Button>(R.id.finish_registration_button)
        finishButton.setOnClickListener {
            runOnUiThread { sendPasswordToServer() }
        }
    }

    fun sendPasswordToServer() {
        val tempPass = findViewById<EditText>(R.id.temp_password)
        val password = findViewById<EditText>(R.id.password)
        val email = intent.getStringExtra("email")

        System.out.println("EMAIL " + email)

        val jsonBody = JSONObject()
        jsonBody.put("method", "registerAccount")
        jsonBody.put("password", password.text.toString())
        jsonBody.put("temp_password", tempPass.text.toString())
        jsonBody.put("email", email);

        val jsonPostRequest = JsonObjectRequest(Request.Method.POST, url, jsonBody,
            { response ->
                //Navigate to home page
                val page = intent.getStringExtra("page")
                System.out.println("PAGE " + page)

                if (page.equals("DOC")) {
                    System.out.println("DOC")
                    val intent = Intent(this, DocumentActivity::class.java).apply {
                        val token = intent?.getStringExtra("token")!!
                        putExtra("token", token)
                        putExtra("email", email)
                    }

                    startActivity(intent)
                }

                if (page.equals("firstPage") || page.equals("newUser")) {
                    System.out.println("First Page")
                    val intent = Intent(this, MainActivity::class.java).apply {

                    }
                    startActivity(intent)
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