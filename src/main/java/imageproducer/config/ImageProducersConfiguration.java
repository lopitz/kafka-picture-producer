package imageproducer.config;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import imageproducer.imageproducer.FileSystemImageProducer;
import imageproducer.imageproducer.ImageProducer;
import imageproducer.imageproducer.MemoryImageProducer;

@Configuration
public class ImageProducersConfiguration {

    private static final Logger logger = getLogger(ImageProducersConfiguration.class);

    @Value("${imagePath:.}")
    private String basePath;

    @Value("${imageHeight:300}")
    private int imageHeight;

    @Value("${imageWidth:400}")
    private int imageWidth;

    @Value("${kafka.topic.name:images}")
    private String imageName;

    @Value("${maxImages:2000}")
    private long maxImages;

    @Value("classpath:kafka_logo.png")
    private Resource iconResource;

    @Bean
    public ImageProducer fileSystemImageProducer() {
        return new FileSystemImageProducer(basePath);
    }

    @Bean
    public ImageProducer memoryImageProducer() {
        return MemoryImageProducer.newBuilder()
                .withHeight(imageHeight)
                .withWidth(imageWidth)
                .withName(imageName)
                .withMaxImages(maxImages)
                .withIcon(loadIcon())
                .build();
    }

    private byte[] loadIcon() {
        byte[] result = new byte[]{};
        if (iconResource.exists()) {
            byte[] buffer = new byte[1024];
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try (InputStream stream = iconResource.getURL().openStream()) {
                int available = 0;
                while ((available = stream.available()) > 0) {
                    int read = stream.read(buffer, 0, Math.min(buffer.length, available));
                    bytes.write(buffer, 0, read);
                }
                bytes.flush();
                result = bytes.toByteArray();
                bytes.close();
            } catch (IOException e) {
                logger.warn("Error reading Kafka logo file", e);
            }
        }
        return result;
    }

}
