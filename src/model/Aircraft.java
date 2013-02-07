package model;

import java.awt.MouseInfo;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Aircraft extends MovingEntity {

	/**
	 * 
	 * @param initialVel
	 */
	private SteeringBehaviors steering;
	public boolean isPressed;
	
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
		
		double distance = this.position.distance(missile.pos());
		
		/*if(this.position.distance(missile.pos()) < 800)
			steering.evade(missile);	
		else
			steering.wander();*/
		
		
		//calculate the acceleration
		//Vector2D accel = force.div(mass);

		//update the velocity
		//velocity = velocity.add(accel);
			
		//make sure vehicle does not exceed maximum velocity per second
		velocity.truncate(maxSpeed);

		//update the position
		position = position.add(velocity);
		
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