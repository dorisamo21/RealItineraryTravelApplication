package edu.ccsu.ritaapp.APIModels

import com.google.android.gms.maps.model.LatLng
import org.json.JSONException
import org.json.JSONObject
import java.time.ZonedDateTime

class ItemEntity(jObject: JSONObject){
    val title: String = jObject.getString("title")
    val startTime: ZonedDateTime? = try{
        ZonedDateTime.parse(jObject.getString("startTime"))
    } catch (error: Exception) {null}

    // setting default values to check if unable to get lat or long
    val latLng: LatLng? = try { LatLng(jObject.getJSONObject("activity").getDouble("latitude")
    ,jObject.getJSONObject("activity").getDouble("longitude"))
            } catch (ex: JSONException) {null}
    val duration: Long = jObject.getLong("duration")

    override fun toString(): String {
        return "title: " + title + " latLng: " + latLng
    }
}