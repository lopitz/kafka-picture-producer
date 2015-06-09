package imageproducer.imageproducer;

public class Image {
    private final byte[] pixels;
    private final String name;

    public Image(String name, byte[] pixels) {
        this.name = name;
        this.pixels = pixels;
    }

    public byte[] getPixels() {
        return pixels;
    }

    public String getName() {
        return name;
    }

}
