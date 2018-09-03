package kamilmilik.gps_tracker.models

import com.google.firebase.database.Query

data class QueryUserModel(val userId: String, val query: Query) {}