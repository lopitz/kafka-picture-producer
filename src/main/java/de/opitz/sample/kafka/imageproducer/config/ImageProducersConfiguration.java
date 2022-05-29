package de.opitz.sample.kafka.imageproducer.config;

import java.io.*;
import java.net.URI;

import de.opitz.sample.kafka.imageproducer.producers.*;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.core.io.Resource;

import static org.slf4j.LoggerFactory.getLogger;

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
        return MemoryImageProducer
            .newBuilder()
            .withHeight(imageHeight)
            .withWidth(imageWidth)
            .withName(imageName)
            .withMaxImages(maxImages)
            .withIcon(loadIcon())
            .build();
    }

    private byte[] loadIcon() {
        if (iconResource.exists()) {
            try {
                return IOUtils.toByteArray(getIconUrl());
            } catch (IOException e) {
                logger.warn("Error reading image file", e);
                throw new UncheckedIOException(e);
            }
        }
        throw new IllegalArgumentException("image file not found \"%s\"".formatted(getIconUrl()));
    }

    private URI getIconUrl() {
        try {
            return iconResource.getURI();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
