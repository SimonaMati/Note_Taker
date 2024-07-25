package com.smatiukaite.notetakingapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.get
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.smatiukaite.notetakingapp.JsonArrayRequestSim.JsonArrayRequestSim
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File

data class Document(val id: String, val title: String, val text: String) {
    override fun toString(): String {
        return title
    }
}

var page = "null"

class DocumentListActivity : AppCompatActivity() {
    private val url = "http://192.168.1.15:12345"
    private val documentList = mutableListOf<Document>()
    private lateinit var adapter: ArrayAdapter<Document>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_document_list)

        val token = intent.getStringExtra("token")!!
        val list = findViewById<ListView>(R.id.list_view)

        // Load documents from the data file
        val file = File(filesDir, "db.data")
        val fileContents = file.readText() // read file contents into a string variable

        // Retrieve the EditText view from the layout with id editTextDocumentName
        val docNameEditText = findViewById<EditText>(R.id.editTextDocumentName)

        // Search for a document
        val searchButton = findViewById<Button>(R.id.buttonSearchFile)
        searchButton.setOnClickListener {
            val docId = docNameEditText.text.toString()
            if (docId.isNotEmpty()) {
                searchForDocument(token, docId)
            } else {
                Toast.makeText(
                    this@DocumentListActivity,
                    "Please enter a document ID",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        //Parsing a string from JSON file, parse into objects
        try {
            val documents = JSONObject(fileContents)

            for (key in documents.keys()) {
                val document = documents.getJSONObject(key)
                val id = key
                // Skip documents without an ID
                if (id.isNotEmpty()) {
                    val title = document.getString("title")
                    val text = document.getString("text")
                    documentList.add(Document(id, title, text))
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        adapter = object : ArrayAdapter<Document>(
            this,
            R.layout.list_view_item,
            documentList
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = convertView ?: LayoutInflater.from(context).inflate(
                    R.layout.list_view_item,
                    parent,
                    false
                )
                val document = getItem(position)!!
                val titleTextView = view.findViewById<TextView>(R.id.list_view_item)
                titleTextView.text = document.title
                return view
            }
        }
        list.adapter = adapter

        //Access the array of types of documents
        val documentOptions = resources.getStringArray(R.array.document_options_array)

        //Access the spinner
        val spinner: Spinner = findViewById<Spinner>(R.id.spinner_documents)
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, documentOptions)
        spinner.adapter = spinnerAdapter

        spinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                showDocuments(position, token, list)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }

    }

    //SHOW A LIST OF DOCUMENTS
    private fun showDocuments(position: Int, token: String, list: ListView) {
        val jsonBody = JSONObject()

        jsonBody.put("method", "getDocuments")
        jsonBody.put("scope", position)

        val jsonPostRequest = object : JsonArrayRequestSim(Method.POST, url, jsonBody,
            { response ->
                try {
                    documentList.clear()
                    for (i in 0 until response.length()) {
                        val document = response.getJSONObject(i)
                        val id = document.getString("id")
                        val title = document.getString("title")
                        val text = document.getString("text")
                        documentList.add(Document(id, title, text))
                    }
                    adapter = ArrayAdapter<Document>(this, R.layout.list_view_item, documentList)
                    list.adapter = adapter
                    list.isVerticalScrollBarEnabled = true

                    page = "docList"
                    list.onItemClickListener = AdapterView.OnItemClickListener{
                            adapterView, view, i, l -> val document = documentList[i]

                        val intent = Intent(this, DocumentActivity::class.java).apply {
                            putExtra("document_id", document.id)
                            putExtra("title", document.title)
                            putExtra("text", document.text)
                            putExtra("token", token)
                            putExtra("page", page)
                        }
                        startActivity(intent)
                    }

                } catch (e: JSONException) {
                    Log.e("JSONException", e.toString())
                }
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

    //SEARCH A DOCUMENT
    private fun searchForDocument(token: String, documentId: String) {
        val jsonBody = JSONObject()
        Log.d("TOKEN IN SEARCH FOR", token)
        jsonBody.put("method", "getDocument")
        jsonBody.put("document_id", documentId)

        if (documentId != "") {
            val jsonPostRequest = object : JsonObjectRequest(Method.POST, url, jsonBody,
                { response ->
                    // handle successful response here
                    val id = response.getString("id")
                    val title = response.getString("title")
                    val text = response.getString("text")
                    val document = Document(id, title, text)
                    page = "docList"

                    // Start the DocumentActivity and pass the document information to it
                    val intent = Intent(this, DocumentActivity::class.java)
                    intent.putExtra("title", title)
                    intent.putExtra("text", text)
                    intent.putExtra("extra", "searchedDocument")
                    intent.putExtra("document_id", documentId)
                    intent.putExtra("token", token)
                    intent.putExtra("page", page)
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
        } else {
            Toast.makeText(this, "Document was not found", Toast.LENGTH_SHORT).show()
        }
    }
}