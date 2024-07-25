package com.smatiukaite.notetakingapp

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import java.io.File

class HistoryFileModel constructor(context: Context) {
    val FILE_NAME = "history_file_model.json"
    val HISTORY_KEY = "history"
    private var history: ArrayList<String> = ArrayList()
    private val file: File = File(context.filesDir, FILE_NAME)
    private val gson: Gson = Gson()

    init {
        load()
    }

    fun save() {
        val map: HashMap<String, Any?> = HashMap()
        map[HISTORY_KEY] = history
        val jsonStr = gson.toJson(map)
        write(jsonStr)
    }

    fun deleteFile() {
        if (file.exists()) {
            history.clear()
        }
    }

    private fun load() {
        val jsonStr = read()

        val jsonObj = gson.fromJson<Map<*, *>>(
            jsonStr,
            MutableMap::class.java
        )

    val saveHistory = jsonObj[HISTORY_KEY] as List<String>?

    if(saveHistory != null)
    {
        history.clear()
        history.addAll(saveHistory)
    }
}

    fun getHistory(): ArrayList<String>? {
        return history
    }

    private fun createEmptyFile(){
        Log.d("history_file", "Creating empty file")
        val map: HashMap<String, Any> = HashMap()
        map[HISTORY_KEY] = ArrayList<String>()
        val jsonStr = gson.toJson(map)
        write(jsonStr)
    }

    private fun write(jsonStr:String){
        Log.d("history_file", "Writing to file")
        file.writeText(jsonStr)
    }

    private fun read():String{
        Log.d("history_file", "Reading to file")
        return if(file.isFile){
            file.readText()
        }else{
            Log.d("history_file", "File not found")
            createEmptyFile()
            read()
        }
    }
}