package com.cloudwick.capstone.dao.entity

import java.io.Serializable
import java.util.Date

import com.fasterxml.jackson.annotation.JsonFormat
import org.springframework.cassandra.core.PrimaryKeyType
import org.springframework.data.cassandra.mapping.{Column, PrimaryKeyColumn, Table}

import scala.beans.BeanProperty

//remove if not needed

@Table("vehicle_data")
class VehicleData extends Serializable {

  @PrimaryKeyColumn(name = "vehicleid", ordinal = 0,
    `type` = PrimaryKeyType.PARTITIONED)
  @BeanProperty
  var vehicleId: String = _

  @Column(value = "vehicletype")
  @BeanProperty
  var vehicleType: String = _

  @Column(value = "routeid")
  @BeanProperty
  var routeId: String = _

  @Column(value = "longitude")
  @BeanProperty
  var longitude: String = _

  @Column(value = "latitude")
  @BeanProperty
  var latitude: String = _

  @Column(value = "timestamp")
  @JsonFormat(shape = JsonFormat.Shape.STRING,
    pattern = "yyyy-MM-dd HH:mm:ss",
    timezone = "PST")
  @BeanProperty
  var timestamp: Date = _

  @Column(value = "speed")
  @BeanProperty
  var speed: Double = _

  @Column(value = "fuellevel")
  @BeanProperty
  var fuelLevel: Double = _

  def this(vehicleId: String,
           vehicleType: String,
           routeId: String,
           latitude: String,
           longitude: String,
           timestamp: Date,
           speed: Double,
           fuelLevel: Double) = {
    this
    this.vehicleId = vehicleId
    this.vehicleType = vehicleType
    this.routeId = routeId
    this.longitude = longitude
    this.latitude = latitude
    this.timestamp = timestamp
    this.speed = speed
    this.fuelLevel = fuelLevel
  }

  override def toString = s"VehicleData($vehicleId, $vehicleType, $routeId, $longitude, $latitude, $timestamp, $speed, $fuelLevel)"
}
