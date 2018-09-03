package kamilmilik.gps_tracker.utils

import android.os.AsyncTask
import android.util.Log
import kamilmilik.gps_tracker.models.TrackingModel
import kamilmilik.gps_tracker.utils.listeners.OnDataAddedListener
import org.json.JSONObject
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * Created by kamil on 07.08.2018.
 */
object RestFirebaseUtils {
    private val TAG = RestFirebaseUtils::class.java.simpleName

    fun addLocationToFirebaseDatabaseByRest(trackingModel: TrackingModel, tokenId: String) {
        try {
            val url = URL("https://licencjat-kid-track.firebaseio.com/Locations/${trackingModel.user_id}.json?auth=" + tokenId)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "PUT"
            conn.setRequestProperty("Accept", "application/json")
            conn.doOutput = true
            conn.doInput = true

            val jsonParam = JSONObject()
            jsonParam.put("email", trackingModel.email)
            jsonParam.put("lat", trackingModel.lat)
            jsonParam.put("lng", trackingModel.lng)
            jsonParam.put("user_id", trackingModel.user_id)
            jsonParam.put("user_name", trackingModel.user_name)

            val bufferedWriter = BufferedWriter(OutputStreamWriter(conn.getOutputStream(), "UTF-8"))
            bufferedWriter.write(jsonParam.toString())
            bufferedWriter.flush()
            bufferedWriter.close()
            val status = conn.responseCode
            val message = conn.responseMessage
            Log.i("STATUS", status.toString())
            Log.i("MSG", message)
            conn.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Created by kamil on 01.06.2018.
     */
    class RestFirebaseAsync(private var trackingModel: TrackingModel, private var tokenId: String, private var onDataAddedListener: OnDataAddedListener) : AsyncTask<Void, Void, String>() {

        override fun doInBackground(vararg p0: Void?): String {
            RestFirebaseUtils.addLocationToFirebaseDatabaseByRest(trackingModel, tokenId)
            return ""
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            onDataAddedListener.onDataAdded()
        }
    }
}