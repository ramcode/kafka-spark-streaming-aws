package com.cloudwick.capstone.consumer

import java.text.SimpleDateFormat
import java.util
import java.util.{Calendar, Date, Properties}

import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.kinesis.AmazonKinesisClient
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.InitialPositionInStream
import com.cloudwick.capstone.dao.entity.VehicleData
import com.cloudwick.capstone.producer.vo._
import com.cloudwick.capstone.producer.{VehicleDataDeserializer, VehicleDataSerializer}
import com.datastax.spark.connector.japi.CassandraJavaUtil
import com.datastax.spark.connector.japi.CassandraStreamingJavaUtil.javaFunctions
import com.google.common.base.Optional
import com.typesafe.config.{Config, ConfigFactory}
import kafka.serializer.StringDecoder
import org.apache.kafka.clients.consumer.{ConsumerRecords, KafkaConsumer}
import org.apache.log4j.Logger
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.{SaveMode, SparkSession}
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.kinesis.KinesisUtils
import org.apache.spark.streaming.{Durations, Seconds, State, StreamingContext}

import scala.collection.JavaConverters._

/**
  * Created by VenkataRamesh on 5/18/2017.
  */
object KafKaStreamConsumer {

  val logger: Logger = Logger.getLogger(KafKaStreamConsumer.getClass.getSimpleName)


  val kafkaConfig: Config = ConfigFactory.load("kafka-broker")
  val cassandraConfig: Config = ConfigFactory.load("cassandra-db")
  val awsConfig: Config = ConfigFactory.load("aws-config")
  val awsComponentConfig = ConfigFactory.load("aws-components")

  val TABLE_NAME = cassandraConfig.getString("TABLE_NAME")


  val KEY_SPACE = cassandraConfig.getString("KEY_SPACE")


  val awsAccessKeyId = awsConfig.getString("awsAccessKeyId")
  val awsSecretKey = awsConfig.getString("awsSecretKey")
  val kinesisStream = awsComponentConfig.getString("kinesisStream")

  def s3Bucket = awsComponentConfig.getString("s3Bucket")

  case class RCMessage(val wikipedia: String, val user: String, val isanonymous: Boolean,
                       val pageurl: String, val page: String, val timestamp: String, val isrobot: Boolean, val namespace: String,
                       val geocity: Option[String], val geostateprovince: Option[String],
                       val geocountry: Option[String], val geocountrycode2: Option[String], val geocountrycode3: Option[String])


  //val vehicleSchema =  StructType(Array(StructField("id",LongType,nullable = true),StructField("role",StringType,nullable = true)))

  def brokerConfigParams(brokers: String, groupId: String, topics: String): Properties = {
    val props = new Properties
    props.put("bootstrap.servers", brokers)
    props.put("group.id", groupId)
    props.put("topics", topics)
    props.put("enable.auto.commit", "true")
    props.put("auto.commit.interval.ms", "1000")
    props.put("session.timeout.ms", "30000")
    //props.put("auto.offset.reset", "largest")
    props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    props.put("value.deserializer", "com.cloudwick.capstone.producer.VehicleDataDeserializer")
    props
  }


