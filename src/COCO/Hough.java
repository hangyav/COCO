package COCO;

import ij.process.*;
import java.awt.*;
import java.util.Set;

/**
* Hough transzformaciot kezelo osztaly.
*/
public class Hough{
    private static int radiusMin;  // Find circles with radius grater or equal radiusMin
    private static int radiusMax;  // Find circles with radius less or equal radiusMax
    private static int radiusInc;  // Increment used to go from radiusMin to radiusMax
    private static int threshold; // An alternative to maxCircles. All circles with
    // a value in the hough space greater then threshold are marked. Higher thresholds
    // results in fewer circles.
    private static byte imageValues[]; // Raw image (returned by ip.getPixels())
    private static double houghValues[][][]; // Hough Space Values
    private static int width; // Hough Space width (depends on image width)
    private static int height;  // Hough Space heigh (depends on image height)
    private static int depth;  // Hough Space depth (depends on radius interval)
    private static int offset; // Image Width
    private static int offx;   // ROI x offset
    private static int offy;   // ROI y offset
    private static int vectorMaxSize;
    private static int lut[][][]; // LookUp Table for rsin e rcos values


    public static Set<Circle> runHough(ImageProcessor ip, int rd, int tresh, int vecSize) {
		return runHough(ip, rd, rd, 1, tresh, vecSize, -1);
	}

    public static Set<Circle> runHough(ImageProcessor ip, int rd, int tresh, int vecSize, int type) {
		return runHough(ip, rd, rd, 1, tresh, vecSize, type);
	}

    public static Set<Circle> runHough(ImageProcessor ip, int rdMin, int rdMax, int rdInc, int tresh, int vecSize) {
		return runHough(ip, rdMin, rdMax, 1, tresh, vecSize, -1);
	}


    public static Set<Circle> runHough(ImageProcessor ip, int rdMin, int rdMax, int rdInc, int tresh, int vecSize, int type) {

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
		vectorMaxSize = vecSize;


		houghTransform();

		// Create image View for Hough Transform.
		ImageProcessor newip = new ByteProcessor(width, height);
		byte[] newpixels = (byte[])newip.getPixels();
		createHoughPixels(newpixels);

		// Create image View for Marked Circles.
		ImageProcessor circlesip = new ByteProcessor(width, height);
		byte[] circlespixels = (byte[])circlesip.getPixels();

		// Mark the center of the found circles in a new image
		Set<Circle> set = getCenterPointsByThreshold(threshold, type);
		drawCircles(circlespixels, set);

		//new ImagePlus("Hough Space [r="+radiusMin+"]", newip).show(); // Shows only the hough space for the minimun radius
		//new ImagePlus(set.size()+" Circles Found", circlesip).show();
		return set;
    }

    private static int buildLookUpTable() {

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

    private static void houghTransform () {

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
    private static void createHoughPixels (byte houghPixels[]) {
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
	private static void drawCircles(byte[] circlespixels, Set<Circle> set) {
		
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
		
		java.util.Iterator<Circle> it = set.iterator();
		while(it.hasNext()) {
			Circle po = it.next();
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


    private static boolean outOfBounds(int y,int x) {
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
    private static Set<Circle> getCenterPointsByThreshold (int threshold, int type) {


        int xMax = -1;
        int yMax = -1;
        int rMax = -1;
        int countCircles = 0;
		Set<Circle> set = new java.util.HashSet<Circle>();


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
				Circle p = new Circle(xMax, yMax, rMax, type);
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
    private static void clearNeighbours(int x,int y, int radius) {
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
