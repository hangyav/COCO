package COCO;

import ij.ImagePlus;
import java.awt.Point;
import ij.gui.OvalRoi;
import ij.gui.Roi;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

/**
* Egy kort reprezental. A kozeppontjat es a sugarat tarolja.
*/
public class Circle extends Point{
	/**
	* Meretek
	*/
	public static final double MAX_5 = 153.502;
	public static final double MIN_5 = 149.942;
	public static final double MAX_10 = 177.496;
	public static final double MIN_10 = 172.806;
	public static final double MAX_20 = 189.407;
	public static final double MIN_20 = 186.108;
	public static final double MAX_50 = 197.747;
	public static final double MIN_50 = 191.110;
	public static final double MAX_100 = 171.584;
	public static final double MIN_100 = 167.445;
	public static final double MAX_200 = 203.856;
	public static final double MIN_200 = 197.191;
	public static final double AVG_5 = 152.146;
	public static final double AVG_10 = 175.752;
	public static final double AVG_20 = 187.836;
	public static final double AVG_50 = 194.025;
	public static final double AVG_100 = 169.946;
	public static final double AVG_200 = 200.909;

	private double radius;
	private static final int border = 10;

	public Circle(int x, int y, double rad){
		super(x, y);
		setRadius(rad);
	}

	public Circle(Point p, double rad){
		super(p);
		setRadius(rad);
	}

	public double getRadius(){
		return radius;
	}

	public void setRadius(double r){
		radius = r;
	}

	public OvalRoi getRoi(int border){
		int r = (int)radius + border;
		return new OvalRoi((int)getX()-r, (int)getY()-r, r*2, r*2);
	}

	public OvalRoi getRoi(){
		return getRoi(0);
	}

	public void draw(ij.process.ImageProcessor ip, java.awt.Color c){
		//java.awt.Color c2 = ip.getColor();
		ip.setColor(c);
		OvalRoi or = getRoi(border);
		ip.fill(or);
		//ip.setColor(c2);
	}

	public int getBlueIntensity(ColorProcessor ip){
		int color = 0;
		OvalRoi or = getRoi();
		//ip.setMask(or.getMask());
		/*ip.setRoi(or);
		ImageProcessor ip2 = ip;
		ip2.setColor(0);
		ip2.fillOutside(or);*/
		byte R[] = new byte[ip.getWidth() * ip.getHeight()],
				G[] = new byte[ip.getWidth() * ip.getHeight()],
				B[] = new byte[ip.getWidth() * ip.getHeight()];
		ip.getRGB(R, G, B);
		int r = sqr((int)radius);
		int db = 0;
		for(int i=(int)(getX()-radius); i<=(int)(getX()+radius); i++){
			for(int j=(int)(getY()-radius); j<=(int)(getY()+radius); j++){
				int ter = sqr(i-(int)getX()) + sqr(j-(int)getY());
				if(ter <= r){
					color += B[i*j];
					db++;
				}
			}
		}
		//new ImagePlus("asdasdasd", ip2).show();
		//return (int)(color/sqr((int)radius)*Math.PI);
		return color/db;
	}

	public static int sqr(int a){
		return a*a;
	}

	public String toString(){
		return "Circle{x=" + getX() + " y=" + getY() + " radius=" + getRadius() + "}";
	}
}
