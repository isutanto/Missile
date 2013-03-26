package view;

import controller.GameController;
import model.GameWorldModel;
import model.Missile;
import model.Missile.Guidance;
import model.Missile.State;
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
import java.awt.event.KeyEvent;
import java.io.IOException;


import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JScrollPane;  
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Action;

import java.io.*;



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
	  
	  boolean mouseAction = false;
	  
	  //public static final int GUIDANCE_Y = PHEIGHT/2;
	  public static final int GUIDANCE_Y = 450;
	  public static final int PURSUIT_X = 250;
	  public static final int PROPORTIONAL_X = 500;
	  public static final int PARALLEL_X = 750;
	  
	  //Used for screen dragging.
	  
	  private int mxLast;
	  private int myLast;
	  
	  private boolean imageDumped = false;
	  private boolean askForDump = false;
	  	 
	  
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
		getContentPane().add(testPanel);
		
		/*testPanel.getInputMap().put(KeyStroke.getKeyStroke("F2"),
	              "doSomething");
		*/
		testPanel.setPreferredSize( new Dimension(PWIDTH, PHEIGHT));
		testPanel.setLayout(new BoxLayout(testPanel, BoxLayout.LINE_AXIS));
		//testPanel.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
		//JScrollPane scrollBar=new JScrollPane(testPanel,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);  
		//JScrollPane scrollBar=new JScrollPane(testPanel);  
		
		//scrollBar.setSize(PWIDTH,PHEIGHT);  
	  //  scrollBar.setLocation(PWIDTH,0);  
	   
		//this.getContentPane().add(scrollBar, BorderLayout.CENTER);    
		
		
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
	    
	   // this.requestFocusInWindow();
	   
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
	if(!askForDump && ((GameWorldModel)getModel()).getMissile().state == State.EXPLODE)
	{
		System.out.println("Click for image dump.");
		askForDump = true;
	}

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
	
	
		for(int i = -1; i < 12; i++)
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
	
	/*if(((GameWorldModel)getModel()).getMissile().state == State.EXPLODE)
		affineTransform.setToTranslation(((GameWorldModel)getModel()).getMissile().pos().x - missileImage.getWidth()/2,
				((GameWorldModel)getModel()).getMissile().pos().y - missileImage.getHeight()/2);
	
	 */
	 affineTransform.setToTranslation(PWIDTH / 2 - missileImage.getWidth()/2,
			PHEIGHT / 2 - missileImage.getHeight()/2);
	
	//rotate with the anchor point as the mid of the image 
		
	 affineTransform.rotate(Math.toRadians(-((GameWorldModel)getModel()).getMissile().curAngle),  missileImage.getWidth()/2, missileImage.getHeight()/2); 
	//draw the image using the AffineTransform 


	Graphics2D g2d = (Graphics2D)dbg;

	
	//g2d.drawImage(missileImage, affineTransform, this); 
	
	
	//Not sure what this draws.
	
	/* dbg.setColor(Color.YELLOW);
	  
	  dbg.drawLine((int)
			 	((GameWorldModel)getModel()).getMissile().getPosition().x , 
		 		(int) ((GameWorldModel)getModel()).getMissile().getPosition().y,
		 		(int) ((GameWorldModel)getModel()).getMissile().getPosition().x, 
		 		(int) ((GameWorldModel)getModel()).getMissile().getPosition().y);
		 		*/
	  
	 int missilePosx = (int) ((GameWorldModel)getModel()).getMissile().getPosition().x - missileImage.getWidth()/2;
	 int missilePosy = (int) ((GameWorldModel)getModel()).getMissile().getPosition().y - missileImage.getHeight()/2;
	 
	 int aircraftPosx = (int) ((GameWorldModel)getModel()).getAircraft().getPosition().x - aircraftImage.getWidth()/2;
	 int aircraftPosy = (int) ((GameWorldModel)getModel()).getAircraft().getPosition().y - aircraftImage.getHeight()/2;
	 
	 aircraftPosx = aircraftPosx - missilePosx + 500;
	 aircraftPosy = aircraftPosy - missilePosy + 250;
	 
	 if(((GameWorldModel)getModel()).getMissile().state == State.EXPLODE)
		  affineTransform.setToTranslation(missilePosx - camx + 395,
					missilePosy + camy - 583);
			
	 g2d.drawImage(missileImage, affineTransform, this); 
	 
	 if(((GameWorldModel)getModel()).getMissile().state != State.EXPLODE)	 
	 drawImage(dbg, aircraftImage, aircraftPosx, aircraftPosy);
	  
	
  //draw airplane
	/*drawImage(dbg, aircraftImage, (int)
		 	((GameWorldModel)getModel()).getAircraft().getPosition().x - aircraftImage.getWidth()/2, 
		 		(int) ((GameWorldModel)getModel()).getAircraft().getPosition().y - aircraftImage.getHeight()/2) ;
	*/
	//drawImage(dbg, aircraftImage, aircraftPosx, aircraftPosy);
  
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
  dbg.drawString("Missile state: " + ((GameWorldModel)getModel()).getMissile().state, 700, 35);
  dbg.drawString("Missile guide: " + ((GameWorldModel)getModel()).getMissile().guide, 700, 50);
  dbg.drawString("Distance to target: " + (int) ((GameWorldModel)getModel()).getMissile().distanceToTarget(), 700, 65);
  dbg.drawString("Current Angle: " + ((GameWorldModel)getModel()).getMissile().curAngle, 700, 80);
  dbg.drawString("Desired Angle: " +  (int) ((GameWorldModel)getModel()).getMissile().desiredAngle, 700, 95);
  dbg.drawString("Missile velocity: " + (int) ((GameWorldModel)getModel()).getMissile().velocity().x + ", " + (int) -((GameWorldModel)getModel()).getMissile().velocity().y, 700, 110);
 


  
  //Force values
  dbg.drawString("Time: " + (int) ((GameWorldModel)getModel()).getMissile().timeSeconds, 700, 345);
  dbg.drawString("Thrust: " + ((GameWorldModel)getModel()).getMissile().thrustForce, 700, 360);
  dbg.drawString("Lift: " + ((GameWorldModel)getModel()).getMissile().liftForce, 700, 375);
  dbg.drawString("Drag: " + ((GameWorldModel)getModel()).getMissile().dragForce, 700, 390);
  dbg.drawString("Weight: " + ((GameWorldModel)getModel()).getMissile().weight, 700, 405);
  dbg.drawString("Map Area: " + "(" + left + ", " + bottom + ", " + (left + 12) + ", " + (bottom + 7) + ")", 700, 420);
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
	
	//Switch between these first 2 ifs for cool stuff
	
	if(!isPaused && guidanceSelected && ((GameWorldModel)getModel()).getMissile().getMissileState()!= State.EXPLODE)
	//if(!isPaused )
		((GameWorldModel)getModel()).update();
	/*else if (((GameWorldModel)getModel()).getMissile().isPressed)
		((GameWorldModel)getModel()).getMissile().dragMissile();
	else if (((GameWorldModel)getModel()).getAircraft().isPressed)
		((GameWorldModel)getModel()).getAircraft().dragAircraft();*/
	
	
	// prompt for action from user, dump data, dump image, restart game.
	if(mouseAction)
		dragView();
}