  def createKafkaStreamingContext(props: Properties, spark: SparkSession): StreamingContext = {
    val sc = spark.sparkContext
    val BatchInterval = Seconds(30)
    val ssc = new StreamingContext(sc, BatchInterval)
    ssc.checkpoint("tmp/iot-streaming-data")
    val kafkaParams: Predef.Map[String, String] = props.asScala.toMap
    val topics = props.get("topics").toString.split(",").toSet
    val stream: InputDStream[(String, VehicleData)] = KafkaUtils.createDirectStream[String, VehicleData, StringDecoder, VehicleDataDecoder](ssc, kafkaParams, topics)
    logger.info("Starting Stream Processing")
    //poi data
    val poiData = new POIData
    poiData.setLatitude(37.79)
    poiData.setLongitude(-122.35)
    poiData.setRadius(30) //30 km
    //broadcast variables. We will monitor vehicles on Route 37 which are of type Truck
    val broadcastPOIValues = ssc.sparkContext.broadcast(new Tuple1[POIData /*,Array[String], Array[String]*/ ](poiData /*, Array("I-8, I-10", "I-80"), Array("Car", "Bus", "Large Truck")*/))
    //call method  to process stream
    val nonFilteredIotDataStream = stream.map((tuple: Tuple2[String, VehicleData]) => tuple._2)
    //filtered stream for total and traffic data calculation
    val iotDataPairStream = nonFilteredIotDataStream.map((iot: VehicleData) => (iot.getVehicleId, iot)).reduceByKey((a: VehicleData, b: VehicleData) => a).map((tuple: Tuple2[String, VehicleData]) => tuple._2)
    //cache stream as it is used in total and window based computation
    iotDataPairStream.cache
    //process data
    processTotalTrafficData(iotDataPairStream, spark, false)
    processWindowTrafficData(iotDataPairStream, spark, false)
    processPOIData(nonFilteredIotDataStream, broadcastPOIValues, spark, false)
    processStreamTrafficData(iotDataPairStream, spark, false)
    ssc
  }


  def createKinesisStreamingContext(kinesisClient: AmazonKinesisClient, spark: SparkSession, topic: String, endPoint: String): StreamingContext = {
    val sc = spark.sparkContext
    val BatchInterval = Seconds(30)
    val ssc = new StreamingContext(sc, BatchInterval)
    ssc.checkpoint("tmp/iot-streaming-data")
    //desribe shards
    val numShards = kinesisClient.describeStream(topic).getStreamDescription().getShards().size
    // Creata a Kinesis stream
    val kinesisStream = KinesisUtils.createStream(ssc,
      "Kinesis-Streaming", topic,
      endPoint, "us-east-1",
      //RegionUtils.getRegionByEndpoint(endPoint).getName(),
      InitialPositionInStream.LATEST, BatchInterval,
      StorageLevel.MEMORY_AND_DISK_SER_2, awsAccessKeyId, awsSecretKey)
    //broadcast variables. We will monitor vehicles on Route 37 which are of type Truck
    val poiData = new POIData
    poiData.setLatitude(37.79)
    poiData.setLongitude(-122.35)
    poiData.setRadius(30) //30 km
    val broadcastPOIValues = ssc.sparkContext.broadcast(new Tuple1[POIData /*,Array[String], Array[String]*/ ](poiData /*, Array("I-8, I-10", "I-80"), Array("Car", "Bus", "Large Truck")*/))
    val stream: DStream[(String, VehicleData)] = kinesisStream.map(x => (topic, new VehicleDataDeserializer().deserialize(topic, x)))
    // The stream is a DStream[(String, String)], consisting of a Kafka topic
    // and a JSON string. We don't need the topic, since it's also part of the
    // JSON.
    logger.info("Starting Stream Processing")
    val nonFilteredIotDataStream = stream.map((tuple: Tuple2[String, VehicleData]) => tuple._2)
    //filtered stream for total and traffic data calculation
    val iotDataPairStream = nonFilteredIotDataStream.map((iot: VehicleData) => (iot.getVehicleId, iot)).reduceByKey((a: VehicleData, b: VehicleData) => a).map((tuple: Tuple2[String, VehicleData]) => tuple._2)
    //cache stream as it is used in total and window based computation
    iotDataPairStream.cache
    //process data
    processPOIData(nonFilteredIotDataStream, broadcastPOIValues, spark, true)
    processTotalTrafficData(iotDataPairStream, spark, true)
    processWindowTrafficData(iotDataPairStream, spark, true)
    processStreamTrafficData(iotDataPairStream, spark, true)
    ssc
  }

  def createConsumer(props: Properties, brokers: String, topics: String): KafkaConsumer[String, String] = {
    val consumer = new KafkaConsumer[String, String](props)
    consumer.subscribe(topics)
    consumer
  }

