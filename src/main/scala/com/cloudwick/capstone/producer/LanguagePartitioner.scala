package com.cloudwick.capstone.producer

import kafka.producer.Partitioner

/**
  * Created by VenkataRamesh on 5/18/2017.
  */
class LanguagePartitioner extends Partitioner {

  override def partition(key: Any, numPartitions: Int): Int = {
    return key.hashCode() % numPartitions
  }
}
