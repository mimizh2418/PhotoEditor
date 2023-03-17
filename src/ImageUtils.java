import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
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

    public static BufferedImage fillRegion(BufferedImage image, Point startPoint, Color fillColor) {
        BufferedImage newImage = copyImage(image);
        iterFloodFill(newImage, startPoint, fillColor);
        return newImage;
    }

    private static void iterFloodFill(BufferedImage image, Point startPoint, Color fillColor) {
        if (startPoint.x < 0 || startPoint.x >= image.getWidth() || startPoint.y < 0 || startPoint.y >= image.getHeight()) {
            throw new IllegalArgumentException("point coordinates out of bounds.");
        }
        int originalColor = image.getRGB(startPoint.x, startPoint.y);
        Stack<Point> stack = new Stack<>();
        stack.push(startPoint);
        while (!stack.isEmpty()) {
            Point p = stack.pop();
            image.setRGB(p.x, p.y, fillColor.getRGB());
            if (p.x > 0 && image.getRGB(p.x - 1, p.y) == originalColor) stack.push(new Point(p.x - 1, p.y));
            if (p.x < image.getWidth() - 1 && image.getRGB(p.x + 1, p.y) == originalColor) stack.push(new Point(p.x + 1, p.y));
            if (p.y > 0 && image.getRGB(p.x, p.y - 1) == originalColor) stack.push(new Point(p.x, p.y - 1));
            if (p.y < image.getHeight() - 1 && image.getRGB(p.x, p.y + 1) == originalColor) stack.push(new Point(p.x, p.y + 1));
        }
    }
}
