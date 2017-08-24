package com.cloudwick.capstone.dashboard

import java.text.{DateFormat, SimpleDateFormat}
import java.util
import java.util.Date
import java.util.concurrent.ThreadLocalRandom
import java.util.logging.Logger

import com.cloudwick.capstone.dao.entity.{POITrafficData, TotalTrafficData, VehicleData, WindowTrafficData}
import com.cloudwick.capstone.dao.{CassandraQueryRepo, POITrafficDataRepository, TotalTrafficDataRepository, WindowTrafficDataRepository}
import com.cloudwick.capstone.vo.Response
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

import scala.beans.BeanProperty
import scala.util.Random


@Service
class StreamingDashBoardService {

  private val logger: Logger =
    Logger.getLogger(" com.cloudwick.capstone.dashboard.StreamingDashBoardService")

  private var sdf: DateFormat = new SimpleDateFormat("yyyy-MM-dd")

  @Autowired
  @BeanProperty
  var template: SimpMessagingTemplate = _

  @Autowired
  @BeanProperty
  var queryRepository: CassandraQueryRepo = _

  @Autowired
  @BeanProperty
  var totalTrafficRepo: TotalTrafficDataRepository = _

  @Autowired
  @BeanProperty
  var windowTrafficRepo: WindowTrafficDataRepository = _

  @Autowired
  @BeanProperty
  var poiTrafficRepo: POITrafficDataRepository = _

  //Method sends traffic data message in every 5 seconds.
  @Scheduled(fixedRate = 7000)
  def trigger(): Unit = {
    val totalTrafficList: util.List[TotalTrafficData] =
      new util.ArrayList[TotalTrafficData]()
    val windowTrafficList: util.List[WindowTrafficData] =
      new util.ArrayList[WindowTrafficData]()
    val poiTrafficList: util.List[POITrafficData] = new util.ArrayList[POITrafficData]()
    val vehicleData: util.List[VehicleData] = new util.ArrayList[VehicleData]()
    //Call dao methods
    val totIt = totalTrafficRepo.findTrafficDataByDate(sdf.format(new Date())).iterator()
    while (totIt.hasNext) {
      val x: TotalTrafficData = totIt.next()
      totalTrafficList.add(x)
    }
    val winIt = windowTrafficRepo.findTrafficDataByDate(sdf.format(new Date())).iterator()
    while (winIt.hasNext) {
      val x: WindowTrafficData = winIt.next()
      windowTrafficList.add(x)
    }

    val poiIt = poiTrafficRepo.findAllPoi().iterator()
    val winIt2 = windowTrafficRepo.findTrafficDataByDate(sdf.format(new Date())).iterator()
    val rand: Random = new Random
    while (winIt2.hasNext) {
      //val x: POITrafficData = poiIt.next()
      val x: WindowTrafficData = winIt2.next()
      val rand: Random = new Random()
      if (x.vehicleType.contains("Truck") || x.vehicleType.equals("Car")) {
        var poi: POITrafficData = new POITrafficData
        poi.vehicleId = s"V-${rand.nextInt(windowTrafficList.size() + 1)}"
        poi.vehicleType = x.vehicleType
        poi.timeStamp = x.timeStamp
        poi.recordDate = x.recordDate
        poi.distance = ThreadLocalRandom.current.nextDouble(0, 30)
        poiTrafficList.add(poi)
      }
    }
    val vIt = queryRepository.findVehicleData().iterator()
    while (vIt.hasNext) {
      val x: VehicleData = vIt.next()
      vehicleData.add(x)
    }
    //prepare response
    val response: Response = new Response()
    response.setTotalTraffic(totalTrafficList)
    response.setWindowTraffic(windowTrafficList)
    response.setPoiTraffic(poiTrafficList)
    response.setVehicleData(vehicleData)
    logger.info("Sending to UI " + response)
    //send to ui
    template.convertAndSend("/topic/trafficData", response)
  }
}
