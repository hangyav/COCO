package COCO;

import java.awt.Point;
import ij.gui.OvalRoi;
import ij.process.ColorProcessor;
import java.awt.Color;

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

	public static final Color COLOR_5 = Color.YELLOW;
	public static final Color COLOR_10 = Color.WHITE;
	public static final Color COLOR_20 = Color.YELLOW;
	public static final Color COLOR_50 = Color.WHITE;
	public static final Color COLOR_100 = Color.GRAY;
	public static final Color COLOR_200 = Color.GRAY;

	public static int YELLOW_LEVEL = 100;
	public static int WHITE_LEVEL = 30;

	private static final int border = 13;

	private double radius;
	private int saturation = -1;
	private int value;

	public Circle(int x, int y, double rad){
		super(x, y);
		setRadius(rad);
	}

	public Circle(Point p, double rad){
		super(p);
		setRadius(rad);
	}

	public Circle(int x, int y, double rad, int value){
		super(x, y);
		setType(value);
		setRadius(rad);
	}

	public Circle(Point p, double rad, int value){
		super(p);
		setType(value);
		setRadius(rad);
	}

	public final int getType() {
		return value;
	}

	public final void setType(int value){
		this.value = value;
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

	public Color getColor(ColorProcessor cp){
		if(isYellow(cp))
			return Color.YELLOW;
		if(isWhite(cp))
			return Color.WHITE;
		return Color.GRAY;
	}

	public Color getColorByType(){
		switch(value){
			case 5: return COLOR_5;
			case 10: return COLOR_10;
			case 20: return COLOR_20;
			case 50: return COLOR_50;
			case 100: return COLOR_100;
			case 200: return COLOR_200;
			default:return null;
		}
	}

	public static double getDistance(Circle c1, Circle c2){
		double xDis = c1.getX()-c2.getX();
		double yDis = c1.getY()-c2.getY();
		return Math.sqrt(xDis*xDis + yDis*yDis);
	}

	public double getDistance(Circle c){
		return getDistance(this, c);
	}

	@Override
	public String toString(){
		return "Circle{x=" + getX() + " y=" + getY() + " radius=" + getRadius() + "}";
	}
}