  def stop(): Unit = {
    val sscOpt = StreamingContext.getActive // returns Option[StreamingContext]
    if ( sscOpt.isDefined ) {
      val ssc = sscOpt.get // get the StreamingContext
      ssc.stop(stopSparkContext = false)
      println("Stopped running streaming context.")
    }
  }


  def main(args: Array[String]): Unit = {
    def BROKERS = kafkaConfig.getString("bootstrap.servers")

    def GROUP_ID = kafkaConfig.getString("kafka-topic")

    def TOPICS = kafkaConfig.getString("kafka-topic")

    val spark = SparkSession.builder()
      .master("local[4]")

      .config("spark.sql.warehouse.dir", "file:///F:/tmp/spark-warehouse")
      .config("spark.cassandra.connection.host", cassandraConfig.getString("com.iot.app.cassandra.host"))
      .config("spark.cassandra.connection.keep_alive_ms", "10000")
      //.config("hive.metastore.uris", "thrift://localhost:9083")
      //.enableHiveSupport()
      .appName("Capstone Project").getOrCreate()

    val props = brokerConfigParams(BROKERS, GROUP_ID, TOPICS)

    val endPoint = "https://kinesis.us-east-1.amazonaws.com"

    val kinesisClient: AmazonKinesisClient = createKinesisClient(awsAccessKeyId, awsSecretKey, endPoint)


    val streamingContext = createKafkaStreamingContext(props, spark)
    //val kinesisStreamingContext = createKinesisStreamingContext(kinesisClient, spark, topic = "vehicle_traffic", endPoint)
    //kinesisStreamingContext.start()
    streamingContext.start()
    /* while (true) {
       Thread.sleep(1000)
       import com.datastax.spark.connector._
       val TABLE_NAME = "vehicle_traffic"
       val rdd = spark.sparkContext.cassandraTable[VehicleData](KEY_SPACE, TABLE_NAME)
       println(rdd.count())

     }*/
    streamingContext.awaitTermination()
    //kinesisStreamingContext.awaitTermination()

  }

  class ConsumerThread(val consumer: KafkaConsumer[String, String]) extends Runnable {
    override def run(): Unit = {
      while (true) {
        val records: util.Map[String, ConsumerRecords[String, String]] = consumer.poll(100)
        val iterator = records.entrySet().iterator()
        while (iterator.hasNext) {
          val topicRecords = iterator.next()
          val it = topicRecords.getValue.records().iterator()
          while (it.hasNext) {
            val record = it.next()
            println(s"Topic: ${topicRecords.getKey}, key: ${record.key()}, value: ${record.value()}")
          }
        }
      }
    }
  }


