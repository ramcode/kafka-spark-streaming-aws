package com.cloudwick.capstone.producer.vo

import java.io.Serializable

import scala.beans.{BeanProperty, BooleanBeanProperty}

//remove if not needed
import scala.collection.JavaConversions._

/**
  * Class to represent attributes of POI
  *
  * @author abaghel
  *
  */
class POIData extends Serializable {

  @BeanProperty
  var latitude: Double = _

  @BeanProperty
  var longitude: Double = _

  @BeanProperty
  var radius: Double = _

}
