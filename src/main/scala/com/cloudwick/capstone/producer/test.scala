package com.cloudwick.capstone.producer

/**
  * Created by VenkataRamesh on 5/22/2017.
  */
class test {

/*
  Consumer:

  val credentials: AWSCredentials = new BasicAWSCredentials(MyAccessKeyID, MySecretKey)
  val credentialsProvider = new AWSCredentialsProvider {
    def getCredentials = credentials

    def refresh: Unit = {}
  }

  val kinesisClientLibConfiguration =
    new KinesisClientLibConfiguration("mytest", "mystream", credentialsProvider, "workerId")
      .withInitialPositionInStream(InitialPositionInStream.LATEST)
      .withRegionName(Regions.US_EAST_1.getName)

  val worker = new Worker(new KinesisSampleIRecordProcessorFactory, kinesisClientLibConfiguration);
  worker.run()




  Producer:

  val kinesisClient = new AmazonKinesisClient(new BasicAWSCredentials(awsAccessKeyId, awsSecretKey))
  kinesisClient.setEndpoint(kinesisEndpointUrl)*/


}
