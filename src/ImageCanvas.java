
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * A JPanel for drawing graphics.

 You should extend this class and override this function:

 public void paintComponent(Graphics pen)

 Some useful methods provided by this class:

 public void repaint();    // calls paintComponent with the correct Graphics pen
 public int getWidth();    // returns the current width of the canvas
 public int getHeight();   // returns the current height of the canvas
 @author Andrew Merrill
 */

public class ImageCanvas extends JPanel {

    //**********************************************************************
    // Constructor: takes the width and height you want for this canvas

    public ImageCanvas(int width, int height) {
        setSize(width, height);
        setPreferredSize(new Dimension(width,height));
        addComponentListener(new ImageCanvasComponentListener());
    }

    //**********************************************************************
    // The resized() function is called whenever the ImageCanvas is resized.
    // By default, it just repaints the canvas.
    // You can override this if you have other jobs to do as well when the canvas is resized.

    void resized() {
        repaint();
    }

    //**********************************************************************
    // This class is used to listen to component events, such as resizing events
    private class ImageCanvasComponentListener extends ComponentAdapter {
        public void componentResized(ComponentEvent event) {
            resized();
        }
    }

}