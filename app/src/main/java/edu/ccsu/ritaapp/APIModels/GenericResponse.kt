package edu.ccsu.ritaapp.APIModels

import org.json.JSONObject

class GenericResponse<T>(jObject: JSONObject) {

    val obj = jObject
    var body: T? = null
    val status = jObject.getInt("status")
    val message: String = jObject.getString("message")

    override fun toString(): String {
        try{
            @Suppress("UNCHECKED_CAST")
            body = obj.get("body") as T
        } catch(e: Exception){
        }
        return "GenericResponse{" +
                "body='" + body + '\'' +
                "status='"+ status + '\'' +
                "message='"+ message + '\'' +
                '}'
    }
}