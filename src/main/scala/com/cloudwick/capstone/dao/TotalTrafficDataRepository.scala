package com.cloudwick.capstone.dao

import java.lang.Iterable

import com.cloudwick.capstone.dao.entity.TotalTrafficData
import org.springframework.data.cassandra.repository.{CassandraRepository, Query}
import org.springframework.stereotype.Repository


/**
  * Created by VenkataRamesh on 5/21/2017.
  */

@Repository
trait TotalTrafficDataRepository
  extends CassandraRepository[TotalTrafficData] {

  @Query(
    "SELECT * FROM traffickeyspace.total_traffic WHERE recorddate = ?0 ALLOW FILTERING")
  def findTrafficDataByDate(date: String): Iterable[TotalTrafficData]

}
