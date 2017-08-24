package com.cloudwick.capstone.producer

import java.util

import com.cloudwick.capstone.dao.entity.VehicleData
import org.apache.kafka.common.serialization.Deserializer
import org.codehaus.jackson.map.ObjectMapper

/**
  * Created by VenkataRamesh on 5/21/2017.
  */
class VehicleDataDeserializer extends Deserializer[VehicleData] {
  override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = {

  }

  override def close(): Unit = {

  }

  override def deserialize(topic: String, data: Array[Byte]): VehicleData = {
    val mapper: ObjectMapper = new ObjectMapper();
    var vehicle: VehicleData = null
    try {
      vehicle = mapper.readValue(data, classOf[VehicleData]);
    } catch {
      case e: Exception =>
        e.printStackTrace()
    }
    vehicle
  }
}
