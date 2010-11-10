package COCO;

import java.awt.Point;
import ij.gui.OvalRoi;

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

	public void draw(ij.process.ImageProcessor ip, java.awt.Color c){
		//java.awt.Color c2 = ip.getColor();
		ip.setColor(c);
		int r = (int)radius + border;
		OvalRoi or = new OvalRoi((int)getX()-r, (int)getY()-r, r*2, r*2);
		ip.fill(or);
		//ip.setColor(c2);
	}

	public String toString(){
		return "Circle{x=" + getX() + " y=" + getY() + " radius=" + getRadius() + "}";
	}
}
