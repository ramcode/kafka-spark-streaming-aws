package com.cloudwick.capstone.producer.vo

import java.util.Date

import com.fasterxml.jackson.annotation.JsonFormat

import scala.beans.BeanProperty

/**
  * Created by VenkataRamesh on 5/22/2017.
  */
class TotalTrafficData extends Serializable {

  @BeanProperty
  var routeId: String = _

  @BeanProperty
  var vehicleType: String = _

  @BeanProperty
  var totalCount: Long = _

  @JsonFormat(shape = JsonFormat.Shape.STRING,
    pattern = "yyyy-MM-dd HH:mm:ss",
    timezone = "MST")
  @BeanProperty
  var timeStamp: Date = _

  @BeanProperty
  var recordDate: String = _

}
