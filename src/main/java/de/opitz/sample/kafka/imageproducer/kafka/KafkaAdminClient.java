package de.opitz.sample.kafka.imageproducer.kafka;

import java.util.*;

import org.apache.kafka.clients.admin.*;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class KafkaAdminClient {

    private static final Logger logger = getLogger(KafkaAdminClient.class);

    private final AdminClient adminClient;
    private final String topicName;
    private final int partitionCount;
    private final int replicationFactor;

    private KafkaAdminClient(Builder builder) {
        partitionCount = builder.partitionCount;
        topicName = builder.topicName;
        replicationFactor = builder.replicationFactor;
        adminClient = builder.adminClient;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public void createTopicIfNecessary() {
        if (topicDoesNotExist()) {
            createTopic();
        }
    }

    private boolean topicDoesNotExist() {
        try {
            return adminClient
                .listTopics()
                .names()
                .get()
                .stream()
                .anyMatch(name -> Objects.equals(name, topicName));
        } catch (Exception e) {
            throw new IllegalStateException("Can't fetch topic names", e);
        }
    }

    private void createTopic() {
        logger.info("Created topic, {}", topicName);
        adminClient.createTopics(List.of(new NewTopic(topicName, partitionCount, (short) replicationFactor)));
    }

    public static final class Builder {
        private int partitionCount;
        private String topicName;
        private int replicationFactor;
        private AdminClient adminClient;

        private Builder() {
        }

        public Builder withPartitionCount(final int partitionCount) {
            this.partitionCount = partitionCount;
            return this;
        }

        public Builder withTopicName(final String topicName) {
            this.topicName = topicName;
            return this;
        }

        public Builder withReplicationFactor(final int replicationFactor) {
            this.replicationFactor = replicationFactor;
            return this;
        }

        public Builder withAdminClient(AdminClient adminClient) {
            this.adminClient = adminClient;
            return this;
        }

        public KafkaAdminClient build() {
            return new KafkaAdminClient(this);
        }
    }
}
