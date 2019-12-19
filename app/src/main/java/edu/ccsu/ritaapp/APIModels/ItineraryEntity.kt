package edu.ccsu.ritaapp.APIModels

import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception
import java.time.ZonedDateTime
import java.time.ZonedDateTime.parse

class ItineraryEntity(jObject : JSONObject){
    val startTime: ZonedDateTime? = try{parse(jObject.getString("startTime"))} catch (error: Exception) {null}
    val items: JSONArray = jObject.getJSONArray("items")
    val totalTravelTime: Int = jObject.getInt("totalTravelTime")
}