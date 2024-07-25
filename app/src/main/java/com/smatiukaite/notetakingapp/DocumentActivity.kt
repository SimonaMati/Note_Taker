package com.smatiukaite.notetakingapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.smatiukaite.notetakingapp.JsonArrayRequestSim.JsonArrayRequestSim
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.nio.charset.Charset
import java.util.*


class DocumentActivity : AppCompatActivity() {
    private val url = "http://192.168.1.15:12345"
    private val saveHandler = Handler()
    private var documentTitle: EditText? = null
    private var documentText: EditText? = null
    private var lastSavedTitle = ""
    private var lastSavedText = ""
    private var page = "docActivity"
    private var docId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_document)

        val view = View(this)
        val token = intent?.getStringExtra("token")!!
        Log.d("TOKEN IN: ", token)

        documentTitle = findViewById(R.id.editTextTitle)
        documentText = findViewById(R.id.editTextMultiLine)

        val searchedDocTitle = intent.getStringExtra("title")
        val searchedDocText = intent.getStringExtra("text")
        if (!searchedDocText.isNullOrEmpty() || !searchedDocTitle.isNullOrEmpty()) {
            documentTitle?.setText(searchedDocTitle)
            documentText?.setText(searchedDocText)
        }

        //If a user searched for a document in another Activity, then pass the name of Activity and ID
        val pageA = intent.getStringExtra("page")

        if (pageA != null) {
            docId = intent.getStringExtra("document_id")!!

            //Show who can access a document that was selected
            showWhoCanAccessDocument(docId, token)

            Log.d("ID in Document ", docId)
        } else if (documentTitle?.text?.length == 0 && documentText?.text?.length == 0) {
        } else {
            //Create or edit a document
            docId = createOrEditDocument(token)!!
            Log.d("ID in Document ", docId)
        }

        //Save a document by clicking on a 'SAVE' button
        val saveDocumentButton = findViewById<ImageButton>(R.id.saveDocumentButton)
        saveDocumentButton.setOnClickListener {
            saveDocument(docId)
        }

//        //Auto save every minute if the user has typed something
//        saveHandler.postDelayed(object : Runnable {
//            override fun run() {
//                if (documentTitle?.text.toString() != lastSavedTitle || documentText?.text.toString() != lastSavedText
//                    && (documentTitle?.text.isNullOrEmpty() && documentText?.text.isNullOrEmpty() && docId.equals(""))) {
//                    saveDocument(docId)
//                }
//                saveHandler.postDelayed(this, 60000) //Execute this runnable again in 60 seconds
//            }
//        }, 60000)

        //Delete Document
        val deleteDocument = findViewById<ImageButton>(R.id.deleteDocumentButton)
        deleteDocument.setOnClickListener {
            deleteDocument(token, docId)
        }

        //Share Document
        val shareDocumentButton = findViewById<ImageButton>(R.id.shareDocumentButton)
        shareDocumentButton.setOnClickListener {
            goToShareDocumentActivity(view, token, docId)
        }

        //Open Document
        val openDocumentList = findViewById<ImageButton>(R.id.openFileButton)
        openDocumentList.setOnClickListener {
            goToDocumentListActivity(token)
        }

        //Reset Password
        val resetPasswordButton = findViewById<ImageButton>(R.id.reset_password_button)
        resetPasswordButton.setOnClickListener {
            goToResetPasswordActivity(view, token)
        }

        //Go to Settings
        val settingsButton = findViewById<ImageButton>(R.id.settingsButton)
        settingsButton.setOnClickListener {
            runOnUiThread { goToSettings(token) }
        }

        // Show who can access document

    }

    //Create or edit document
    fun createOrEditDocument(token: String): String? {
        // Check if either title or text is empty
        if (documentTitle?.text.isNullOrEmpty() && documentText?.text.isNullOrEmpty()) {
            return null
        }

        //Time
        val time = System.currentTimeMillis().toString()
        var id = UUID.randomUUID().toString()
        val jsonBody = JSONObject()
        val documentBody = JSONObject()

        jsonBody.put("method", "setDocument")
        documentBody.put("title", documentTitle?.text.toString())
        documentBody.put("text", documentText?.text.toString())
        documentBody.put("id", id)
        documentBody.put("creation_date", time)
        jsonBody.put("document", documentBody)

        val jsonString = jsonBody.toString()

        //Save to local cache (db.data)
        saveToDbFile(jsonString)

        //Save to server
        saveToServer(jsonString, token, docId)

        //Set last saved values
        lastSavedTitle = documentTitle?.text.toString()
        lastSavedText = documentText?.text.toString()

        return id
    }

    private fun saveDocument(id: String) {
        val token = intent.getStringExtra("token")!!

// Check if either title or text is empty
        if (documentTitle?.text.isNullOrEmpty() && documentText?.text.isNullOrEmpty() && docId.equals(
                ""
            )
        ) {
            Toast.makeText(this, "Document WAS NOT saved!", Toast.LENGTH_SHORT).show()
            return
        } else if (documentTitle?.text.isNullOrEmpty() && documentText?.text.isNullOrEmpty()) {
            Toast.makeText(this, "Document WAS NOT saved", Toast.LENGTH_SHORT).show()
            return
        } else {
            createOrEditDocument(token)
            Toast.makeText(this, "Document saved", Toast.LENGTH_SHORT).show()
        }
    }

