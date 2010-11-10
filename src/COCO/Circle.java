package COCO;

import java.awt.Point;

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

	private double radius;

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

	public String toString(){
		return "Circle{x=" + getX() + " y=" + getY() + " radius=" + getRadius() + "}";
	}
}
