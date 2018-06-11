package kamilmilik.licencjat_gps_kid.utils

import android.os.AsyncTask
import kamilmilik.licencjat_gps_kid.models.TrackingModel

/**
 * Created by kamil on 01.06.2018.
 */
class RestFirebaseAsync(var trackingModel: TrackingModel, var onDataAddedListener: OnDataAddedListener) : AsyncTask<Void, Void, String>(){
    override fun doInBackground(vararg p0: Void?): String {
        Tools.addLocationToFirebaseDatabaseByRest(trackingModel.email, trackingModel.lat!!, trackingModel.lng!!, trackingModel.time!!, trackingModel.user_id!!, trackingModel.user_name!!)
        return ""
    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
        onDataAddedListener.onDataAdded()
    }
}