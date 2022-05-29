package de.opitz.sample.kafka.imageproducer.producers;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Stream;

import org.slf4j.Logger;

public class FileSystemImageProducer implements ImageProducer {

    private static final Logger logger = getLogger(FileSystemImageProducer.class);

    private final String basePath;

    public FileSystemImageProducer(String basePath) {
        this.basePath = basePath;
    }

    @Override
    public Stream<Optional<Image>> createImageStream() {
        var path = Paths.get(basePath);
        logger.info("Path to be scanned for images {}", path.toAbsolutePath());
        try {
            return Files
                .list(path)
                .filter(element -> element.toString().matches("(?i).*png|jpe?g|gif"))
                .map(this::loadImage);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Optional<Image> loadImage(Path element) {
        try {
            return Optional.of(new Image(element.toString(), Files.readAllBytes(element)));
        } catch (IOException e) {
            logger.warn("error reading file {}", element);
        }
        return Optional.empty();
    }
}
