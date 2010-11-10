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
		prepare(ip);
		image.updateAndDraw();
	}
}
