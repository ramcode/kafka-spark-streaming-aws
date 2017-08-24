package com.cloudwick.capstone.producer

import java.util

import com.cloudwick.capstone.dao.entity.VehicleData
import org.apache.kafka.common.serialization.Serializer
import org.codehaus.jackson.map.ObjectMapper

import scala.runtime.Nothing$

/**
  * Created by VenkataRamesh on 5/21/2017.
  */
class VehicleDataSerializer extends Serializer[VehicleData] {
  override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = {

  }

  override def serialize(topic: String, data: VehicleData): Array[Byte] = {
    var retVal: Array[Byte] = null

    val objectMapper = new ObjectMapper()
    try
      retVal = objectMapper.writeValueAsString(data).getBytes
    catch {
      case e: Exception =>
        e.printStackTrace()
    }
    retVal
  }

  def serializetoJson(vehicleData: VehicleData): String = {
    val objectMapper = new ObjectMapper()
    objectMapper.writeValueAsString(vehicleData)
  }

  override def close(): Unit = {}
}
