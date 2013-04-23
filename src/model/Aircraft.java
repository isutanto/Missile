
package model;

import java.awt.MouseInfo;
import java.io.IOException;

import javax.imageio.ImageIO;

import model.Missile.State;
import java.util.Random;

public class Aircraft extends MovingEntity {

	/**
	 * 
	 * @param initialVel
	 */
	private SteeringBehaviors steering;
	public boolean isPressed;
	
	// Itteration on random evasion
	public int N = 40;
	public int randomInt; 
	
	// Angle of maneuver for aircraft add on
	/*
	public double curAngle=0;	
	public double desiredAngle;
	*/
	
	
	public Aircraft(Vector2D position,
			double radius,
			Vector2D velocity,
			double maxSpeed,
			Vector2D heading,
			double mass,
			Vector2D scale,
			double turnRate,
			double maxForce) {
		super(position, radius, velocity, maxSpeed, heading, mass,
						scale, turnRate, maxForce);
		try {
		    image = ImageIO.read(getClass().getResource("airplane.gif") );
		} catch (IOException e) {
			System.out.println("Can't find airplane.gif!");
		}
		
		steering = new SteeringBehaviors(this);
		steering.wanderOn();
		steering.evadeOn();
		steering.fleeOn();
		
	}
	
	/**
	 * @param initialVel
	 */
	public SteeringBehaviors getSteering(){
		return steering;
		
	}

	public double getAngle(Vector2D targetPos) { 
	    double angle = (double) Math.toDegrees(Math.atan2(targetPos.x - position.x, targetPos.y - position.y)); 
	 
	    if(angle < 0)
	        angle += 360; 
	   
	    return angle - 90; 
	
	}
	
	

	public void update(Missile missile, double delta) {
		
		//Vector2D force = steering.calculate();
		
		//double distance = this.position.distance(missile.pos());
		
		if(this.position.distance(missile.pos()) < 250 && missile.getMissileState() != State.EXPLODE)//800)
		{
			steering.evade(missile);
			//update velocity when doing the evade or flee   need to improve the evasion technique
			velocity = steering.getMyTarget().velocity;

			// Randomize the evasion technique
			if (N == 40)
			{
				Random rand = new Random();
				randomInt = rand.nextInt(10);
				N = 0;
			}
			
			//position = position.evasionLoop(velocity,this.maxForce(),-90);
			position = position.evasionUp(velocity,this.maxForce());
			
			/*
			if ((this.pos().y - missile.pos().y) > 0 )
				position = position.evasionDown(velocity,this.maxForce());
			else
				position = position.evasionUp(velocity,this.maxForce());*/
			
			/*
			if (randomInt < 4)
				{
					position = position.evasionDown(velocity,this.maxForce());
					N++;
				}
			else if (randomInt >= 4 && randomInt < 7)
				{
					position = position.evasionUp(velocity,this.maxForce());
					N++;
				}
			else
				{
					position = position.evasionLoop(velocity,10,80);
					N++;
				}*/
				
		}
				
		else
		{
			steering.wander();
			position = position.add(velocity);
		}
		
		
		//calculate the acceleration
		//Vector2D accel = force.div(mass);

		//update the velocity
		//velocity = steering.getMyTarget().velocity;
			
		//make sure vehicle does not exceed maximum velocity per second
		velocity.truncate(maxSpeed);

		//update the position
		/*if ((this.pos().y - missile.pos().y) < 0 )
		position = position.sub(velocity);
		else
			position = position.add(velocity);*/
		
		//if the vehicle has a non zero velocity the heading and side vectors must 
		//be updated
		if (!velocity.isZero())
		{    
			heading = new Vector2D(velocity);
			heading.normalize();

			side = heading.perp();
		}
}
		
	/*public double getVelocity() {
		throw new UnsupportedOperationException();
	}
	*/
	
	public Vector2D getPosition(){
		return this.position;
	}

	public void setVelocity(double velocity) {
		throw new UnsupportedOperationException();
	}
	
	public void dragAircraft() {
		position.x = MouseInfo.getPointerInfo().getLocation().x;
		position.y = MouseInfo.getPointerInfo().getLocation().y;
	}

	@Override
	public void render() {
		// TODO Auto-generated method stub
		
	}

}