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
  * Entity class for total_traffic db table
  *
  * @author abaghel
  *
  */
@Table("total_traffic")
class TotalTrafficData extends Serializable {

  @PrimaryKeyColumn(name = "routeid",
                    ordinal = 0,
                    `type` = PrimaryKeyType.PARTITIONED)
  @BeanProperty
  var routeId: String = _

  @PrimaryKeyColumn(name = "recordDate",
                    ordinal = 1,
                    `type` = PrimaryKeyType.CLUSTERED)
  @BeanProperty
  var recordDate: String = _

  @PrimaryKeyColumn(name = "vehicletype",
                    ordinal = 2,
                    `type` = PrimaryKeyType.CLUSTERED)
  @BeanProperty
  var vehicleType: String = _

  @Column(value = "totalcount")
  @BeanProperty
  var totalCount: Long = _

  @JsonFormat(shape = JsonFormat.Shape.STRING,
              pattern = "yyyy-MM-dd HH:mm:ss",
              timezone = "MST")
  @Column(value = "timestamp")
  @BeanProperty
  var timeStamp: Date = _

  override def toString(): String =
    "TrafficData [routeId=" + routeId + ", vehicleType=" +
      vehicleType +
      ", totalCount=" +
      totalCount +
      ", timeStamp=" +
      timeStamp +
      "]"

}
