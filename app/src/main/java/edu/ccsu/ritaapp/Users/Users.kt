package edu.ccsu.ritaapp.Users

import org.json.JSONObject

class Users(jObject: JSONObject) {
    val id: String = jObject.getString("id")
    val username:String = jObject.getString("username")
    val password:Long = jObject.getLong("password")



    override fun equals(other: Any?): Boolean {
        return this.id == (other as Users).id
    }

    override fun toString(): String {
        return "Users(id='$id', username='$username')"
    }

}