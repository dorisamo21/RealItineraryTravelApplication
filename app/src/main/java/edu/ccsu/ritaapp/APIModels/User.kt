package edu.ccsu.ritaapp.APIModels

import org.json.JSONArray
import org.json.JSONObject

class User(jObject: JSONObject){
    val email: String = jObject.getString("email")
    val username: String = jObject.getString("username")
    val preferences: String = jObject.getString("preferences")
    val friends : String? = jObject.getString("friends")


    override fun toString(): String {
        return "User{" +
                "email='" + email + '\'' +
                "username='"+ username + '\'' +
                "friends='"+ friends + '\'' +
                "preferences='"+ preferences + '\''+
                '}'
    }

    override fun equals(other: Any?): Boolean {
        return this.email == (other as User).email
    }
}