package com.cloudwick.capstone.dashboard

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.scheduling.annotation.EnableScheduling


/**
  * Created by VenkataRamesh on 5/21/2017.
  */


/**
  * Spring boot application class for Dashboard.
  */
@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = Array("com.cloudwick.capstone.dashboard", "com.cloudwick.capstone.dao"))
class StreamingDashboard {
}

object StreamingDashboard {
  def main(args: Array[String]): Unit = {
    SpringApplication.run(classOf[StreamingDashboard], args: _*)
  }


}
