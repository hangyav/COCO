import ij.*;
import ij.gui.*;
import ij.plugin.filter.Binary;
import ij.plugin.filter.PlugInFilter;
import ij.process.*;
import java.awt.*;
import java.util.Set;
import java.util.HashSet;

public class COCO_ implements PlugInFilter{
	/**
	* Meretek
	*/
	private static final double MAX_5 = 153.502;
	private static final double MIN_5 = 149.942;
	private static final double MAX_10 = 177.496;
	private static final double MIN_10 = 172.806;
	private static final double MAX_20 = 189.407;
	private static final double MIN_20 = 186.108;
	private static final double MAX_50 = 197.747;
	private static final double MIN_50 = 191.110;
	private static final double MAX_100 = 171.584;
	private static final double MIN_100 = 167.445;
	private static final double MAX_200 = 203.856;
	private static final double MIN_200 = 197.191;


	/**
	* Egy instance (ablak) melyben a kezelt kép megjelenik.
	*/
	private ImagePlus image; 
	
	/**
	* A plugin meghivasakor fut le ez a metodus.
	*
	* @param arg 
	* @param im Az instance melyben a kép helyezkedik el. 
	*
	* @return Milyen tipusú képet kezel a plugin (pl.: mindent, 8bit, stb.).
	*/
	public int setup(String arg, ImagePlus im){
		image = im;//ezt lehetne clone-ozni
		new ImageConverter(image).convertToGray8();
		return DOES_ALL; //Milyen kepeket kezel a plugin (ALL)
	}

	private void prepare(ImageProcessor ip){
		//new ImageConverter(image).convertToGray8();
		//ImageProcessor ip = image.getProcessor();
		ip.autoThreshold();
		//elkereses ide
		BinaryProcessor bp = new BinaryProcessor(new ByteProcessor(ip.getBufferedImage()));
		bp.erode();
		bp.erode();
		bp.dilate();
		bp.dilate();
		Binary b = new Binary();
		b.setup("fill", image);
		b.run(ip);
		//FloodFiller ff = new FloodFiller(ip);
		//ff.particleAnalyzerFill();
	}

	/**
	* A metodus hajtja vegre a megfelelo transzformaciot a kapott
	* {@code ImageProcessor} segitsegevel.
	*
	* @param ip Az objektum kezeli a kepet.
	*/
	public void run(ImageProcessor ip){
		java.io.BufferedReader in = new java.io.BufferedReader(
			new java.io.InputStreamReader(System.in));
        GenericDialog gd = new GenericDialog("COCO", IJ.getInstance());
        gd.addNumericField("Feladat :", 0, 0);
		gd.showDialog();
		int i = (int)gd.getNextNumber();
		if(i == 1)
		prepare(ip);
		else if(i == 2)
			runHough(ip, (int)MIN_5, (int)MAX_5, 1, 240);
		image.updateAndDraw();
	}

	/**
	* Hough
	*/
    public int radiusMin;  // Find circles with radius grater or equal radiusMin
    public int radiusMax;  // Find circles with radius less or equal radiusMax
    public int radiusInc;  // Increment used to go from radiusMin to radiusMax
    public int threshold = -1; // An alternative to maxCircles. All circles with
    // a value in the hough space greater then threshold are marked. Higher thresholds
    // results in fewer circles.
    byte imageValues[]; // Raw image (returned by ip.getPixels())
    double houghValues[][][]; // Hough Space Values
    public int width; // Hough Space width (depends on image width)
    public int height;  // Hough Space heigh (depends on image height)
    public int depth;  // Hough Space depth (depends on radius interval)
    public int offset; // Image Width
    public int offx;   // ROI x offset
    public int offy;   // ROI y offset
    private int vectorMaxSize = 50;
    int lut[][][]; // LookUp Table for rsin e rcos values



    public Set<Point> runHough(ImageProcessor ip, int rdMin, int rdMax, int rdInc, int tresh) {

        imageValues = (byte[])ip.getPixels();
        Rectangle r = ip.getRoi();


        offx = r.x;
        offy = r.y;
        width = r.width;
        height = r.height;
        offset = ip.getWidth();
		radiusMin = rdMin;
		radiusMax = rdMax;
		radiusInc = rdInc;
        depth = ((radiusMax-radiusMin)/radiusInc)+1;
		threshold = tresh;


		houghTransform();

		// Create image View for Hough Transform.
		ImageProcessor newip = new ByteProcessor(width, height);
		byte[] newpixels = (byte[])newip.getPixels();
		createHoughPixels(newpixels);

		// Create image View for Marked Circles.
		ImageProcessor circlesip = new ByteProcessor(width, height);
		byte[] circlespixels = (byte[])circlesip.getPixels();

		// Mark the center of the found circles in a new image
		Set<Point> set = getCenterPointsByThreshold(threshold);
		drawCircles(circlespixels, set);

		new ImagePlus("Hough Space [r="+radiusMin+"]", newip).show(); // Shows only the hough space for the minimun radius
		new ImagePlus(set.size()+" Circles Found", circlesip).show();
		return set;
    }

