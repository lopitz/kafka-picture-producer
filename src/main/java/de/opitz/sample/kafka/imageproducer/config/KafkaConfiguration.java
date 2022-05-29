package de.opitz.sample.kafka.imageproducer.config;

import java.util.Properties;

import de.opitz.sample.kafka.imageproducer.ImageHandler;
import de.opitz.sample.kafka.imageproducer.kafka.*;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;

@Configuration
public class KafkaConfiguration {

    @Value("${kafka.broker.host:localhost:9092}")
    private String kafkaBrokerHost;

    @Value("${kafka.partition.count:1}")
    private int partitionCount;

    @Value("${kafka.replication.count:1}")
    private int replicationFactor;

    @Value("${kafka.topic.name:images}")
    private String topicName;

    @Bean
    ImageHandler createKafkaImageProducer() {
        return KafkaImageProducer
            .newBuilder()
            .withProducer(createKafkaProducer())
            .withTopicName(topicName)
            .build();
    }

    private KafkaProducer<String, byte[]> createKafkaProducer() {
        return new KafkaProducer<>(createKafkaProperties());
    }

    private Properties createKafkaProperties() {
        var props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBrokerHost);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, "0");
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "KafkaImageProducer");
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 10);
        return props;
    }

    @Bean
    KafkaAdminClient createZookeeperClient(AdminClient adminClient) {
        var client = KafkaAdminClient
            .newBuilder()
            .withPartitionCount(partitionCount)
            .withReplicationFactor(replicationFactor)
            .withTopicName(topicName)
            .withAdminClient(adminClient)
            .build();
        client.createTopicIfNecessary();
        return client;
    }

    @Bean
    AdminClient createAdminClient() {
        return AdminClient.create(createKafkaProperties());
    }

}
