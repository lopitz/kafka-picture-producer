package de.opitz.sample.kafka.imageproducer.config;

import java.util.Set;

import javax.inject.Inject;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ConversionServiceFactoryBean;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;

import de.opitz.sample.kafka.imageproducer.producers.ImageProducer;

@Configuration
public class ConversionConfiguration {

    @Bean
    @Inject
    public ConversionService conversionService(ApplicationContext applicationContext) {
        var bean = new ConversionServiceFactoryBean();
        bean.setConverters(Set.of(new StringToImageProducerConverter(applicationContext)));
        bean.afterPropertiesSet();
        return bean.getObject();
    }

    private static class StringToImageProducerConverter implements Converter<String, ImageProducer> {

        private final ApplicationContext applicationContext;

        public StringToImageProducerConverter(ApplicationContext applicationContext) {
            this.applicationContext = applicationContext;
        }

        @Override
        public ImageProducer convert(String source) {
            try {
                return applicationContext.getBean(source, ImageProducer.class);
            } catch (Exception e) {
                throw new IllegalStateException(
                        String.format("Cannot convert %s to ImageProducer. %s", source, e.getMessage()));
            }
        }
    }
}
