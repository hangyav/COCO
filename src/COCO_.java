import ij.*;
import ij.gui.*;
import ij.plugin.filter.Binary;
import ij.plugin.filter.PlugInFilter;
import ij.plugin.filter.ParticleAnalyzer;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.frame.RoiManager;
import ij.process.*;
import java.util.Iterator;
import java.util.Set;
import COCO.Hough;
import COCO.Circle;
import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

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
		ByteProcessor bp = (ByteProcessor)ip.convertToByte(false);
		//close:
		bp.dilate(1,background);
		bp.erode(1,background);
		//felesleges kitoltesek eltavolitasa 1:
		ResultsTable rt = new ResultsTable();
		ParticleAnalyzer panal = new ParticleAnalyzer(ParticleAnalyzer.ADD_TO_MANAGER,Measurements.CENTER_OF_MASS|Measurements.AREA,rt,10000.,2000000.,0.,1.);//inf-nel az egeszet bejeloli...
		ip.invertLut();
		panal.analyze(image);
		ip.invertLut();
		//System.out.println("itt1");
		RoiManager roiMan = RoiManager.getInstance();
		//Hashtable rois = roiMan.getROIs();
		//Roi[] rois2 = roiMan.getRoisAsArray();
		Roi[] rois2;
		if(null!=roiMan){
			rois2 = roiMan.getRoisAsArray();
		}else{
			rois2 = new Roi[0];
		}
		//System.out.println("itt2");

		Binary b = new Binary();
		b.setup("fill", image);
		b.run(ip);
		//felesleges pottyek eltavolitasa:
		bp.erode(2,background);
		bp.dilate(2,background);
		//felesleges kitoltesek kivonasa, 2:
		for(int i=0; i<rois2.length; i++){
			roiMan.select(i);
			ip.setColor(255);
			ip.fill(rois2[i]);
		}
		if(null!=roiMan){
			roiMan.close();
		}
		IJ.run("Select None");
		/////////////////////////////

		//objektumok lekerese, hogy korvonalazzuk oket////////////////
		//ImagePlus tempImagePlus = (ImagePlus)image.clone();
		//ByteProcessor bp2 = new ByteProcessor(image.getImage());
	//ByteProcessor bp2 = (ByteProcessor)ip.duplicate();
		//ByteProcessor bp2 = (ByteProcessor)tempImage.convertToByte(false);
