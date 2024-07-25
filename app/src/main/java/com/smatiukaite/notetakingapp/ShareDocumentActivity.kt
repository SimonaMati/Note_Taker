package com.smatiukaite.notetakingapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONObject
import org.w3c.dom.Text

class ShareDocumentActivity : AppCompatActivity() {
    var url = "http://192.168.1.15:12345"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share_document)
        val view = View(this)

        // Share Document after clicking on a button
        val shareButton = findViewById<Button>(R.id.shareButton)
        shareButton.setOnClickListener {
            goToDocumentActivity(view)
        }
    }

    fun goToDocumentActivity(view: View) {
        val email = findViewById<EditText>(R.id.editTextEmail).text.toString()
        val id = findViewById<EditText>(R.id.editTextID)
        id.setTextIsSelectable(true)
        val token = intent.getStringExtra("token")!!

        val jsonBody = JSONObject()
        val accessorsArray = JSONArray()

        jsonBody.put("method", "setDocumentAccessors")
        jsonBody.put("document_id", id.text.toString())
        accessorsArray.put(email)
        jsonBody.put("accessors", accessorsArray)

        val jsonPostRequest = object : JsonObjectRequest(
            Request.Method.POST, url, jsonBody,
            { response ->
                val intent = Intent(this, DocumentActivity::class.java).apply {
                    putExtra("token", token)
                }
                Toast.makeText(this, "Document was shared", Toast.LENGTH_SHORT).show()
                startActivity(intent)
            },
            { error ->
                // handle error response here
                val errorMessage = error.message
                Toast.makeText(this, "Document CANNOT BE shared", Toast.LENGTH_SHORT).show()
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

}