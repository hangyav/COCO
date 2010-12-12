package COCO;

import java.awt.Point;
import ij.gui.OvalRoi;
import ij.process.ColorProcessor;

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

	public static int YELLOW_LEVEL = 100;
	public static int WHITE_LEVEL = 30;

	private double radius;
	private static final int border = 13;
	private int saturation = -1;

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

	public final void setRadius(double r){
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

	public int getHueIntensity(ColorProcessor ip){
		if(saturation != -1)
			return saturation;
		int color = 0;
		int db = 0;
		byte H[] = new byte[ip.getWidth() * ip.getHeight()],
				S[] = new byte[ip.getWidth() * ip.getHeight()],
				B[] = new byte[ip.getWidth() * ip.getHeight()];
		ip.getHSB(H, S, B);
		int r = sqr((int)radius);
		for(int i=(int)(getX()-radius); i<=(int)(getX()+radius); i++){
			for(int j=(int)(getY()-radius); j<=(int)(getY()+radius); j++){
				int ter = sqr(i-(int)getX()) + sqr(j-(int)getY());
				if(ter <= r){
					db++;
					int pos = j*ip.getWidth() + i;
					int cv = (S[pos]&255);
					if((S[pos]&255) <70){
						//ip.set(pos, 1000);
						cv = 0;
					}
					try{
						color += cv;
					}catch(IndexOutOfBoundsException e){
						System.out.println(e.getMessage());
					}
				}
			}
		}
		saturation = color/db;
		return saturation;
	}

	public boolean isYellow(ColorProcessor cp){
		if(getHueIntensity(cp) > YELLOW_LEVEL)
			return true;
		return false;
	}

	public boolean isWhite(ColorProcessor cp){
		if(getHueIntensity(cp) < WHITE_LEVEL)
			return true;
		return false;
	}

	public boolean isTwoColored(ColorProcessor cp){
		if(!isYellow(cp) && !isWhite(cp))
			return true;
		return false;
	}

	public static int sqr(int a){
		return a*a;
	}

	@Override
	public String toString(){
		return "Circle{x=" + getX() + " y=" + getY() + " radius=" + getRadius() + "}";
	}
}
