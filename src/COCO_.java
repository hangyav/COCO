import ij.*;
import ij.gui.Roi;
import ij.plugin.filter.Binary;
import ij.plugin.filter.PlugInFilter;
import ij.process.*;

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
		image = im;
		new ImageConverter(image).convertToGray8();
		return DOES_ALL; //Milyen kepeket kezel a plugin (ALL)
	}

	/**
	* A metodus hajtja vegre a megfelelo trszformaciot a kapott
	* {@code ImageProcessor} segitsegevel.
	*
	* @param ip Az objektum kezeli a kepet.
	*/
	public void run(ImageProcessor ip){
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
		image.updateAndDraw();
	}
}
