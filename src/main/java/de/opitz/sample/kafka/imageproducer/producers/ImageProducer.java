package de.opitz.sample.kafka.imageproducer.producers;

import java.util.Optional;
import java.util.stream.Stream;

public interface ImageProducer {
    Stream<Optional<Image>> createImageStream();
}
