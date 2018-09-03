package kamilmilik.gps_tracker

import android.location.Location
import kamilmilik.gps_tracker.utils.LocationUtils
import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

class LocationUtilTest {

    @Test
    fun locationFilterTest_LowAccuracyDeltaAccuracyPlusButLowerThan50HighDistance() {
        val currentBestLocation = mock(Location::class.java)
        `when`(currentBestLocation.provider).thenReturn("fused")
        `when`(currentBestLocation.accuracy).thenReturn(2240.87F)
        `when`(currentBestLocation.latitude).thenReturn(50.3841213)
        `when`(currentBestLocation.longitude).thenReturn(21.3440134)
        `when`(currentBestLocation.time).thenReturn(20)

        val location = mock(Location::class.java)
        `when`(location.provider).thenReturn("fused")
        `when`(location.accuracy).thenReturn(2200.0F)
        `when`(location.latitude).thenReturn(50.4106839)
        `when`(location.longitude).thenReturn(21.2307314)
        `when`(location.time).thenReturn(50)

        val distanceBetweenTwoLocations = distanceTo(currentBestLocation, location) // ~8100m
        `when`(currentBestLocation.distanceTo(location)).thenReturn(distanceBetweenTwoLocations)

        Assert.assertEquals(false, LocationUtils.isBetterLocation(null, location, currentBestLocation))
    }

    @Test
    fun locationFilterTest_LowAccuracyDeltaAccuracyPlusButLowerThan50NotHighDistance() {
        val currentBestLocation = mock(Location::class.java)
        `when`(currentBestLocation.provider).thenReturn("fused")
        `when`(currentBestLocation.accuracy).thenReturn(2240.87F)
        `when`(currentBestLocation.latitude).thenReturn(50.3862749)
        `when`(currentBestLocation.longitude).thenReturn(21.3406333)
        `when`(currentBestLocation.time).thenReturn(20)

        val location = mock(Location::class.java)
        `when`(location.provider).thenReturn("fused")
        `when`(location.accuracy).thenReturn(2200.0F)
        `when`(location.latitude).thenReturn(50.3824531)
        `when`(location.longitude).thenReturn(21.3464593)
        `when`(location.time).thenReturn(50)

        val distanceBetweenTwoLocations = distanceTo(currentBestLocation, location)// ~ 593m
        `when`(currentBestLocation.distanceTo(location)).thenReturn(distanceBetweenTwoLocations)

        Assert.assertEquals(true, LocationUtils.isBetterLocation(null, location, currentBestLocation))
    }

    @Test
    fun locationFilterTest_LowAccuracyDeltaAccuracyPlusHigherThan50NotHighDistance() {
        val currentBestLocation = mock(Location::class.java)
        `when`(currentBestLocation.provider).thenReturn("fused")
        `when`(currentBestLocation.accuracy).thenReturn(2240.87F)
        `when`(currentBestLocation.latitude).thenReturn(50.3862749)
        `when`(currentBestLocation.longitude).thenReturn(21.3406333)
        `when`(currentBestLocation.time).thenReturn(20)

        val location = mock(Location::class.java)
        `when`(location.provider).thenReturn("fused")
        `when`(location.accuracy).thenReturn(2300.0F)
        `when`(location.latitude).thenReturn(50.3824531)
        `when`(location.longitude).thenReturn(21.3464593)
        `when`(location.time).thenReturn(50)

        val distanceBetweenTwoLocations = distanceTo(currentBestLocation, location) // ~ 593m
        `when`(currentBestLocation.distanceTo(location)).thenReturn(distanceBetweenTwoLocations)

        Assert.assertEquals(false, LocationUtils.isBetterLocation(null, location, currentBestLocation))
    }

    @Test
    fun locationFilterTest_HighAccuracyDeltaAccuracyMinusNotHighDistance() {
        val currentBestLocation = mock(Location::class.java)
        `when`(currentBestLocation.provider).thenReturn("fused")
        `when`(currentBestLocation.accuracy).thenReturn(439.917F)
        `when`(currentBestLocation.latitude).thenReturn(50.3839196)
        `when`(currentBestLocation.longitude).thenReturn(21.3441866)
        `when`(currentBestLocation.time).thenReturn(20)

        val location = mock(Location::class.java)
        `when`(location.provider).thenReturn("fused")
        `when`(location.accuracy).thenReturn(312.11F)
        `when`(location.latitude).thenReturn(50.3840197)
        `when`(location.longitude).thenReturn(21.3441855)
        `when`(location.time).thenReturn(50)

        val distanceBetweenTwoLocations = distanceTo(currentBestLocation, location) // ~ 11m
        `when`(currentBestLocation.distanceTo(location)).thenReturn(distanceBetweenTwoLocations)

        Assert.assertEquals(true, LocationUtils.isBetterLocation(null, location, currentBestLocation))
    }

