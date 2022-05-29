package de.opitz.sample.kafka.imageproducer.kafka;

import de.opitz.sample.kafka.imageproducer.ImageHandler;
import de.opitz.sample.kafka.imageproducer.producers.Image;
import org.apache.kafka.clients.producer.*;
import org.slf4j.*;

public class KafkaImageProducer implements ImageHandler {

    private static final Logger logger = LoggerFactory.getLogger(KafkaImageProducer.class);

    private final Producer<String, byte[]> producer;
    private final String topicName;

    private KafkaImageProducer(Builder builder) {
        producer = builder.producer;
        topicName = builder.topicName;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @Override
    public void imageCreatedWithNameAndData(Image image) {
        var producerRecord = new ProducerRecord<>(topicName, image.name(), image.pixels());
        producer.send(producerRecord);
        logger.info("Image {} sent to Kafka", image.name());
    }

    public static final class Builder {
        private Producer<String, byte[]> producer;
        private String topicName;

        private Builder() {
        }

        public Builder withProducer(Producer<String, byte[]> producer) {
            this.producer = producer;
            return this;
        }

        public Builder withTopicName(String topicName) {
            this.topicName = topicName;
            return this;
        }

        public KafkaImageProducer build() {
            return new KafkaImageProducer(this);
        }
    }
}
