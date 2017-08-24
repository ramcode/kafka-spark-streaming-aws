package com.cloudwick.capstone.dao

import java.lang.Iterable

import com.cloudwick.capstone.dao.entity.WindowTrafficData
import org.springframework.data.cassandra.repository.{CassandraRepository, Query}
import org.springframework.stereotype.Repository

@Repository
trait WindowTrafficDataRepository
  extends CassandraRepository[WindowTrafficData] {

  @Query(
    "SELECT * FROM traffickeyspace.window_traffic WHERE recorddate = ?0 ALLOW FILTERING")
  def findTrafficDataByDate(
                             date: String): Iterable[WindowTrafficData]

}