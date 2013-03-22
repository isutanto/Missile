package model;
public abstract class MovingEntity extends BaseGameEntity {

	protected Vector2D velocity;
	
	/**
	 * a normalized vector pointing in the direction the entity is heading
	 */
	protected Vector2D heading;
	
	public SteeringBehaviors steering;
	
	/**
	 * a vector perpendicular to the heading vector
	 */
	protected Vector2D side;
	
	protected double mass;
	
	/**
	 * the maximum speed this entity may travel at
	 */
	protected double maxSpeed;
	
	/**
	 * the maximum force this entity can produce to power itself (think rockets and thrust)
	 */
	protected double maxForce;
	
	/**
	 * the maximum rate (radians per second)this vehicle can rotate
	 */
	protected double maxTurnRate;
		
	public MovingEntity(Vector2D position,
						double radius,
						Vector2D velocity,
						double maxSpeed,
						Vector2D heading,
						double mass,
						Vector2D scale,
						double turnRate,
						double maxForce) {
		this.heading = heading;
		this.velocity = velocity;
		this.mass = mass;
		this.side = heading.perp();
		this.maxSpeed = maxSpeed;
		this.maxTurnRate = turnRate;
		this.maxForce = maxForce;
		this.position = position;
		this.boundingRadius = radius;
		this.scale = scale;
	}
	
	public Vector2D velocity() { return velocity; }
	protected void setVelocity(Vector2D newVel) { velocity = newVel; }
	
	public double mass() { return mass; };
	
	public Vector2D side() { return side; }
	
	public double maxSpeed() { return maxSpeed; }
	/**
	 * the maximum speed this entity may travel at
	 */
	protected void setMaxSpeed(double newSpeed) { maxSpeed = newSpeed; }
	/**
	 * the maximum speed this entity may travel at
	 */
	protected double getMaxSpeed() { return maxSpeed; }
	
	public double maxForce() { return maxForce; }
	/**
	 * the maximum force this entity can produce to power itself (think rockets and thrust)
	 */
	protected void setMaxForce(double mf) { maxForce = mf; }
	
	public boolean isSpeedMaxedOut() { return maxSpeed * maxSpeed >= velocity.lengthSq(); }
	public double speed() { return velocity.length(); }
	public double speedSq() { return velocity.lengthSq(); }
	
	public Vector2D heading() { return heading; }
	/**
	 * a normalized vector pointing in the direction the entity is heading
	 */
	protected void setHeading(Vector2D newHeading) {
		if (newHeading.lengthSq() - 1.0 < 0.00001) {
			System.err.println("Warning: Heading set to 0!");
			return;
		}
		
		heading = newHeading;
		side = heading.perp();
	}
	
	public double maxTurnRate() { return maxTurnRate; }
	/**
	 * the maximum rate (radians per second)this vehicle can rotate
	 */
	protected void setMaxTurnRate(double val) { maxTurnRate = val; }


}