/*		bp.erode(3,background);
		bp.erode(3,background);

		panal = new ParticleAnalyzer(ParticleAnalyzer.ADD_TO_MANAGER,Measurements.CENTER_OF_MASS|Measurements.AREA,rt,10000.,2000000.,0.,1.);
		panal.analyze(image,bp);
		bp.dilate(3,background);
		bp.dilate(3,background);
		roiMan = RoiManager.getInstance();
		Roi[] rois3 = roiMan.getRoisAsArray();
		for(int i=0; i<rois3.length; i++){
			roiMan.select(i);
			ip.setColor(255);
			ip.fill(rois3[i]);
		}
		System.out.println("var3");
		roiMan.close();
		IJ.run("Select None");
*/
		/////////////////////////////////////////////////////////////
		/*for(int i=0; i<rois2.length; i++){
			roiMan.select(i);
			//IJ.wait(1500);
			ip.setColor(0);
			ip.fill(rois2[i]);
		}*/
		//System.out.println(rt);


		//System.out.println(ip.isBinary());
		bp.outline();
		IJ.wait(50);
		ip.invertLut();
		ip.invert();
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
		prepare(ip);
		int[] th = original.getHistogram();
		double tmp = 0;
		int db = 0;
		for(int i=0;i<th.length;i++){
			tmp += i*th[i];
			db += th[i];
		}
		int tresh = (int)tmp/db + 30;
		Set<Circle>  allcircles = new HashSet<Circle>();

		System.out.println("5os keresese");
		Set<Circle> circles = Hough.runHough(ip, (int)Circle.AVG_5, tresh, 10, 5);//lehet max 15tel magasabbra
		allcircles.addAll(circles);
		drawCircles(ip, circles);
		image.updateAndDraw();

		System.out.println("100as keresese");
		circles = Hough.runHough(ip, (int)Circle.AVG_100, tresh, 10, 100);
		allcircles.addAll(circles);
		drawCircles(ip, circles);
		image.updateAndDraw();

		System.out.println("10es keresese");
		circles = Hough.runHough(ip, (int)Circle.AVG_10, tresh, 10, 10);
		allcircles.addAll(circles);
		drawCircles(ip, circles);
		image.updateAndDraw();

		System.out.println("20as keresese");
		circles = Hough.runHough(ip, (int)Circle.AVG_20, tresh, 10, 20);//lehet max 10zel magasabbra, de neha kisebb kellene
		allcircles.addAll(circles);
		drawCircles(ip, circles);
		image.updateAndDraw();

		System.out.println("50es keresese");
		circles = Hough.runHough(ip, (int)Circle.AVG_50, tresh, 10, 50);
		allcircles.addAll(circles);
		drawCircles(ip, circles);
		image.updateAndDraw();

		System.out.println("200as keresese");
		circles = Hough.runHough(ip, (int)Circle.AVG_200, tresh, 10, 200);
		allcircles.addAll(circles);
		drawCircles(ip, circles);
		image.updateAndDraw();
		System.out.println();

		circles = analyze(allcircles);
		showCircles(original, circles);
		//new ImagePlus("Original", original).show();

		image.setProcessor(image.getTitle(), original);
		image.updateAndDraw();
	}

	private void drawCircles(ImageProcessor ip, Set<Circle> set){
		Iterator<Circle> it = set.iterator();
		while(it.hasNext()){
			Circle c = it.next();
			if(c.getColor((ColorProcessor)original) == c.getColorByType())
				c.draw(ip, java.awt.Color.WHITE);
		}
	}

	private void showCircles(ImageProcessor ip, Set<Circle> set){
		Iterator<Circle> it = set.iterator();
		ip.setFont(new Font("", 100, 100));
		ip.setLineWidth(5);
		int sum = 0;
		while(it.hasNext()){
			Circle c = it.next();
			sum += c.getType();
			ip.setColor(Color.GREEN);
			ip.draw(c.getRoi());
			ip.setColor(Color.BLACK);
			/*if(c.isYellow((ColorProcessor)original))
				ip.drawString("Y", (int)c.getX()-50, (int)c.getY()-50);
			if(c.isWhite((ColorProcessor)original))
				ip.drawString("W", (int)c.getX(), (int)c.getY());
			if(c.isTwoColored((ColorProcessor)original))
				ip.drawString("T", (int)c.getX(), (int)c.getY());*/
			//ip.drawString(c.getHueIntensity((ColorProcessor)original)+"", (int)c.getX()-100, (int)c.getY()-100);
			ip.drawString(c.getType()+"", (int)c.getX(), (int)c.getY());
		}
		ip.setColor(Color.ORANGE);
		ip.drawString(sum + " Ft.", ip.getWidth()/3, 110);
	}

	private Set<Circle> analyze(Set<Circle> circles){
		Set<Circle> cir = new HashSet<Circle>();
		Iterator<Set<Circle>> it = getCircleGroups(circles).iterator();
		while(it.hasNext()){
			Set<Circle> cset = it.next();
			if(cset.size() == 1){
				cir.add(cset.iterator().next());
				continue;
			}
			Iterator<Circle> cit = cset.iterator();
			while(cit.hasNext()){
				Circle c = cit.next();
				if(c.getColor((ColorProcessor)original) == c.getColorByType()){
					cir.add(c);
					break;
				}
			}
		}
		return cir;
	}

	private List<Set<Circle>> getCircleGroups(Set<Circle> circles){
		List<Set<Circle>> list = new ArrayList<Set<Circle>>();
		Iterator<Circle> it = circles.iterator();
		while(it.hasNext()){
			Circle c = it.next();
			boolean b = true;
			Iterator<Set<Circle>> lit = list.iterator();
			while(lit.hasNext() && b){
				Set<Circle> cset = lit.next();
				Iterator<Circle> cit = cset.iterator();
				while(cit.hasNext()){
					Circle cir = cit.next();
					if(c.getDistance(cir) < Circle.AVG_5/2){
						cset.add(c);
						b = false;
						break;
					}
				}
			}
			if(b){
				Set<Circle> cset = new TreeSet<Circle>();
				cset.add(c);
				list.add(cset);
			}
		}
		return list;
	}
}
