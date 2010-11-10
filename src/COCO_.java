import ij.*;
import ij.gui.*;
import ij.plugin.filter.Binary;
import ij.plugin.filter.PlugInFilter;
import ij.process.*;
import java.util.Set;
import java.util.Iterator;
import COCO.Hough;
import COCO.Circle;

public class COCO_ implements PlugInFilter{

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
		int foreground, background;
		foreground = Prefs.blackBackground?255:0;
		if (ip.isInvertedLut()){
			foreground = 255 - foreground;
		}
		background = 255 - foreground;

		ip.autoThreshold();
		//elkereses ide
		ByteProcessor bp = (ByteProcessor)ip.convertToByte(false);
		//close:
		bp.dilate(1,background);
		bp.erode(1,background);

		Binary b = new Binary();
		b.setup("fill", image);
		b.run(ip);

		//felesleges pottyek eltavolitasa:
		bp.erode(2,background);
		bp.dilate(2,background);

		bp.outline();

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
		else if(i == 2){
			Set<Circle> circles = Hough.runHough(ip, (int)Circle.MIN_5, (int)Circle.MAX_5, 1, 240, 10);
			drawCircles(ip, circles);
			System.out.println(circles);
		}
		image.updateAndDraw();
	}

	private void drawCircles(ImageProcessor ip, Set<Circle> set){
		Iterator<Circle> it = set.iterator();
		ip.setColor(java.awt.Color.WHITE);
		while(it.hasNext()){
			Circle c = it.next();
			int r = (int)c.getRadius()+5;
			OvalRoi or = new OvalRoi((int)c.getX()-r, (int)c.getY()-r, r*2, r*2);
			ip.fill(or);
		}
	}
}
