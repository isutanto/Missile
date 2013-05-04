
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
	/*public double airCurAngle;
	public double angle;
	public int angleGet = 0;
	public double airDesiredAngle;
	public double TURNRATE = 10;*/
	
	
	
	public int detectionDist = 300;
	public Vector2D gForce;
	
	
	
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
				
		if(this.position.distance(missile.pos()) < detectionDist && (missile.getMissileState() != State.EXPLODE || missile.getMissileState() != State.SELFDESTRUCT))//800)
		{
			Vector2D evadeVelocity = steering.evade(missile);

			evadeVelocity = evadeVelocity.absX();
		
			Vector2D curVelocity = velocity;
			
			double speed = curVelocity.length();
			double desiredSpeed = evadeVelocity.length();
		
			Vector2D acc = (evadeVelocity.sub(velocity)).div(System.nanoTime() - GameWorldModel.lastCall);

			
			if (!evadeVelocity.isZero())
				velocity = velocity.add(evadeVelocity);
			
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

	// Aircraft maneuvering option after missile has been detected
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
			else 
			{
				position.x = position.x + velocity.x* Missile.timeSeconds;
				position.y = position.y + velocity.y * Missile.timeSeconds;
				if (position.y > 360)
					position.y = 360;
			}

		}

	}

	@Override
	public void render() {
		// TODO Auto-generated method stub
		
	}
	
	
/*  // Loop evasion does not work yet.!! 	
	private void adjustCurAngle(){
		 if((int) airDesiredAngle < airCurAngle)
			airCurAngle += TURNRATE;
		else if((int)airDesiredAngle > airCurAngle)
			airCurAngle -= TURNRATE;
	}
	
	public double getAngle() { 
	    //double angle = (double) Math.toDegrees(Math.atan2(this.position.x, this.position.y)); 
	    //double angle = (double) Math.toDegrees(Math.atan2(pursuer.position.x - position.x, pursuer.position.y - position.y)); 
		if (angleGet == 0){
			angle = Missile.desiredAngle;
			angleGet = 1;
			angle = -1 * angle;
		}
			
	    System.out.println("Current Angle is " + angle);
	    
	    //if(angle < 0)
	      //  angle += 360; 
	   
	    return angle; //- 90; 	
	}
	
	public void Loop()
	{
		airCurAngle = getAngle();
		airDesiredAngle = airCurAngle;
		adjustCurAngle();
		//angle = airCurAngle;
		//velocity = 
		position.x = position.x + (velocity.x * Math.cos(Math.toRadians(airCurAngle)) * Missile.timeSeconds);
		position.y = position.y + velocity.y * Missile.timeSeconds * Math.sin(Math.toRadians(airCurAngle));
				
	}*/
	
	public void Wave()
	{
		// Randomize the wave evasion technique
					if (N == 40)
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
							N = 39;
							odd = 1;
						}
					}
				else 
					{	
						position.x = position.x + (velocity.x * Math.cos(Math.toRadians(-45)) * Missile.timeSeconds);
						position.y = position.y + velocity.y * Missile.timeSeconds * Math.sin(Math.toRadians(-45));
					}		
	}

}