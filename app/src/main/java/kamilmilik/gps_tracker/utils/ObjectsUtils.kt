package kamilmilik.gps_tracker.utils

import android.location.Location
import com.google.firebase.auth.FirebaseUser
import kamilmilik.gps_tracker.models.TrackingModel
import kamilmilik.gps_tracker.models.UserUniqueKey

object ObjectsUtils {

    fun <T1 : Any, T2 : Any, R : Any> safeLet(p1: T1?, p2: T2?, block: (T1, T2) -> R?): R? {
        return if (p1 != null && p2 != null) block(p1, p2) else null
    }

    fun <T1 : Any, T2 : Any, T3 : Any, R : Any> safeLet(p1: T1?, p2: T2?, p3: T3?, block: (T1, T2, T3) -> R?): R? {
        return if (p1 != null && p2 != null && p3 != null) block(p1, p2, p3) else null
    }

    fun <T1 : Any, T2 : Any, T3 : Any, T4 : Any, R : Any> safeLet(p1: T1?, p2: T2?, p3: T3?, p4: T4?, block: (T1, T2, T3, T4) -> R?): R? {
        return if (p1 != null && p2 != null && p3 != null && p4 != null) block(p1, p2, p3, p4) else null
    }

    fun <T1 : Any, T2 : Any, T3 : Any, T4 : Any, T5 : Any, R : Any> safeLet(p1: T1?, p2: T2?, p3: T3?, p4: T4?, p5: T5?, block: (T1, T2, T3, T4, T5) -> R?): R? {
        return if (p1 != null && p2 != null && p3 != null && p4 != null && p5 != null) block(p1, p2, p3, p4, p5) else null
    }

    fun <T1 : Any, T2 : Any, T3 : Any, T4 : Any, T5 : Any, T6 : Any, R : Any> safeLet(p1: T1?, p2: T2?, p3: T3?, p4: T4?, p5: T5?, p6: T6?, block: (T1, T2, T3, T4, T5, T6) -> R?): R? {
        return if (p1 != null && p2 != null && p3 != null && p4 != null && p5 != null && p6 != null) block(p1, p2, p3, p4, p5, p6) else null
    }

    fun <R : Any> safeLetFirebaseUser(user: FirebaseUser?, block: (String, String, String) -> R) {
        user?.let {
            safeLet(user.uid, user.email, user.displayName) { uid, email, name ->
                block(uid, email, name)
            }
        }
    }

    fun <R : Any> safeLetTrackingModel(user: FirebaseUser?, location: Location?, block: (String, String, String, String, String) -> R) {
        location?.let { loc ->
            safeLetFirebaseUser(user) { uid, email, name ->
                block(uid, email, name, loc.latitude.toString(), loc.longitude.toString())
            }
        }
    }

    fun <R : Any> safeLetTrackingModel(trackingModel: TrackingModel?, block: (String, String, String, String, String) -> R) {
        trackingModel?.let { user ->
            safeLet(user.user_id, user.email, user.lat, user.lng, user.user_name) { id, email, lat, lng, name ->
                block(id, email, name, lat, lng)
            }
        }
    }

    fun <R : Any> safeLetRestFirebase(user: FirebaseUser?, location: Location?, tokenId: String?, block: (String, String, String, String, String, String) -> R) {
        safeLet(location, tokenId) { loc, token ->
            safeLetFirebaseUser(user) { uid, email, name ->
                block(uid, email, name, loc.latitude.toString(), loc.longitude.toString(), token)
            }
        }
    }

    fun <R : Any> safeLetUserUniqueKey(userUniqueKey: UserUniqueKey?, block: (String, String, String) -> R) {
        userUniqueKey?.let {
            safeLet(userUniqueKey.user_id, userUniqueKey.user_email, userUniqueKey.user_name) { id, email, name ->
                block(id, email, name)
            }

        }
    }
}