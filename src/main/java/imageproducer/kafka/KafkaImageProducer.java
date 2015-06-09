package imageproducer.kafka;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

import imageproducer.ImageHandler;
import imageproducer.imageproducer.Image;

public class KafkaImageProducer implements ImageHandler {

    private final static Logger logger = LoggerFactory.getLogger(KafkaImageProducer.class);

    private final Producer<String, byte[]> producer;
    private final Timer timer;
    private final MetricRegistry metricRegistry;
    private final String topicName;

    private KafkaImageProducer(Builder builder) {
        metricRegistry = builder.metricRegistry;
        producer = builder.producer;
        topicName = builder.topicName;
        timer = createMetrics();
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    private Timer createMetrics() {
        return metricRegistry.timer(MetricRegistry.name("outbound", "kafka", "publishing"));
    }

    @Override
    public void imageCreatedWithNameAndData(Image image) {
        Timer.Context context = timer.time();
        try {
            ProducerRecord<String, byte[]> record = new ProducerRecord<>(topicName, image.getName(), image.getPixels());
            producer.send(record);
        } finally {
            context.stop();
        }
        logger.info("Image {} sent to Kafka", image.getName());
    }

    public static final class Builder {
        private MetricRegistry metricRegistry;
        private Producer<String, byte[]> producer;
        private String topicName;

        private Builder() {
        }

        public Builder withMetricRegistry(final MetricRegistry metricRegistry) {
            this.metricRegistry = metricRegistry;
            return this;
        }

        public Builder withProducer(final Producer<String, byte[]> producer) {
            this.producer = producer;
            return this;
        }

        public Builder withTopicName(final String topicName) {
            this.topicName = topicName;
            return this;
        }

        public KafkaImageProducer build() {
            return new KafkaImageProducer(this);
        }
    }
}
