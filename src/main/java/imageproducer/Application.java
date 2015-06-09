package imageproducer;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricRegistry;

import imageproducer.imageproducer.ImageProducer;

@SpringBootApplication
public class Application {

    @Inject
    private ImageHandler handler;

    @Inject
    private MetricRegistry metrics;

    @Value("${imageProducer:fileSystemImageProducer}")
    private ImageProducer imageProducer;

    public static void main(String[] args) throws IOException {
        ApplicationContext ctx = SpringApplication.run(Application.class, args);
        Application application = ctx.getBean(Application.class);
        application.startReport();
        application.startProducingKafkaEvents();
    }

    void startReport() {
        ConsoleReporter reporter = ConsoleReporter.forRegistry(metrics)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();
        reporter.start(10, TimeUnit.SECONDS);
    }

    private void startProducingKafkaEvents() {
        imageProducer.createImageStream()
                .forEach(imageOptional -> imageOptional.ifPresent(handler::imageCreatedWithNameAndData));
    }

    @Bean
    public MetricRegistry createMetricRegistry() {
        return new MetricRegistry();
    }

}
