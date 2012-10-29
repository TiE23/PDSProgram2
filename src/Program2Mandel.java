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
		if (resX < 50 || resX > 8000 || resY < 50 || resY > 8000
				|| imgZoom > 1 || viewingX < 0 || viewingX > 1
				|| viewingY < 0 || viewingY > 1 || maxIt < 1
				|| maxIt > 10000 || palletSize < 1 || palletSize > 255) {
			System.out.println("One or more of your arguments was " +
					"outside of acceptable/sane bounds.");
			return;
		}
		
		pixelArray = new int[resX * resY];
		width = resX;
		height = resY;
		iterations = maxIt;
		zoom = imgZoom;
		viewX = viewingX;
		viewY = viewingY;
		colorPallet = new int[palletSize];
		I = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		
		createColorPallet();		// Initialize the pallet
		
		long startTime = System.currentTimeMillis();
		calculatePixels(0, height-1);	// Calculate the Mandelbrot image
		performanceReport(startTime);
		
		paintPixels();				// Save pixelArray to a BufferedImage
		
		saveImg(imageOption);	// Save BufferedImage to a file
		
	}
	
	
	/**Default constructor.
	 * Builds a Mandelbrot image with basic settings. Does not save to disk.
	 */
	public Program2Mandel() {
		pixelArray = new int[500 * 500];
		width = 500;
		height = 500;
		iterations = 64;
		zoom = 1;
		viewX = 0;
		viewY = 0;
		colorPallet = new int[64];
		I = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		
		createColorPallet();		// Initialize the pallet
		
		long startTime = System.currentTimeMillis();
		calculatePixels(0, height-1);	// Calculate the Mandelbrot image
		performanceReport(startTime);
		
		paintPixels();				// Save pixelArray to a BufferedImage
		
		saveImg(0);	// Save BufferedImage to a file
	}
	
	
	/**Imports the pixel array into the BufferedImage using a standard method
	 * BufferedImage.setRGB()
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
	 * 
	 * @param startY Starting render line
	 * @param endY Ending render line
	 */
	private void calculatePixels(int startY, int endY) {
		int lines = endY - startY + 1;
		
		/* The y-axis is kept in "lines" needed to render. When writing to
		 * an array, we use y-1 for 0-start counting. */
		for (int y = 1; y <= lines; y++) {
			for (int x = 0; x < width; x++) {
				
				// Finding mathematical location of a pixel for the Mandelbrot
				double r = zoom / Math.min(width, height);
				double dx = 2.5 * (x * r + viewX) - 2.0;
				double dy = 1.25 - 2.5 * ((y-1) * r + viewY);
				
				// Perform Mandelbrot calculation on this point
				int iteration = mandel(dx, dy);
				pixelArray[((y-1) * width) + x] 
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
		return new Integer(( r<<16 | g<<8 | b ));
	}
	
	
	/**Difference in milliseconds from startTime to present
	 * 
	 * @param startTime 
	 * @return The difference in milliseconds from startTime to present
	 */
	private long timerStop(long startTime) {
		return System.currentTimeMillis() - startTime;
	}
	
	
	/**Bonus function prints out execution performance statistics 
	 * 
	 * @param startTime
	 * @return execution time if needed
	 */
	private long performanceReport(long startTime) {
		long calcTime = timerStop(startTime);
		System.out.println("Time to calculate pixels: " + calcTime + "ms");
		return calcTime;
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
