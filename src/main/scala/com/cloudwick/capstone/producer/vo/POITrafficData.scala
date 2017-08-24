package com.cloudwick.capstone.producer.vo

import java.util.Date

import com.fasterxml.jackson.annotation.JsonFormat

import scala.beans.BeanProperty

/**
  * Created by VenkataRamesh on 5/22/2017.
  */
class POITrafficData extends Serializable {

  @BeanProperty
  var vehicleId: String = _

  @BeanProperty
  var distance: Double = _

  @BeanProperty
  var vehicleType: String = _

  @JsonFormat(shape = JsonFormat.Shape.STRING,
    pattern = "yyyy-MM-dd HH:mm:ss",
    timezone = "MST")
  @BeanProperty
  var timeStamp: Date = _

}
