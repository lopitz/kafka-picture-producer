package imageproducer.kafka;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.Properties;

import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;

import kafka.admin.AdminUtils;
import kafka.utils.ZKStringSerializer$;

public class ZookeeperClient {

    private static final Logger logger = getLogger(ZookeeperClient.class);

    private final ZkClient zkClient;
    private final String zookeeperHost;
    private final String topicName;
    private final int partitionCount;
    private final int replicationFactor;

    private ZookeeperClient(Builder builder) {
        partitionCount = builder.partitionCount;
        zookeeperHost = builder.zookeeperHost;
        topicName = builder.topicName;
        replicationFactor = builder.replicationFactor;
        zkClient = createZookeeperClient();
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    private ZkClient createZookeeperClient() {
        int sessionTimeoutMs = 10000;
        int connectionTimeoutMs = 10000;
        return new ZkClient(zookeeperHost, sessionTimeoutMs, connectionTimeoutMs, ZKStringSerializer$.MODULE$);
    }

    public void createTopicIfNecessary() {
        if (topicDoesNotExist(zkClient)) {
            createTopic(zkClient);
        }
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

    public static final class Builder {
        private int partitionCount;
        private String zookeeperHost;
        private String topicName;
        private int replicationFactor;

        private Builder() {
        }

        public Builder withPartitionCount(final int partitionCount) {
            this.partitionCount = partitionCount;
            return this;
        }

        public Builder withZookeeperHost(final String zookeeperHost) {
            this.zookeeperHost = zookeeperHost;
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

        public ZookeeperClient build() {
            return new ZookeeperClient(this);
        }
    }
}