    /*boolean readParameters() {

        GenericDialog gd = new GenericDialog("Hough Parameters", IJ.getInstance());
        gd.addNumericField("Minimum radius (in pixels) :", 150, 0);
        gd.addNumericField("Maximum radius (in pixels)", 200, 0);
        gd.addNumericField("Increment radius (in pixels) :", 25, 0);
        gd.addNumericField("Number of Circles (NC): (enter 0 if using threshold)", 0, 0);
        gd.addNumericField("Threshold: (not used if NC > 0)", 60, 0);

        gd.showDialog();

        if (gd.wasCanceled()) {
            return(false);
        }

        radiusMin = (int) gd.getNextNumber();
        radiusMax = (int) gd.getNextNumber();
        radiusInc = (int) gd.getNextNumber();
        depth = ((radiusMax-radiusMin)/radiusInc)+1;
        maxCircles = (int) gd.getNextNumber();
        threshold = (int) gd.getNextNumber();
        if (maxCircles > 0) {
            useThreshold = false;
            threshold = -1;
        } else {
            useThreshold = true;
            if(threshold < 0) {
                IJ.showMessage("Threshold must be greater than 0");
                return(false);
            }
        }
        return(true);

    }*/

    private int buildLookUpTable() {

        int i = 0;
        int incDen = Math.round (8F * radiusMin); 

        lut = new int[2][incDen][depth];

        for(int radius = radiusMin;radius <= radiusMax;radius = radius+radiusInc) {
            i = 0;
            for(int incNun = 0; incNun < incDen; incNun++) {
                double angle = (2*Math.PI * (double)incNun) / (double)incDen;
                int indexR = (radius-radiusMin)/radiusInc;
                int rcos = (int)Math.round ((double)radius * Math.cos (angle));
                int rsin = (int)Math.round ((double)radius * Math.sin (angle));
                if((i == 0) | (rcos != lut[0][i][indexR]) & (rsin != lut[1][i][indexR])) {
                    lut[0][i][indexR] = rcos;
                    lut[1][i][indexR] = rsin;
                    i++;
                }
            }
        }

        return i;
    }

    private void houghTransform () {

        int lutSize = buildLookUpTable();

        houghValues = new double[width][height][depth];

        int k = width - 1;
        int l = height - 1;

        for(int y = 1; y < l; y++) {
            for(int x = 1; x < k; x++) {
                for(int radius = radiusMin;radius <= radiusMax;radius = radius+radiusInc) {
                    if( imageValues[(x+offx)+(y+offy)*offset] != 0 )  {// Edge pixel found
                        int indexR=(radius-radiusMin)/radiusInc;
                        for(int i = 0; i < lutSize; i++) {

                            int a = x + lut[1][i][indexR]; 
                            int b = y + lut[0][i][indexR]; 
                            if((b >= 0) & (b < height) & (a >= 0) & (a < width)) {
                                houghValues[a][b][indexR] += 1;
                            }
                        }

                    }
                }
            }

        }

    }


    // Convert Values in Hough Space to an 8-Bit Image Space.
    private void createHoughPixels (byte houghPixels[]) {
        double d = -1D;
        for(int j = 0; j < height; j++) {
            for(int k = 0; k < width; k++)
                if(houghValues[k][j][0] > d) {
                    d = houghValues[k][j][0];
                }

        }

        for(int l = 0; l < height; l++) {
            for(int i = 0; i < width; i++) {
                houghPixels[i + l * width] = (byte) Math.round ((houghValues[i][l][0] * 255D) / d);
            }

        }
    }

