package view;

import controller.GameController;
import model.GameWorldModel;
import model.Missile.Guidance;
import model.ModelEvent;
import model.Vector2D;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;



@SuppressWarnings("serial")
public class GameView extends JFrameView implements Runnable, MouseListener{

	  public static final int PWIDTH = 1000;   // size of panel
	  public static final int PHEIGHT = 500; 
	  private static final int NO_DELAYS_PER_YIELD = 16;
	  private static int MAX_FRAME_SKIPS = 5;
	  private long gameStartTime;
	  private Thread animator;           
	  private boolean running = false; 
	  char lastOrien;
	  private static int FPS = 60;
	  private int lastFps;
	  private int fps;
	  private long afterTime;
	  
	  //public static final int GUIDANCE_Y = PHEIGHT/2;
	  public static final int GUIDANCE_Y = 450;
	  public static final int PURSUIT_X = 250;
	  public static final int PROPORTIONAL_X = 500;
	  public static final int PARALLEL_X = 750;
	  	 
	  
	public boolean guidanceSelected;

	  private volatile boolean isPaused = false;
	  private JPanel testPanel;
	  private long period = (long) 1000000000.0/FPS;
	  
		
		BufferedImage parallelButt;
		BufferedImage pursuitButt;
		BufferedImage proportionalButt;
	  
	  int xpos;//for mouse coord
	  int ypos;
	  boolean mouseEntered; 

	  // variable that will be true when the user clicked i the rectangle  
	  // the we will draw. 
	  boolean rect1Clicked; 
	  
	  // off screen rendering
	  private Graphics dbg; 
	  private Image dbImage = null;
	  
	  //coordinates for center of camera.
	  int camx;
	  int camy;
	  
	  int xdif;
	  int ydif;
	  
	

	  public GameView(GameWorldModel model, GameController controller)
	  {
		super(model, controller); 
		testPanel = new JPanel();
		testPanel.setPreferredSize( new Dimension(PWIDTH, PHEIGHT));
		testPanel.setLayout(new BoxLayout(testPanel, BoxLayout.LINE_AXIS));
		//testPanel.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
	    setFocusable(true);
	    requestFocus(); 
	    addMouseListener(this);
	    getContentPane().add(testPanel, BorderLayout.SOUTH);
	    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    setTitle("Missile Launcher!!");
	 
	  
	    
		try {
			 parallelButt = ImageIO.read(getClass().getResource("parallel.gif") );
			 pursuitButt = ImageIO.read(getClass().getResource("pursuit_1.gif") );
			 proportionalButt = ImageIO.read(getClass().getResource("proportional.gif") );
		} catch (IOException e) {
			System.out.println("Can't find button images");
		}
		
	    pack();
	    setResizable(false);  
	    setVisible(true);
	  }



	  public void run()
	  /* The frames of the animation are drawn inside the while loop. */
	  {
		camx = (int) ((GameWorldModel)getModel()).getMissile().getPosition().x - 100;
		camy = (int) ((GameWorldModel)getModel()).getMissile().getPosition().y + 40;
		
		fps = 0;
		lastFps = 0;
	    long beforeTime, timeDiff, sleepTime;
	    long overSleepTime = 0L;
	    int noDelays = 0;
	    long excess = 0L;
	
	    gameStartTime = System.nanoTime();
	    beforeTime = gameStartTime;

		running = true;
		((GameWorldModel)getModel()).update();

		while(running) {
			int prevposx = (int) ((GameWorldModel)getModel()).getMissile().getPosition().x; 
			 	
			int prevposy = (int) ((GameWorldModel)getModel()).getMissile().getPosition().y;
			
			gameUpdate();
			
			int curposx = (int) ((GameWorldModel)getModel()).getMissile().getPosition().x; 
		 	
			int curposy = (int) ((GameWorldModel)getModel()).getMissile().getPosition().y;
			
			xdif = curposx - prevposx;
			ydif = curposy - prevposy;
			
			camx += xdif;
			camy -= ydif;
			
			gameRender(xdif, ydif);
			paintScreen();
			
			fps++;
	     
	     	afterTime = System.nanoTime();
	     	
	     	if((afterTime - gameStartTime) >= 1000000000L){
	     		gameStartTime = System.nanoTime();
	     		lastFps = fps;
	     		fps = 0;
	     	}
	     	timeDiff = afterTime - beforeTime;
	     	sleepTime = (period - timeDiff) - overSleepTime;  

	     	if (sleepTime > 0) {   // some time left in this cycle
	     		try {
	     			Thread.sleep(sleepTime/1000000L);  // nano -> ms
	     		}
	     		catch(InterruptedException ex){}
	     		overSleepTime = (System.nanoTime() - afterTime) - sleepTime;
	     	}
	     	else {    // sleepTime <= 0; the frame took longer than the period
	     		excess -= sleepTime;  // store excess time value
	     		overSleepTime = 0L;

	     		if (++noDelays >= NO_DELAYS_PER_YIELD) {
	     			Thread.yield();   // give another thread a chance to run
	     			noDelays = 0;
	        	}
	        
	     	}

	      beforeTime = System.nanoTime();

	      /* If frame animation is taking too long, update the game state
	         without rendering it, to get the updates/sec nearer to
	         the required FPS. */
	      int skips = 0;
	      while((excess > period) && (skips < MAX_FRAME_SKIPS)) {
	        excess -= period;
	       gameUpdate();
	        skips++;
	      }
	      
		}
	    System.exit(0); 
	  } 
	  
