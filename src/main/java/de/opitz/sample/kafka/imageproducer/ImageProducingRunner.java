package de.opitz.sample.kafka.imageproducer;

import de.opitz.sample.kafka.imageproducer.producers.ImageProducer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ImageProducingRunner implements CommandLineRunner {

    private final ImageHandler handler;
    private final ImageProducer imageProducer;

    public ImageProducingRunner(ImageHandler handler, @Value("${imageProducer:fileSystemImageProducer}") ImageProducer imageProducer) {
        this.handler = handler;
        this.imageProducer = imageProducer;
    }

    @Override
    public void run(String... args) {
        startProducingKafkaEvents();
    }

    private void startProducingKafkaEvents() {
        imageProducer
            .createImageStream()
            .forEach(imageOptional -> imageOptional.ifPresent(handler::imageCreatedWithNameAndData));
    }

}
