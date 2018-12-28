kafka-picture-producer
======================

Simple sample [Kafka](https://kafka.apache.org) producer that reads image files from a given directory and stores them into a Kafka topic. 
The Kafka magic is done inside [kafka.KafkaImageProducer](src/main/java/imageproducer/kafka/KafkaImageProducer.java#L108).
Together with [kafka-picture-consumer](../../../kafka-picture-consumer) it demonstrates the different working modes Queuing and Publish/Subscribe of Kafka.

Start this application and the [kafka-picture-consumer](../../../kafka-picture-consumer), the kafka-picture-producer, lay back and enjoy your movie. :smirk:

Preconditions
-------------
You'll need a running Kafka and Zookeeper. [You may find some information on creating the test setup at the end of this document.](#test-setup)
Furthermore you gonna need a movie split into single frames (google for vlc scene video filter)...

Usage
-----

In the easiest way you simply run

    java -jar kafka-picture-producer-0.1.0.jar 

(make sure you went to build/libs folder before the execution)

but there are also some command line arguments

    java -jar kafka-picture-producer-0.1.0.jar [--imagePath] [--zookeeper.broker.host] [--kafka.topic] [--kafka.broker.host] [--kafka.partition.count] [--kafka.replication.count]

| argument name             | argument value                                  | default                 |
| ------------------------- | ----------------------------------------------- | ----------------------- |
| --zookeeper.broker.host   | zookeeper host (needed for topic creation only) | localhost:2181          |
| --kafka.topic             | topic the images are published to               | images                  |
| --kafka.broker.host       | a Kafka broker to connect initially             | localhost:9092          |
| --kafka.partition.count   | numbers of partitions for the topic             | 1                       |
| --kafka.replication.count | numbers of brokers this topic is replicated to  | 1                       |
| --imageProducer           | name of the image producer to use, <br>possible values are<ul><li>fileSystemImageProducer (to read images from file system)</li><li>memoryImageProducer (for creating images on the fly --imagePath ignored)</li></ul> | fileSystemImageProducer |
| --imagePath               | path to be used to read images (png) from, used by fileSystemImageProducer  | .

These command lines could also be set in the [application.properties](src/main/resources/application.properties)

About the application
---------------------
This project uses [Spring Boot](http://projects.spring.io/spring-boot/) as application framework and Gradle to build. The application was written against Kafka 0.8.2.1. 

Build
-----

This project uses [Gradle](https://gradle.org/) for building the application. Simply run

    ./gradlew assemble

to build the executable jar file. You then will find it under build/libs.

Test setup
----------
It's using Kafka 0.8.2.1 that was built against Scala 2.10 so your first download is kafka_2.10-0.8.2.1.tgz from [https://kafka.apache.org/downloads.html]
Untar this file to a folder of your choice and change to the freshly created folder 

    kafka_2.10-0.8.2.1

From there start the following commands (please wait a bit after each command to give the whole system the opportunity to start up without error :smirk: ):

    bin/zookeeper-server-start.sh config/zookeeper.properties &
    bin/kafka-server-start.sh config/server.properties &
    
As the kafka-picture-producer automatically creates the needed topic, you're done. Now you can start the producer as described in the [Usage section](#usage).

Demo
----
The demo is meant to show what [Apache Kafka](https://kafka.apache.org) means with _log_ (so, ordered set of messages, stored regardless whether or how often they are consumed) explain the differences between Queuing and Publish/Subscribe modes of [Apache Kafka](https://kafka.apache.org). Start the [test setup](#test-setup).

Now start two consumer instances of the [kafka-image-consumer](../../../kafka-picture-consumer/blob/master/README.md#usage) with the same consumer id so e.g.

    java -Djava.awt.headless=false -jar kafka-picture-consumer-0.1.0.jar --kafka.group.id=1 &
    java -Djava.awt.headless=false -jar kafka-picture-consumer-0.1.0.jar --kafka.group.id=1 &
    
and another one with a different consumer id, like so:

    java -Djava.awt.headless=false -jar kafka-picture-consumer-0.1.0.jar --kafka.group.id=2 &

Arrange the windows so you can see them all.

Now start the [producer](#usage) and observe what happens on the consumer windows. 

_Please pay attention that the messages (the images in this demo) are only stored for 3 minutes so hurry_ :smirk:

So what will happen? You'll see the movie running (too fast for sure :smirk:) on two consumer windows, one will stay blank. As the consumer group id is displayed on the title of the windows you'll find that the consumer that stays blank has the same consumer id as another consumer. This is the Kafka Queuing mode with the edge case that there are less partitions than consumers.

This demo also shows the Publish/Subscribe mode. You'll find that consumers with different consumer group ids will pull the messages, so this works somehow like broadcasting.

What else do you see? The single movie frames are shown in the correct order. So first message that came in will be consumed first.

Now start an additional consumer (I hope you do so within the 3 minutes lifetime of the messages), like so

    java -Djava.awt.headless=false -jar kafka-picture-consumer-0.1.0.jar --kafka.group.id=3 &
    
You'll find the movie starts playing. This is so, because Kafka keeps the messages (until the configured timeout) regardless of how often the messages where consumed.
