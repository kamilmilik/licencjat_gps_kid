package kamilmilik.licencjat_gps_kid.Helper

import com.google.android.gms.location.FusedLocationProviderClient
import kamilmilik.licencjat_gps_kid.models.UserMarkerInformationModel

/**
 * Created by kamil on 07.04.2018.
 */
interface OnMarkerAddedCallback {
    fun onMarkerAddedListener()
    fun myLocationMarkerAddedListener(isMyLocationSet : Boolean)
    fun userConnectionMarkerAddedListener(isUserConnectionSet : Boolean)
    fun updateChangeUserNameInRecycler(userInformation : UserMarkerInformationModel)
}