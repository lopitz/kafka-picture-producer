package imageproducer.config;

import java.util.Properties;

import javax.inject.Inject;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.codahale.metrics.MetricRegistry;

import imageproducer.ImageHandler;
import imageproducer.kafka.KafkaImageProducer;
import imageproducer.kafka.ZookeeperClient;

@Configuration
public class KafkaConfiguration {

    @Value("${kafka.broker.host:localhost:9092}")
    private String kafkaBrokerHost;

    @Value("${zookeeper.broker.host:localhost}")
    private String zookeeperHost;

    @Value("${kafka.partition.count:1}")
    private int partitionCount;

    @Value("${kafka.replication.count:1}")
    private int replicationFactor;

    @Value("${kafka.topic.name:images}")
    private String topicName;

    @Inject
    @Bean
    public ImageHandler createKafkaImageProducer(MetricRegistry metricRegistry) {
        return KafkaImageProducer.newBuilder()
                .withMetricRegistry(metricRegistry)
                .withProducer(createKafkaProducer())
                .withTopicName(topicName)
                .build();
    }

    private KafkaProducer<String, byte[]> createKafkaProducer() {
        Properties props = createKafkaProperties();
        return new KafkaProducer<>(props);
    }

    private Properties createKafkaProperties() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBrokerHost);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, "0");
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "KafkaImageProducer");
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 10);
        return props;
    }

    @Bean
    public ZookeeperClient createZookeeperClient() {
        ZookeeperClient client = ZookeeperClient.newBuilder()
                .withPartitionCount(partitionCount)
                .withReplicationFactor(replicationFactor)
                .withTopicName(topicName)
                .withZookeeperHost(zookeeperHost)
                .build();
        client.createTopicIfNecessary();
        return client;
    }


}
