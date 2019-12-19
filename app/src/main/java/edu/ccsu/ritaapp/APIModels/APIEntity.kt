package edu.ccsu.ritaapp.APIModels

import com.google.android.gms.maps.model.LatLng
import org.json.JSONObject
import java.time.ZonedDateTime
import java.time.ZonedDateTime.parse

class APIEntity(jObject: JSONObject) {
    val type: TYPE? = TYPE.valueOf(jObject.getString("type"))
    val service: SERVICE? = SERVICE.valueOf(jObject.getString("service"))
    val id: String = jObject.getString("id")
    val imageUrl: String? = jObject.getString("imageUrl")
    val latLng: LatLng = LatLng(jObject.getDouble("latitude"), jObject.getDouble("longitude"))
    val address: String? = jObject.getString("address")
    val title: String? = jObject.getString("title")
    val description: String? = jObject.getString("description")
    val duration: Long = jObject.getLong("duration") // in seconds
    val startTime: ZonedDateTime? = try{parse(jObject.getString("startTime"))} catch (error: Exception) {null}
    val endTime: ZonedDateTime? = try{parse(jObject.getString("endTime"))} catch (error: Exception) {null}
    //val categories: Array<String>? = jObject.getJSONArray("categories") as Array<String>

    override fun toString(): String {
        return "Activity{" +
                "type=" + type +
                //", service=" + service +
                ", id='" + id + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", latitude=" + latLng.latitude +
                ", longitude=" + latLng.longitude +
                ", address='" + address + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", duration=" + duration +
                ", startTime=" + startTime +
                //", endTime=" + endTime +
                '}'
    }

    enum class TYPE {
        EVENT, PLACE
    }
    enum class SERVICE {
        HERE, TICKETMASTER, PREDICTHQ, ROUTE
    }

    override fun equals(other: Any?): Boolean {
        return this.id == (other as APIEntity).id
    }
}