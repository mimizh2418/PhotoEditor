import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class PhotoEditor {
    private BufferedImage image;
    private final JFrame mainFrame = new JFrame("Photo Editor - Macrohard Draw");
    private final PhotoCanvas canvas = new PhotoCanvas(500, 500);
    private final JFileChooser chooser = new JFileChooser();

    public PhotoEditor() {
        mainFrame.setLayout(new BorderLayout());
        mainFrame.add(new ControlPanel(), BorderLayout.WEST);
        mainFrame.add(canvas, BorderLayout.CENTER);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.pack();
        mainFrame.setVisible(true);
    }

    class PhotoCanvas extends ImageCanvas {
        private int imageWidth, imageHeight, imageX, imageY;

        public PhotoCanvas(int width, int height) {
            super(width, height);
        }

        private void calculateImageSize() {
            if (image != null) {
                if (getWidth() < getHeight()) {
                    imageWidth = getWidth();
                    imageHeight = image.getHeight() * getWidth() / image.getWidth();
                    imageX = 0;
                    imageY = getHeight() / 2 - imageHeight / 2;
                } else {
                    imageWidth = image.getWidth() * getHeight() / image.getHeight();
                    imageHeight = getHeight();
                    imageX = getWidth() / 2 - imageWidth / 2;
                    imageY = 0;
                }
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (image != null) {
                calculateImageSize();
                g.drawImage(image, imageX, imageY, imageWidth, imageHeight, null); // TODO: 2/28/2023 Config draw position
            }
        }

        class ScribbleMouseListener implements MouseListener, MouseMotionListener {

            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {}

            @Override
            public void mouseReleased(MouseEvent e) {}

            @Override
            public void mouseEntered(MouseEvent e) {}

            @Override
            public void mouseExited(MouseEvent e) {}

            @Override
            public void mouseDragged(MouseEvent e) {}

            @Override
            public void mouseMoved(MouseEvent e) {}
        }
    }

    class ControlPanel extends JPanel {
        public ControlPanel() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            add(Box.createGlue());

            add(new OpenFileButton());
            add(new NewFileButton());
            add(new SaveFileButton());

            add(Box.createGlue());
        }
    }

    class OpenFileButton extends JButton implements ActionListener {
        public OpenFileButton() {
            super("Open file");
            addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (chooser.showOpenDialog(mainFrame) == JFileChooser.APPROVE_OPTION) {
                try {
                    image = ImageIO.read(chooser.getSelectedFile());
                    canvas.repaint();
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(mainFrame, "ERROR: " + ex.getMessage());
                }
            }
        }
    }

    class NewFileButton extends JButton implements ActionListener {
        private final ImageSizeInput sizeInput = new ImageSizeInput();

        public NewFileButton() {
            super("New image");
            addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (JOptionPane.showConfirmDialog(mainFrame, sizeInput, "Specify image dimensions", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                image = new BufferedImage(
                        (int) sizeInput.widthInput.getValue(), (int) sizeInput.heightInput.getValue(), BufferedImage.TYPE_INT_RGB
                );
                canvas.repaint();
            }
        }

        class ImageSizeInput extends JPanel {
            JSpinner heightInput = new JSpinner(new SpinnerNumberModel(500, 1, Integer.MAX_VALUE, 1));
            JSpinner widthInput = new JSpinner(new SpinnerNumberModel(500, 1, Integer.MAX_VALUE, 1));

            public ImageSizeInput() {
                setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

                JPanel widthInputSection = new JPanel();
                widthInputSection.setLayout(new BoxLayout(widthInputSection, BoxLayout.X_AXIS));
                widthInputSection.add(new JLabel("Image width (px): "));
                widthInputSection.add(widthInput);
                JPanel heightInputSection = new JPanel();
                heightInputSection.setLayout(new BoxLayout(heightInputSection, BoxLayout.X_AXIS));
                heightInputSection.add(new JLabel("Image height (px): "));
                heightInputSection.add(heightInput);

                add(widthInputSection);
                add(heightInputSection);
            }
        }
    }

    class SaveFileButton extends JButton implements ActionListener {
        public SaveFileButton() {
            super("Save");
            addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (image == null) {
                JOptionPane.showMessageDialog(mainFrame, "No image to save!");
            }
            else if (chooser.showSaveDialog(mainFrame) == JFileChooser.APPROVE_OPTION) {
                try {
                    ImageIO.write(image, "png", chooser.getSelectedFile());
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(mainFrame, "ERROR: " + ex.getMessage());
                }
            }
        }
    }
    
    public static void main(String[] args) {
        new PhotoEditor();
    }
}
