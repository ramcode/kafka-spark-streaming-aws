package com.cloudwick.capstone.dao.entity

import java.io.Serializable

import java.util.Date

import org.springframework.cassandra.core.PrimaryKeyType

import org.springframework.data.cassandra.mapping.Column

import org.springframework.data.cassandra.mapping.PrimaryKeyColumn

import org.springframework.data.cassandra.mapping.Table

import com.fasterxml.jackson.annotation.JsonFormat

import scala.beans.{BeanProperty, BooleanBeanProperty}

//remove if not needed
import scala.collection.JavaConversions._

/**
  * Entity class for poi_traffic db table
  *
  * @author abaghel
  *
  */
@Table("poi_traffic")
class POITrafficData extends Serializable {

  @JsonFormat(shape = JsonFormat.Shape.STRING,
              pattern = "yyyy-MM-dd HH:mm:ss",
              timezone = "CST")
  @PrimaryKeyColumn(name = "timeStamp",
                    ordinal = 0,
                    `type` = PrimaryKeyType.PARTITIONED)
  @BeanProperty
  var timeStamp: Date = _

  @PrimaryKeyColumn(name = "recordDate",
                    ordinal = 1,
                    `type` = PrimaryKeyType.CLUSTERED)
  @BeanProperty
  var recordDate: String = _

  @Column(value = "vehicleId")
  @BeanProperty
  var vehicleId: String = _

  @Column(value = "distance")
  @BeanProperty
  var distance: Double = _

  @Column(value = "vehicleType")
  @BeanProperty
  var vehicleType: String = _

}
