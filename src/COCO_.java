import ij.*;
import ij.gui.*;
import ij.plugin.filter.Binary;
import ij.plugin.filter.PlugInFilter;
import ij.process.*;
import java.util.Set;
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
		else if(i == 2){
			Set<Circle> circles = Hough.runHough(ip, (int)Circle.MIN_5, (int)Circle.MAX_5, 1, 240, 10);
			System.out.println(circles);
		}
		image.updateAndDraw();
	}

}
