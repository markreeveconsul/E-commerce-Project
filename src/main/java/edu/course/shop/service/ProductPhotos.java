package edu.course.shop.service;
import edu.course.shop.exception.ValidationException;
import java.nio.file.*;
import java.io.*;
import javax.imageio.ImageIO;

/** Checks actual image content, not just the filename. No external file is retained. */
public final class ProductPhotos {
    public static final int MAX_BYTES = 5 * 1024 * 1024;
    private ProductPhotos() {}
    public static byte[] read(Path file) {
        try (InputStream input = Files.newInputStream(file)) {
            byte[] bytes = input.readNBytes(MAX_BYTES + 1);
            validate(bytes);
            return bytes;
        } catch (IOException e) { throw new ValidationException("The photo could not be read. Choose another file."); }
    }
    public static void validate(byte[] bytes) {
        if (bytes == null) return;
        if (bytes.length == 0) throw new ValidationException("Choose a nonempty PNG or JPEG photo.");
        if (bytes.length > MAX_BYTES) throw new ValidationException("Choose a photo smaller than 5 MB.");
        try (var stream = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
            var readers = ImageIO.getImageReaders(stream);
            if (!readers.hasNext()) throw new ValidationException("Choose a valid PNG or JPEG photo.");
            var reader = readers.next();
            try {
                reader.setInput(stream);
                String format = reader.getFormatName();
                if (!format.equalsIgnoreCase("png") && !format.equalsIgnoreCase("jpeg"))
                    throw new ValidationException("Only PNG and JPEG photos are supported.");
                int width = reader.getWidth(0), height = reader.getHeight(0);
                if (width < 1 || height < 1 || width > 4096 || height > 4096)
                    throw new ValidationException("Photo dimensions must be at most 4096 × 4096 pixels.");
                if (reader.read(0) == null) throw new IOException("Unreadable image");
            } finally { reader.dispose(); }
        } catch (IOException | IllegalArgumentException e) {
            throw new ValidationException("This photo is damaged or cannot be opened.");
        }
    }
}
