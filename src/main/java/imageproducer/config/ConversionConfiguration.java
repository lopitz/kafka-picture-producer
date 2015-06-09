package imageproducer.config;

import javax.inject.Inject;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ConversionServiceFactoryBean;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;

import com.google.common.collect.Sets;

import imageproducer.imageproducer.ImageProducer;

@Configuration
public class ConversionConfiguration {

    @Bean
    @Inject
    public ConversionService conversionService(ApplicationContext applicationContext) {
        ConversionServiceFactoryBean bean = new ConversionServiceFactoryBean();
        bean.setConverters(Sets.newHashSet(new StringToImageProducerConverter(applicationContext)));
        bean.afterPropertiesSet();
        return bean.getObject();
    }

    private class StringToImageProducerConverter implements Converter<String, ImageProducer> {


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
