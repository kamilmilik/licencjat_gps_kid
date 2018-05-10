package kamilmilik.licencjat_gps_kid.models


/**
 * Created by kamil on 29.04.2018.
 */
data class UserMarkerInformationModel(var email : String, var userName : String){
    override fun equals(obj: Any?): Boolean {
        if (this === obj) return true
        if (obj !is UserMarkerInformationModel) return false

        return this.email == obj.email && this.userName == obj.userName
    }

    override fun hashCode(): Int {
        var hash = 3

        hash = 7 * hash + this.email.hashCode()
        hash = 7 * hash + this.userName.hashCode()
        return hash
    }
}