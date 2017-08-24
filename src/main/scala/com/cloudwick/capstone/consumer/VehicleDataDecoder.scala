package com.cloudwick.capstone.consumer

import com.cloudwick.capstone.dao.entity.VehicleData
import kafka.serializer.Decoder
import kafka.utils.VerifiableProperties
import org.codehaus.jackson.map.ObjectMapper

/**
  * Created by VenkataRamesh on 5/22/2017.
  */
class VehicleDataDecoder extends Decoder[VehicleData] {

  def this(verifiableProperties: VerifiableProperties) = {
    this
  }

  override def fromBytes(bytes: Array[Byte]): VehicleData = {
    try
      return new ObjectMapper().readValue(bytes, classOf[VehicleData])
    catch {
      case e: Exception =>
        e.printStackTrace()
    }
    return null
  }
}
