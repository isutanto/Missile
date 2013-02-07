package model;

import java.awt.image.BufferedImage;
import java.awt.image.*;

public abstract class BaseGameEntity {


	/**
	 * 
	 * @param time_elapsed
	 */

	
	protected Vector2D position;
	protected Vector2D scale;
	public BufferedImage image;
	
	protected double boundingRadius;
	

	public void update(double delta) {}
	public abstract void render();
	
	
	public Vector2D pos() { return position; }
	public void setPos(Vector2D pos) { position = pos; }
	
	public double getBRadius() { return boundingRadius; }
	public void setBRadius(double r) { boundingRadius = r; }
	
	
	public Vector2D scale() { return scale; }
	protected void setScale(Vector2D val) { boundingRadius *= Math.max(val.x, val.y) / Math.max(scale.x, scale.y); scale = val; }
	public void setScale(double val) { boundingRadius *= (val / Math.max(scale.x, scale.y)); scale = new Vector2D(val, val); }
	
	


}