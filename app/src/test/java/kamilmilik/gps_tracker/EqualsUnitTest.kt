package kamilmilik.gps_tracker

import junit.framework.Assert
import kamilmilik.gps_tracker.models.UserBasicInfo
import org.junit.Test

/**
 * Created by kamil on 15.08.2018.
 */
class EqualsUnitTest{

    private val userId = "Zx_ftk83"

    private val userEmail = "test@test.pl"

    private val userName = "test"

    @Test
    fun testEquals_Symmetric(){
        val user1 = UserBasicInfo(userId, userEmail, userName)
        val user2 = UserBasicInfo(userId, userEmail, userName)
        Assert.assertTrue(user1 == user2 && user2 == user1)
        Assert.assertTrue(user1.hashCode() == user2.hashCode())
    }

    @Test
    fun testEquals_Reflexive(){
        val user1 = UserBasicInfo(userId, userEmail, userName)
        Assert.assertTrue(user1 == user1)
    }

    @Test
    fun testEquals_Transitive(){
        val user1 = UserBasicInfo(userId, userEmail, userName)
        val user2 = UserBasicInfo(userId, userEmail, userName)
        val user3 = UserBasicInfo(userId, userEmail, userName)
        Assert.assertTrue(user1 == user2 && user2 == user3 && user1 == user3)

    }

    @Test
    fun testHashMap_Contains(){
        val map1 = HashMap<UserBasicInfo, Any>()
        val user1 = UserBasicInfo(userId, userEmail, userName)
        val user2 = UserBasicInfo(userId, userEmail, userName)
        map1[user1] = "any"
        Assert.assertEquals(true, map1.contains(user2))
        user2.userName = "new.test@test.pl"
        Assert.assertEquals(false, map1.contains(user2))
    }
}