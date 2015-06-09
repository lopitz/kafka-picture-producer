package imageproducer.imageproducer;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Stream;

import org.slf4j.Logger;

import com.google.common.base.Throwables;

public class FileSystemImageProducer implements ImageProducer {

    private static final Logger logger = getLogger(FileSystemImageProducer.class);

    private final String basePath;

    public FileSystemImageProducer(String basePath) {
        this.basePath = basePath;
    }

    @Override
    public Stream<Optional<Image>> createImageStream() {
        Path path = Paths.get(basePath);
        logger.info("Path to be scanned for images {}", path.toAbsolutePath().toString());
        try {
            return Files.list(path).filter(element -> element.toString().matches("(?i).*png|je?pg|gif")).map(element -> {
                Image result = null;
                try {
                    result = new Image(element.toString(), Files.readAllBytes(element));
                } catch (IOException e) {
                    logger.warn("error reading file {}", element);
                }
                return Optional.ofNullable(result);
            });
        } catch (IOException e) {
            Throwables.propagate(e);
        }
        return null;
    }
}
