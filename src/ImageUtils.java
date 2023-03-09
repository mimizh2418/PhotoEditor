import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.function.Function;

public class ImageUtils {
    private ImageUtils() {}

    public static BufferedImage copyImage(BufferedImage image) {
        return new BufferedImage(image.getColorModel(), image.copyData(null), image.getColorModel().isAlphaPremultiplied(), null);
    }

    public static BufferedImage filterImage(BufferedImage image, Function<Color, Color> transformer) {
        BufferedImage filteredImage = copyImage(image);
        for (int i = 0; i < filteredImage.getWidth(); i++) {
            for (int j = 0; j < filteredImage.getHeight(); j++) {
                Color transformedColor = transformer.apply(new Color(filteredImage.getRGB(i, j)));
                filteredImage.setRGB(i, j, transformedColor.getRGB());
            }
        }
        return filteredImage;
    }
}
