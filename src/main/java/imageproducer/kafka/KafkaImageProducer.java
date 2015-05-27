package imageproducer.kafka;

import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.I0Itec.zkclient.ZkClient;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

import imageproducer.ImageHandler;
import kafka.admin.AdminUtils;
import kafka.utils.ZKStringSerializer$;

@Named
public class KafkaImageProducer implements ImageHandler {

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
    private MetricRegistry metricRegistry;

    private Producer<String, byte[]> producer;

    private Logger logger = LoggerFactory.getLogger(KafkaImageProducer.class);
    private Timer timer;

    @PostConstruct
    private void initProducer() {
        createTopicIfNecessary();
        createKafkaProducer();
        createMetrics();
    }

    private void createTopicIfNecessary() {
        ZkClient zkClient = createZookeeperClient();
        if (topicDoesNotExist(zkClient)) {
            createTopic(zkClient);
        }
    }

    private ZkClient createZookeeperClient() {
        int sessionTimeoutMs = 10000;
        int connectionTimeoutMs = 10000;
        return new ZkClient(zookeeperHost, sessionTimeoutMs, connectionTimeoutMs, ZKStringSerializer$.MODULE$);
    }

    private void createMetrics() {
        timer = metricRegistry.timer(MetricRegistry.name("outbound", "kafka", "publishing"));
    }

    private boolean topicDoesNotExist(ZkClient zkClient) {
        return !AdminUtils.topicExists(zkClient, topicName);
    }

    private void createTopic(ZkClient zkClient) {
        logger.info("Created topic, {}", topicName);
        Properties properties = new Properties();
        properties.put("delete.retention.ms", "180000");
        AdminUtils.createTopic(zkClient, topicName, partitionCount, replicationFactor, properties);
    }

    private void createKafkaProducer() {
        Properties props = createKafkaProperties();
        producer = new KafkaProducer<>(props);
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

    @Override
    public void imageCreatedWithNameAndData(String name, byte[] rawData) {
        Timer.Context context = timer.time();
        try {
            ProducerRecord<String, byte[]> record = new ProducerRecord<>(topicName, name, rawData);
            producer.send(record);
        } finally {
            context.stop();
        }
        logger.info("Image {} sent to Kafka", name);
    }
}
