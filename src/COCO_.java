import ij.*;
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
		return DOES_ALL; //Milyen kepeket kezel a plugin (ALL)
	}

	/**
	* A metodus hajtja vegre a megfelelo trszformaciot az kapott
	* {@code ImageProcessor} segitsegevel.
	*
	* @param ip Az objektum kezeli a kepet.
	*/
	public void run(ImageProcessor ip){
		ip.invert();
		image.updateAndDraw();
	}
}
