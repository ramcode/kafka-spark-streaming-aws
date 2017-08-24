package com.cloudwick.capstone.vo

import java.io.Serializable

import com.cloudwick.capstone.dao.entity.{POITrafficData, TotalTrafficData, VehicleData, WindowTrafficData}

import scala.beans.BeanProperty

//remove if not needed

class Response extends Serializable {

  @BeanProperty
  var totalTraffic: java.util.List[TotalTrafficData] = _

  @BeanProperty
  var windowTraffic: java.util.List[WindowTrafficData] = _

  @BeanProperty
  var poiTraffic: java.util.List[POITrafficData] = _

  @BeanProperty
  var vehicleData: java.util.List[VehicleData] = _

}
