import ij.*;
import ij.gui.*;
import ij.plugin.filter.Binary;
import ij.plugin.filter.PlugInFilter;
import ij.process.*;
import ij.plugin.Thresholder;
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

		ip.invertLut();
		System.out.println(ip.isBinary());
		//bp.outline();


		//ij.plugin.Thresholder thr = new ij.plugin.Thresholder();
		//thr.run("mask");
		//IJ.run("Make Binary");

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
        GenericDialog gd = new GenericDialog("COCO", IJ.getInstance());
        gd.addNumericField("Feladat :", 0, 0);
		gd.showDialog();
		int i = (int)gd.getNextNumber();
		if(i == 1)
			prepare(ip);
		else if(i == 2){
			int tresh = 400;
			/*System.out.println("5os keresese");
			Set<Circle> circles = Hough.runHough(ip, (int)Circle.MIN_5, (int)Circle.MAX_5, 1, tresh, 10);
			drawCircles(ip, circles);
			System.out.println("5-os erme " + circles.size() + " db :" + circles);
			image.updateAndDraw();
			System.out.println("10es keresese");
			circles = Hough.runHough(ip, (int)Circle.MIN_10, (int)Circle.MAX_10, 1, tresh, 10);
			drawCircles(ip, circles);
			System.out.println("10-es erme " + circles.size() + " db :" + circles);
			image.updateAndDraw();
			System.out.println("20as keresese");
			circles = Hough.runHough(ip, (int)Circle.MIN_20, (int)Circle.MAX_20, 1, tresh, 10);
			drawCircles(ip, circles);
			System.out.println("20-as erme " + circles.size() + " db :" + circles);
			image.updateAndDraw();
			System.out.println("50es keresese");
			circles = Hough.runHough(ip, (int)Circle.MIN_50, (int)Circle.MAX_50, 1, tresh, 10);
			drawCircles(ip, circles);
			System.out.println("50-es erme " + circles.size() + " db :" + circles);
			image.updateAndDraw();
			System.out.println("100as keresese");
			circles = Hough.runHough(ip, (int)Circle.MIN_100, (int)Circle.MAX_100, 1, tresh, 10);
			drawCircles(ip, circles);
			System.out.println("100-as erme " + circles.size() + " db :" + circles);
			image.updateAndDraw();
			System.out.println("200as keresese");
			circles = Hough.runHough(ip, (int)Circle.MIN_200, (int)Circle.MAX_200, 1, tresh, 10);
			drawCircles(ip, circles);
			System.out.println("200-as erme " + circles.size() + " db :" + circles);
			image.updateAndDraw();*/

			System.out.println("5os keresese");
			Set<Circle> circles = Hough.runHough(ip, (int)Circle.AVG_5, 249, 10);
			drawCircles(ip, circles);
			System.out.println("5-os erme " + circles.size() + " db :" + circles);
			image.updateAndDraw();
			System.out.println("10es keresese");
			circles = Hough.runHough(ip, (int)Circle.AVG_10, tresh, 10);
			drawCircles(ip, circles);
			System.out.println("10-es erme " + circles.size() + " db :" + circles);
			image.updateAndDraw();
			System.out.println("20as keresese");
			circles = Hough.runHough(ip, (int)Circle.AVG_20, tresh, 10);
			drawCircles(ip, circles);
			System.out.println("20-as erme " + circles.size() + " db :" + circles);
			image.updateAndDraw();
			System.out.println("50es keresese");
			circles = Hough.runHough(ip, (int)Circle.AVG_50, tresh, 10);
			drawCircles(ip, circles);
			System.out.println("50-es erme " + circles.size() + " db :" + circles);
			image.updateAndDraw();
			System.out.println("100as keresese");
			circles = Hough.runHough(ip, (int)Circle.AVG_100, tresh, 10);
			drawCircles(ip, circles);
			System.out.println("100-as erme " + circles.size() + " db :" + circles);
			image.updateAndDraw();
			System.out.println("200as keresese");
			circles = Hough.runHough(ip, (int)Circle.AVG_200, tresh, 10);
			drawCircles(ip, circles);
			System.out.println("200-as erme " + circles.size() + " db :" + circles);
			image.updateAndDraw();
		}
		image.updateAndDraw();
	}

	private void drawCircles(ImageProcessor ip, Set<Circle> set){
		Iterator<Circle> it = set.iterator();
		while(it.hasNext())
			it.next().draw(ip, java.awt.Color.WHITE);
	}
}
