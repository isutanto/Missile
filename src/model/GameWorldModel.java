
package model;



import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import model.Missile.State;

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
	public static long lastCall;
	private int MAP[][]; 
	private int MAPX = 20;
	private int MAPY = 20;
	Random randomGenerator;
	
	
	public GameWorldModel(){
		
	//Generate background	
	randomGenerator = new Random();
	MAP = new int [MAPX][MAPY];
	for(int i = 0; i < MAPX; i++)
	{ 
	   MAP[i][4] = 6;
	   MAP[i][5] = 1;
	   for(int j = 3; j >= 0; j--)
	   {
		  MAP[i][j] = 7;   
	   }
	}
	for(int i = 0; i < MAPX; i++)
		for(int j = 6; j < MAPY; j++)
		{
			int d = randomGenerator.nextInt(10);
			if(d >= 1)
				MAP[i][j] = 1;
			else
			   MAP[i][j] = 2 + randomGenerator.nextInt(4);
		}
	

			//nice Wave when airplane max speed close to the missile (va = 5.5  Vm = 5.6)

	        aircraft = new Aircraft(new Vector2D (400, -200), 5, new Vector2D(3, 0), 5.4, new Vector2D (900, 100), 300, new Vector2D(5,5), 1, 5.0);
			missile = new Missile(new Vector2D (200, 400), 5, new Vector2D(0, 0), 5.6, new Vector2D (0, 0), 5000, 
					new Vector2D(5, 5), 1.0, 5.0, aircraft);

			
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
			    i7 = ImageIO.read(getClass().getResource("i7.gif") );
			} catch (IOException e) {
				System.out.println("Can't find i7.gif!");
			}
			
			lastCall = System.nanoTime();
			
	}

public Missile getMissile(){
	return missile;
}

public Aircraft getAircraft(){
	return aircraft;
}


public Image getMap(int i,int j) {
	if(i >= MAPX || j >= MAPY)
	{
		resizeMap();
	}
	int imageNumber = 0;
	if(i > 0 && j > 0)
	   imageNumber = MAP[i][j];
	
	
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
public String getMapName(int i,int j) {
	if(i >= MAPX || j >= MAPY)
	{
		resizeMap();
	}
	int imageNumber = MAP[i][j];
	
	
	switch (imageNumber) {
	case 1: return "i1.gif";
	case 2: return "i2.gif";
	case 3: return "i3.gif";
	case 4: return "i4.gif";
	case 5: return "i5.gif";
	case 6: return "i6.gif";
	case 7: return "i7.gif";
	default: return "i1.gif";
	}
	
}
public void resizeMap()
{
   System.out.print("Resizing map...");
   MAPX *= 2;
   MAPY *= 2;
   System.out.println(": New size = " + MAPX + " x " + MAPY + ".");
   
   int temp[][] = new int[MAPX][MAPY];
   for(int i = 0; i < MAPX/2; i++)
	   for(int j = 0; j < MAPY/2; j++)
		   temp[i][j] = MAP[i][j];
   
   
   for(int i = MAPX/2; i < MAPX; i++)
	{ 
	   temp[i][4] = 6;
	   temp[i][5] = 1;
	   for(int j = 3; j >= 0; j--)
	   {
		  temp[i][j] = 7;   
	   }
	}
   
   randomGenerator = new Random();
   for(int i = MAPX/2; i < MAPX; i++)
		for(int j = 6; j < MAPY; j++)
		{
			int d = randomGenerator.nextInt(10);
			if(d >= 1)
				temp[i][j] = 1;
			else
			   temp[i][j] = 2 + randomGenerator.nextInt(4);
		}
   
   for(int i = 0; i < MAPX; i++)
		for(int j = MAPY/2; j < MAPY; j++)
		{
			int d = randomGenerator.nextInt(10);
			if(d >= 1)
				temp[i][j] = 1;
			else
			   temp[i][j] = 2 + randomGenerator.nextInt(4);
		}
  
   
   MAP = temp;
}
public void update() {
	
	missile.update(aircraft, System.nanoTime() - lastCall);
	aircraft.update(missile, System.nanoTime() - lastCall);
	
	if(missile.position.y >= GROUND.y){
		missile.position.setValue(new Vector2D(missile.position.x, GROUND.y));
		missile.velocity.setValue(new Vector2D(0, 0));
		missile.image = kaboom;
	    missile.state = Missile.State.EXPLODE;
	}

	if(missile.position.distance(aircraft.position) <= Missile.BLAST_RADIUS){
		missile.image = kaboom;
		aircraft.image=kaboom;   
		missile.velocity.Zero();
		aircraft.velocity.Zero();
		missile.state = Missile.State.EXPLODE;
	}
	
	if(missile.getMissileState() == State.SELFDESTRUCT)
	{
		missile.image = kaboom;
	}
		
	lastCall = System.nanoTime();
	
}

public BufferedImage boom(){return kaboom;}
public int getMapX(){ return MAPX;}
public int getMapY(){ return MAPY;}
}


