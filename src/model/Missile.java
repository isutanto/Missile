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
	
	public Vector2D gForce;
	
	public enum State {
		ACCEL, FREEFALL, GROUND, EXPLODE, MISSED
	}
	public State state;
	
	public enum Guidance{
		PROPOTIONAL, PURSUIT, PARALLEL, NONE
	}
	public Guidance guide;
	
	//Set to true if the mouse was clicked within the missile
	public boolean isPressed;
	
	//time for engine to burnout
	public static final long BURNOUT = 50;
	
	//total time elapsed in nanoseconds
	private double totalTime;
	
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
	
	private static final double TURN_RATE = 3; // angle turn rate
	private static final int COOLDOWN = 2; //time missile flies at 90 degrees until guidance kicks in
	
	//Array of positions 
	public ArrayList<Vector2D> positionArray;
	
	//Equation constants
	//Lift force
	private static final double Cy = 1; //lift coefficient
	private static final double rho = 1.4; //density of air
	private static final double Sb = 2.1; //wing span
	private static final double MISSED_SPEEDUP = 2.0;
	private static final double MISSED_ANGLE = -255.0;
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

	}
	
	public void update(MovingEntity target, double delta) {
		totalTime += delta;
		timeSeconds = totalTime/1000000000L;
		
		//calculate forces
		calculateThrust();
		calculateLift();
		calculateDrag();
		calculateWeight();
        //add up, calculate acceleration/position
		
		if(state == State.ACCEL || state == State.MISSED){
			thrustForce = (int) thrust.x;
			dragForce = (int) drag.x;
			liftForce = (int) lift.x;
		
			//convert forces into acceleration
		
			Vector2D accel4 = new Vector2D (0, -weight/mass);
		
		    Vector2D force = thrust.add(lift);
		    force = force.add(drag);
		    force = force.add(accel4);
		    Vector2D accel = force.div(mass);
			//add vector /mass * delta t
			
			//add accelerations to velocity -- (multiply by delta t -- )
			velocity = velocity.add(accel.mul(delta));
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
			positionArray.add(new Vector2D(position));
		}
		
		
		//if the vehicle has a non zero velocity the heading and side vectors must 
		//be updated
		if (!velocity.isZero())
		{    
			heading = new Vector2D(velocity);
			heading.normalize();
			
			side = heading.perp();
		}
}

	private void updatePosition() {
		if(state == State.GROUND || state == State.EXPLODE)
			velocity.Zero();
		else if (state == State.ACCEL){
			System.out.println("Accel State");
			position.x = position.x + (velocity.x * Math.cos(Math.toRadians(curAngle)) * timeSeconds);
			position.y = position.y + velocity.y * timeSeconds * Math.sin(Math.toRadians(curAngle));	
		}else if (state == State.FREEFALL){
			System.out.println("FreeFall State");
			position.y = position.y + velocity.y * timeSeconds * Math.sin(Math.toRadians(curAngle)) + .5 * .098 * timeSeconds*timeSeconds;
			position.x = position.x + (velocity.x * Math.cos(Math.toRadians(curAngle)) * timeSeconds);
		}
		else if (state == State.MISSED){
			System.out.println("Missed State");
			//desiredAngle = missile heading and aircraft position
			double x;
			double y;
			x = steering.myTarget.pos().x;
			y = steering.myTarget.pos().y; 
			Vector2D temp = new Vector2D (x, y);
			
			//Vector2D temp = steering.myTarget.pos();
			temp.normalize();
			desiredAngle = Math.toDegrees(Math.acos(heading.dot(temp)));
			Vector2D w1;
			//w1 = steering.wander();
			w1 = steering.myTarget.steering.wander();
			System.out.println("w1:"+w1);
			Vector2D w2;
		    w2 = w1.sub(position);
		    w2.normalize();
		    w2 = w2.mul(velocity.length() * MISSED_SPEEDUP);
			position = position.add(w2);
		}
	}

	private void getDesiredAngle() {
		
		if (guide == Guidance.PROPOTIONAL && timeSeconds > COOLDOWN && state != State.GROUND)
			desiredAngle = steering.proportionalWander();
		else if(guide == Guidance.PURSUIT && timeSeconds > COOLDOWN && state != State.GROUND)
			desiredAngle = steering.pursuitWander(steering.getMyTarget());	
		else if(guide == Guidance.PARALLEL &&  calledOnce != true && timeSeconds > COOLDOWN && state != State.GROUND){
			desiredAngle = steering.parallelWander(steering.getMyTarget());
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
		    //curAngle = desiredAngle;
	}
	
	
	
	//loop -- target
	public double getAngle(Vector2D targetPos) { 
	    double angle = (double) Math.toDegrees(Math.atan2(targetPos.x - position.x, targetPos.y - position.y)); 
	    System.out.println("getAngle()"+(angle-90));
	    //260 255
	    if(angle-90 < MISSED_ANGLE){
	    	System.out.println("Angle < 0");
	        state = State.MISSED;
	        System.out.println("State: "+state);
	        
	    }
	    return angle -90;
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
		if(timeSeconds < BURNOUT && state != State.MISSED){
			System.out.println("State is != MISSED");
//			state = State.ACCEL;
			System.out.println("State: "+state);
		}
		else if (state != State.EXPLODE && state!=State.MISSED) 
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
		thrust = new Vector2D(temp, -temp); //magnitude -- vector along normalize angle multiply temp
		//thrust = new Vector2D(Math.cos(Math.toRadians(curAngle)), -Math.sin(Math.toRadians(curAngle)));
		System.out.println(thrust);
		thrust.mul(temp);
		System.out.println("Temp"+temp);
		System.out.println("CurAngle"+curAngle);
		
	} //x,y coordinates from sin and cos -> normalized vector
	
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
	
	public State getMissileState(){
		return state;
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
			guide = Guidance.PROPOTIONAL;
		
	}

	public void dragMissile() {
		position.x = MouseInfo.getPointerInfo().getLocation().x;		
	}

}