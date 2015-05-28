kafka-picture-producer
======================

Simple sample Kafka producer that reads PNG files from a given directory and stores them into a Kafka topic. 
The Kafka magic is done inside [kafka.KafkaImageProducer](src/main/java/imageproducer/kafka/KafkaImageProducer.java#L108).
Start the [kafka-picture-consumer](../kafka-picture-consumer), the kafka-picture-producer, lay back and enjoy your movie. :smirk:

Usage
-----

In the easiest way you simply run

    java -jar kafka-picture-producer-0.1.0.jar 

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
