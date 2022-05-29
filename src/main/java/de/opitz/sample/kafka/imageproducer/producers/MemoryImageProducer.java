package de.opitz.sample.kafka.imageproducer.producers;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.*;

import javax.imageio.ImageIO;

import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class MemoryImageProducer implements ImageProducer {

    public static final Font BIG_FONT = new Font("Arial", Font.BOLD, 20);

    private static final Logger logger = getLogger(MemoryImageProducer.class);
    private static final byte[] EMPTY_BYTE_ARRAY = {};

    private final int width;
    private final int height;
    private final String name;
    private final long maxImages;

    private final byte[] icon;
    private final BufferedImage background;
    private long imageNumber;

    private MemoryImageProducer(Builder builder) {
        height = builder.height;
        width = builder.width;
        name = builder.name;
        maxImages = builder.maxImages;
        icon = builder.icon;
        background = createBackgroundImage();
    }

    private BufferedImage createBackgroundImage() {
        var image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        var graphics = prepareGraphicsContext(image);
        drawKafkaBackground(graphics);
        drawImageName(graphics);
        return image;
    }

    private Graphics2D prepareGraphicsContext(BufferedImage image) {
        var graphics = image.createGraphics();
        graphics.setBackground(Color.WHITE);
        var rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        rh.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHints(rh);
        graphics.setColor(Color.white);
        graphics.fillRect(0, 0, width, height);
        return graphics;
    }

    private void drawKafkaBackground(Graphics2D graphics) {
        if (icon != null) {
            loadAndDrawImage(graphics, icon);
        }
    }

    private void loadAndDrawImage(Graphics2D graphics, byte[] imageData) {
        try {
            var iconImage = ImageIO.read(new ByteArrayInputStream(imageData));
            if (iconImage != null) {
                var kafkaHeight = iconImage.getHeight();
                graphics.drawImage(iconImage, 30, (height - kafkaHeight) / 2, null);
            }
        } catch (IOException e) {
            logger.warn("Image data of Kafka icon could not be interpreted as image", e);
        }
    }

    private void drawImageName(Graphics2D graphics) {
        graphics.setFont(BIG_FONT);
        graphics.setColor(Color.BLACK);
        var bigFontMetrics = graphics.getFontMetrics(BIG_FONT);
        var textWidth = bigFontMetrics.stringWidth(name);
        graphics.drawString(name, (width - textWidth) / 2F, bigFontMetrics.getHeight() * 2F);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @Override
    public Stream<Optional<Image>> createImageStream() {
        var spliterator =
            new Spliterators.AbstractSpliterator<Optional<Image>>(maxImages, Spliterator.SIZED) {
                @Override
                public boolean tryAdvance(Consumer<? super Optional<Image>> action) {
                    if (imageNumber < maxImages) {
                        action.accept(Optional.of(createImage(imageNumber++)));
                        return true;
                    }
                    return false;
                }
            };
        return StreamSupport.stream(spliterator, false);
    }

    private Image createImage(long currentImageNumber) {
        var image = drawImage(currentImageNumber);
        var bytes = encodeImage(image);
        return new Image(String.format("%s - %s", name, currentImageNumber), bytes);
    }

    private BufferedImage drawImage(long currentImageNumber) {
        var image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        var graphics = image.createGraphics();
        var rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        rh.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHints(rh);
        graphics.drawImage(background, 0, 0, null);
        graphics.setBackground(Color.WHITE);
        graphics.setFont(BIG_FONT);
        graphics.setColor(Color.DARK_GRAY);
        var text = String.format("# %s", currentImageNumber);
        var bigFontMetrics = graphics.getFontMetrics(BIG_FONT);
        var textWidth = bigFontMetrics.stringWidth(text);
        graphics.drawString(text, (width - textWidth) / 2f, (height - bigFontMetrics.getHeight()) / 2f);
        return image;
    }

    private byte[] encodeImage(BufferedImage image) {
        var bytes = EMPTY_BYTE_ARRAY;
        try (var bout = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", bout);
            bout.flush();
            bytes = bout.toByteArray();
        } catch (IOException e) {
            logger.warn("Error creating image", e);
        }
        return bytes;
    }

    public static final class Builder {
        private int height;
        private int width;
        private String name;
        public long maxImages;
        public byte[] icon = null;

        private Builder() {
        }

        public Builder withHeight(int height) {
            this.height = height;
            return this;
        }

        public Builder withWidth(int width) {
            this.width = width;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withMaxImages(long maxImages) {
            this.maxImages = maxImages;
            return this;
        }

        public Builder withIcon(byte[] icon) {
            this.icon = icon;
            return this;
        }

        public MemoryImageProducer build() {
            return new MemoryImageProducer(this);
        }
    }
}
