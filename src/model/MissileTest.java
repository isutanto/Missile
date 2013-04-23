package model;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class MissileTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testCalculateLift() {
		Vector2D lift = new Vector2D();
		Vector2D velocity = new Vector2D();
		double rho = 1.4;
		double Sb = 2.1;
		double Cy = 1;
		velocity.x = 20;
		velocity.y = 12;
		
		lift = velocity.sq();
		lift = lift.mul(rho);
		lift = lift.div(2);
		lift = lift.mul(Sb);
		lift = lift.mul(Cy);
		lift.y = -lift.y;
		
		assertEquals(-211.68, lift.y, 0);

	}
	
	@Test
	public void testCalculateDrag(){
		Vector2D velocity = new Vector2D();
		Vector2D drag = new Vector2D();
		double rho = 1.4;
		double Sb = 2.1;
		double Cx = 1;
		
		velocity.x = 100;
		velocity.y = 36;
		
		drag = velocity.sq();
		drag = drag.mul(rho);
		drag = drag.div(2);
		drag = drag.mul(Sb);
		drag = drag.mul(Cx);
		
		double length = drag.length();
		
		assertEquals(14822.9377053, length, 0.00005);
		
	}
	
	@Test
	public void testCalculateThrust(){
		Vector2D thrust;
		Vector2D position = new Vector2D();
		position.x = 25;
		position.y = 160;
		
		double Pc = 24;
		double Sb = 2.1;
		double Gc = 5;
		double u = 80;
		double g = 9.8;
		double Ph = 0;
		
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
		
		assertEquals(80.7163265, temp, 0.000005);
	}
	
	@Test
	public void testCalculateWeight(){
		double percent;
		double mass = 2000;
		int weight;
		Vector2D position = new Vector2D();
		position.x = 150;
		position.y = 375;
		
		int G0 = (int) (.0097 * mass); 
		
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
		
		assertEquals(18, weight, 0);
		
	}

}