public void mouseClicked (MouseEvent me) { 
	
	if(!guidanceSelected){
		//pursuit distance
		double distPursuit = Math.sqrt( (me.getX() - GameView.PURSUIT_X) * (me.getX() - GameView.PURSUIT_X) + (me.getY() - GameView.GUIDANCE_Y) * (me.getY() - GameView.GUIDANCE_Y));
	
		//proportional distance
		double distProp = Math.sqrt( (me.getX() - GameView.PROPORTIONAL_X) * (me.getX() - GameView.PROPORTIONAL_X) + (me.getY() - GameView.GUIDANCE_Y) * (me.getY() - GameView.GUIDANCE_Y));

		//parallel distance
		double distParallel = Math.sqrt( (me.getX() - GameView.PARALLEL_X) * (me.getX() - GameView.PARALLEL_X) + (me.getY() - GameView.GUIDANCE_Y) * (me.getY() - GameView.GUIDANCE_Y));

		if(distPursuit < Missile.BLAST_RADIUS){
			((GameController)getController()).operation("pursuit");
			guidanceSelected = true;
			((GameWorldModel)getModel()).lastCall = System.nanoTime();	
		}
		else if(distProp < Missile.BLAST_RADIUS){
			((GameController)getController()).operation("proportional");
			guidanceSelected = true;
			((GameWorldModel)getModel()).lastCall = System.nanoTime();
		}
		else if (distParallel < Missile.BLAST_RADIUS){
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


public void restart()
{
	
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
		mouseAction = false;
	}

	

	public void dragView(){
		
		 
	
		if(((GameWorldModel)getModel()).getMissile().state == State.EXPLODE)
		{
		
	     	int ydif = 0;
		    int xdif = 0;
		 
	     	xdif = MouseInfo.getPointerInfo().getLocation().x - mxLast;
		    ydif = MouseInfo.getPointerInfo().getLocation().y - myLast;
		
	
		
		
		    camx -= xdif;
	        if(camx < -400)
		       camx = -400;
		
		    camy += ydif;
            if(camy < 300)
		       camy = 300;
			
		    mxLast =  MouseInfo.getPointerInfo().getLocation().x;
		    myLast =  MouseInfo.getPointerInfo().getLocation().y;
		
		
		
		}
	}
		
	
	

	

	@Override
	public void mousePressed(MouseEvent me) {
		
		if(!imageDumped && ((GameWorldModel)getModel()).getMissile().state == State.EXPLODE)
		{
				dumpImage();
				imageDumped = true;
		}
		
	
		if(((GameWorldModel)getModel()).getMissile().state == State.EXPLODE)
		{
				  mouseAction = true;
				  mxLast =  MouseInfo.getPointerInfo().getLocation().x;
				  myLast = MouseInfo.getPointerInfo().getLocation().y;	
		}
		
		else if(((GameWorldModel)getModel()).getMissile().guide == Guidance.NONE){
			
			  
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
	
	
	public void dumpImage()
	{/*
		System.out.println("Dumping map image...");
		int x = ((GameWorldModel)getModel()).getMapX();
		int y = ((GameWorldModel)getModel()).getMapY();
		
		
		
		BufferedImage result = new BufferedImage(
               x, y,
                BufferedImage.TYPE_INT_RGB);
         Graphics g = result.getGraphics();
         for(int i = 0; i < x; i++)
        	 for(int j = 0; j < y; j++)
        	 {
        		 BufferedImage bi = null;
        		 try{
        			 
        		 String mapName = ((GameWorldModel)getModel()).getMapName(i,j);
        		 bi = ImageIO.read(this.getClass().getResource(mapName));
        		 }
        		 catch (IOException e)
        		 {
        			 String s = e.getMessage();
                	 System.out.println(s);
        		 }
        		 g.drawImage(bi, x, y, null);
        	 }
         try{
         ImageIO.write(result,"gif",new File("result.gif"));
         }
         catch (IOException e)
         {
        	 String s = e.getMessage();
        	 System.out.println(s);
         }
         System.out.println("Dump complete. Image located under vpproject folder.");
         System.out.println("Drag mouse to move map.");*/
	}
	
}