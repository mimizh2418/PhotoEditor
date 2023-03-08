import java.awt.*;
import java.util.function.*;

public class ImageFilterType {
    public enum ColorFilterType {
        GRAYSCALE(color -> {
            int average = (color.getRed() + color.getGreen() + color.getBlue()) / 3;
            return new Color(average, average, average);
        }),
        INVERT(color -> new Color(255 - color.getRed(), 255 - color.getGreen(), 255 - color.getBlue())),
        SHIFT(color -> new Color(color.getBlue(), color.getRed(), color.getGreen())),
        REMOVE_RED(color -> new Color(0, color.getGreen(), color.getBlue())),
        REMOVE_GREEN(color -> new Color(color.getRed(), 0, color.getBlue())),
        REMOVE_BLUE(color -> new Color(color.getRed(), color.getGreen(), 0));

        public final Function<Color, Color> transformer;

        ColorFilterType(Function<Color, Color> transformer) {
            this.transformer = transformer;
        }
    }
}