//    private fun saveDocument(id: String) {
//        val token = intent.getStringExtra("token")!!
//
//        // Check if either title or text is empty
//        if (documentTitle?.text.isNullOrEmpty() && documentText?.text.isNullOrEmpty()) {
//            return
//        } else {
//            createOrEditDocument(token)
//            Toast.makeText(this, "Document saved", Toast.LENGTH_SHORT).show()
//        }
//    }

    fun saveToServer(jsonString: String, token: String, id: String) {
        val jsonBody = JSONObject(jsonString)

        // Check if either title or text is empty
        if (documentTitle?.text.isNullOrEmpty() && documentText?.text.isNullOrEmpty()) {
            return
        } else {
            val jsonPostRequest = object : JsonObjectRequest(Request.Method.POST, url, jsonBody,
                { response ->
                    // handle successful response here
                },
                { error ->
                    // handle error response here
                }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers: MutableMap<String, String> = HashMap()
                    headers["AUTHO_TOKEN"] = token
                    return headers
                }

                override fun getBodyContentType(): String {
                    return "application/json"
                }

                override fun getParams(): MutableMap<String, String> {
                    val params = HashMap<String, String>()
                    params["id"] = id
                    return params
                }
            }

            //Add the request to the Volley request queue
            Volley.newRequestQueue(applicationContext).add(jsonPostRequest)
        }
    }

    private fun saveToDbFile(jsonString: String) {
        val file = File(filesDir, "db.data")
        val fileOutputStream = FileOutputStream(file, true)
        val outputStreamWriter = OutputStreamWriter(fileOutputStream, Charset.forName("UTF-8"))
        outputStreamWriter.write(jsonString)
        outputStreamWriter.write("\n")
        outputStreamWriter.flush()
        outputStreamWriter.close()
    }

    //Go to Document List Activity
    private fun goToDocumentListActivity(token: String) {
        val email = intent.getStringExtra("email").toString()
        val intent = Intent(this, DocumentListActivity::class.java).apply {
            putExtra("email", email)
            Log.d("EMAIL ", email)
            putExtra("token", token)
        }
        startActivity(intent)
    }

    //Go to Settings Activity
    fun goToSettings(token: String) {
        val intent = Intent(this, SettingsActivity::class.java).apply {
            putExtra("email", intent.getStringExtra("email"))
            putExtra("token", token)
        }
        startActivity(intent)
    }

    fun goToShareDocumentActivity(view: View, token: String, id: String) {
        val intent = Intent(this, ShareDocumentActivity::class.java).apply {
            putExtra("token", token)
            putExtra("id", id)
        }
        startActivity(intent)
    }

    fun goToResetPasswordActivity(view: View, token: String) {
        val intent = Intent(this, ResetPasswordDOCActivity::class.java).apply {
            putExtra("token", token)
        }
        startActivity(intent)
    }

    fun deleteDocument(token: String, id: String) {
        val file = File(filesDir, "db.data")
        if (!file.exists()) {
            return
        }

        val jsonBody = JSONObject()
        jsonBody.put("method", "deleteDocument")
        jsonBody.put("document_id", id)
        jsonBody.put("token", token);

        val jsonPostRequest = object : JsonObjectRequest(Method.POST, url, jsonBody,
            { response ->

                try {
                    Log.d("ID NUMBER IN DELETE ", id)
                    // Read the contents of the file into a JSON array
                    val jsonString = file.readText()
                    val jsonArray = JSONArray(jsonString)

                    // Remove the document with the specified ID
                    for (i in 0 until jsonArray.length()) {
                        val document = jsonArray.getJSONObject(i)
                        if (document.getString("id") == id) {
                            jsonArray.remove(i)
                            break
                        }
                    }

                    // Write the updated JSON array back to the file
                    file.writeText(jsonArray.toString())

                    // Delete the file if it's empty
                    if (jsonArray.length() == 0) {
                        file.delete()
                    }
                } catch (e: JSONException) {
                    Log.e("JSON Error", "Error parsing JSON", e)
                } catch (e: FileNotFoundException) {
                    Log.e("File Error", "File not found", e)
                } catch (e: IOException) {
                    Log.e("IO Error", "Error reading/writing file", e)
                }

                Toast.makeText(this, "Document deleted", Toast.LENGTH_SHORT).show()

                //Update the title and text with an empty values
                documentTitle?.setText("")
                documentText?.setText("")
                docId = ""
            },
            { error ->
                // handle error response here
                val errorMessage = error.message
                Toast.makeText(this, "Document CANNOT be deleted", Toast.LENGTH_SHORT).show()
                if (errorMessage != null) {
                    Log.e("Volley Error", errorMessage)
                }
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers: MutableMap<String, String> = HashMap()
                headers["AUTHO_TOKEN"] = token
                return headers
            }

            override fun getBodyContentType(): String {
                return "application/json"
            }
        }

        // add the request to the Volley request queue
        Volley.newRequestQueue(applicationContext).add(jsonPostRequest)
    }

    fun showWhoCanAccessDocument(id: String, token: String) {
        val jsonBody = JSONObject()
        jsonBody.put("method", "getDocumentAccessors")
        jsonBody.put("document_id", id)
        jsonBody.put("token", token);

        val jsonPostRequest = object : JsonArrayRequestSim(Method.POST, url, jsonBody,
            { response ->
                val emails = mutableListOf<String>()
                for (i in 0 until response.length()) {
                    val email = response.getString(i)
                    emails.add(email)
                }

                val message = "Users who can access the document: " + emails.joinToString(", ")
                val toast = Toast.makeText(this, message, Toast.LENGTH_LONG)
                Log.d("TOAST: ", message)
                toast.setGravity(Gravity.CENTER_HORIZONTAL or Gravity.CENTER_VERTICAL, 0, 0)
                toast.show()
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

}