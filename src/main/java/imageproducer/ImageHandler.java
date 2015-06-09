package imageproducer;

import imageproducer.imageproducer.Image;

@FunctionalInterface
public interface ImageHandler {

    void imageCreatedWithNameAndData(Image image);
}
