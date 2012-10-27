import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;

import javax.swing.JFrame;
import javax.swing.JPanel;
 
public class MandelbrotTest {
  @SuppressWarnings("deprecation")
public static void main(String[] args) {
    JFrame frame = new MandelbrotFrame();
    frame.show();
  }
}
 
@SuppressWarnings("serial")
class MandelbrotFrame extends JFrame {
  public MandelbrotFrame() {
    setTitle("MandelbrotTest");
    setSize(800, 800);
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });
 
    Container contentPane = getContentPane();
    contentPane.add(new MandelbrotPanel(), "Center");
  }
}
 
@SuppressWarnings("serial")
class MandelbrotPanel extends JPanel {
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    BufferedImage image = new BufferedImage(getWidth(), getHeight(),
        BufferedImage.TYPE_INT_ARGB);
    generate(image);
    g.drawImage(image, 0, 0, null);
  }
 
  public void generate(BufferedImage image) {
    int width = image.getWidth();
    int height = image.getHeight();
    WritableRaster raster = image.getRaster();
    ColorModel model = image.getColorModel();
 
    Color fractalColor = Color.red;
    int argb = fractalColor.getRGB();
    Object colorData = model.getDataElements(argb, null);
 
    for (int i = 0; i < width; i++)
      for (int j = 0; j < height; j++) {
        double a = XMIN + i * (XMAX - XMIN) / width;
        double b = YMIN + j * (YMAX - YMIN) / height;
        if (!escapesToInfinity(a, b))
          raster.setDataElements(i, j, colorData);
      }
  }
 
  private boolean escapesToInfinity(double a, double b) {
    double x = 0.0;
    double y = 0.0;
    int iterations = 0;
    do {
      double xnew = x * x - y * y + a;
      double ynew = 2 * x * y + b;
      x = xnew;
      y = ynew;
      iterations++;
      if (iterations == MAX_ITERATIONS)
        return false;
    } while (x <= 2 && y <= 2);
    return true;
  }
 
  private static final double XMIN = -2;
 
  private static final double XMAX = 2;
 
  private static final double YMIN = -2;
 
  private static final double YMAX = 2;
 
  private static final int MAX_ITERATIONS = 255;
}