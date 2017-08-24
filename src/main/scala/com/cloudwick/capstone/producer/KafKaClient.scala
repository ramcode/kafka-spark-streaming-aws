package com.cloudwick.capstone.producer

import java.util.{Date, Properties, Random, UUID}

import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.kinesis.AmazonKinesisClient
import com.amazonaws.services.kinesis.model.PutRecordRequest
import com.cloudwick.capstone.dao.entity.VehicleData
import com.google.gson.{Gson, JsonElement}
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.kafka.clients.producer._
import org.apache.log4j.Logger

import scala.collection.mutable
import scala.io.Source
import scala.util.parsing.json.{JSON, JSONFormat, JSONObject}

/**
  * Created by VenkataRamesh on 5/17/2017.
  */
object KafKaClient {

  val latitudes = Source.fromInputStream(getClass.getResourceAsStream("/latitude.txt"))
  val longitudes = Source.fromInputStream(getClass.getResourceAsStream("/longitude.txt"))
  val latitudeList = latitudes.getLines().filter(x => x != null && x.length > 0).toList
  val longitudeList = longitudes.getLines().filter(x => x != null && x.length > 0).toList

  val kafkaConfig:Config = ConfigFactory.load("kafka-broker")
  val cassandraConfig:Config = ConfigFactory.load("cassandra-db")
  val awsConfig:Config = ConfigFactory.load("aws-config")

  val logger: Logger = Logger.getLogger("com.cloudwick.capstone.producer.KafKaClient")
  val TOPIC = kafkaConfig.getString("kafka-topic")

  case class GeoCoding(val countryCode2: String, val city: String, val latitude: String, val country: String, val longitude: String, val stateProvince: String, val countryCode3: String)


  case class RCMessage(val isRobot: String, val channel: String, val timestamp: String,
                       val url: String, val isUnpatrolled: String, val page: String,
                       val wikipedia: String, val wikipediaURL: String, val comment: String,
                       val userURL: String, val pageURL: String, val delta: String, val flag: String,
                       val isNewPage: String, val isAnonymous: String, val geocoding: String, val user: String, val namespace: String
                      )


  /*def intiStreamParams: Properties = {
    val params = new Properties
    params.put("wikiRCHost", "54.187.66.67")
    params.put("wikiRCPort", "9002")
    params
  }*/

  def main(args: Array[String]): Unit = {
   /* val wikiRCHost = "54.213.33.240"
    val wikiRCPort = 9002
    val wikiRCAddr = InetAddress.getByName(wikiRCHost)
    val socket = new Socket(wikiRCAddr, wikiRCPort)*/
    val brokers = kafkaConfig.getString("bootstrap.servers")
    val awsAccessKeyId = awsConfig.getString("awsAccessKeyId")
    val awsSecretKey = awsConfig.getString("awsSecretKey")
    val kinesisStream = awsConfig.getString("kinesisStream")
    val producer = createProducer(brokerConfigParams(brokers))
    val stream = new Runnable {
      def run(): Unit = {
        generateIoTEvent(producer, TOPIC)
        //generateIoTEventKinesis(kinesisClient(awsAccessKeyId, awsSecretKey), kinesisStream)
      }
    }
    println("starting streaming")
    new Thread(stream).start()
  }

  def brokerConfigParams(brokers: String): Properties = {
    val props = new Properties
    props.put("bootstrap.servers", brokers)
    props.put("acks", "all")
    props.put("retries", "0")
    props.put("batch.size", "16384")
    props.put("linger.ms", "1")
    props.put("buffer.memory", "33554432")
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    //props.put("serializer.class", "com.cloudwick.capstone.dao.entity.VehicleData")
    props.put("value.serializer", "com.cloudwick.capstone.producer.VehicleDataSerializer")
    props.put("partitioner.class", "com.cloudwick.capstone.producer.LanguagePartitioner")
    props
  }

  def createProducer(configProps: Properties): KafkaProducer[String, VehicleData] = {
    new KafkaProducer[String, VehicleData](configProps)
  }

  def sendMessage(producer: KafkaProducer[String, String], rcMessage: String) = {
    var map = JSON.parseFull(rcMessage).get.asInstanceOf[Map[String, Any]]
    val gson = new Gson()
    val jsonElement = gson.fromJson(rcMessage, classOf[JsonElement])
    var jsonObject = jsonElement.getAsJsonObject
    jsonObject.addProperty("id", UUID.randomUUID().toString)
    //println(jsonObject.toString)
    val language = map("channel").toString.split("\\.")(0).substring(1)
    val page = map("page")
    val messageToSend = jsonObject.toString
    producer.send(new ProducerRecord[String, String](TOPIC, language, messageToSend), new Callback {
      override def onCompletion(metadata: RecordMetadata, exception: Exception): Unit = {
        logger.info(s"Message: $messageToSend, Kafka Topic: ${metadata.topic()}, Partition: ${metadata.partition()}")
      }
    })
  }

  def parseJSON(json: String): RCMessage = {
    println(json)
    var jsonMap = Map[String, Any]()
    val messageMap = JSON.parseFull(json).get.asInstanceOf[Map[String, Any]]
    messageMap.foreach(x => x match {
      case a: (String, Any) => jsonMap += (a._1 -> a._2)
      case b: (String, Map[String, Any]) => jsonMap += (b._1 -> JSON.parseFull(JSONObject(b._2).toString(JSONFormat.defaultFormatter)).asInstanceOf[GeoCoding])
      //map += (x._1 -> x._2.asInstanceOf[Map[String,String]].asInstanceOf[GeoCoding])
      case _ =>
    })


    //val language = rCMessage.channel.split(".")(0).substring(1)
    println(language)
    val rCMessage: RCMessage = JSON.parseFull(JSONObject(jsonMap).toString(JSONFormat.defaultFormatter)).asInstanceOf[RCMessage]
    rCMessage
  }

