package kamilmilik.gps_tracker.models

import com.google.gson.*
import java.lang.reflect.Type
import com.google.gson.JsonPrimitive
import com.google.gson.JsonObject



/**
 * Created by kamil on 01.05.2018.
 */
class MyCustomJsonSerializer :  JsonSerializer<UserAndPolygonKeyModel> {
    override fun serialize(src: UserAndPolygonKeyModel?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        val result = JsonObject()
        result.add("userId", JsonPrimitive(src!!.userId))
        result.add("polygonKey", JsonPrimitive(src.polygonKey))
        return result
    }


}