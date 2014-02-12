package org.bullbots.component.core;

import edu.wpi.first.wpilibj.CANJaguar;
import edu.wpi.first.wpilibj.can.CANTimeoutException;

import org.bullbots.util.UserDebug;

/**
 * @author Clay Kuznia
 */
public class Jaguar {
    
    private CANJaguar jag;
    
    private final int ID;
    
    private final double LOWEST_ACCEPTABLE_VOLTAGE = 10.0;
    
    private int voltCheckCount = 0;
    
    private final boolean hasEncoder;
        
    public Jaguar(int ID, double p, double i, double d) {
	this.ID = ID;
        hasEncoder = true;
        
        try {
            // Initializing jaguar
	    jag = new CANJaguar(ID);
            configureJaguar(p, i, d);
            checkIncomingVoltage();
        }
        catch(CANTimeoutException e) {
	    e.printStackTrace();
	    UserDebug.print("Error initializing Jaguar #" + ID);
	}
    }
    
    public Jaguar(int ID) {
        this.ID = ID;
        hasEncoder = false;
        
        try {
            // Initializing jaguar
	    jag = new CANJaguar(ID);
            configureJaguar();
            checkIncomingVoltage();
        }
        catch(CANTimeoutException e) {
	    e.printStackTrace();
	    UserDebug.print("Error initializing Jaguar #" + ID);
	}
    }
    
    public void driveUsingVoltage(double voltage) {
	// Voltage unit is from -1.0 to 1.0
	try {
	    setControlMode(CANJaguar.ControlMode.kPercentVbus);
	    jag.setX(voltage);
	}
	catch(CANTimeoutException e) {
	    e.printStackTrace();
	    UserDebug.print("Error driving using voltage on Jaguar #" + ID);
	}
    }
    
    public void driveUsingSpeed(double rpm) {
	// Speed unit is in rotations-per-minute
	try {
	    setControlMode(CANJaguar.ControlMode.kSpeed);
	    jag.setX(rpm);
	}
	catch(CANTimeoutException e) {
	    e.printStackTrace();
	    UserDebug.print("Error driving using speed on Jaguar #" + ID);
	}
    }
    
    public void driveUsingPosition(double rotations) {
	// Position unit is in rotations
	try {
	    setControlMode(CANJaguar.ControlMode.kPosition);
	    jag.setX(rotations);
	}
	catch(CANTimeoutException e) {
	    e.printStackTrace();
	    UserDebug.print("Error driving using position on Jaguar #" + ID);
	}
    }
    
    public void setControlMode(CANJaguar.ControlMode mode) {
	try {
	    checkIncomingVoltage();
	    
	    if(jag.getControlMode() != mode) {
		jag.changeControlMode(mode);
		if(hasEncoder) configureJaguar(jag.getP(), jag.getI(), jag.getD());
                else configureJaguar();
	    }
	}
        catch(CANTimeoutException e){
	    e.printStackTrace();
	    UserDebug.print("Error setting the control mode " + mode + " on Jaguar #" + ID);
	}
    }
    
    private void configureJaguar(double p, double i, double d) {
	try {
            // Initialize PID and encoder stuff
            jag.setSpeedReference(CANJaguar.SpeedReference.kQuadEncoder);
            jag.setPositionReference(CANJaguar.PositionReference.kQuadEncoder);
            jag.configEncoderCodesPerRev(360);
            jag.setPID(p, i, d);
            
            // Configuring the rest of the settings
            configureJaguar();
	}
	catch(CANTimeoutException e) {
	    e.printStackTrace();
	    UserDebug.print("Error configuring Jaguar #" + ID);
	}
    }
    
    private void configureJaguar() {
        try {
            jag.setVoltageRampRate(0.0);
            jag.configMaxOutputVoltage(12);
            jag.enableControl();
        }
        catch(CANTimeoutException e) {
	    e.printStackTrace();
	    UserDebug.print("Error configuring Jaguar #" + ID);
	}
    }
    
    private void checkIncomingVoltage() {
	try {
	    // Only check for voltage every 50 iterations
	    if(voltCheckCount >= 50) {
		voltCheckCount = 0;
		double incomingVoltage = jag.getBusVoltage();
		
		// If battery voltage is too low
		if(incomingVoltage <= LOWEST_ACCEPTABLE_VOLTAGE) {
		    UserDebug.print("WARNING! Incoming voltage is at " + incomingVoltage + "v on Jaguar #" + ID);
		}
	    }
	    voltCheckCount++;
	}
	catch(CANTimeoutException e) {
	    e.printStackTrace();
	    UserDebug.print("Error checking the incoming voltage of Jaguar #" + ID);
	}
    }
    
    public double getX() {
	try {
	    return jag.getX();
	}
	catch(CANTimeoutException e) {
	    e.printStackTrace();
	    UserDebug.print("Error using getX() on Jaguar #" + ID);
	}
	return 0;
    }
    
     public void setX(double amount) {
	try {
	    jag.setX(amount);
	}
	catch(CANTimeoutException e) {
	    e.printStackTrace();
	    UserDebug.print("Error using setX() on Jaguar #" + ID);
	}
    }
     
     public double getSpeed() {
        try {
	    return jag.getSpeed();
	}
	catch(CANTimeoutException e) {
	    e.printStackTrace();
	    UserDebug.print("Error using getSpeed() on Jaguar #" + ID);
	}
        return 0.0;
     }
     
     public double getOutputCurrent() {
         try {
             return jag.getOutputCurrent();
         }
         catch(CANTimeoutException e) {
	    e.printStackTrace();
	    UserDebug.print("Error using getCurrent() on Jaguar #" + ID);
         }
         return 0.0;
     }
}
