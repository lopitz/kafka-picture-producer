kafka-picture-producer
======================

Simple sample [Kafka][kafka-homepage] producer that reads image files from a given directory and stores them into a Kafka topic. 
The Kafka magic is done inside [kafka.KafkaImageProducer](src/main/java/de/opitz/sample/kafka/imageproducer/kafka/KafkaImageProducer.java).
Together with [kafka-picture-consumer](../../../kafka-picture-consumer) it demonstrates the different working modes Queuing and Publish/Subscribe of Kafka.

Start this application and the [kafka-picture-consumer](../../../kafka-picture-consumer), the kafka-picture-producer, lay back and enjoy your movie. :smirk:

Preconditions
-------------
You'll need a running Kafka and Zookeeper instance. [You may find some information on creating the test setup at the end of this document.](#test-setup)
Furthermore, you are going to need a movie split into single frames (google for vlc scene video filter) or use the _memoryImageProducer_ (default)...

Usage
-----

In the easiest way you simply run

    java -jar kafka-picture-producer-0.1.0.jar 

(make sure you went to `target` folder before the execution)

but there are also some command line arguments

    java -jar kafka-picture-producer-0.1.0.jar [--imagePath] [--zookeeper.broker.host] [--kafka.topic] [--kafka.broker.host] [--kafka.partition.count] [--kafka.replication.count]

| argument name             | argument value                                                                                                                                                                                                         | default                 |
|---------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------|
| --zookeeper.broker.host   | zookeeper host (needed for topic creation only)                                                                                                                                                                        | localhost:2181          |
| --kafka.topic             | topic the images are published to                                                                                                                                                                                      | images                  |
| --kafka.broker.host       | a Kafka broker to connect initially                                                                                                                                                                                    | localhost:9092          |
| --kafka.partition.count   | numbers of partitions for the topic                                                                                                                                                                                    | 1                       |
| --kafka.replication.count | numbers of brokers this topic is replicated to                                                                                                                                                                         | 1                       |
| --imageProducer           | name of the image producer to use, <br>possible values are<ul><li>fileSystemImageProducer (to read images from file system)</li><li>memoryImageProducer (for creating images on the fly --imagePath ignored)</li></ul> | fileSystemImageProducer |
| --imagePath               | path to be used to read images (png) from, used by fileSystemImageProducer                                                                                                                                             | .                       |

These command lines could also be set in the [application.yml](src/main/resources/application.yml)

About the application
---------------------
This project uses [Spring Boot](http://projects.spring.io/spring-boot/) as application framework and Apache Maven to build. The application was written against 
Kafka 3.2. 

Build
-----

This project uses [Maven](https://maven.apache.org/) for building the application. Simply run

    ./mvnw clean install 

to build the executable jar file. You then will find it under target.

Test setup
----------
The easiest way to get started locally is to use docker. Please consult the [Kafka documentation][1] to learn more about the commands needed to run Kafka 
inside of docker.

Demo
----
The demo is meant to show what [Apache Kafka](https://kafka.apache.org) means with _log_ (so, ordered set of messages, stored regardless whether or how often they are consumed) explain the differences between Queuing and Publish/Subscribe modes of [Apache Kafka](https://kafka.apache.org). Start the [test setup](#test-setup).

Now start two consumer instances of the [kafka-image-consumer](../../../kafka-picture-consumer/blob/master/README.md#usage) with the same consumer id so e.g.

    java -Djava.awt.headless=false -jar kafka-picture-consumer-2.0.0.jar --kafka.group.id=1 &
    java -Djava.awt.headless=false -jar kafka-picture-consumer-2.0.0.jar --kafka.group.id=1 &
    
and another one with a different consumer id, like so:

    java -Djava.awt.headless=false -jar kafka-picture-consumer-2.0.0.jar --kafka.group.id=2 &

Arrange the windows so you can see them all.

Now start the [producer](#usage) and observe what happens on the consumer windows. 

_Please pay attention that the messages (the images in this demo) are only stored for 3 minutes so hurry_ :smirk:

So what will happen? You'll see the movie running (too fast for sure :smirk:) on two consumer windows, one will stay blank. As the consumer group id is displayed on the title of the windows you'll find that the consumer that stays blank has the same consumer id as another consumer. This is the Kafka Queuing mode with the edge case that there are less partitions than consumers.

This demo also shows the Publish/Subscribe mode. You'll find that consumers with different consumer group ids will pull the messages, so this works somehow like broadcasting.

What else do you see? The single movie frames are shown in the correct order. So first message that came in will be consumed first.

Now start an additional consumer (I hope you do so within the 3 minutes lifetime of the messages), like so

    java -Djava.awt.headless=false -jar kafka-picture-consumer-0.1.0.jar --kafka.group.id=3 &
    
You'll find the movie starts playing. This is so, because Kafka keeps the messages (until the configured timeout) regardless of how often the messages where consumed.

[kafka-homepage]: https://kafka.apache.org
[1]: https://developer.confluent.io/get-started/java/#kafka-setup
