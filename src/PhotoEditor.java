import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Stack;

public class PhotoEditor {
    public static final int DEFAULT_BRUSH_SIZE = 10;
    public static final Color DEFAULT_DRAW_COLOR = Color.BLACK;

    private final Stack<BufferedImage> undoStack = new Stack<>();
    private final Stack<BufferedImage> redoStack = new Stack<>();

    private BufferedImage image;
    private Graphics2D imageGraphics;
    private final JFrame mainFrame = new JFrame("Photo Editor - Macrohard Draw");
    private final PhotoCanvas canvas = new PhotoCanvas(750, 750);
    private final JFileChooser chooser = new JFileChooser();

    private int drawSize = DEFAULT_BRUSH_SIZE;
    private Color drawColor = DEFAULT_DRAW_COLOR;



    public PhotoEditor() {
        mainFrame.setLayout(new BorderLayout());
        mainFrame.add(new ControlPanel(), BorderLayout.WEST);
        mainFrame.add(canvas, BorderLayout.CENTER);
        mainFrame.setJMenuBar(new EditorMenuBar());
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.pack();
        mainFrame.setVisible(true);
    }

    public void newImage(int width, int height) {
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        updateImageGraphics();
        imageGraphics.setColor(Color.WHITE);
        imageGraphics.fillRect(0, 0, image.getWidth(), image.getHeight());
        undoStack.clear();
        redoStack.clear();
        updateHistory();
        if (canvas != null) canvas.repaint();
    }

    public void newImage(File file) {
        try {
            image = ImageIO.read(file);
            updateImageGraphics();
            undoStack.clear();
            redoStack.clear();
            updateHistory();
            if (canvas != null) canvas.repaint();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(mainFrame, "ERROR: " + ex.getMessage());
        }
    }

    public void setImage(BufferedImage newImage) {
        image = copyImage(newImage);
        updateImageGraphics();
    }

    public void updateImageGraphics() {
        imageGraphics = image.createGraphics();
        imageGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }

    public BufferedImage copyImage(BufferedImage image) {
        return new BufferedImage(image.getColorModel(), image.copyData(null), image.getColorModel().isAlphaPremultiplied(), null);
    }

    public void updateHistory() {
        if (image != null) {
            if (!redoStack.isEmpty()) redoStack.clear();
            undoStack.push(copyImage(image));
        }
    }

    public void undo() {
        if (image != null) {
            if (undoStack.size() > 1) {
                BufferedImage newImage = undoStack.pop();
                redoStack.push(copyImage(newImage));
                setImage(undoStack.get(undoStack.size() - 1));
                canvas.repaint();
            }
        }
    }

    public void redo() {
        if (image != null) {
            if (redoStack.size() > 0) {
                BufferedImage newImage = redoStack.pop();
                undoStack.push(copyImage(newImage));
                setImage(undoStack.get(undoStack.size() - 1));
                canvas.repaint();
            }
        }
    }

    class PhotoCanvas extends ImageCanvas {
        int imageWidth, imageHeight, imageX, imageY;

        public PhotoCanvas(int width, int height) {
            super(width, height);
            newImage(width, height);
            ScribbleMouseListener listener = new ScribbleMouseListener();
            addMouseListener(listener);
            addMouseMotionListener(listener);
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
                g.drawImage(image, imageX, imageY, imageWidth, imageHeight, null);
            }
        }

        class ScribbleMouseListener implements MouseListener, MouseMotionListener {
            private Point prev;
            private boolean isHeld;

            private Point actualToImageCoords(Point actual) {
                if (image == null) return null;
                int x = actual.x;
                int y = actual.y;
                double scale = ((double) image.getWidth()) / ((double) imageWidth);
                if (x < imageX || x > imageX + imageWidth || y < imageY || y > imageY + imageHeight) return null;
                return new Point((int) ((x - imageX) * scale), (int) ((y - imageY) * scale));
            }

