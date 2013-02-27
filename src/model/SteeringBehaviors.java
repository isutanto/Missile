package model;

import java.util.Random;


public class SteeringBehaviors {

	/**
	 * 
	 * @param Vector2D
	 */
	
	/** the radius of the constraining circle for the wander behavior */
	public static final double wanderRad = 1.2;
	/** distance the wander circle is projected in front of the agent */
	public static final double wanderDist   = 2.0;
	/** the maximum amount of displacement along the circle each frame */
	public static final double wanderJitterPerSec = 40.0;
	
	private enum BehaviorType{
		NONE(1), 
		SEEK(2),
		PURSUIT(3),
		FLEE(4),
		EVADE(5),
		WANDER(6);
		

		private int value;
		private BehaviorType(int i) {value = i;}
		public int getValue() {return value;}
	};
	
	public static final double weightPursuit = 5.0;
	public static final double weightFlee = 6.0;
	public static final double weightSeek = 5.0;
	public static final double weightEvade = 4.0;
	public static final double weightWander = 2.0;
	private double lastAngle;
	public static double maxAngleRate = 2; //Two degrees per second
	public static double intialAngle = 90;
	double curAngle;

	public	Missile entity;

	public MovingEntity myTarget;
	
	//private GameWorldModel world;	//world data
	
	private Vector2D steeringForce;	//total steering force--combined effect of ON behaviors
	
	private Vector2D target;	//current target
	
	
	private Vector2D wanderTarget; 
	private double wanderJitter;
	private double wanderRadius;
	private double wanderDistance;
	
	private int flags;
	//private BehaviorType behaviorType;
	
	/** this function tests if a specific bit of flags is set */
	private boolean On(BehaviorType bt) {
		return (flags & bt.getValue()) == bt.getValue();
	}
	
	public void setTarget(MovingEntity newTarget){
		myTarget = newTarget;
	}
	
	public boolean accumulateForce(Vector2D runningTot, Vector2D forceToAdd) {
		//calculate how much steering force the vehicle has used so far
		double magnitudeSoFar = runningTot.length();

		//calculate how much steering force remains to be used by this vehicle
		double magnitudeRemaining = entity.maxForce() - magnitudeSoFar;

		//return false if there is no more force left to use
		if (magnitudeRemaining <= 0.0)
			return false;

		//calculate the magnitude of the force we want to add
		double magnitudeToAdd = forceToAdd.length();

		//if the magnitude of the sum of ForceToAdd and the running total
		//does not exceed the maximum force available to this vehicle, just
		//add together. Otherwise add as much of the ForceToAdd vector is
		//possible without going over the max.
		if (magnitudeToAdd < magnitudeRemaining) {
			runningTot.setValue(runningTot.add(forceToAdd));
		} else {
			magnitudeToAdd = magnitudeRemaining;

			//add it to the steering force
			forceToAdd.normalize();
			// Dirty hack due to the way it was ported.
			runningTot.setValue(runningTot.add(forceToAdd.mul(magnitudeToAdd))); 
		}

		return true;
	}
	
	public double angleBetween(){
		double angle;
		Vector2D angleBetween = myTarget.pos().sub(entity.position);
		angleBetween.normalize();
		angle =	(180*Math.acos(angleBetween.dot(entity.heading)));
		if(myTarget.pos().x < entity.pos().x){
			angle = -angle;
			if(angle > lastAngle){
				lastAngle = angle;
				return angle;
			}else
				return lastAngle += .01;
		}else{
			if(angle < lastAngle){
				lastAngle = angle;
				return angle;
			}else
				return lastAngle -= .01;
		}
	}
	
	
/******* STEERING BEHAVIORS*******/


	public Vector2D seek(final Vector2D target) {

		Vector2D desiredVelocity = target.sub(entity.pos());
		desiredVelocity.normalize();
		desiredVelocity = desiredVelocity.mul(entity.maxForce());

		return (desiredVelocity.sub(entity.velocity()));

	}
	
	public Vector2D flee(final Vector2D target){
		System.out.println("in flee");
		
		final double panicDistSq = 100.0 * 100.0;
		// change entity to myTarget
		if(myTarget.pos().distanceSq(target) > panicDistSq){
			return new Vector2D();
		}

		Vector2D desiredVelocity = (myTarget.pos()).sub(target);
		desiredVelocity.normalize();
		desiredVelocity = desiredVelocity.mul(myTarget.maxForce());
		
		return (desiredVelocity.sub(myTarget.velocity()));	
//--------------------------------
	}
	
	public Vector2D evade(Missile pursuer){
		System.out.println("in evade");
		
		//change entity to myTarget
		Vector2D toPursuer = (pursuer.pos()).sub(myTarget.pos());//(entity.pos());
		double lookAheadTime = toPursuer.length() / (myTarget.maxForce() + pursuer.speed());
		//-----------------------
					
		
		return flee(pursuer.pos().add(pursuer.velocity().mul(lookAheadTime)));
	}
	
	public Vector2D wander() {
		//first, add a small random vector to the target's position
		System.out.println("in wander");
	
		//need to look at
		
		/*
		wanderTarget = wanderTarget.add(new Vector2D( new Random().nextDouble()* wanderJitter, new Random().nextDouble() * wanderJitter));
		//reproject this new vector back on to a unit circle
		wanderTarget.normalize();
		//increase the length of the vector to the same as the radius
		//of the wander circle
		wanderTarget = wanderTarget.mul(wanderRadius);
		//move the target into a position WanderDist in front of the agent
		Vector2D target2 = wanderTarget.add(new Vector2D(wanderDistance, 0));
		//project the target into world space
		Vector2D newTarget = Transformations.pointToLocalSpace(target2, entity.heading(), entity.side(), entity.pos());
		//and steer towards it
		return newTarget.sub(entity.pos()); */
		
		Vector2D continueOn = new Vector2D(); 
		
		return continueOn; 
		
		
	}
	
