import java.awt.*;
import java.awt.image.BufferedImage;

public class Kernel {
    private final double[][] matrix;
    private final double multiplier;

    public static final Kernel BLUR = new Kernel(new double[][] {
            {1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1}
    }, 1.0/25.0);

    public static final Kernel GAUSSIAN_BLUR = new Kernel(new double[][] {
            {1, 4,  6,  4,  1},
            {4, 16, 24, 26, 4},
            {6, 24, 36, 24, 6},
            {4, 16, 24, 26, 4},
            {1, 4,  6,  4,  1}
    }, 1.0/256.0);

    public static final Kernel SHARPEN = new Kernel(new double[][] {
            {0, -1, 0},
            {-1, 5, -1},
            {0, -1, 0}
    });

    public static final Kernel LAPLACIAN = new Kernel(new double[][] {
            {-1, -1, -1},
            {-1,  8, -1},
            {-1, -1, -1},
    });

    public Kernel(double[][] matrix) {
        this(matrix, 1);
    }

    public Kernel(double[][] matrix, double multiplier) {
        if (matrix.length == 0) throw new IllegalArgumentException("Matrix cannot be empty.");
        int rowLength = matrix[0].length;
        if (rowLength == 0) throw new IllegalArgumentException("Matrix cannot have empty rows.");
        for (double[] row : matrix) {
            if (row.length != rowLength) throw new IllegalArgumentException("Matrix cannot have rows of different lengths.");
        }
        this.matrix = matrix;
        this.multiplier = multiplier;
    }

    public double getPixel(int row, int col) {
        return matrix[row][col] * multiplier;
    }

    public int getHeight() {
        return matrix.length;
    }

    public int getWidth() {
        return matrix[0].length;
    }

    public static BufferedImage applyFilter(BufferedImage original, Kernel kernel) {
        BufferedImage image = ImageUtils.copyImage(original);
        for (int row = 0; row < image.getHeight(); row++) {
            for (int col = 0; col < image.getWidth(); col++) {
                int red = 0;
                int green = 0;
                int blue = 0;
                for (int i = 0; i < kernel.getHeight(); i++) {
                    for (int j = 0; j < kernel.getWidth(); j++) {
                        int x = Math.floorMod(col + (j - kernel.getWidth()/2), image.getWidth());
                        int y = Math.floorMod(row + (i - kernel.getHeight()/2), image.getHeight());
                        Color pixelColor = new Color(original.getRGB(x, y));
                        red += pixelColor.getRed() * kernel.getPixel(i, j);
                        green += pixelColor.getGreen() * kernel.getPixel(i, j);
                        blue += pixelColor.getBlue() * kernel.getPixel(i, j);
                    }
                }
                red = Math.max(Math.min(red, 255), 0);
                green = Math.max(Math.min(green, 255), 0);
                blue = Math.max(Math.min(blue, 255), 0);

                image.setRGB(col, row, new Color(red, green, blue).getRGB());
            }
        }
        return image;
    }
}
