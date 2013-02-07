package model;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import junit.framework.TestCase;

public class Vector2DTest extends TestCase{

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		double x = 2;
		double y = 5;
		
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testLength() {
		double x = 2;
		double y = 5;
		double length;
		length = Math.sqrt(x * x + y * y);
		
		assertEquals(5.385164807134504, length, 0);
				
	}
	
	@Test
	public void testLengthSq() {
		double x = 4;
		double y = 3;
		double sqLength;
		sqLength = x * x + y * y;
		
		assertEquals(25, sqLength, 0);
	}
	
	/*
	@Test
	public void testDot(){
		double dot;
		Vector2D vect1;
		Vector2D vect2;
		
		vect1 = new Vector2D();
		vect2 = new Vector2D();
		
		vect1.x = 3;
		vect1.y = 4;
		vect2.x = 2;
		vect2.y = 7;

		dot = (vect1.x)*(vect2.x) + (vect1.y)*(vect2.y);
		
		assertEquals(34, dot, 0);
	}
	*/
	
	

}
