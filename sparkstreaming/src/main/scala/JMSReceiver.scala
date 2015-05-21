/**
 * DataStax Academy Sample Application
 *
 * Copyright 2013 DataStax
 *
 */

import java.util.UUID
import javax.jms._
import javax.naming.InitialContext
import org.apache.activemq.ActiveMQConnectionFactory
import org.apache.spark.storage.StorageLevel
import org.joda.time.DateTime
import com.datastax.spark.connector.streaming._
import org.apache.spark._
import org.apache.spark.streaming._
import org.apache.spark.streaming.receiver.Receiver
import org.apache.spark.streaming.StreamingContext._
import com.datastax.spark.connector.streaming._
import com.datastax.spark.connector.cql.CassandraConnector


class JMSReceiver(topic_name: String, connection_url: String) extends Receiver[String](StorageLevel.MEMORY_AND_DISK_2) with Logging
{

//  var connection: Connection
//  var consumer: MessageConsumer
//  var session: Session
//  var destination: Destination

  override def onStart(): Unit = {
    // Create a ConnectionFactory
    val connectionFactory = new ActiveMQConnectionFactory(connection_url)

    // Create a Connection
    val connection = connectionFactory.createConnection()
    connection.start()

    connection.setExceptionListener(new ExceptionPrinter)

    // Create a Session
    val session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)

    // Create the destination (Topic or Queue)
    val topic = session.createTopic(topic_name)

    // Create a MessageConsumer from the Session to the Topic or Queue
    val consumer = session.createConsumer(topic)

    consumer.setMessageListener(new MessageListener {
      override def onMessage(message: Message): Unit = {
        val str = message match {
          case textmsg : TextMessage => textmsg.getText
          case _ => "Invalid Message"
        }

        store(str)

      }
    })
  }


  override def onStop(): Unit = {

  }

}

class ExceptionPrinter extends ExceptionListener {
 def onException (ex: JMSException) {
    System.out.println("JMS Exception occured.  Shutting down client." + ex);
  }
}
