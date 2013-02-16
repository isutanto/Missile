package model;



import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import view.GameView;import java.awt.image.*;
import java.awt.*;
import java.util.Random;


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
	BufferedImage i1,i2,i3,i4,i5,i6,i7;
	BufferedImage kaboom;
	public long lastCall;
	private int MAP[][]; 
	private int MAPX = 100;
	private int MAPY = 100;
	Random randomGenerator;
	
	
	public GameWorldModel(){
		
	//Generate background	
	randomGenerator = new Random();
	MAP = new int [MAPX][MAPY];
	for(int i = 0; i < MAPX; i++)
	{ 
	   MAP[i][4] = 6;
	   MAP[i][5] = MAP[i][6] = 1;
	   for(int j = 3; j >= 0; j--)
	   {
		  MAP[i][j] = 7;   
	   }
	}
	for(int i = 0; i < MAPX; i++)
		for(int j = 7; j < MAPY; j++)
		{
			int d = randomGenerator.nextInt(10);
			if(d >= 1)
				MAP[i][j] = 1;
			else
			   MAP[i][j] = 2 + randomGenerator.nextInt(4);
		}
	
			//aircraft = new Aircraft(new Vector2D (500, 100), 5, new Vector2D(5.2, 0), 400, new Vector2D (900, 100), 300, new Vector2D(5,5), 1, 2.0);
	        aircraft = new Aircraft(new Vector2D (400, -1000), 5, new Vector2D(5.2, 0), 400, new Vector2D (900, 100), 300, new Vector2D(5,5), 1, 2.0);
			missile = new Missile(new Vector2D (200, 440), 5, new Vector2D(0, 0), 2, new Vector2D (0, 0), 5000, 
					new Vector2D(0, 0), 1, 2, aircraft);
			
			aircraft.getSteering().setTarget(missile); 
			
			try {
			    background = ImageIO.read(getClass().getResource("background.gif") );
			    
			} catch (IOException e) {
				System.out.println("Can't find background.gif!");
			}
			
			//Read in images
			try {
			    kaboom = ImageIO.read(getClass().getResource("kaboom.gif") );
			} catch (IOException e) {
				System.out.println("Can't find kaboom.gif!");
			}
			try {
			    i1 = ImageIO.read(getClass().getResource("i1.gif") );
			} catch (IOException e) {
				System.out.println("Can't find i1.gif!");
			}
			try {
			    i2 = ImageIO.read(getClass().getResource("i2.gif") );
			} catch (IOException e) {
				System.out.println("Can't find i2.gif!");
			}
			try {
			    i3 = ImageIO.read(getClass().getResource("i3.gif") );
			} catch (IOException e) {
				System.out.println("Can't find i3.gif!");
			}
			try {
			    i4 = ImageIO.read(getClass().getResource("i4.gif") );
			} catch (IOException e) {
				System.out.println("Can't find i4.gif!");
			}
			try {
			    i5 = ImageIO.read(getClass().getResource("i5.gif") );
			} catch (IOException e) {
				System.out.println("Can't find i5.gif!");
			}
			try {
			    i6 = ImageIO.read(getClass().getResource("i6.gif") );
			} catch (IOException e) {
				System.out.println("Can't find i6.gif!");
			}
			try {
			    i7 = ImageIO.read(getClass().getResource("i7.png") );
			} catch (IOException e) {
				System.out.println("Can't find i7.png!");
			}
			
			lastCall = System.nanoTime();
			
	}

public Missile getMissile(){
	return missile;
}

public Aircraft getAircraft(){
	return aircraft;
}

/*public Image getMap() {
	return background;
}*/

public Image getMap(int i,int j) {
	
	int imageNumber = MAP[i][j];
	
	switch (imageNumber) {
	case 1: return i1;
	case 2: return i2;
	case 3: return i3;
	case 4: return i4;
	case 5: return i5;
	case 6: return i6;
	case 7: return i7;
	default: return i1;
	}
	
}

public void update() {
	
	missile.update(aircraft, System.nanoTime() - lastCall);
	aircraft.update(missile, System.nanoTime() - lastCall);
	
	/*if(aircraft.position.x > GameView.PWIDTH)
		aircraft.position.x = 0;*/
	if(missile.position.y >= GROUND.y){
		missile.position.setValue(new Vector2D(missile.position.x, GROUND.y));
		missile.velocity.setValue(new Vector2D(0, 0));
		missile.image = kaboom;
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
