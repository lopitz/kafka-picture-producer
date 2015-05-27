package imageproducer.run;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricRegistry;

import imageproducer.ImageHandler;

/**
 * TODO (lopitz): Describe the purpose of this class.
 */
@SpringBootApplication
@EnableConfigurationProperties
@ComponentScan(basePackages = {"imageproducer.kafka", "imageproducer.run"})
public class Application {

    private static final Logger logger = getLogger(Application.class);

    @Value("${imagePath:.}")
    private String basePath;

    @Inject
    private ImageHandler handler;

    @Inject
    private MetricRegistry metrics;

    public static void main(String[] args) throws IOException {
        ApplicationContext ctx = SpringApplication.run(Application.class, args);
        Application application = ctx.getBean(Application.class);
        application.startReport();
        Path path = Paths.get(application.basePath);
        logger.info("Path to be scanned for images {}", path.toAbsolutePath().toString());
        Files.list(path).filter(element -> element.toString().endsWith("png")).forEach(element -> {
            try {
                application.handler.imageCreatedWithNameAndData(element.getFileName().toString(),
                        Files.readAllBytes(element));
            } catch (IOException e) {
                logger.warn("error reading file {}", element);
            }
        });
    }

    void startReport() {
        ConsoleReporter reporter = ConsoleReporter.forRegistry(metrics)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();
        reporter.start(10, TimeUnit.SECONDS);
    }

    @Bean
    public MetricRegistry createMetricRegistry() {
        return new MetricRegistry();
    }
}
