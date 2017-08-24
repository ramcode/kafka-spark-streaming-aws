name := "Capstone-KafkaStreaming"

version := "1.0"

scalaVersion := "2.11.8"

dependencyOverrides += "com.fasterxml.jackson.core" % "jackson-core" % "2.8.7"
dependencyOverrides += "com.fasterxml.jackson.core" % "jackson-databind" % "2.8.7"
dependencyOverrides += "com.fasterxml.jackson.module" % "jackson-module-scala_2.11" % "2.8.7"

// https://mvnrepository.com/artifact/org.scala-lang/scala-library
libraryDependencies += "org.scala-lang" % "scala-library" % "2.11.8" exclude("org.slf4j","slf4j-log4j12")


// https://mvnrepository.com/artifact/org.apache.kafka/kafka_2.10
libraryDependencies += "org.apache.kafka" % "kafka_2.11" % "0.8.2.1" exclude("org.slf4j","slf4j-log4j12")


// https://mvnrepository.com/artifact/org.apache.kafka/kafka-clients
libraryDependencies += "org.apache.kafka" % "kafka-clients" % "0.8.2.1" exclude("org.slf4j","slf4j-log4j12")

// https://mvnrepository.com/artifact/org.apache.spark/spark-core_2.10
libraryDependencies += "org.apache.spark" % "spark-core_2.11" % "2.1.1" exclude("org.slf4j","slf4j-log4j12")


// https://mvnrepository.com/artifact/org.apache.spark/spark-streaming_2.10
libraryDependencies += "org.apache.spark" % "spark-streaming_2.11" % "2.1.1" exclude("org.slf4j","slf4j-log4j12")

// https://mvnrepository.com/artifact/org.apache.spark/spark-sql_2.10
libraryDependencies += "org.apache.spark" % "spark-sql_2.11" % "2.1.1" exclude("org.slf4j","slf4j-log4j12")


// https://mvnrepository.com/artifact/org.apache.spark/spark-streaming-kafka_2.10
libraryDependencies += "org.apache.spark" % "spark-streaming-kafka_2.11" % "1.6.3" exclude("org.slf4j","slf4j-log4j12")



// https://mvnrepository.com/artifact/org.apache.spark/spark-mllib_2.10
libraryDependencies += "org.apache.spark" % "spark-mllib_2.11" % "2.1.1" exclude("org.slf4j","slf4j-log4j12")

// https://mvnrepository.com/artifact/com.datastax.spark/spark-cassandra-connector-unshaded_2.10
libraryDependencies += "com.datastax.spark" % "spark-cassandra-connector-unshaded_2.11" % "2.0.2" exclude("org.slf4j","slf4j-log4j12")

// https://mvnrepository.com/artifact/org.apache.spark/spark-hive_2.11
libraryDependencies += "org.apache.spark" % "spark-hive_2.11" % "2.1.1" exclude("org.slf4j","slf4j-log4j12")


// https://mvnrepository.com/artifact/org.springframework.boot/spring-boot
libraryDependencies += "org.springframework.boot" % "spring-boot" % "1.5.3.RELEASE" exclude("org.slf4j","slf4j-log4j12")


// https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-websocket
libraryDependencies += "org.springframework.boot" % "spring-boot-starter-websocket" % "1.5.3.RELEASE" exclude("org.slf4j","slf4j-log4j12")

//https://mvnrepository.com/artifact/com.google.code.gson/gson
libraryDependencies += "com.google.code.gson" % "gson" % "2.8.0" exclude("org.slf4j","slf4j-log4j12")

// https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-data-cassandra
libraryDependencies += "org.springframework.boot" % "spring-boot-starter-data-cassandra" % "1.5.3.RELEASE"

// https://mvnrepository.com/artifact/com.amazonaws/amazon-kinesis-client
libraryDependencies += "com.amazonaws" % "amazon-kinesis-client" % "1.6.1"

// https://mvnrepository.com/artifact/org.apache.spark/spark-streaming-kinesis-asl_2.11
libraryDependencies += "org.apache.spark" % "spark-streaming-kinesis-asl_2.11" % "2.1.1"

// https://mvnrepository.com/artifact/com.typesafe/config
libraryDependencies += "com.typesafe" % "config" % "1.3.1"









/*// https://mvnrepository.com/artifact/com.typesafe.play/play-json_2.11
libraryDependencies += "com.typesafe.play" % "play-json_2.11" % "2.5.14"

libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.4" */




        