	  public void addNotify()
	  { super.addNotify();
	    startGame();    
	  }
	  private void startGame(){ 
	    if (animator == null || !running) {
	      animator = new Thread(this);
		  animator.start();}
	    }
private void paintScreen(){ 
  Graphics g;
  try {
    g = this.getGraphics();
    if ((g != null) && (dbImage != null))
    g.drawImage(dbImage, 0, 0, null);
   
    g.dispose();
  }
  catch (Exception e)
  { System.out.println("Graphics context error: " + e);  }
}

private void gameRender(int x, int y)
{
	if (dbImage == null){
		dbImage = createImage(PWIDTH, PHEIGHT);
    if (dbImage == null) {
    	System.out.println("dbImage is null");
    	return;
    }
    else
    	dbg = dbImage.getGraphics();
  }
	//draw background
	//dbg.drawImage(((GameWorldModel)getModel()).getMap(), 10, 10, null);
	//dbg.clearRect(0, 0, PWIDTH, PHEIGHT);
	
	
	
	BufferedImage missileImage = ((GameWorldModel)getModel()).getMissile().image;
	BufferedImage aircraftImage = ((GameWorldModel)getModel()).getAircraft().image;
	
	
	
	//draw view
	
	int left = camx / 100 + 5; 
	int bottom = (camy / 100) - 3 ; 
	
	int remx = camx % 100;
	int remy = camy % 100;
	
	
		for(int i = 0; i < 12; i++)
			for(int j = 0; j < 7; j++)
			{
				dbg.drawImage(((GameWorldModel)getModel()).getMap(i + left, j + bottom), -remx + (i * 100), 455 + remy - (j * 100) , null);
			}
		
	
	

	//creating the AffineTransform instance 
	AffineTransform affineTransform = new AffineTransform(); 
	//set the translation to the mid of the component 
	//affineTransform.setToTranslation(((GameWorldModel)getModel()).getMissile().pos().x - missileImage.getWidth()/2,
		//((GameWorldModel)getModel()).getMissile().pos().y - missileImage.getHeight()/2); 
	//affineTransform.setToTranslation(((GameWorldModel)getModel()).getMissile().pos().x - missileImage.getWidth()/2,
		//((GameWorldModel)getModel()).getMissile().pos().y - missileImage.getHeight()/2);
	affineTransform.setToTranslation(PWIDTH / 2 - missileImage.getWidth()/2,
			PHEIGHT / 2 - missileImage.getHeight()); 
	
	//rotate with the anchor point as the mid of the image 
	affineTransform.rotate(Math.toRadians(-((GameWorldModel)getModel()).getMissile().curAngle),  missileImage.getWidth()/2, missileImage.getHeight()/2); 
	//draw the image using the AffineTransform 


	Graphics2D g2d = (Graphics2D)dbg;

	g2d.drawImage(missileImage, affineTransform, this); 
	
	 dbg.setColor(Color.YELLOW);
	  
	  dbg.drawLine((int)
			 	((GameWorldModel)getModel()).getMissile().getPosition().x , 
		 		(int) ((GameWorldModel)getModel()).getMissile().getPosition().y,
		 		(int) ((GameWorldModel)getModel()).getMissile().getPosition().x, 
		 		(int) ((GameWorldModel)getModel()).getMissile().getPosition().y);
	  
	 int misslePosx = (int) ((GameWorldModel)getModel()).getMissile().getPosition().x - missileImage.getWidth()/2;
	 int misslePosy = (int) ((GameWorldModel)getModel()).getMissile().getPosition().y - missileImage.getHeight()/2;
	 
	 int aircraftPosx = (int) ((GameWorldModel)getModel()).getAircraft().getPosition().x - aircraftImage.getWidth()/2;
	 int aircraftPosy = (int) ((GameWorldModel)getModel()).getAircraft().getPosition().y - aircraftImage.getHeight()/2;
	 
	 aircraftPosx = aircraftPosx - misslePosx + 500;
	 aircraftPosy = aircraftPosy - misslePosy + 250;
	  
	
  //draw airplane
	/*drawImage(dbg, aircraftImage, (int)
		 	((GameWorldModel)getModel()).getAircraft().getPosition().x - aircraftImage.getWidth()/2, 
		 		(int) ((GameWorldModel)getModel()).getAircraft().getPosition().y - aircraftImage.getHeight()/2) ;
	*/
	drawImage(dbg, aircraftImage, aircraftPosx, aircraftPosy);
  
  dbg.setFont(new Font(Font.DIALOG, Font.BOLD, 15));
  
  //draw buttons
  
  if(!guidanceSelected){
	drawImage(dbg, pursuitButt, GameView.PURSUIT_X - pursuitButt.getWidth()/2, GUIDANCE_Y - pursuitButt.getHeight()/2 - 8 );

	drawImage(dbg, proportionalButt, GameView.PROPORTIONAL_X - pursuitButt.getWidth()/2, GUIDANCE_Y - proportionalButt.getHeight()/2 - 8 );
	
	drawImage(dbg,parallelButt, GameView.PARALLEL_X - parallelButt.getWidth()/2, GUIDANCE_Y - parallelButt.getHeight()/2 - 8 );
  }
  
 
  
  
//draw trail
  

dbg.setColor(Color.RED);
for (int i = 0; i < ((GameWorldModel)getModel()).getMissile().positionArray.size() - 1; i++){
	  dbg.drawLine(400 -camx + (int)((GameWorldModel)getModel()).getMissile().positionArray.get(i).x,
			-600 + camy + (int)((GameWorldModel)getModel()).getMissile().positionArray.get(i).y, 
			 400 -camx  + (int)((GameWorldModel)getModel()).getMissile().positionArray.get(i + 1).x,
			 -600 + camy + (int)((GameWorldModel)getModel()).getMissile().positionArray.get(i + 1).y);
 }
 

  //FPS
  dbg.setColor(Color.BLACK);
  dbg.drawString("FPS: " + lastFps, 900, 490);
  
  //draw missile's state
  dbg.drawString("Missile state: " + ((GameWorldModel)getModel()).getMissile().state, 800, 35);
  dbg.drawString("Missile guide: " + ((GameWorldModel)getModel()).getMissile().guide, 800, 50);
  dbg.drawString("Distance to target: " + (int) ((GameWorldModel)getModel()).getMissile().distanceToTarget(), 800, 65);
  dbg.drawString("Current Angle: " + ((GameWorldModel)getModel()).getMissile().curAngle, 800, 80);
  dbg.drawString("Desired Angle: " +  (int) ((GameWorldModel)getModel()).getMissile().desiredAngle, 800, 95);
  dbg.drawString("Missile velocity: " + (int) ((GameWorldModel)getModel()).getMissile().velocity().x + ", " + (int) -((GameWorldModel)getModel()).getMissile().velocity().y, 800, 110);
 


  
  //Force values
  dbg.drawString("Time: " + (int) ((GameWorldModel)getModel()).getMissile().timeSeconds, 900, 385);
  dbg.drawString("Thrust: " + ((GameWorldModel)getModel()).getMissile().thrustForce, 900, 400);
  dbg.drawString("Lift: " + ((GameWorldModel)getModel()).getMissile().liftForce, 900, 415);
  dbg.drawString("Drag: " + ((GameWorldModel)getModel()).getMissile().dragForce, 900, 430);
  dbg.drawString("Weight: " + ((GameWorldModel)getModel()).getMissile().weight, 900, 445);
  dbg.drawString("Map Area: " + "(" + left + ", " + bottom + ", " + (left + 12) + ", " + (bottom + 7) + ")", 825, 460);
 // dbg.drawString("G-Force: " + (int) ((GameWorldModel)getModel()).getMissile().gForce.y, 900, 460);

 
 
}
private void drawImage(Graphics dbg2, Image image, int x, int y){ 
  //if (image == null) {
   // dbg2.setColor(Color.yellow);
    //dbg2.fillRect(x, y, 20, 20);
   // dbg2.setColor(Color.black);
   // dbg2.drawString("??", x+10, y+10);
  //}
 // else
	
    dbg2.drawImage(image, x, y, this);
}
private void gameUpdate(){
	if(!isPaused && guidanceSelected)
		((GameWorldModel)getModel()).update();
	else if (((GameWorldModel)getModel()).getMissile().isPressed)
		((GameWorldModel)getModel()).getMissile().dragMissile();
	else if (((GameWorldModel)getModel()).getAircraft().isPressed)
		((GameWorldModel)getModel()).getAircraft().dragAircraft();
}

public void mouseClicked (MouseEvent me) { 
	
	if(!guidanceSelected){
		//pursuit distance
		double distPursuit = Math.sqrt( (me.getX() - GameView.PURSUIT_X) * (me.getX() - GameView.PURSUIT_X) + (me.getY() - GameView.GUIDANCE_Y) * (me.getY() - GameView.GUIDANCE_Y));
	
		//proportional distance
		double distProp = Math.sqrt( (me.getX() - GameView.PROPORTIONAL_X) * (me.getX() - GameView.PROPORTIONAL_X) + (me.getY() - GameView.GUIDANCE_Y) * (me.getY() - GameView.GUIDANCE_Y));

		//parallel distance
		double distParallel = Math.sqrt( (me.getX() - GameView.PARALLEL_X) * (me.getX() - GameView.PARALLEL_X) + (me.getY() - GameView.GUIDANCE_Y) * (me.getY() - GameView.GUIDANCE_Y));

		if(distPursuit < 50){
			((GameController)getController()).operation("pursuit");
			guidanceSelected = true;
			((GameWorldModel)getModel()).lastCall = System.nanoTime();	
		}
		else if(distProp < 50){
			((GameController)getController()).operation("proportional");
			guidanceSelected = true;
			((GameWorldModel)getModel()).lastCall = System.nanoTime();
		}
		else if (distParallel < 50){
			((GameController)getController()).operation("parallel");
			guidanceSelected = true;
			((GameWorldModel)getModel()).lastCall = System.nanoTime();
		}
	}else
		((GameController)getController()).operation(isPaused);
}


public void pauseGame(){
	isPaused = true;
}

public void resumeGame(){
	isPaused = false;
	((GameWorldModel)getModel()).lastCall = System.nanoTime();
}





