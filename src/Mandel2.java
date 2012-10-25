import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;

public final class Mandel2 extends Applet implements MouseListener {
  private int max = 64;
  private Color[] colors = new Color[50];
  private double viewX = 0.0;
  private double viewY = 0.0;
  private double zoom = 1.0;
  private int mouseX;
  private int mouseY;

public static void main(String[] args) {
    Frame frame = new Frame("Fractal Viewer");
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });
    Applet applet = new Mandel2();
    frame.add(applet, BorderLayout.CENTER);
    frame.setSize(400, 400);
    frame.setVisible(true);
    applet.init();
    applet.start();
  }
  
  public void init() {
    addMouseListener(this);
    for (int i = 0; i < colors.length; i++) {
	  //int c = 2 * i * 256 / colors.length;
	  //if (c > 255)
	  //  c = 511 - c;
	  int c = ( i / colors.length) * 255;
		
	  colors[i] = new Color(c, c, c);
    }
  }

  public void paint(Graphics g) {
    Dimension size = getSize();
    for (int y = 0; y < size.height; y++) {
      for (int x = 0; x < size.width; x++) {
        double r = zoom / Math.min(size.width, size.height);
        double dx = 2.5 * (x * r + viewX) - 2.0;
        double dy = 1.25 - 2.5 * (y * r + viewY);
        int value = mandel(dx, dy);
        g.setColor(colors[value % colors.length]);
        g.drawLine(x, y, x, y);
      }
    }
  }

  public void update(Graphics g) {
    paint(g);
  }

  private int mandel(double px, double py) {
    double zx = 0.0, zy = 0.0;
    double zx2 = 0.0, zy2 = 0.0;
    int value = 0;
    while (value < max && zx2 + zy2 < 4.0) {
      zy = 2.0 * zx * zy + py;
      zx = zx2 - zy2 + px;
      zx2 = zx * zx;
      zy2 = zy * zy;
      value++;
    }
    return value == max ? 0 : value;
  }

 //////////////////////
  
  public void mousePressed(MouseEvent e) {
    mouseX = e.getX();
    mouseY = e.getY();
  }

  public void mouseReleased(MouseEvent e) {
    int x = e.getX();
    int y = e.getY();
    if ((e.getModifiers() & InputEvent.BUTTON1_MASK) != 0) {
      if (x != mouseX && y != mouseY) {
        int w = getSize().width;
        int h = getSize().height;
        viewX += zoom * Math.min(x, mouseX) / Math.min(w, h);
        viewY += zoom * Math.min(y, mouseY) / Math.min(w, h);
        zoom *= Math.max((double)Math.abs(x - mouseX) / w, (double)Math.abs(y - mouseY) / h);
      }
    }
    else if ((e.getModifiers() & InputEvent.BUTTON3_MASK) != 0) {
      max += max / 4;
    }
    else {
      max = 64;
      viewX = viewY = 0.0;
      zoom = 1.0;
    }
    repaint();
    System.out.println("Zoom level: " + zoom + ", Max Iterations: " + max );
  }

  public void mouseClicked(MouseEvent e) {}
  public void mouseEntered(MouseEvent e) {}
  public void mouseExited(MouseEvent e) {}
}