    @Test
    fun locationFilterTest_HighAccuracyDeltaAccuracyPlusHigherThan50NotHighDistance() {
        val currentBestLocation = mock(Location::class.java)
        `when`(currentBestLocation.provider).thenReturn("fused")
        `when`(currentBestLocation.accuracy).thenReturn(439.917F)
        `when`(currentBestLocation.latitude).thenReturn(50.3862749)
        `when`(currentBestLocation.longitude).thenReturn(21.3406333)
        `when`(currentBestLocation.time).thenReturn(20)

        val location = mock(Location::class.java)
        `when`(location.provider).thenReturn("fused")
        `when`(location.accuracy).thenReturn(1100.11F)
        `when`(location.latitude).thenReturn(50.3824531)
        `when`(location.longitude).thenReturn(21.3464593)
        `when`(location.time).thenReturn(50)

        val distanceBetweenTwoLocations = distanceTo(currentBestLocation, location) // ~ 593m
        `when`(currentBestLocation.distanceTo(location)).thenReturn(distanceBetweenTwoLocations)

        Assert.assertEquals(false, LocationUtils.isBetterLocation(null, location, currentBestLocation))
    }

    @Test
    fun distanceBetweenTwoPointsMeasureTest_Meters(){
        val currentBestLocation = mock(Location::class.java)
        `when`(currentBestLocation.latitude).thenReturn(50.3862749)
        `when`(currentBestLocation.longitude).thenReturn(21.3406333)

        val location = mock(Location::class.java)
        `when`(location.latitude).thenReturn(50.3824531)
        `when`(location.longitude).thenReturn(21.3464593)

        val distanceBetweenTwoLocations = distanceTo(currentBestLocation, location) // ~ 593m
        `when`(currentBestLocation.distanceTo(location)).thenReturn(distanceBetweenTwoLocations)

        val measure = LocationUtils.calculateDistanceBetweenTwoPoints(currentBestLocation, location).measure
        Assert.assertEquals("m", measure)
    }

    @Test
    fun distanceBetweenTwoPointsMeasureTest_Kilometers(){
        val currentBestLocation = mock(Location::class.java)
        `when`(currentBestLocation.latitude).thenReturn(50.3841213)
        `when`(currentBestLocation.longitude).thenReturn(21.3440134)

        val location = mock(Location::class.java)
        `when`(location.latitude).thenReturn(50.4106839)
        `when`(location.longitude).thenReturn(21.2307314)

        val distanceBetweenTwoLocations = distanceTo(currentBestLocation, location) // ~ 8100m
        `when`(currentBestLocation.distanceTo(location)).thenReturn(distanceBetweenTwoLocations)

        val measure = LocationUtils.calculateDistanceBetweenTwoPoints(currentBestLocation, location).measure
        Assert.assertEquals("km", measure)
    }

    @Test
    fun locationFilterTest_WrongAccuracyHighDistance(){
        val currentBestLocation = mock(Location::class.java)
        `when`(currentBestLocation.latitude).thenReturn(50.2893544 )
        `when`(currentBestLocation.longitude).thenReturn(21.4764)
        `when`(currentBestLocation.provider).thenReturn("fused")
        `when`(currentBestLocation.accuracy).thenReturn(369.936F)
        `when`(currentBestLocation.time).thenReturn(20)

        val location = mock(Location::class.java)
        `when`(location.provider).thenReturn("fused")
        `when`(location.accuracy).thenReturn(899.999F)
        `when`(location.latitude).thenReturn(50.2989184)
        `when`(location.longitude).thenReturn(21.4539946)
        `when`(location.time).thenReturn(10)

        val distanceBetweenTwoLocations = distanceTo(currentBestLocation, location)// ~ 1914.1192m
        `when`(currentBestLocation.distanceTo(location)).thenReturn(distanceBetweenTwoLocations)

        Assert.assertEquals(false, LocationUtils.isBetterLocation(null, location, currentBestLocation))
    }

    private fun distanceTo(locationFrom : Location, locationTo : Location): Float {

        val locationFromLatitude = locationFrom.latitude
        val locationFromLongitude = locationFrom.longitude

        val locationToLatitude = locationTo.latitude
        val locationToLongitude = locationTo.longitude
        val radius = 6371 // Radius of the earth.

        val latDistance = Math.toRadians(locationToLatitude - locationFromLatitude)
        val lonDistance = Math.toRadians(locationToLongitude - locationFromLongitude)
        val a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) + (Math.cos(Math.toRadians(locationFromLatitude)) * Math.cos(Math.toRadians(locationToLatitude))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2))
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        var distance = radius.toDouble() * c * 1000.0 // Convert to meters.


        distance = Math.pow(distance, 2.0)

        return Math.sqrt(distance).toFloat()
    }
}