package de.opitz.sample.kafka.imageproducer;

import de.opitz.sample.kafka.imageproducer.producers.Image;

@FunctionalInterface
public interface ImageHandler {
    void imageCreatedWithNameAndData(Image image);
}
