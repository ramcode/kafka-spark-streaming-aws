package com.cloudwick.capstone.producer.vo

import scala.beans.BeanProperty

/**
  * Created by VenkataRamesh on 5/22/2017.
  */
class AggregateKey extends Serializable {

  @BeanProperty
  var routeId: String = _
  @BeanProperty
  var vehicleType: String = _

  def this(routeId: String, vehicleType: String) = {
    this
    this.routeId = routeId
    this.vehicleType = vehicleType
  }


  override def hashCode: Int = {
    val prime = 31
    var result = 1
    result = prime * result + (if (routeId == null) 0
    else routeId.hashCode)
    result = prime * result + (if (vehicleType == null) 0
    else vehicleType.hashCode)
    result
  }

  override def equals(obj: Any): Boolean = {
    if (obj != null && obj.isInstanceOf[AggregateKey]) {
      val other = obj.asInstanceOf[AggregateKey]
      if (other.getRouteId != null && other.getVehicleType != null) if ((other.getRouteId == this.routeId) && (other.getVehicleType == this.vehicleType)) return true
    }
    false
  }

}
