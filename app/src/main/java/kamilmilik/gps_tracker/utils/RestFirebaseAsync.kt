package kamilmilik.gps_tracker.utils

import android.os.AsyncTask
import kamilmilik.gps_tracker.models.TrackingModel

/**
 * Created by kamil on 01.06.2018.
 */
class RestFirebaseAsync(private var trackingModel: TrackingModel, private var tokenId : String, private var onDataAddedListener: OnDataAddedListener) : AsyncTask<Void, Void, String>(){
    override fun doInBackground(vararg p0: Void?): String {
        Tools.addLocationToFirebaseDatabaseByRest(trackingModel, tokenId)
        return ""
    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
        onDataAddedListener.onDataAdded()
    }
}