	@Override
	public void modelChanged(ModelEvent event) {}
	@Override
	public void mouseEntered(MouseEvent arg0) {}
	@Override
	public void mouseExited(MouseEvent arg0) {}
	@Override
	public void mouseReleased(MouseEvent me) {
		((GameWorldModel)getModel()).getMissile().isPressed = false;
		((GameWorldModel)getModel()).getAircraft().isPressed = false;
	}



	@Override
	public void mousePressed(MouseEvent me) {
		
		if(((GameWorldModel)getModel()).getMissile().guide == Guidance.NONE){
			double distMissile = Math.sqrt( (me.getX() - ((GameWorldModel)getModel()).getMissile().pos().x * (me.getX() - ((GameWorldModel)getModel()).getMissile().pos().x) 
				+ (me.getY() - ((GameWorldModel)getModel()).getMissile().pos().y) * (me.getY() - ((GameWorldModel)getModel()).getMissile().pos().y)));
	
			double distAircraft = Math.sqrt( (me.getX() - ((GameWorldModel)getModel()).getAircraft().pos().x * (me.getX() - ((GameWorldModel)getModel()).getAircraft().pos().x) 
				+ (me.getY() - ((GameWorldModel)getModel()).getAircraft().pos().y) * (me.getY() - ((GameWorldModel)getModel()).getAircraft().pos().y)));
		
			if(distMissile < 70)
				((GameWorldModel)getModel()).getMissile().isPressed = true;
			else if(distAircraft < 70)
				((GameWorldModel)getModel()).getAircraft().isPressed = true;
		}
	}
}