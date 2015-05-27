package imageproducer;

@FunctionalInterface
public interface ImageHandler {

    void imageCreatedWithNameAndData(String name, byte[] rawData);
}
