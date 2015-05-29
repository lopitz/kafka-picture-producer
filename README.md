kafka-picture-producer
======================

Simple sample [Kafka](https://kafka.apache.org) producer that reads PNG files from a given directory and stores them into a Kafka topic. 
The Kafka magic is done inside [kafka.KafkaImageProducer](src/main/java/imageproducer/kafka/KafkaImageProducer.java#L108).
Together with [kafka-picture-consumer](../../../kafka-picture-consumer) it demonstrates the different working modes Queuing and Publish/Subscribe of Kafka.

Start the [kafka-picture-consumer](../../../kafka-picture-consumer), the kafka-picture-producer, lay back and enjoy your movie. :smirk:

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

    java -jar kafka-picture-producer-0.1.0.jar [--imagePath] [--zookeeper.connect] [--kafka.topic] [--kafka.broker.host] [--kafka.partition.count] [--kafka.replication.count]

| argument name             | argument value                                  | default        |
| ------------------------- | ----------------------------------------------- | -------------- |
| --imagePath               | path to be used to read images (png) from       | .              |
| --zookeeper.connect       | zookeeper host (needed for topic creation only) | localhost:2181 |
| --kafka.topic             | topic the images are published to               | images         |
| --kafka.broker.host       | a Kafka broker to connect initially             | localhost:9092 |
| --kafka.partition.count   | numbers of partitions for the topic             | 1              |
| --kafka.replication.count | numbers of brokers this topic is replicated to  | 1              |
 
These command lines could also be set in the [application.properties](src/main/resources/application.properties)

About the application
---------------------
This project uses [Spring Boot](http://projects.spring.io/spring-boot/) as application framework and Gradle to build. The application was written against Kafka 0.8.2.1. 

Build
-----

This project uses [Gradle](https://gradle.org/) for building the application. Simply run

    ./gradlew assemble

to build the executable jar file. You then will find it under build/libs

Test setup
----------
It's using Kafka 0.8.2.1 that was built against Scala 2.10 so your first download is kafka_2.10-0.8.2.1.tgz from [https://kafka.apache.org/downloads.html]
Untar this file to a folder of your choice and change to the freshly created folder 

    kafka_2.10-0.8.2.1

From there start the following commands (please wait a bit after each command to give the whole system the opportunity to start up without error :smirk: ):

    bin/zookeeper-server-start.sh config/zookeeper.properties &
    bin/kafka-server-start.sh config/server.properties &
    
As the kafka-picture-producer automatically creates the needed topic, you're done. Now you can start the producer as described in the [Usage section](#usage).
