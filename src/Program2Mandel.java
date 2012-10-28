// Kyle Geib - Program 2 - CSS434 Fall 2012 - Dr Fukuda - October 30th 2012
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;


public class Program2Mandel {
	private BufferedImage I;
	
	private int[] pixelArray;
	
	private int[] colorPallet;
	private double viewX;
	private double viewY;
	private double zoom;
	
	private int iterations;
	private int width;
	private int height;
	

	/** Constructor for Program2Mandel
	 * 
	 * @param resX
	 * @param resY
	 * @param maxIt
	 * @param palletSize
	 * @param imgZoom
	 * @param viewingX
	 * @param viewingY
	 * @param imageOption
	 */
	public Program2Mandel(int resX, int resY, int maxIt, int palletSize,
			double imgZoom, double viewingX, double viewingY, int imageOption){
		
		// Catching bad values
		if (resX < 1 || resX > 5000 || resY < 1 || resY > 5000
				|| imgZoom > 1 || viewingX < 0 || viewingX > 1
				|| viewingY < 0 || viewingY > 1 || maxIt < 1
				|| maxIt > 10000 || palletSize < 1 || palletSize > 255)
			return;
		
		pixelArray = new int[resX * resY];
		width = resX;
		height = resY;
		iterations = maxIt;
		zoom = imgZoom;
		viewX = viewingX;
		viewY = viewingY;
		colorPallet = new int[palletSize];
		I = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		
		createColorPallet();	// Initialize the pallet
		calculatePixels();		// Calculate the Mandelbrot image
		paintPixels();			// Save pixelArray to a BufferedImage
		
		saveImg(imageOption);	// Save BufferedImage to a file
		
	}
	
	/**Imports the pixel array into the BufferedImage using a standard method
	 */
	private void paintPixels() {
		I.setRGB(0, 0, width, height, pixelArray, 0, width);
	}
	
	/**Creates a file and uses ImageIO.write to save the BufferedImage to
	 * a local file location.
	 * @param option 0 = No Image; 1 = JPEG; 2 = PNG
	 */
	private void saveImg(int option) {
		
		if (option < 0 || option > 2)
			return;
		
		try {
			File file = new File("mandelbrot.png");
			
			switch(option) {
			case 0:	break;							// No image saved.
			case 1: ImageIO.write(I, "jpg", file);	// JPG, lower quality+size
			case 2: ImageIO.write(I, "png", file);	// PNG, higher quality+size
			}
		} catch (IOException e) { e.printStackTrace(); }
	}
	
	/**Performs a nested loop that does the calculations of a Mandelbrot
	 * image with support for zoom and varying locations.
	 */
	private void calculatePixels() {
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				
				// Finding mathematical location of a pixel for the Mandelbrot
				double r = zoom / Math.min(width, height);
				double dx = 2.5 * (x * r + viewX) - 2.0;
				double dy = 1.25 - 2.5 * (y * r + viewY);
				
				// Perform Mandelbrot calculation on this point
				int iteration = mandel(dx, dy);
				pixelArray[(y * width) + x] 
						= colorPallet[iteration % colorPallet.length];
			}
		}
	}
	
	/**Function that performs the mathematically heavy lifting to determine if
	 * a point being tested on the graph is within bounds.
	 * 
	 * @param px X point of calculation
	 * @param py Y point of calculation
	 * @return # of iterations taken
	 */
	private int mandel(double px, double py) {
		double zx = 0.0, zy = 0.0;
		double zx2 = 0.0, zy2 = 0.0;
		int iteration = 0;
		while (iteration < iterations && zx2 + zy2 < 4.0) {
			zy = 2.0 * zx * zy + py;
			zx = zx2 - zy2 + px;
			zx2 = zx * zx;
			zy2 = zy * zy;
			iteration++;
		}
		return iteration == iterations ? 0 : iteration;
	}
	
	/**Creates an array of set RGB colors that shows different tones depending
	 * on the number of iterations of the Mandelbrot set needed to rule the
	 * pixel out.
	 */
	private void createColorPallet() {

		for (int i = 0; i < colorPallet.length; i++) {
			// Generate a gradient of black to white.
			int c = (( i * 2 * 255 ) / colorPallet.length);
			if (c > 255)
				c = 511 - c;

			// Create some color tones by turning down different colors
			colorPallet[i] = getRGBInt( (int) (c/4), (int) (c/1.3), c );
		}
	}
	
	/**Uses Bitwise operations to place the three 8-bit colors into a single
	 * integer that takes up the first 24 bit places
	 * 
	 * @param r	red value
	 * @param g	green value
	 * @param b blue value
	 * @return integer representing all three values in 24 bits
	 */
	private int getRGBInt(int r, int g, int b) {
		return new Integer( ( r<<16 | g<<8 | b ) );
	}
	
	/**Instantiates the class from the console
	 * @param args String array of arguments from console
	 */
	public static void main(String args[]) {
		
		// Console use information
		if (args.length != 8) {
			System.out.println("Please place arguments in this order:\n"+
					"	Render Width, Render Height, Iterations,\n " + 
					"	Color Pallet Size (<256), Zoom Level (<=1),\n" + 
					"	Initial X coord (0-1), Initial Y coord(0-1),\n" +
					"	[Image format; 1 = jpg, 2 = png, 0 = none]");
		return;
		}
		
		// Instantiate the class and pass in the various arguments
		new Program2Mandel(
				Integer.parseInt(args[0]),
				Integer.parseInt(args[1]),
				Integer.parseInt(args[2]),
				Integer.parseInt(args[3]),
				Double.parseDouble(args[4]),
				Double.parseDouble(args[5]),
				Double.parseDouble(args[6]),
				Integer.parseInt(args[7]));
	}

	
}
