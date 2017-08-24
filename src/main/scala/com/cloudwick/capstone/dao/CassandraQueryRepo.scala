package com.cloudwick.capstone.dao

import java.lang.Iterable

import com.cloudwick.capstone.dao.entity.VehicleData
import org.springframework.data.cassandra.repository.{CassandraRepository, Query}
import org.springframework.stereotype.Repository

/**
  * Created by VenkataRamesh on 5/21/2017.
  */

@Repository
trait CassandraQueryRepo extends CassandraRepository[VehicleData] {

  @Query("SELECT * FROM traffickeyspace.vehicle_traffic")
  def findVehicleData(): Iterable[VehicleData]

  /* @Query("SELECT * FROM traffickeyspace.total_traffic WHERE recorddate = ?0 ALLOW FILTERING")
   def findTotalTrafficDataByDate(date: String): Iterable[TotalTrafficData]

   @Query("SELECT * FROM traffickeyspace.window_traffic WHERE recorddate = ?0 ALLOW FILTERING")
   def findWindowTrafficDataByDate(date: String): Iterable[WindowTrafficData]

   @Query("SELECT * FROM traffickeyspace.poi_traffic")
   def findPoiData(): Iterable[POITrafficData]*/

}
