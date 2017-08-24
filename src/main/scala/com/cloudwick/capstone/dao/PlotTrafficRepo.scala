package com.cloudwick.capstone.dao

import com.cloudwick.capstone.dao.entity.{PlotData, VehicleData}
import org.springframework.data.cassandra.repository.{CassandraRepository, Query}
import org.springframework.stereotype.Repository

/**
  * Created by VenkataRamesh on 5/25/2017.
  */

@Repository
trait PlotTrafficRepo extends CassandraRepository[PlotData] {


  @Query("SELECT * FROM traffickeyspace.plot_data")
  def findPlotData(): Iterable[PlotData]

  /* @Query("SELECT * FROM traffickeyspace.total_traffic WHERE recorddate = ?0 ALLOW FILTERING")
   def findTotalTrafficDataByDate(date: String): Iterable[TotalTrafficData]

   @Query("SELECT * FROM traffickeyspace.window_traffic WHERE recorddate = ?0 ALLOW FILTERING")
   def findWindowTrafficDataByDate(date: String): Iterable[WindowTrafficData]

   @Query("SELECT * FROM traffickeyspace.poi_traffic")
   def findPoiData(): Iterable[POITrafficData]*/


}
