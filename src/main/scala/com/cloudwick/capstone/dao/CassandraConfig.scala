package com.cloudwick.capstone.dao

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.{Bean, Configuration, PropertySource}
import org.springframework.core.env.Environment
import org.springframework.data.cassandra.config.CassandraClusterFactoryBean
import org.springframework.data.cassandra.config.java.AbstractCassandraConfiguration
import org.springframework.data.cassandra.mapping.BasicCassandraMappingContext
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories


/**
  * Spring bean configuration for Cassandra db.
  *
  * @author abaghel
  *
  */
@Configuration
@PropertySource(value = Array("classpath:cassandra-db.properties"))
@EnableCassandraRepositories(basePackages = Array("com.cloudwick.capstone.dao"))
class CassandraConfig extends AbstractCassandraConfiguration {
  @Autowired private var environment: Environment = null

  @Bean override def cluster: CassandraClusterFactoryBean = {
    val cluster = new CassandraClusterFactoryBean
    cluster.setContactPoints(environment.getProperty("com.iot.app.cassandra.host"))
    cluster.setPort(environment.getProperty("com.iot.app.cassandra.port").toInt)
    cluster
  }

  @Bean override def cassandraMapping = new BasicCassandraMappingContext

  @Bean override protected def getKeyspaceName: String = environment.getProperty("com.iot.app.cassandra.keyspace")
}