            private void completeStroke() {
                prev = null;
                updateHistory();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                Point imageCoords = actualToImageCoords(e.getPoint());
                if (image != null && imageCoords != null) {
                    imageGraphics.setColor(drawColor);
                    imageGraphics.setStroke(new BasicStroke(drawSize, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    imageGraphics.drawLine(imageCoords.x, imageCoords.y, imageCoords.x, imageCoords.y);
                    repaint();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {}

            @Override
            public void mouseReleased(MouseEvent e) {
                if (actualToImageCoords(e.getPoint()) != null) completeStroke();
            }

            @Override
            public void mouseEntered(MouseEvent e) {}

            @Override
            public void mouseExited(MouseEvent e) {}

            @Override
            public void mouseDragged(MouseEvent e) {
                Point imageCoords = actualToImageCoords(e.getPoint());
                if (image != null && imageCoords != null && prev != null) {
                    imageGraphics.setStroke(new BasicStroke(drawSize, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    imageGraphics.setColor(drawColor);
                    imageGraphics.drawLine(imageCoords.x, imageCoords.y, prev.x, prev.y);
                    prev = imageCoords;
                    repaint();
                } else prev = imageCoords;
            }

            @Override
            public void mouseMoved(MouseEvent e) {}
        }
    }

    class ControlPanel extends JPanel {
        public ControlPanel() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            add(Box.createGlue());

            add(new BrushSizeChooserPanel());
            add(new BrushColorChooserButton());

            add(Box.createGlue());
        }
    }

    class BrushSizeChooserPanel extends JPanel {
        private final JLabel sliderLabel = new JLabel("Brush size: " + DEFAULT_BRUSH_SIZE);

        public BrushSizeChooserPanel() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            add(sliderLabel);
            add(new BrushSizeChooser());
        }

        class BrushSizeChooser extends JSlider implements ChangeListener {
            public BrushSizeChooser() {
                super(0, 30, DEFAULT_BRUSH_SIZE);
                setPaintLabels(true);
                setMajorTickSpacing(5);
                addChangeListener(this);
            }

            @Override
            public synchronized void stateChanged(ChangeEvent e) {
                drawSize = getValue();
                sliderLabel.setText("Brush size: " + drawSize);
            }
        }
    }

    class BrushColorChooserButton extends JButton implements ActionListener {
        public BrushColorChooserButton() {
            super("Change color");
            addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Color selectedColor = JColorChooser.showDialog(mainFrame, "Select pen color", drawColor);
            if (selectedColor != null) drawColor = selectedColor;
        }
    }

    class EditorMenuBar extends JMenuBar {
        public EditorMenuBar() {
            JMenu fileMenu = new JMenu("File");
            fileMenu.add(new OpenFileButton());
            fileMenu.add(new NewFileButton());
            fileMenu.add(new SaveFileButton());
            add(fileMenu);

            JMenu editMenu = new JMenu("Edit");
            editMenu.add(new UndoButton());
            editMenu.add(new RedoButton());
            add(editMenu);
        }
    }

    class OpenFileButton extends JMenuItem implements ActionListener {
        public OpenFileButton() {
            super("Open");
            setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
            addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (chooser.showOpenDialog(mainFrame) == JFileChooser.APPROVE_OPTION) {
                newImage(chooser.getSelectedFile());
            }
        }
    }

    class NewFileButton extends JMenuItem implements ActionListener {
        private final ImageSizeInput sizeInput = new ImageSizeInput();

        public NewFileButton() {
            super("New");
            setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
            addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (JOptionPane.showConfirmDialog(mainFrame, sizeInput, "New image", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                newImage((int) sizeInput.widthInput.getValue(), (int) sizeInput.heightInput.getValue());
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

    class SaveFileButton extends JMenuItem implements ActionListener {
        public SaveFileButton() {
            super("Save as...");
            setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
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

    class UndoButton extends JMenuItem implements ActionListener {
        public UndoButton() {
            super("Undo");
            setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK));
            addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            undo();
        }
    }

    class RedoButton extends JMenuItem implements ActionListener {
        public RedoButton() {
            super("Redo");
            setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
            addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            redo();
        }
    }

    public static void main(String[] args) {
        new PhotoEditor();
    }
}
