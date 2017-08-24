package com.cloudwick.capstone.dao

import org.springframework.cassandra.core.PrimaryKeyType
import org.springframework.data.cassandra.mapping.{Column, PrimaryKeyColumn, Table}

import scala.beans.BeanProperty

/**
  * Created by VenkataRamesh on 5/21/2017.
  */

@Table("wiki_rc_table")
class RcMessageEntity extends Serializable {

  @PrimaryKeyColumn(name = "id", `type` = PrimaryKeyType.PARTITIONED)
  @BeanProperty
  var id: String = _

  @Column(value = "wikipedia")
  @BeanProperty
  var wikipedia: String = _
  @Column(value = "user")
  @BeanProperty
  var user: String = _
  @Column(value = "isanonymous")
  @BeanProperty
  var isanonymous: Boolean = _
  @Column(value = "pageurl")
  @BeanProperty
  var pageurl: String = _
  @Column(value = "page")
  @BeanProperty
  var page: String = _
  @Column(value = "timestamp")
  @BeanProperty
  var timestamp: String = _
  @Column(value = "isrobot")
  @BeanProperty
  var isrobot: Boolean = _
  @Column(value = "namespace")
  @BeanProperty
  var namespace: String = _
  @Column(value = "geocity")
  @BeanProperty
  var geocity: Option[String] = _
  @Column(value = "geostateprovince")
  @BeanProperty
  var geostateprovince: Option[String] = _
  @Column(value = "geocountry")
  @BeanProperty
  var geocountry: Option[String] = _
  @Column(value = "geocountrycode2")
  @BeanProperty
  var geocountrycode2: Option[String] = _
  @Column(value = "geocountrycode3")
  @BeanProperty
  var geocountrycode3: Option[String] = _

}
