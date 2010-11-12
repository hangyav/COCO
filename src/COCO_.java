import ij.*;
import ij.gui.*;
import ij.plugin.filter.Binary;
import ij.plugin.filter.PlugInFilter;
import ij.plugin.filter.ParticleAnalyzer;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.frame.RoiManager;
import java.util.Hashtable;
import ij.process.*;
import java.util.Iterator;
import java.util.Set;
import COCO.Hough;
import COCO.Circle;
import java.awt.Font;

public class COCO_ implements PlugInFilter{

	/**
	* Egy instance (ablak) melyben a kezelt kép megjelenik.
	*/
	private ImagePlus image;

	private ImageProcessor original;
	
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
		original = new ColorProcessor(image.getImage());
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
		ResultsTable rt = new ResultsTable();
		ParticleAnalyzer panal = new ParticleAnalyzer(ParticleAnalyzer.ADD_TO_MANAGER,Measurements.CENTER_OF_MASS|Measurements.AREA,rt,10000.,2000000.,0.,1.);//inf-nel az egeszet bejeloli...
		ip.invertLut();
		panal.analyze(image);
		ip.invertLut();
		//ip.invert();
		RoiManager roiMan = RoiManager.getInstance();
		Hashtable rois = roiMan.getROIs();
		Roi[] rois2 = roiMan.getRoisAsArray();

		Binary b = new Binary();
		b.setup("fill", image);
		b.run(ip);
		//felesleges pottyek eltavolitasa:
		bp.erode(2,background);
		bp.dilate(2,background);

		for(int i=0; i<rois2.length; i++){
			roiMan.select(i);
			ip.setColor(255);
			ip.fill(rois2[i]);
			//IJ.wait(1000);
		}
		
		//System.out.println(rois);
		int jjj=1;
		for(Object o : rois.values()){
			/*Roi roi = (Roi)o;
			roiMan.select(jjj++);
			//ip.setRoi(roi);
			//roi.getMask().setValue(0);
			//roi.getMask().fill();
			IJ.wait(1000);
			ip.setValue(255);
			ip.fill(roi.getMask());
			IJ.wait(1000);*/
			//System.out.println(o);
			/*IJ.wait(1000);
			Roi roi = (Roi)o;
			ip.setColor(0);
			ip.fill(roi);*/
		}
			//IJ.wait(1000);
		/*for(int i=1; i<=roiMan.getCount(); i++){
			roiMan.select(i);
			ip.setValue(255);
			ip.fill();
			IJ.wait(1000);
			//IJ.run("Set...","255");
		}*/
		//roiMan.select(image,-1);
		//roiMan.runCommand("deselect all");
		roiMan.close();
		IJ.run("Select None");
		
		System.out.println(rt);


		System.out.println(ip.isBinary());
		bp.outline();
		IJ.wait(50);
		ip.invertLut();
		ip.invert();
		//ip.invert();
		//bp.applyLut();
		image.updateAndDraw();




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
        /*GenericDialog gd = new GenericDialog("COCO", IJ.getInstance());
        gd.addNumericField("Feladat :", 0, 0);
		gd.showDialog();
		int i = (int)gd.getNextNumber();
		if(i == 1){*/
			prepare(ip);
		/*}
		else if(i == 2){*/
			int tresh = 400;
			System.out.println("5os keresese");
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
			image.updateAndDraw();

			/*System.out.println("5os keresese");
			Set<Circle> circles = Hough.runHough(ip, (int)Circle.AVG_5, 249, 10);
			drawCircles(ip, circles);
			showCircles("5", original, circles);
			System.out.println("5-os erme " + circles.size() + " db :" + circles);
			image.updateAndDraw();
			System.out.println("10es keresese");
			circles = Hough.runHough(ip, (int)Circle.AVG_10, tresh, 10);
			drawCircles(ip, circles);
			showCircles("10", original, circles);
			System.out.println("10-es erme " + circles.size() + " db :" + circles);
			image.updateAndDraw();
			System.out.println("20as keresese");
			circles = Hough.runHough(ip, (int)Circle.AVG_20, tresh, 10);
			drawCircles(ip, circles);
			showCircles("20", original, circles);
			System.out.println("20-as erme " + circles.size() + " db :" + circles);
			image.updateAndDraw();
			System.out.println("50es keresese");
			circles = Hough.runHough(ip, (int)Circle.AVG_50, tresh, 10);
			drawCircles(ip, circles);
			showCircles("50", original, circles);
			System.out.println("50-es erme " + circles.size() + " db :" + circles);
			image.updateAndDraw();
			System.out.println("100as keresese");
			circles = Hough.runHough(ip, (int)Circle.AVG_100, tresh, 10);
			drawCircles(ip, circles);
			showCircles("100", original, circles);
			System.out.println("100-as erme " + circles.size() + " db :" + circles);
			image.updateAndDraw();
			System.out.println("200as keresese");
			circles = Hough.runHough(ip, (int)Circle.AVG_200, tresh, 10);
			drawCircles(ip, circles);
			showCircles("200", original, circles);
			System.out.println("200-as erme " + circles.size() + " db :" + circles);
			image.updateAndDraw();
			new ImagePlus("Original", original).show();*/
		//}
		image.updateAndDraw();
	}

	private void drawCircles(ImageProcessor ip, Set<Circle> set){
		Iterator<Circle> it = set.iterator();
		while(it.hasNext()){
			Circle c = it.next();
			c.draw(ip, java.awt.Color.WHITE);
			System.out.println(c.getBlueIntensity((ColorProcessor)original));
		}
	}

	private void showCircles(String msg, ImageProcessor ip, Set<Circle> set){
		Iterator<Circle> it = set.iterator();
		ip.setFont(new Font("", 100, 100));
		while(it.hasNext()){
			Circle c = it.next();
			ip.drawString(msg, (int)c.getX(), (int)c.getY());
		}
	}
}