	public double proportional() {
		return entity.getAngle(getMyTarget().position);
	}
	
	public double pursuit(MovingEntity evader){		
		Vector2D toEvader = evader.pos().sub(entity.pos());
		double lookAheadTime = toEvader.length() / (entity.maxSpeed() + evader.speed());
		
		Vector2D desiredVector = new Vector2D(evader.pos());
		desiredVector = desiredVector.add(evader.velocity().mul(lookAheadTime));
		return entity.getAngle(desiredVector);
	}
	
	public double parallel(MovingEntity target){
		Vector2D toEvader = target.pos().sub(entity.pos());
		double lookAheadTime = toEvader.length() / (entity.maxSpeed() + target.speed());
		
		Vector2D desiredVector = new Vector2D(target.pos());
		desiredVector = desiredVector.add(target.velocity().mul(lookAheadTime));
		return entity.getAngle(desiredVector);
		
	}

	/****END OF BEHAVIORS******/
	

	private Vector2D calculatePrioritized(){

		Vector2D force = new Vector2D();
	//TODO add choices for pursuit, parallel and proportional here
		
		/*if (On(BehaviorType.PURSUIT))
		{
			force = pursuit(myTarget).mul(weightPursuit);

			if (!accumulateForce(steeringForce, force)) 
				return steeringForce;
		}
		if (On(BehaviorType.SEEK))
		{
			force = seek(target).mul(weightSeek);
			
			if (!accumulateForce(steeringForce, force)) 
				return steeringForce;
		}*/
		
		if (On(BehaviorType.WANDER))
		{
			force = wander().mul(weightWander);

			if (!accumulateForce(steeringForce, force)) return steeringForce;
		}
		
		if (On(BehaviorType.FLEE))
		{
			force = flee(target).mul(weightFlee);

			if (!accumulateForce(steeringForce, force)) return steeringForce;
		}


		return steeringForce;
	}

	
	/****CONSTRUCTORS****/
	
	public SteeringBehaviors(MovingEntity subject){
		flags						= 0;
		this.entity = (Missile) subject;
		target = new Vector2D();
		steeringForce = new Vector2D();
		wanderDistance				= wanderDist;
		wanderJitter				= wanderJitterPerSec;
		wanderRadius				= wanderRad;
		
		//stuff for the wander behavior
		double theta = Math.random() * (2* Math.PI);

		//create a vector to a target position on the wander circle
		wanderTarget = new Vector2D(wanderRadius * Math.cos(theta), wanderRadius * Math.sin(theta));
				
	}
	
	public SteeringBehaviors(MovingEntity subject, MovingEntity tar){
		flags			= 0;
		this.entity = (Missile) subject;
		this.myTarget = tar;
		this.lastAngle = 180;
		curAngle = intialAngle;
		steeringForce = new Vector2D();
		target = new Vector2D();
		
		wanderDistance				= wanderDist;
		wanderJitter				= wanderJitterPerSec;
		wanderRadius				= wanderRad;
		
		//stuff for the wander behavior
		double theta = Math.random() * (2* Math.PI);

		//create a vector to a target position on the wander circle
		wanderTarget = new Vector2D(wanderRadius * Math.cos(theta), wanderRadius * Math.sin(theta));
				
	}
	
	public SteeringBehaviors(Aircraft aircraft) {
		// TODO Auto-generated constructor stub
		
		//insert behaviour for the aircraft *want to test it!!*
	
	}

	public Vector2D calculate(){
		//reset the steering force
		steeringForce.Zero();
		
		steeringForce = calculatePrioritized();
		return steeringForce;
	}
	
	/** calculates the component of the steering force that is parallel with
	 * the entity heading */
	public double forwardComponent(){
		return entity.heading().dot(steeringForce);
	}
	
	/** calculates the component of the steering force that is perpendicular
	 * with the RavenBot heading */
	public double sideComponent(){
		return entity.side().dot(steeringForce);
	}
	
	public MovingEntity getMyTarget(){
		return myTarget;
	}
	
	public void setTarget(Vector2D t) { target = t; }
	public final Vector2D target() { return target; }
	public final Vector2D force() { return steeringForce; }
	
	public void seekOn() { flags |= BehaviorType.SEEK.getValue(); }
	public void pursuitOn() { flags |= BehaviorType.PURSUIT.getValue(); }
	public void fleeOn() { flags |= BehaviorType.FLEE.getValue(); }
	public void evadeOn() { flags |= BehaviorType.EVADE.getValue(); }
	public void wanderOn() { flags |= BehaviorType.WANDER.getValue(); }
	
	public void seekOff() { if(On(BehaviorType.SEEK)) flags ^= BehaviorType.SEEK.getValue(); }
	public void wanderOff() { if(On(BehaviorType.WANDER)) flags ^= BehaviorType.WANDER.getValue(); }
	public void pursuitOff() { if(On(BehaviorType.PURSUIT)) flags ^= BehaviorType.PURSUIT.getValue(); }
	public void fleeOff() { if(On(BehaviorType.FLEE)) flags ^= BehaviorType.FLEE.getValue(); }
	
	public boolean seekIsOn() { return On(BehaviorType.SEEK); }
	public boolean wanderIsOn() { return On(BehaviorType.WANDER); }
	public boolean pursuitIsOn() { return On(BehaviorType.PURSUIT); }
	
	public final double wanderJitter() { return wanderJitter; }
	public final double wanderDistance() { return wanderDistance; }
	public final double wanderRadius() { return wanderRadius; }
	
}