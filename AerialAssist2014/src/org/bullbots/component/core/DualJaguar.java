package org.bullbots.component.core;

import edu.wpi.first.wpilibj.CANJaguar;

/**
 * @author Clay Kuznia
 */
public class DualJaguar {
    
    private final Jaguar JAG_1, JAG_2;
    
    public DualJaguar(int ID1, int ID2, double P, double I, double D) {
	JAG_1 = new Jaguar(ID1, P, I, D);
	JAG_2 = new Jaguar(ID2, P, I, D);
    }
    
    public void driveUsingSpeed(double forwardSpeed) {
	// Setting modes that way the getX() returns the correct value
	JAG_1.setControlMode(CANJaguar.ControlMode.kSpeed);
	JAG_2.setControlMode(CANJaguar.ControlMode.kSpeed);
	
	// If the speeds are not up to the set speed AND equal, them keep setting them to that speed
	if(JAG_1.getX() != forwardSpeed || JAG_2.getX() != forwardSpeed) {
	    JAG_1.driveUsingSpeed(forwardSpeed);
	    JAG_2.driveUsingSpeed(forwardSpeed);
	}
    }
    
    public void driveUsingVoltage(double forwardVoltage) {
	JAG_1.driveUsingVoltage(forwardVoltage);
	JAG_2.driveUsingVoltage(forwardVoltage);
    }
    
    public void driveUsingPosition(double rotation) {
	JAG_1.driveUsingPosition(rotation);
	JAG_2.driveUsingPosition(rotation);
    }
}