  def processTotalTrafficData(filteredIotDataStream: DStream[VehicleData], spark: SparkSession, toAWS: Boolean): Unit = { // We need to get count of vehicle group by routeId and vehicleType
    val countDStreamPair = filteredIotDataStream.map((iot: VehicleData) => new Tuple2(new AggregateKey(iot.getRouteId, iot.getVehicleType), 1L)).reduceByKey((a: Long, b: Long) => a + b)
    val trafficDStream = countDStreamPair.map(totalTrafficDataFunc)
    // Map Cassandra table column
    val columnNameMappings = new util.HashMap[String, String]
    columnNameMappings.put("routeId", "routeid")
    columnNameMappings.put("vehicleType", "vehicletype")
    columnNameMappings.put("totalCount", "totalcount")
    columnNameMappings.put("timeStamp", "timestamp")
    columnNameMappings.put("recordDate", "recorddate")
    if ( toAWS ) {
      val datehour = new SimpleDateFormat("yyyy-MM-dd").format(new Date(System.currentTimeMillis())).toString
      val currHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY).toString
      val timestamp = Calendar.getInstance().get(Calendar.MINUTE) + "-" + Calendar.getInstance().get(Calendar.SECOND)
      val s3location = s"s3n://$awsAccessKeyId:$awsSecretKey@$s3Bucket/total-traffic/date=$datehour/hour=$currHour/$timestamp"
      trafficDStream.foreachRDD(rdd => {
        val sqlContext = spark.sqlContext
        rdd.map(x => x.routeId + "," + x.vehicleType + "," + x.totalCount + "," + x.recordDate + "," + x.timeStamp).coalesce(1).saveAsTextFile(s3location)
      })
    }
    // call CassandraStreamingJavaUtil function to save in DB
    else javaFunctions(trafficDStream).writerBuilder("traffickeyspace", "total_traffic", CassandraJavaUtil.mapToRow(classOf[TotalTrafficData], columnNameMappings)).saveToCassandra()
  }


  def processWindowTrafficData(filteredIotDataStream: DStream[VehicleData], spark: SparkSession, toAWS: Boolean): Unit = { // reduce by key and window (30 sec window and 10 sec slide).
    val countDStreamPair = filteredIotDataStream.map((iot: VehicleData) => (new AggregateKey(iot.getRouteId, iot.getVehicleType), 1L)).reduceByKeyAndWindow((a: Long, b: Long) => a + b, Durations.seconds(60), Durations.seconds(30))
    // Transform to dstream of TrafficData
    val trafficDStream = countDStreamPair.map(windowTrafficDataFunc)
    // Map Cassandra table column
    val columnNameMappings = new util.HashMap[String, String]
    columnNameMappings.put("routeId", "routeid")
    columnNameMappings.put("vehicleType", "vehicletype")
    columnNameMappings.put("totalCount", "totalcount")
    columnNameMappings.put("timeStamp", "timestamp")
    columnNameMappings.put("recordDate", "recorddate")

    val datehour = new SimpleDateFormat("yyyy-MM-dd").format(new Date(System.currentTimeMillis())).toString
    if ( toAWS ) {
      val datehour = new SimpleDateFormat("yyyy-MM-dd").format(new Date(System.currentTimeMillis())).toString
      val currHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY).toString
      val timestamp = Calendar.getInstance().get(Calendar.MINUTE) + "-" + Calendar.getInstance().get(Calendar.SECOND)
      val s3location = s"s3n://$awsAccessKeyId:$awsSecretKey@$s3Bucket/window-traffic/date=$datehour/hour=$currHour/$timestamp"
      trafficDStream.foreachRDD(rdd => {
        val sqlContext = spark.sqlContext
        rdd.map(x => x.routeId + "," + x.vehicleType + "," + x.totalCount + "," + x.recordDate + "," + x.timeStamp).coalesce(1).saveAsTextFile(s3location)
      })
    }
    else
    // call CassandraStreamingJavaUtil function to save in DB
      javaFunctions(trafficDStream).writerBuilder("traffickeyspace", "window_traffic", CassandraJavaUtil.mapToRow(classOf[WindowTrafficData], columnNameMappings)).saveToCassandra()
  }

  /**
    * Method to get the vehicles which are in radius of POI and their distance from POI.
    *
    * @param nonFilteredIotDataStream original IoT data stream
    * @param broadcastPOIValues       variable containing POI coordinates, route and vehicle types to monitor.
    */
  def processPOIData(nonFilteredIotDataStream: DStream[VehicleData], broadcastPOIValues: Broadcast[Tuple1[POIData /*, Array[String], Array[String]*/ ]], spark: SparkSession, toAWS: Boolean): Unit = { // Filter by routeId,vehicleType and in POI range
    val iotDataStreamFiltered = nonFilteredIotDataStream.filter((iot: VehicleData) => /*(broadcastPOIValues.value._2.exists(x => x == iot.routeId)) && (broadcastPOIValues.value._3.exists(x => iot.vehicleType == x)) &&*/ GeoDistanceCalculator.isInPOIRadius(iot.getLatitude.toDouble, iot.getLongitude.toDouble, broadcastPOIValues.value._1.getLatitude, broadcastPOIValues.value._1.getLongitude, broadcastPOIValues.value._1.getRadius))
    // pair with poi
    val poiDStreamPair = iotDataStreamFiltered.map((iot: VehicleData) => new Tuple2[VehicleData, POIData](iot, broadcastPOIValues.value._1))
    // Transform to dstream of POITrafficData
    val trafficDStream = poiDStreamPair.map(poiTrafficDataFunc)
    println(s"POi Count: ${trafficDStream.foreachRDD(rdd => rdd.count())}")
    val columnNameMappings = new util.HashMap[String, String]
    columnNameMappings.put("vehicleId", "vehicleid")
    columnNameMappings.put("distance", "distance")
    columnNameMappings.put("vehicleType", "vehicletype")
    columnNameMappings.put("timeStamp", "timestamp")
    if ( toAWS ) {
      val datehour = new SimpleDateFormat("yyyy-MM-dd").format(new Date(System.currentTimeMillis())).toString
      val currHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY).toString
      val timestamp = Calendar.getInstance().get(Calendar.MINUTE) + "-" + Calendar.getInstance().get(Calendar.SECOND)
      val s3location = s"s3n://$awsAccessKeyId:$awsSecretKey@$s3Bucket/poi-traffic/date=$datehour/hour=$currHour/$timestamp"
      trafficDStream.foreachRDD(rdd => {
        rdd.map(x => x.distance + "," + x.vehicleId + "," + x.vehicleType + "," + x.timeStamp).coalesce(1).saveAsTextFile(s3location)
      })
    }
    else
      javaFunctions(trafficDStream).writerBuilder("traffickeyspace", "poi_traffic", CassandraJavaUtil.mapToRow(classOf[POITrafficData], columnNameMappings)).withConstantTTL(120).saveToCassandra //keeping data for 2 minutes
    ()
  }


  //Function to create TotalTrafficData object from IoT data
  private val totalTrafficDataFunc = (tuple: Tuple2[AggregateKey, Long]) => {
    def foo(tuple: Tuple2[AggregateKey, Long]) = {
      logger.debug("Total Count : " + "key " + tuple._1.getRouteId + "-" + tuple._1.getVehicleType + " value " + tuple._2)
      val trafficData = new TotalTrafficData
      trafficData.setRouteId(tuple._1.getRouteId)
      trafficData.setVehicleType(tuple._1.getVehicleType)
      trafficData.setTotalCount(tuple._2)
      trafficData.setTimeStamp(new Date)
      trafficData.setRecordDate(new SimpleDateFormat("yyyy-MM-dd").format(new Date))
      trafficData
    }

    foo(tuple)
  }

  //Function to create WindowTrafficData object from IoT data
  private val windowTrafficDataFunc = (tuple: Tuple2[AggregateKey, Long]) => {
    def foo(tuple: Tuple2[AggregateKey, Long]) = {
      logger.debug("Window Count : " + "key " + tuple._1.getRouteId + "-" + tuple._1.getVehicleType + " value " + tuple._2)
      val trafficData = new WindowTrafficData
      trafficData.setRouteId(tuple._1.getRouteId)
      trafficData.setVehicleType(tuple._1.getVehicleType)
      trafficData.setTotalCount(tuple._2)
      trafficData.setTimeStamp(new Date)
      trafficData.setRecordDate(new SimpleDateFormat("yyyy-MM-dd").format(new Date))
      trafficData
    }

    foo(tuple)
  }

  //Function to create POITrafficData object from IoT data
  private val poiTrafficDataFunc = (tuple: Tuple2[VehicleData, POIData]) => {
    def foo(tuple: Tuple2[VehicleData, POIData]) = {
      val poiTraffic = new POITrafficData
      poiTraffic.setVehicleId(tuple._1.getVehicleId)
      poiTraffic.setVehicleType(tuple._1.getVehicleType)
      poiTraffic.setTimeStamp(new Date)
      val distance = GeoDistanceCalculator.getDistance(tuple._1.getLatitude.toDouble, tuple._1.getLongitude.toDouble, tuple._2.getLatitude, tuple._2.getLongitude)
      logger.debug("Distance for " + tuple._1.getLatitude + "," + tuple._1.getLongitude + "," + tuple._2.getLatitude + "," + tuple._2.getLongitude + " = " + distance)
      poiTraffic.setDistance(distance)
      poiTraffic
    }

    foo(tuple)
  }


  def processStreamTrafficData(filteredIotDataStream: DStream[VehicleData], spark: SparkSession, toAWS: Boolean): Unit = { // We need to get count of vehicle group by routeId and vehicleType
    // Map Cassandra table column
    val columnNameMappings = new util.HashMap[String, String]
    columnNameMappings.put("vehicleId", "vehicleid")
    columnNameMappings.put("routeId", "routeid")
    columnNameMappings.put("vehicleType", "vehicletype")
    columnNameMappings.put("timeStamp", "timestamp")
    columnNameMappings.put("latitude", "latitude")
    columnNameMappings.put("longitude", "longitude")
    columnNameMappings.put("fuelLevel", "fuellevel")
    columnNameMappings.put("speed", "speed")
    if ( toAWS ) {
      val datehour = new SimpleDateFormat("yyyy-MM-dd").format(new Date(System.currentTimeMillis())).toString
      val timestamp = Calendar.getInstance().get(Calendar.MINUTE) + "-" + Calendar.getInstance().get(Calendar.SECOND)
      val currHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY).toString
      val s3location = s"s3n://$awsAccessKeyId:$awsSecretKey@$s3Bucket/traffic-data/date=$datehour/$currHour/$timestamp"
      filteredIotDataStream.foreachRDD(rdd => {
        val sqlContext = spark.sqlContext
        val df = sqlContext.createDataFrame(rdd, classOf[VehicleData])
        rdd.map(x => x.routeId + "," + x.vehicleId + "," + x.vehicleType + "," + x.latitude + "," + x.longitude + "," + x.timestamp + "," + x.speed + "," + x.fuelLevel).coalesce(1).saveAsTextFile(s3location)
      })
    }
    else
    // call CassandraStreamingJavaUtil function to save in DB
      javaFunctions(filteredIotDataStream).writerBuilder("traffickeyspace", "vehicle_traffic", CassandraJavaUtil.mapToRow(classOf[VehicleData], columnNameMappings)).saveToCassandra()
  }


  //Funtion to check processed vehicles.
  private val processedVehicleFunc = (String: String, iot: Optional[VehicleData], state: State[Boolean]) => {
    def foo(String: String, iot: Optional[VehicleData], state: State[Boolean]) = {
      var vehicle = new Tuple2[VehicleData, Boolean](iot.get, false)
      if ( state.exists ) vehicle = new Tuple2[VehicleData, Boolean](iot.get, true)
      else state.update(true)
      vehicle
    }

    foo(String, iot, state)
  }

  def createKinesisClient(awsAccessKeyId: String, awsSecretKey: String, endPoint: String): AmazonKinesisClient = {
    val credentials = new BasicAWSCredentials(awsAccessKeyId, awsSecretKey)
    val kinesisClient = new AmazonKinesisClient(credentials)
    kinesisClient.setEndpoint(endPoint)
    println(s"key:$awsAccessKeyId, secret key: $awsSecretKey")
    //kinesisClient.setEndpoint("https://kinesis.us-west-2.amazonaws.com", "kinesis", Regions.US_WEST_2.toString)
    kinesisClient
  }

  def saveToS3(stream: DStream[VehicleData], s3location: String, spark: SparkSession): Unit = {
    stream.map(x => (new VehicleDataSerializer().serializetoJson(x)): String).foreachRDD(rdd => {
      val sqlContext = spark.sqlContext
      import sqlContext.implicits._
      rdd.toDF().write.format("json").mode(SaveMode.Append).save(s3location)
    })
  }

}
