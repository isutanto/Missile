package model;

import java.awt.MouseInfo;
import java.io.IOException;
import java.util.*;
import javax.imageio.ImageIO;

/**
 * 
 * @author Jason Herzog / Claudia Alvarado
 *
 *Missile class models the physics of a missile. Missile class contains an instance of SteeringBehaviors to model
 *different guidance algorithms.
 *
 */
public class Missile extends MovingEntity {
	
	public static final int BLAST_RADIUS = 54;
	private SteeringBehaviors steering;
	public Vector2D gForce;
	
	public enum State {
		ACCEL, FREEFALL, GROUND, EXPLODE
	}
	public State state;
	
	public enum Guidance{
		PROPORTIONAL, PURSUIT, PARALLEL, NONE
	}
	public Guidance guide;
	
	//Set to true if the mouse was clicked within the missile
	public boolean isPressed;
	
	//time for engine to burnout
	public static final long BURNOUT = 15;
	
	//total time elapsed in nanoseconds
	public double totalTime;
	
	//total time elapsed in seconds
	public double timeSeconds;
	
	//Missile's forces
	private Vector2D lift;
	private Vector2D drag;
	private Vector2D thrust;	
	
	//Rounded values of forces to draw to screen
	public int liftForce;
	public int dragForce;
	public int thrustForce;
	
	private static final double TURN_RATE = 1; // angle turn rate
	private static final int COOLDOWN = 2; //time missile flies at 90 degrees until guidance kicks in
	
	//Array of positions 
	public ArrayList<Vector2D> positionArray;
	public ArrayList<Double> speedArray;
	
	//Equation constants
	//Lift force
	private static final double Cy = 1; //lift coefficient
	private static final double rho = 1.4; //density of air
	private static final double Sb = 2.1; //wing span
	
	//Thrust force
	private static final double Gc = 5; //Gallons of fuel consumed per second
	private static final double g = 9.8; // acceleration of gravity m/s^2
	private double u = 80; 				//speed of gas out of nozzle
	private static final double Pc = 24; //pressure of gas at nozzle
	private double Ph; 					//pressure at altitude h
	//Drag force
	private static final double Cx = 1; //drag coefficient
	
	//initial weight
	private int G0;
	
	//current weight
	public int weight;
	
	public double curAngle=90;	
	public double desiredAngle;
	
	private int positionTimeCount = 0;	
	
	//add angle turn speed in degress or radians
	//add max angle turn speed
	
	boolean calledOnce;

	public Missile(Vector2D position,
			double radius,
			Vector2D velocity,
			double maxSpeed,
			Vector2D heading,
			double mass,
			Vector2D scale,
			double turnRate,
			double maxForce, MovingEntity target) {
		super(position, radius, velocity, maxSpeed, heading, mass,
						scale, turnRate, maxForce);
		
		try {
		    image = ImageIO.read(getClass().getResource("missile.gif") );
		} catch (IOException e) {
			System.out.println("Can't find missile.gif!");
		}
		
		 G0 = (int) (.0097 * mass);
		steering = new SteeringBehaviors(this, target);
		desiredAngle = curAngle = 90;
		positionArray = new ArrayList<Vector2D>(10);
		state = State.ACCEL;
		//guide = Guidance.PROPOTIONAL;
		//guide = Guidance.PURSUIT;
		//guide = Guidance.PARALLEL;
		
		guide = Guidance.NONE;
		calledOnce = false;
		
		speedArray = new ArrayList<Double>(10);

	}
	
	public void update(MovingEntity target, double delta) {
		totalTime += delta;
		timeSeconds = totalTime/1000000000L;
		
		//calculate forces
		calculateThrust();
		calculateLift();
		calculateDrag();
		calculateWeight();

		
		if(state == State.ACCEL){
			thrustForce = (int) thrust.x;
			dragForce = (int) drag.x;
			liftForce = (int) lift.x;
		
			//convert forces into acceleration
			Vector2D accel1 = thrust.div(mass);		
			Vector2D accel2 = lift.div(mass);
			Vector2D accel3 = drag.div(mass);
			Vector2D accel4 = new Vector2D (0, -weight/mass);
			
			//gForce = new Vector2D (accel1.add(accel2).add(accel3));
			//gForce = gForce.div(.0098);
			//gForce.y = Math.abs(gForce.y);
			
			//add accelerations to velocity
			velocity = velocity.add(accel1);
			velocity = velocity.sub(accel2);
			velocity = velocity.sub(accel3);
			velocity = velocity.sub(accel4);
			velocity.truncate(this.maxSpeed);
			
		}else{
			thrustForce = 0;
			dragForce = 0;
			liftForce = 0;
		}
		
		getDesiredAngle();
		adjustCurAngle();
		updateState(timeSeconds);
		updatePosition();
	
		//update array
		
		if(positionTimeCount < timeSeconds){ //add a point to be drawn every .5 seconds
			positionTimeCount += .5;
			if(!(state == State.EXPLODE))
		  	   positionArray.add(new Vector2D(position));
		}
		
		//if the vehicle has a non zero velocity the heading and side vectors must 
		//be updated
		if (!velocity.isZero())
		{    
			heading = new Vector2D(velocity);
			heading.normalize();
			
			side = heading.perp();
			
			speedArray.add(velocity.length());
		}
}

