package com.cloudwick.capstone.consumer

//remove if not needed
import scala.collection.JavaConversions._

object GeoDistanceCalculator {

  /**
    * Method to get shortest distance over the earth�s surface in Kilometer between two locations
    *
    * @param lat1 latitude of location A
    * @param lon1 longitude of location A
    * @param lat2 latitude of location B
    * @param lon2 longitude of location B
    * @return distance between A and B in Kilometer
    *
    */
  def getDistance(lat1: Double,
                  lon1: Double,
                  lat2: Double,
                  lon2: Double): Double = {
    //Earth radius in KM
    val r: Int = 6371
    val latDistance: java.lang.Double = Math.toRadians(lat2 - lat1)
    val lonDistance: java.lang.Double = Math.toRadians(lon2 - lon1)
    val a: java.lang.Double = Math.sin(latDistance / 2) * Math.sin(
      latDistance / 2) +
      Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
        Math.sin(lonDistance / 2) *
        Math.sin(lonDistance / 2)
    val c: java.lang.Double = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    val distance: Double = r * c
    distance
  }

  /**
    * Method to check if current location is in radius of point of interest (POI) location
    *
    * @param currentLat latitude of current location
    * @param currentLon longitude of current location
    * @param poiLat     latitude of POI location
    * @param poiLon     longitude of POI location
    * @param radius     radius in Kilometer from POI
    * @return true if in POI radius otherwise false
    *
    */
  def isInPOIRadius(currentLat: Double,
                    currentLon: Double,
                    poiLat: Double,
                    poiLon: Double,
                    radius: Double): Boolean = {
    val distance: Double = getDistance(currentLat, currentLon, poiLat, poiLon)
    if (distance <= radius) {
      true
    }
    false
  }

}
