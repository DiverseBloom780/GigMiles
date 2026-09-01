package com.gigmiles.app.navigation

import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class OpenNavigationClient(private val http: OkHttpClient = OkHttpClient()) {
    suspend fun geocode(address: String): NavigationDestination? = withContext(Dispatchers.IO) {
        val url = Uri.parse("https://nominatim.openstreetmap.org/search").buildUpon()
            .appendQueryParameter("format", "jsonv2")
            .appendQueryParameter("limit", "1")
            .appendQueryParameter("q", address)
            .build()
        val request = Request.Builder().url(url.toString())
            .header("User-Agent", "GigMiles/0.1 (open-source delivery mileage app)").build()
        http.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return@withContext null
            val item = org.json.JSONArray(response.body?.string().orEmpty()).optJSONObject(0) ?: return@withContext null
            NavigationDestination(address, item.optDouble("lat"), item.optDouble("lon"))
        }
    }

    suspend fun route(originLat: Double, originLon: Double, destination: NavigationDestination): List<NavigationInstruction> = withContext(Dispatchers.IO) {
        val body = JSONObject().apply {
            put("locations", org.json.JSONArray().apply {
                put(JSONObject().put("lat", originLat).put("lon", originLon))
                put(JSONObject().put("lat", destination.latitude).put("lon", destination.longitude))
            })
            put("costing", "auto")
            put("units", "miles")
            put("directions_options", JSONObject().put("units", "miles"))
        }.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder().url("https://valhalla1.openstreetmap.de/route").post(body).build()
        http.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return@withContext emptyList()
            val maneuvers = JSONObject(response.body?.string().orEmpty())
                .optJSONArray("trip")?.optJSONObject(0)?.optJSONArray("legs")
                ?.optJSONObject(0)?.optJSONArray("maneuvers") ?: return@withContext emptyList()
            List(maneuvers.length()) { index ->
                val maneuver = maneuvers.getJSONObject(index)
                NavigationInstruction(
                    maneuver.optString("instruction", "Continue"),
                    maneuver.optDouble("length", 0.0),
                    maneuver.optDouble("begin_lat", 0.0),
                    maneuver.optDouble("begin_lon", 0.0)
                )
            }
        }
    }
}
