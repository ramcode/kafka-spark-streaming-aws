package com.cloudwick.capstone.dao

import java.lang.Iterable

import com.cloudwick.capstone.dao.entity.POITrafficData
import org.springframework.data.cassandra.repository.{CassandraRepository, Query}
import org.springframework.stereotype.Repository


@Repository
trait POITrafficDataRepository extends CassandraRepository[POITrafficData] {


  @Query("SELECT * FROM traffickeyspace.poi_traffic ")
  def findAllPoi(): Iterable[POITrafficData]
}