  @throws[InterruptedException]
  private def generateIoTEvent(producer: KafkaProducer[String, VehicleData], topic: String): Unit = {
    val routeList: scala.List[String] = List[String]("I-5", "I-5W", "I-8", "I-10", "I-15", "I-15E", "I-40", "I-80")
    val vehicleTypeList: scala.List[String] = List[String]("Large Truck", "Small Truck", "Car", "Bus", "Taxi", "Bike", "Dodge")
    val rand: Random = new Random
    logger.info("Sending events")
    // generate event in loop
    while ( {
      true
    }) {
      var eventList: mutable.MutableList[VehicleData] = new mutable.MutableList[VehicleData]
      var i: Int = 0
      while ( {
        i < 100
      }) { // create 100 vehicles
        val vehicleId: String = UUID.randomUUID.toString
        val vehicleType: String = vehicleTypeList(rand.nextInt(7))
        val routeId: String = routeList(rand.nextInt(8))
        val timestamp: Date = new Date
        val speed: Double = rand.nextInt(100 - 20) + 20
        // random speed between 20 to 100
        val fuelLevel: Double = rand.nextInt(40 - 10) + 10
        var j: Int = 0
        val latitude: String = latitudeList(rand.nextInt(latitudeList.length))
        val longitude: String = longitudeList(rand.nextInt(latitudeList.length))
        val event: VehicleData = new VehicleData(vehicleId, vehicleType, routeId, latitude, longitude, timestamp, speed, fuelLevel)
        eventList += (event)
        i += 1
      }
      scala.util.Random.shuffle(eventList) // shuffle for random events
      for (event <- eventList) {
        producer.send(new ProducerRecord[String, VehicleData](TOPIC, event.vehicleId, event), new Callback {
          override def onCompletion(metadata: RecordMetadata, exception: Exception): Unit = {
            logger.info(s"Message: $event, Kafka Topic: ${metadata.topic()}, Partition: ${metadata.partition()}")
          }
        })
        Thread.sleep(100) //random delay of 1 to 3 seconds
      }
    }
  }


  @throws[InterruptedException]
  private def generateIoTEventKinesis(producer: AmazonKinesisClient, topic: String): Unit = {
    val routeList: scala.List[String] = List[String]("I-5", "I-5W", "I-8", "I-10", "I-15", "I-15E", "I-40", "I-80")
    val vehicleTypeList: scala.List[String] = List[String]("Large Truck", "Small Truck", "Car", "Bus", "Taxi", "Bike", "Dodge")
    val rand: Random = new Random
    logger.info("Sending events")
    // generate event in loop
    while ( {
      true
    }) {
      var eventList: mutable.MutableList[VehicleData] = new mutable.MutableList[VehicleData]
      var i: Int = 0
      while ( {
        i < 100
      }) { // create 100 vehicles
        val vehicleId: String = UUID.randomUUID.toString
        val vehicleType: String = vehicleTypeList(rand.nextInt(7))
        val routeId: String = routeList(rand.nextInt(8))
        val timestamp: Date = new Date
        val speed: Double = rand.nextInt(100 - 20) + 20
        // random speed between 20 to 100
        val fuelLevel: Double = rand.nextInt(40 - 10) + 10
        var j: Int = 0
        val latitude: String = latitudeList(rand.nextInt(latitudeList.length))
        val longitude: String = longitudeList(rand.nextInt(latitudeList.length))
        val event: VehicleData = new VehicleData(vehicleId, vehicleType, routeId, latitude, longitude, timestamp, speed, fuelLevel)
        eventList += (event)
        i += 1
      }
      scala.util.Random.shuffle(eventList) // shuffle for random events
      for (event <- eventList) {
        import java.nio.ByteBuffer

        import com.amazonaws.AmazonClientException
        logger.info("Putting vehicle data to kinesis stream: " + event.toString)
        val putRecord = new PutRecordRequest
        putRecord.setStreamName(topic)
        val bytes = new VehicleDataSerializer().serialize(topic, event)
        // We use the ticker symbol as the partition key, explained in the Supplemental Information section below.
        putRecord.setPartitionKey(event.getVehicleId)
        putRecord.setData(ByteBuffer.wrap(bytes))
        try
          producer.putRecord(putRecord)
        catch {
          case ex: AmazonClientException =>
            logger.warn("Error sending record to Amazon Kinesis.", ex)
        }
        Thread.sleep(100) //random delay of 1 to 3 seconds
      }
    }
  }


  private def getCoordinates(routeId: String) = {
    val rand = new Random()
    var latPrefix = 0
    var longPrefix = -0
    if (routeId == "I-10") {
      latPrefix = 33
      longPrefix = -96
    }
    else if (routeId == "I-40") {
      latPrefix = 34
      longPrefix = -97
    }
    else if (routeId == "I-80") {
      latPrefix = 35
      longPrefix = -98
    }
    val lati = latPrefix + rand.nextFloat
    val longi = longPrefix + rand.nextFloat
    lati + "," + longi
  }

  def kinesisClient(awsAccessKeyId: String, awsSecretKey: String): AmazonKinesisClient = {
    val credentials = new BasicAWSCredentials(awsAccessKeyId, awsSecretKey)
    val kinesisClient = new AmazonKinesisClient(credentials)
    println(s"key:$awsAccessKeyId, secret key: $awsSecretKey")
    //kinesisClient.setEndpoint("https://kinesis.us-west-2.amazonaws.com", "kinesis", Regions.US_WEST_2.toString)
    kinesisClient
  }
}