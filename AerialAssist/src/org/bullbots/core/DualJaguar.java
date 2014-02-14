package org.bullbots.core;

import edu.wpi.first.wpilibj.CANJaguar;

/**
 * @author Clay Kuznia
 */
public class DualJaguar {
    
    private final Jaguar MASTER_JAG, SLAVE_JAG;
    
    public DualJaguar(int MASTER_ID, int SLAVE_ID, double P, double I, double D) {
	MASTER_JAG = new Jaguar(MASTER_ID, P, I, D);
	SLAVE_JAG = new Jaguar(SLAVE_ID);
    }
    
    public void driveUsingVoltage(double votage) {
	MASTER_JAG.driveUsingVoltage(votage);
	SLAVE_JAG.driveUsingVoltage(votage);
    }
    
    public void driveUsingSpeed(double RPM) {
	MASTER_JAG.setControlMode(CANJaguar.ControlMode.kSpeed);
        
        System.out.print("\nMaster getX(): " + MASTER_JAG.getX() + " RPM: " + RPM);
        
        // Updating the speed of the master jag
        if(MASTER_JAG.getX() != RPM) {
            MASTER_JAG.setX(RPM);
            System.out.print("Master jag was updated.");
        }
        
        // Setting them in current mode in order to get the current
        MASTER_JAG.setControlMode(CANJaguar.ControlMode.kCurrent);
        SLAVE_JAG.setControlMode(CANJaguar.ControlMode.kCurrent);
        
	// If the slave current does not match the master, then update the slave's current to the master's
	if(SLAVE_JAG.getOutputCurrent() != MASTER_JAG.getOutputCurrent()) {
            SLAVE_JAG.setX(MASTER_JAG.getOutputCurrent());
            
            System.out.print("Slave jag was updated.");
	}
    }
    
    public void driveUsingPosition(double rotations) {
	MASTER_JAG.driveUsingPosition(rotations);
	SLAVE_JAG.driveUsingPosition(rotations);
    }
    
    public void stop() {
        MASTER_JAG.stop();
        SLAVE_JAG.stop();
    }
    
    public double getSpeed() {
        MASTER_JAG.setControlMode(CANJaguar.ControlMode.kSpeed);
        return MASTER_JAG.getSpeed();
    }
}