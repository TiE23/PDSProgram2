NOTE: THIS REPO HAS TWO BRANCHES

How to run?
  • $cd CSS434/Program2
		○ Navigate to the folder
	• $javac Program2Mandel.java
		○ Compile the source code
	• $mpdboot -n [procs] -v
		○ Start up MPI processes (also supports single machine execution)
	• $prunjava [procs] -Xmx300m Program2Mandel 1000 1000 100 100 1 0 0 1
		○ Run it, args details below
	• $mpdallexit
		○ Stop all MPI processes

                                                        args Chart
+-------------+----------------+-----------------+-----------------+-------------------+---------------+---------+---------+--------------+
| ArgNum      | args[1]        | args[2]         | args[3]         | args[4]           | args[5]       | args[6] | args[7] | args[8]      |
+-------------+----------------+-----------------+-----------------+-------------------+---------------+---------+---------+--------------+
| Description |	Width of image | Height of image | Iteration depth | color Pallet size | Zoom          | ViewX   | ViewY   | Image option |
|             | (<10000)       | (<10000)        | (<20000)        | (<255)            | (<1 decimals) | (0-1)   | (0-1)   |              |
+-------------+----------------+-----------------+-----------------+-------------------+---------------+---------+---------+--------------+
| Sample      | 1000           | 1000            | 500             | 255               | .5            | .25     | .25     | 0 = none     |
|             |                |                 |                 |                   |               |         |         | 1 = jpg      |
|             |                |                 |                 |                   |               |         |         | 2 = png      |
+-------------+----------------+-----------------+-----------------+-------------------+---------------+---------+---------+--------------+

A basic, run of the mill set of args that generates a jpg for you:
																	1000 1000 100 100 1 0 0 1

Nice args (zoom viewX viewY)
0.00568 0.707 0.218
0.00014392 0.7082 0.2215
0.0000010233 0.90475 0.5008

