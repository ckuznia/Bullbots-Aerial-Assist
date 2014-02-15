package org.bullbots.core;

import edu.wpi.first.wpilibj.CANJaguar;
import edu.wpi.first.wpilibj.can.CANTimeoutException;
import org.usfirst.frc1891.AerialAssist.Robot;

/**
 * @author Clay Kuznia
 */
public class Jaguar extends CANJaguar {
        
    private final int ID;
    private final double LOWEST_ACCEPTABLE_VOLTAGE = 10.0;
    private int voltCheckCount = 0;
    private final boolean hasEncoder;
        
    public Jaguar(int ID, double p, double i, double d) throws CANTimeoutException {
	super(ID);
        
        this.ID = ID;
        hasEncoder = true;
        // Initializing jaguar
        configureJaguar(p, i, d);
        checkIncomingVoltage();
    }
    
    public Jaguar(int ID) throws CANTimeoutException {
        super(ID);
        this.ID = ID;
        hasEncoder = false;
        // Initializing jaguar
        configureJaguar();
        checkIncomingVoltage();
    }
    
    public void driveUsingVoltage(double value) {
	// Voltage range is from -1.0 to 1.0
        setControlMode(CANJaguar.ControlMode.kPercentVbus);
        try {
            this.setX(value);
        } catch (CANTimeoutException ex) {
            ex.printStackTrace();
        }
    }
    
    public void driveUsingSpeed(double value) {
        setControlMode(CANJaguar.ControlMode.kSpeed);
        try {
            System.out.println("mode: "+getControlMode()+ "Ecode:"+getControlMode());
            System.out.println("P:"+getP()+" I:"+getI()+" D:"+getD());
            this.setX(value);
        } catch (CANTimeoutException ex) {
            ex.printStackTrace();
        }
    }
    
    public void driveUsingPosition(double value) {
	// Position unit is in rotations
        setControlMode(CANJaguar.ControlMode.kPosition);
        try {
            this.setX(value);
        } catch (CANTimeoutException ex) {
            ex.printStackTrace();
        }
    }
    
    public void setControlMode(CANJaguar.ControlMode mode) {
	try {
	    checkIncomingVoltage();

	    if(!this.getControlMode().equals(mode)) {
		this.changeControlMode(mode);
		if(hasEncoder) configureJaguar(this.getP(), this.getI(), this.getD());
                else configureJaguar();
	    }
	}
        catch(CANTimeoutException e){
	    e.printStackTrace();
	    System.out.print("Error setting the control mode " + mode + " on Jaguar #" + ID);
	}
    }
    
    private void configureJaguar(double p, double i, double d) {
	try {
            // Initialize PID and encoder stuff
            this.setSpeedReference(CANJaguar.SpeedReference.kQuadEncoder);
            this.setPositionReference(CANJaguar.PositionReference.kQuadEncoder);
            this.configEncoderCodesPerRev(360);
            this.setPID(p, i, d);
            
            // Configuring the rest of the settings
            configureJaguar();
	}
	catch(CANTimeoutException e) {
	    e.printStackTrace();
	    System.out.print("Error configuring Jaguar #" + ID);
	}
    }
    
    private void configureJaguar() {
        try {
            this.setVoltageRampRate(0.0);
            this.configMaxOutputVoltage(12);
            this.enableControl();
        }
        catch(CANTimeoutException e) {
	    e.printStackTrace();
	    System.out.print("Error configuring Jaguar #" + ID);
	}
    }
    
    private void checkIncomingVoltage() {
	try {
	    // Only check for voltage every 50 iterations
	    if(voltCheckCount >= 50) {
		voltCheckCount = 0;
		double incomingVoltage = this.getBusVoltage();

		// If battery voltage is too low
		if(incomingVoltage <= LOWEST_ACCEPTABLE_VOLTAGE) {
		    System.out.print("WARNING! Incoming voltage is at " + incomingVoltage + "v on Jaguar #" + ID);
		}
	    }
	    voltCheckCount++;
	}
	catch(CANTimeoutException e) {
	    e.printStackTrace();
	    System.out.print("Error checking the incoming voltage of Jaguar #" + ID);
	}
    }
    
    public void stop() {
        try {
            this.setX(0.0);
        } catch (CANTimeoutException ex) {
            ex.printStackTrace();
            System.out.print("Error calling stop() on Jaguar #" + ID);
        }
    }
}