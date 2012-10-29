// Kyle Geib - Program 2 - CSS434 Fall 2012 - Dr Fukuda - October 30th 2012
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

import mpi.*;

public class Program2Mandel {
	private BufferedImage I;	// Rendered image object
	
	private int[] pixelArray;	// Rendered image pixel array
	
	private int[] colorPallet;
	private double viewX;
	private double viewY;
	private double zoom;
	
	private int iterations;	// Iteration depth
	private int width;		// Width of render
	private int height;		// Height of render
	
	private int myRank;		// MPI - this instance's rank
	private int nProcs;		// MPI - number of nodes involved

	final static int tagFromMaster = 1;
    final static int tagFromSlave = 2;
    final static int master = 0;
	
	/**Constructor for Master Node
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
			double imgZoom, double viewingX, double viewingY, int imageOption)
			throws MPIException {
		// MPI processing as the master
		myRank = MPI.COMM_WORLD.Rank();
		nProcs = MPI.COMM_WORLD.Size();
		
		// Catching bad values 
		if (resX < 1 || resX > 10000 || resY < nProcs || resY > 10000
				|| imgZoom > 1 || imgZoom <= 0
				|| viewingX < 0 || viewingX > 1
				|| viewingY < 0 || viewingY > 1 || maxIt < 1
				|| maxIt > 20000 || palletSize < 1 || palletSize > 255
				|| imageOption < 0 || imageOption > 2) {
			System.out.println("One or more of your arguments was " +
					"outside of acceptable/sane bounds.");
			System.exit( -1 );
		}
		
		// Basic initialization as determined by the user's input
		width = resX;
		height = resY;
		iterations = maxIt;
		zoom = imgZoom;
		viewX = viewingX;
		viewY = viewingY;
		createColorPallet(palletSize);		// Initialize the pallet
		pixelArray = new int[width * height];
		
		/* May throw up memory errors for very large images (over 16mp). 
		 * Use the java argument -Xmx to increase maximum memory. 
		 * -Xmx300m for example would give 300mb of memory to this program.
		 * Place the argument like so: 
		 * "prunjava [procs] -Xmx300m Program2Mandel [args]" */
		I = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		
		// Sending render information
		double[] renderArgs = new double[7];
		renderArgs[0] = width;
		renderArgs[1] = height;
		renderArgs[2] = iterations;
		renderArgs[3] = zoom;
		renderArgs[4] = viewX;
		renderArgs[5] = viewY;
		renderArgs[6] = palletSize;
		
		// Send render information to all slaves
		for (int r = 1; r < nProcs; r++) 
			MPI.COMM_WORLD.Send(renderArgs, 0, 7, MPI.DOUBLE, r, 0);

		// Calculate this computer's share
		int startY, endY, lines;
		lines = height/nProcs;	// Number of lines this node is responsible for
		startY = myRank * lines;
		endY = ( (myRank+1) * lines) - 1;
		
		/* If (height/nProcs) doesn't round nicely, the last node will render
		 * the remainder number of lines of the image. */
		if ( ((myRank+2) * lines) > height ) {
			endY = height-1;
			lines = height - startY;
		}
		
		// Render the image
		long startTime = System.currentTimeMillis();
		calculatePixels(startY, endY);	// Calculate the Mandelbrot image
		long[] execTime = new long[1];
		execTime[0] = timerStop(startTime);
		performanceReport(execTime[0], myRank);	// Report the execution time
		
		// If there are any slave nodes...
		if (nProcs > 1) {
		
			// Now receive the execution times of the slave nodes
			long totalExecTime = execTime[0];
			
			for (int source = 1; source < nProcs; source++) {
				// Receive the execution time of this slave node
				MPI.COMM_WORLD.Recv(execTime, 0, 1, MPI.LONG, source, 0);
				performanceReport(execTime[0], source);
				totalExecTime += execTime[0];
			}
			
			long receiveStart = 0;
			long totalReceiveTime = 0;
			
			// Receive the image data
			System.out.print("\nReceiving pixel arrays: ");
			for (int source = 1; source < nProcs; source++) {
				// Calculating the pixels worked by other nodes
				lines = height/nProcs;	
				startY = source * lines;
				endY = ( (source+1) * lines) - 1;
				
				if ( ((source+2) * lines) > height ) {
					endY = height-1;
					lines = height - startY;
				}
				
				
				receiveStart = System.currentTimeMillis();
				// Receive the pixels from the source
				MPI.COMM_WORLD.Recv(pixelArray, startY*width, lines*width, 
						MPI.INT, source, 0);
				System.out.print(source + " Received! ");
				
				// Clock the transfer time
				totalReceiveTime += timerStop(receiveStart);
				
			}
			System.out.println("\n\nReceived all image data..." +
					"\nPixel Array Transfer time: " + totalReceiveTime + "ms" +
					"\nCumulative execution time: " + totalExecTime + "ms");
		}
		
		// Print out some time information
		System.out.println("Turn-around time: " + 
				timerStop(startTime) + "ms");