	private void updatePosition() {
		if(state == State.GROUND || state == State.EXPLODE)
			velocity.Zero();
		else if (state == State.ACCEL){
			position.x = position.x + (velocity.x * Math.cos(Math.toRadians(curAngle)) * timeSeconds);
			position.y = position.y + velocity.y * timeSeconds * Math.sin(Math.toRadians(curAngle));
		}else if (state == State.FREEFALL){
			position.y = position.y + velocity.y * timeSeconds * Math.sin(Math.toRadians(curAngle)) + .5 * .098 * timeSeconds*timeSeconds;
			position.x = position.x + (velocity.x * Math.cos(Math.toRadians(curAngle)) * timeSeconds);
		}
	
		
		
	}
	

	private void getDesiredAngle() {
		
		if (guide == Guidance.PROPORTIONAL && timeSeconds > COOLDOWN && state != State.GROUND)
			desiredAngle = steering.proportional();
		else if(guide == Guidance.PURSUIT && timeSeconds > COOLDOWN && state != State.GROUND)
			desiredAngle = steering.pursuit(steering.getMyTarget());	
		else if(guide == Guidance.PARALLEL &&  calledOnce != true && timeSeconds > COOLDOWN && state != State.GROUND){
			desiredAngle = steering.parallel(steering.getMyTarget());
			calledOnce = true;
		}
		
		if(state == State.FREEFALL)
			desiredAngle = -90;
		else if(state == State.GROUND || state == State.EXPLODE)
			desiredAngle = curAngle;
		
	}
	
	private void adjustCurAngle(){
		 if((int) desiredAngle < curAngle)
			curAngle -= TURN_RATE;
		else if((int)desiredAngle > curAngle)
			curAngle += TURN_RATE;
	}
	
	
	
	
	public double getAngle(Vector2D targetPos) { 
	    double angle = (double) Math.toDegrees(Math.atan2(targetPos.x - position.x, targetPos.y - position.y)); 
	 
	    if(angle < 0)
	        angle += 360; 
	   
	    return angle - 90; 
	
	}

	private void calculateWeight() {
		double percent;
		if(position.y < 0)
			percent = .90;
		else if(position.y < 50)
			percent = .91;
		else if(position.y < 100)
			percent = .92;
		else if (position.y < 150)
			percent = .93;
		else if (position.y < 200)
			percent = .94;
		else if (position.y < 250)
			percent = .95;
		else if (position.y < 300)
			percent = .96;
		else if (position.y < 350)
			percent = .97;
		else if (position.y < 400)
			percent = .98;
		else 
			percent = 1.0;
		weight = (int) (G0*percent);
		
	}

	private void updateState(double timeSeconds) {
		if(timeSeconds < BURNOUT)
			state = State.ACCEL;
		else if (state != State.EXPLODE)
			state = State.FREEFALL;
		if(position.y >= GameWorldModel.GROUND.y)
			state = State.GROUND;
		
			
	}

	private void calculateLift() {
		lift = velocity.sq();
		lift = lift.mul(rho);
		lift = lift.div(2);
		lift = lift.mul(Sb);
		lift = lift.mul(Cy);
		lift.y = -lift.y;
	}
	
	private void calculateThrust(){
		if(position.y < 150)
			Ph = 7;
		else if(position.y < 250)
			Ph = 5;
		else if (position.y < 350)
			Ph = 2;
		else if (position.y < 450)
			Ph = 1;

		double Pf = (Pc - Ph);
		double temp = (Gc * u)/g;
		temp += Pf*Sb;
		thrust = new Vector2D(temp, -temp);
	}
	
	private void calculateDrag(){
		drag = velocity.sq();
		drag = drag.mul(rho);
		drag = drag.div(2);
		drag = drag.mul(Sb);
		drag = drag.mul(Cx);
	}

	public Vector2D getPosition(){
		return this.position;
	}
	
	public double getCurAngle(){
		return curAngle;
	}
	
	public void setState(State newState){
		state = newState;
	}
	
	public double distanceToTarget(){
		return position.distance(steering.getMyTarget().pos());
	}
	
	
	@Override
	public void render() {
		// TODO Auto-generated method stub
		
	}

	public void setGuidance(String option) {
		if(option == "pursuit")
			guide = Guidance.PURSUIT;
		else if(option == "parallel")
			guide = Guidance.PARALLEL;
		else if (option == "proportional")
			guide = Guidance.PROPORTIONAL;
		
	}

	public void dragMissile() {
		//position.x = MouseInfo.getPointerInfo().getLocation().x;		
	}
	public ArrayList<Double> getSpeedArray()
	{
		return speedArray;
	}
	public double getSpeed()
	{
       return velocity.length();
	}

}