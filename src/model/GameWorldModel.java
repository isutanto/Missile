package model;



import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import view.GameView;import java.awt.image.*;
import java.awt.*;


/**
 * 
 * @author Jason Herzog
 *Class GameWorldModel is a singleton class shared by other classes. 
 *GameWorldModel handles events between entities, collision methods
 * and initialization of all world objects
 */
public class GameWorldModel extends AbstractModel {
	 
	public static final Vector2D GROUND = new Vector2D(0, 460);
	private Aircraft aircraft;
	private Missile missile;
	BufferedImage background;
	BufferedImage kaboom;
	public long lastCall;
	
	
	public GameWorldModel(){
			aircraft = new Aircraft(new Vector2D (500, 100), 5, new Vector2D(2.2, 0), 400, new Vector2D (900, 100), 300, new Vector2D(5,5), 1, 2.0);
			
			missile = new Missile(new Vector2D (100, 440), 5, new Vector2D(0, 0), 2, new Vector2D (0, 0), 5000, 
					new Vector2D(0, 0), 1, 2, aircraft);
			
			aircraft.getSteering().setTarget(missile); 
			
			try {
			    background = ImageIO.read(getClass().getResource("background.gif") );
			} catch (IOException e) {
				System.out.println("Can't find background.gif!");
			}
			
			try {
			    kaboom = ImageIO.read(getClass().getResource("kaboom.gif") );
			} catch (IOException e) {
				System.out.println("Can't find kaboom.gif!");
			}
			
			lastCall = System.nanoTime();
			
	}

public Missile getMissile(){
	return missile;
}

public Aircraft getAircraft(){
	return aircraft;
}

public Image getMap() {
	return background;
}

public void update() {
	
	missile.update(aircraft, System.nanoTime() - lastCall);
	aircraft.update(missile, System.nanoTime() - lastCall);
	
	/*if(aircraft.position.x > GameView.PWIDTH)
		aircraft.position.x = 0;*/
	if(missile.position.y >= GROUND.y){
		missile.position.setValue(new Vector2D(missile.position.x, GROUND.y));
		missile.velocity.setValue(new Vector2D(0, 0));	
	}
	
	if(missile.position.distance(aircraft.position) <= 50){
		missile.image = kaboom;
		aircraft.image=kaboom;
		missile.velocity.Zero();
		aircraft.velocity.Zero();
		missile.state = Missile.State.EXPLODE;
	}
		
	lastCall = System.nanoTime();
	
}
}