	// Draw the circles found in the original image.
	public void drawCircles(byte[] circlespixels, Set<Point> set) {
		
		int roiaddr=0;
		for( int y = offy; y < offy+height; y++) {
			for(int x = offx; x < offx+width; x++) {
				// Copy;
				circlespixels[roiaddr] = imageValues[x+offset*y];
				// Saturate
				if(circlespixels[roiaddr] != 0 )
					circlespixels[roiaddr] = 100;
				else
					circlespixels[roiaddr] = 0;
				roiaddr++;
			}
		}
		byte cor = -1;
		// Redefine these so refer to ROI coordinates exclusively
		int offset = width;
		int offx=0;
		int offy=0;
		
		java.util.Iterator<Point> it = set.iterator();
		while(it.hasNext()) {
			Point po = it.next();
			int i = (int)po.getX();
			int j = (int)po.getY();
			// Draw a gray cross marking the center of each circle.
			for( int k = -10 ; k <= 10 ; ++k ) {
				int p = (j+k+offy)*offset + (i+offx);
				if(!outOfBounds(j+k+offy,i+offx))
					circlespixels[(j+k+offy)*offset + (i+offx)] = cor;
				if(!outOfBounds(j+offy,i+k+offx))
					circlespixels[(j+offy)*offset   + (i+k+offx)] = cor;
			}
			for( int k = -2 ; k <= 2 ; ++k ) {
				if(!outOfBounds(j-2+offy,i+k+offx))
					circlespixels[(j-2+offy)*offset + (i+k+offx)] = cor;
				if(!outOfBounds(j+2+offy,i+k+offx))
					circlespixels[(j+2+offy)*offset + (i+k+offx)] = cor;
				if(!outOfBounds(j+k+offy,i-2+offx))
					circlespixels[(j+k+offy)*offset + (i-2+offx)] = cor;
				if(!outOfBounds(j+k+offy,i+2+offx))
					circlespixels[(j+k+offy)*offset + (i+2+offx)] = cor;
			}
		}
	}


    private boolean outOfBounds(int y,int x) {
        if(x >= width)
            return(true);
        if(x <= 0)
            return(true);
        if(y >= height)
            return(true);
        if(y <= 0)
            return(true);
        return(false);
    }

    /** Search circles having values in the hough space higher than a threshold

    @param threshold The threshold used to select the higher point of Hough Space
    */
    private Set<Point> getCenterPointsByThreshold (int threshold) {


        int xMax = -1;
        int yMax = -1;
        int rMax = -1;
        int countCircles = 0;
		Set<Point> set = new java.util.HashSet<Point>();


        for(int c = 0; c < vectorMaxSize; c++) {
            double counterMax = threshold;
            for(int radius = radiusMin;radius <= radiusMax;radius = radius+radiusInc) {


                int indexR = (radius-radiusMin)/radiusInc;
                for(int y = 0; y < height; y++) {
                    for(int x = 0; x < width; x++) {
                        if(houghValues[x][y][indexR] > counterMax) {
                            counterMax = houghValues[x][y][indexR];
                            xMax = x;
                            yMax = y;
                            rMax = radius;
                        }
                    }

                }
            }

			if(xMax != -1 && yMax != -1){
				Point p = new Point (xMax, yMax);
				if(set.add(p)){
					clearNeighbours(xMax,yMax,rMax);
				}
			}
        }
		return set;
    }

    /** Clear, from the Hough Space, all the counter that are near (radius/2) a previously found circle C.
        
    @param x The x coordinate of the circle C found.
    @param x The y coordinate of the circle C found.
    @param x The radius of the circle C found.
    */
    private void clearNeighbours(int x,int y, int radius) {
        // The following code just clean the points around the center of the circle found.
        double halfRadius = radius / 2.0F;
	double halfSquared = halfRadius*halfRadius;

        int y1 = (int)Math.floor ((double)y - halfRadius);
        int y2 = (int)Math.ceil ((double)y + halfRadius) + 1;
        int x1 = (int)Math.floor ((double)x - halfRadius);
        int x2 = (int)Math.ceil ((double)x + halfRadius) + 1;

        if(y1 < 0)
            y1 = 0;
        if(y2 > height)
            y2 = height;
        if(x1 < 0)
            x1 = 0;
        if(x2 > width)
            x2 = width;

        for(int r = radiusMin;r <= radiusMax;r = r+radiusInc) {
            int indexR = (r-radiusMin)/radiusInc;
            for(int i = y1; i < y2; i++) {
                for(int j = x1; j < x2; j++) {	      	     
                    if(Math.pow (j - x, 2D) + Math.pow (i - y, 2D) < halfSquared) {
                        houghValues[j][i][indexR] = 0.0D;
                    }
                }
            }
        }
    }
}
