
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
	public int N = 50;
	public int randomInt; 
	public int randomSelct;
	public int odd = 0;
	public int finish = 0;
	
	// Angle for loop
	
	public double airCurAngle;	
	public double airDesiredAngle;
	public double TURNRATE = 10;
	//public double angle;
	
	
	
	public int detectionDist = 300;
	public Vector2D gForce;
	
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
				
		double distance = this.position.distance(missile.pos());
		/*System.out.println("Distance to Missile : " + distance);
		
		System.out.println("Missile position X: " + missile.pos().x);
		System.out.println("Missle position y: " + missile.pos().y);
		System.out.println("This position X: " + this.position.x);
		System.out.println("This position y: " + this.position.y);*/
				
		if(this.position.distance(missile.pos()) < detectionDist && (missile.getMissileState() != State.EXPLODE || missile.getMissileState() != State.SELFDESTRUCT))//800)
		{
			Vector2D evadeVelocity = steering.evade(missile);

			evadeVelocity = evadeVelocity.absX();
		
			Vector2D curVelocity = velocity;
			
			double speed = curVelocity.length();
			System.out.println("Current speed " + velocity);
			double desiredSpeed = evadeVelocity.length();
			System.out.println("Desired speed " + evadeVelocity);
		
			Vector2D acc = (evadeVelocity.sub(velocity)).div(System.nanoTime() - GameWorldModel.lastCall);
			System.out.println("Desired acc " + acc);
			System.out.println("Desired acc scalar " + acc.length());
			//double acc = (desiredSpeed - speed)/();
			//System.out.println("Acc needed " + acc * 10000000);

			
			if (!evadeVelocity.isZero())
				velocity = velocity.add(evadeVelocity);
			
			
			//System.out.println("Velocity in Aircraft " + velocity);
			velocity.truncate(maxSpeed);
			
			updateAircraftPos(1);	
		}				
		else
		{
			steering.wander();
			velocity.truncate(maxSpeed);
			updateAircraftPos(0);
		}
		
		
		//calculate the acceleration
		//Vector2D accel = force.div(mass);

		//update the velocity
		//velocity = steering.getMyTarget().velocity;
			
		//make sure vehicle does not exceed maximum velocity per second
		//velocity.truncate(maxSpeed);

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
		
	private void updateAircraftPos(int choice) {
		

		if (choice == 0){
			position.x = position.x + velocity.x* Missile.timeSeconds;
			position.y = position.y + velocity.y * Missile.timeSeconds;
			if (position.y > 360)
				position.y = 360;
		}
		else if (choice == 1)
		{
			if (finish == 0){
			Random rand;
			rand = new Random();
			randomSelct = rand.nextInt(2);
			finish = 1;
			}
			
			if(randomSelct <=1)
			{
				Wave();
				N++;
				if( N == 50)
					finish = 0;
			}
			else //if ((1 < randomSelct) && (randomSelct <= 2) )
			{
				position.x = position.x + velocity.x* Missile.timeSeconds;
				position.y = position.y + velocity.y * Missile.timeSeconds;
				if (position.y > 360)
					position.y = 360;
			}//*/
			/*else
			{
				Loop();
			}*/

		}/*else
		{

		}*/
	}

	@Override
	public void render() {
		// TODO Auto-generated method stub
		
	}
	
	private void adjustCurAngle(){
		 if((int) airDesiredAngle < airCurAngle)
			airCurAngle += TURNRATE;
		else if((int)airDesiredAngle > airCurAngle)
			airCurAngle -= TURNRATE;
		 
		// System.out.println("Current Angle is " + airCurAngle);
	}
	
	public double getAngle() { 
	    double angle = (double) Math.toDegrees(Math.atan2(this.position.x, this.position.y)); 
	    
	    System.out.println("Current Angle is " + angle);
	    
	    if(angle < 0)
	        angle += 360; 
	   
	    return angle - 90; 	
	}
	
	public void Loop()
	{
		airCurAngle = getAngle();
		airDesiredAngle = 180;
		adjustCurAngle();
		//velocity = 
		position.x = position.x + (velocity.x * Math.cos(Math.toRadians(airCurAngle)) * Missile.timeSeconds);
		position.y = position.y + velocity.y * Missile.timeSeconds * Math.sin(Math.toRadians(airCurAngle));
		
		
	}
	
	public void Wave()
	{
		// Randomize the evasion technique
					if (N == 30)
					{
						//Random rand;//, angle;
						//rand = new Random();
						//randomInt = rand.nextInt(10);
						if (odd == 0)
							odd = 1;
						else
							odd = 0;
						N = 0;
					}
					
					if (odd == 0)
					{
						position.x = position.x + (velocity.x * Math.cos(Math.toRadians(45)) * Missile.timeSeconds);
						position.y = position.y + velocity.y * Missile.timeSeconds * Math.sin(Math.toRadians(45));
						if (position.y > 360){
							position.y = 360;
							N = 49;
							//odd = 1;
						}

						//N++;
					}
				else 
					{	
						position.x = position.x + (velocity.x * Math.cos(Math.toRadians(-45)) * Missile.timeSeconds);
						position.y = position.y + velocity.y * Missile.timeSeconds * Math.sin(Math.toRadians(-45));
						//System.out.println("Y position is " + position.y);

						// check for ground
							
						//N++;
					}		
	}

}