		// Save pixelArray to a BufferedImage
		I.setRGB(0, 0, width, height, pixelArray, 0, width);
		
		saveImg(imageOption);	// Save BufferedImage to a file	
	}
	
	
	/**Constructor for Slave nodes
	 * 
	 */
	public Program2Mandel() throws MPIException {
		// MPI processing as a slave
		myRank = MPI.COMM_WORLD.Rank();
		nProcs = MPI.COMM_WORLD.Size();
		
		// Receive render information from Master
		double[] renderArgs = new double[7];
		
		MPI.COMM_WORLD.Recv(renderArgs, 0, 7, MPI.DOUBLE, master, 0);

		width = (int)renderArgs[0];
		height = (int)renderArgs[1];
		iterations = (int)renderArgs[2];
		zoom = renderArgs[3];
		viewX = renderArgs[4];
		viewY = renderArgs[5];
		
		createColorPallet((int)renderArgs[6]);	// Initialize the pallet
		pixelArray = new int[width * height];	// Initialize the pixelArray
		
		// Calculate this computer's share
		int startY, endY, lines;
		lines = height/nProcs;	// Number of lines this node is responsible for
		startY = myRank * lines;
		endY = ( (myRank+1) * lines) - 1;
		
		/* If (height/nProcs) doesn't round nicely, the last node will render
		 * the remainder number of lines of the image. */
		if ( ((myRank+2) * lines) > height ) {
			endY = height-1;
			lines = height - startY;
		}
		
		// Render the image
		long startTime = System.currentTimeMillis();
		long[] execTime = new long[1];
		calculatePixels(startY, endY);	// Calculate the Mandelbrot image
		execTime[0] = timerStop(startTime);

		// Send this slave's execution time to master
		MPI.COMM_WORLD.Send(execTime, 0, 1, MPI.LONG, master, 0);
		MPI.COMM_WORLD.Send(pixelArray, 
				startY*width, lines*width, MPI.INT, master, 0);
	}
	
	
	/**Performs a nested loop that does the calculations of a Mandelbrot
	 * image with support for zoom and varying locations.
	 * 
	 * @param startY Starting render line (zero base count)
	 * @param endY Ending render line (zero base count)
	 */
	private void calculatePixels(int startY, int endY) {
		//System.out.println("Rendering lines " + startY + " to " + endY);
		for (int y = startY; y <= endY; y++) {
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
	private void createColorPallet(int palletSize) {
		
		colorPallet = new int[palletSize];
		
		for (int i = 0; i < colorPallet.length; i++) {
			// Generate a gradient of black to white.
			int c = (( i * 2 * 255 ) / colorPallet.length);
			if (c > 255)
				c = 511 - c;

			// Create some color tones by turning down different colors
			colorPallet[i] = getRGBInt( (int) (c/4), (int) (c/1.3), c );
		}
	}
	
	
	/**Creates a file and uses ImageIO.write to save the BufferedImage to
	 * a local file location.
	 * 
	 * @param option 0 = No Image; 1 = JPEG; 2 = PNG -- Error handling 
	 * performed in constructor
	 */
	private void saveImg(int option) {
		try {
			File file;
			switch(option) {
			case 0:	break;							// No image saved.
			case 1: file = new File("mandelbrot.jpg");
					ImageIO.write(I, "jpg", file);	// JPG, lower quality+size		
					break;
			case 2: file = new File("mandelbrot.png");
					ImageIO.write(I, "png", file);	// PNG, higher quality+size
					break;
			}
		} catch (IOException e) { e.printStackTrace(); }
		
		if (option != 0 )
			System.out.println("Saved to file.");
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
	private void performanceReport(long calcTime, int rank) {
		System.out.println("Execution time of node " + 
				rank + ":  " + calcTime + "ms");
	}
	
	
	/**Instantiates the class from the console
	 * @param args String array of arguments from console
	 */
	public static void main(String args[]) throws MPIException {
		MPI.Init( args );
		
		// Master node
		if (MPI.COMM_WORLD.Rank() == 0 ) {
			
			// Console use information
			if (args.length != 1 + (8*2)) {
				System.out.println("Please place arguments in this order:\n"+
						"	Render Width, Render Height, Iterations,\n " + 
						"	Color Pallet Size (<256), Zoom Level (<=1),\n" + 
						"	Initial X coord (0-1), Initial Y coord(0-1),\n" +
						"	[Image format; 1 = jpg, 2 = png, 0 = none]");
				System.exit( -1 );
			}
			
			// Instantiate the class and pass in the various arguments
			new Program2Mandel(
					Integer.parseInt(args[1]),
					Integer.parseInt(args[2]),
					Integer.parseInt(args[3]),
					Integer.parseInt(args[4]),
					Double.parseDouble(args[5]),
					Double.parseDouble(args[6]),
					Double.parseDouble(args[7]),
					Integer.parseInt(args[8]));
		
		
		} else {	// Slave nodes
			new Program2Mandel();
		}
		
		// Terminate the MPI Library
		MPI.Finalize();
	}
}
