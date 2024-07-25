package com.smatiukaite.notetakingapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class SettingsActivity : AppCompatActivity() {
    private var requestQueue: RequestQueue? = null
    var url = "http://192.168.1.15:12345"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val token = intent.getStringExtra("token")!!
        Log.wtf("TOKEN 2 ", token)

        //Save changes
        val saveChangesButton = findViewById<Button>(R.id.saveChangesButton)
        saveChangesButton.setOnClickListener {
            runOnUiThread{ saveChangesAndReturnToDocumentActivity(token) }
        }

        //Sign out
        val signOutButton = findViewById<Button>(R.id.signOutButton2)
        signOutButton.setOnClickListener {
            runOnUiThread { signOutAccount(token) }
        }

        //Delete Account
        val deleteAccountButton = findViewById<Button>(R.id.deleteAccountButton)
        deleteAccountButton.setOnClickListener {
            runOnUiThread { deleteAccount(token) }
        }

        requestQueue = Volley.newRequestQueue(applicationContext)
    }

    fun saveChangesAndReturnToDocumentActivity(token: String){
        val firstName = findViewById<EditText>(R.id.editTextFirstName).text.toString()
        val lastName = findViewById<EditText>(R.id.editTextLastName).text.toString()
        val additionalInfo = findViewById<EditText>(R.id.editTextAdditionalInfo).text.toString()

        val jsonBody = JSONObject()
        val accountBody = JSONObject()

        jsonBody.put("method", "setAccount")
        accountBody.put("first_name", firstName)
        accountBody.put("last_name", lastName)
        accountBody.put("extra", additionalInfo)
        jsonBody.put("account",accountBody)

        val jsonPostRequest = object:JsonObjectRequest(Request.Method.POST, url, jsonBody,
            { response ->
                val intent = Intent(this, DocumentActivity::class.java)
                startActivity(intent)

                //Clear values after successful request
                findViewById<EditText>(R.id.editTextFirstName).setText("")
                findViewById<EditText>(R.id.editTextLastName).setText("")
                findViewById<EditText>(R.id.editTextAdditionalInfo).setText("")
            },
            { error ->
                // handle error response here
                val errorMessage = error.message
                if (errorMessage != null) {
                    Log.e("Volley Error", errorMessage)
                }
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers: MutableMap<String, String> = HashMap()
                headers["AUTHO_TOKEN"] = token

                return headers
            }
        }

        // add the request to the Volley request queue
        Volley.newRequestQueue(applicationContext).add(jsonPostRequest)
    }

    fun signOutAccount(token:String) {
        val jsonBody = JSONObject()
        jsonBody.put("method", "signOut")

        val jsonPostRequest = object: JsonObjectRequest(Request.Method.POST, url, jsonBody,
            { response ->
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            },
            { error ->
                // handle error response here
                val errorMessage = error.message
                if (errorMessage != null) {
                    Log.e("Volley Error", errorMessage)
                }
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers: MutableMap<String, String> = HashMap()
                headers["AUTHO_TOKEN"] = token
                return headers
            }
        }

        // add the request to the Volley request queue
        Volley.newRequestQueue(applicationContext).add(jsonPostRequest)
    }

    fun deleteAccount(token:String) {
        val jsonBody = JSONObject()
        jsonBody.put("method", "deleteAccount")
        jsonBody.put("token", token);

        val jsonPostRequest = object : JsonObjectRequest(Request.Method.POST, url, jsonBody,
            { response ->
                val intent = Intent(this, MainActivity::class.java).apply {
                }
                startActivity(intent)
            },
            { error ->
                // handle error response here
                val errorMessage = error.message
                if (errorMessage != null) {
                    Log.e("Volley Error", errorMessage)
                }
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers: MutableMap<String, String> = HashMap()
                headers["AUTHO_TOKEN"] = token

                return headers
            }
        }

        // add the request to the Volley request queue
        Volley.newRequestQueue(applicationContext).add(jsonPostRequest)
    }

    override fun onDestroy() {
        super.onDestroy()
        // stop Volley request queue
        requestQueue?.stop()
    }

}