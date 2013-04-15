
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
	
	public int detectionDist = 400;
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
		
		//Vector2D force = steering.calculate();
		
		
		double distance = this.position.distance(missile.pos());
		/*System.out.println("Distance to Missile : " + distance);
		
		System.out.println("Missile position X: " + missile.pos().x);
		System.out.println("Missle position y: " + missile.pos().y);
		System.out.println("This position X: " + this.position.x);
		System.out.println("This position y: " + this.position.y);*/
		
		
		if(this.position.distance(missile.pos()) < detectionDist && (missile.getMissileState() != State.EXPLODE || missile.getMissileState() != State.SELFDESTRUCT))//800)
		{
			
			Vector2D evadeVelocity = steering.evade(missile);
			//update velocity when doing the evade or flee   need to improve the evasion technique
			//velocity = steering.getMyTarget().velocity;
			evadeVelocity = evadeVelocity.absX();
			System.out.println("evadeVelocity for aircraft " + evadeVelocity);
			System.out.println("Curent velocity " + velocity);
			
			if (!evadeVelocity.isZero())
				velocity = velocity.add(evadeVelocity);
			
			
			System.out.println("Velocity in Aircraft " + velocity);
			velocity.truncate(maxSpeed);
			

			
			updateAircraftPos(1);
			
			/*
			if ((this.pos().y - missile.pos().y) > 0 )
				position = position.evasionDown(velocity,this.maxForce());
			else
				position = position.evasionUp(velocity,this.maxForce());*/
			
			/*
			if (randomInt < 4)
				{
					Vector2D temp = position;
					if (position.sub(missile.pos()).y >= 0){
						double groundDetect = this.position.y - GameWorldModel.GROUND.y;
						System.out.println("Distance to ground " + groundDetect);
						if ( (int) groundDetect > 10)
							position = position.evasionDown(velocity,this.maxForce());
							else
							{
								position = position.evasionUp(velocity,this.maxForce());
								N=39;
							}
					}
						
					else
						position = position.evasion(velocity, this.maxForce());
					if (position.distance(temp)> 20)
							System.out.println("Change of position more than : " + steering.wanderDist);
					N++;
				}
			else //if (randomInt >= 4 && randomInt < 7)
				{	
					Vector2D temp = position;
					if (position.sub(missile.pos()).y < 0)
						position = position.evasionUp(velocity,this.maxForce());
					else
						position = position.evasion(velocity, this.maxForce());
					
					if (position.distance(temp)> 20)
						System.out.println("Change of position more than : " + steering.wanderDist);
					N++;
				}*/
			/*else
				{
					position = position.evasionLoop(velocity,10,80);
					N++;
				}*/
				
		}
				
		else
		{

			steering.wander();
			updateAircraftPos(0);
			//position = position.add(velocity);
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
	

	
	private void updateAircraftPos(int choice) {
		

		if (choice == 0){
			position.x = position.x + velocity.x* Missile.timeSeconds;
			position.y = position.y + velocity.y * Missile.timeSeconds;
		}
		else
		{
			// Randomize the evasion technique
			if (N == 40)
			{
				Random rand;//, angle;
				rand = new Random();
				randomInt = rand.nextInt(10);
				//angle = new Random();
				//randomInt = rand.nextInt(90);
				N = 0;
			}
			
			if (randomInt <= 5)
			{
				position.x = position.x + (velocity.x * Math.cos(Math.toRadians(45)) * Missile.timeSeconds);
				position.y = position.y + velocity.y * Missile.timeSeconds * Math.sin(Math.toRadians(45));
				N++;
			}
		else //if (randomInt >= 4 && randomInt < 7)
			{	
				position.x = position.x + (velocity.x * Math.cos(Math.toRadians(-45)) * Missile.timeSeconds);
				if(position.y > -10)
					position.y = position.y + velocity.y * Missile.timeSeconds * Math.sin(Math.toRadians(-45));
				else
				{
					position.y = position.y + velocity.y*Missile.timeSeconds;
					N = 39;
				}
				N++;
			}

		}
	}

	@Override
	public void render() {
		// TODO Auto-generated method stub
		
	}

}