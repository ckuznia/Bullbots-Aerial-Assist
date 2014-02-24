package org.bullbots.core;

import edu.wpi.first.wpilibj.CANJaguar;
import edu.wpi.first.wpilibj.can.CANTimeoutException;

/**
 * @author Clay Kuznia
 */
public class Jaguar extends CANJaguar {
        
    private final int ID;
    private final double LOWEST_ACCEPTABLE_VOLTAGE = 11.5;
    private int voltCheckCount = 0;
    private final boolean hasEncoder;
        
    public Jaguar(int ID, double p, double i, double d) throws CANTimeoutException {
	super(ID);
        System.out.println("Found: " + ID + " with encoder");
        this.ID = ID;
        hasEncoder = true;
        // Initializing jaguar
        configureJaguar(p, i, d);
        checkIncomingVoltage();
    }
    
    public Jaguar(int ID) throws CANTimeoutException {
        super(ID);
        System.out.println("Found: " + ID);
        this.ID = ID;
        hasEncoder = false;
        // Initializing jaguar
        configureJaguar();
        checkIncomingVoltage();
    }
    
    private void setValue(double value) {
        try {
            
            /*
            this.setX(value);
            System.out.println("setX was called at " + value + " on Jaguar # " + ID + "\n");
            */
            
            /*
            OLD CODE IS COMMENTED BELOW, ABOVE IS FOR TESTING
            */
            
            
            
            
            // Rounding the value in order not to overload the cRIO
            double roundedValue = roundValue(value);
            
            //System.out.println("SETVALUE ::: roundedValue: " + roundedValue + " getX(): " + this.getX());
            
            // BELOW IS COMMENTED OUT because i wanted to make sure
            // that the value to the jaguar was continually set, otherwise the
            // motor will stop. So i removed the 'if' statement
            
            // Only updating the jaguar if the value has changed
            //if(roundedValue != this.getX()) {
                // Making sure the voltage isn't getting to low
                checkIncomingVoltage();
                this.setX(roundedValue);
                System.out.println("setX was called at " + roundedValue + " on Jaguar # " + ID + "\n");
            //}
                    
                    
            
        } catch (CANTimeoutException ex) {
            ex.printStackTrace();
        }
    }
    
    public void driveUsingVoltage(double voltage) {
	// Voltage range is from -1.0 to 1.0
        setControlMode(CANJaguar.ControlMode.kPercentVbus);
        setValue(voltage);
    }
    
    public void driveUsingSpeed(double RPM) {
        // Speed unit is in RPMs
        // PIDS are REQUIRED in order to use this mode
        setControlMode(CANJaguar.ControlMode.kSpeed);
        setValue(RPM);
    }
    
    public void driveUsingPosition(double rotations) {
	// Position unit is in rotations
        // PIDS are REQUIRED in order to use this mode
        setControlMode(CANJaguar.ControlMode.kPosition);
        setValue(rotations);
    }
    
    public void driveUsingCurrent(double current) {
        // Current unit is in Amps
        // PIDS are REQUIRED in order to use this mode
        setControlMode(CANJaguar.ControlMode.kCurrent);
        setValue(current);
    }
    
    public void setControlMode(CANJaguar.ControlMode mode) {
	try {
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
            this.configEncoderCodesPerRev(1440);
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
            this.setVoltageRampRate(0);
            this.configMaxOutputVoltage(12);
            this.enableControl(0.0);
        }
        catch(CANTimeoutException e) {
	    e.printStackTrace();
	    System.out.print("Error configuring Jaguar #" + ID);
	}
    }
    
    private void checkIncomingVoltage() {
	try {
	    // Only check for voltage every 75 iterations
	    if(voltCheckCount >= 75) {
		voltCheckCount = 0;
		double incomingVoltage = this.getBusVoltage();
                
		// If battery voltage is too low
		if(incomingVoltage <= LOWEST_ACCEPTABLE_VOLTAGE) {
		    System.out.print("WARNING! Incoming voltage is at " + incomingVoltage + "v on Jaguar #" + ID);
		}
	    }
            else voltCheckCount++;
	}
	catch(CANTimeoutException e) {
	    e.printStackTrace();
	    System.out.print("Error checking the incoming voltage of Jaguar #" + ID);
	}
    }
    
    public void stop() {
        driveUsingVoltage(0.0);
    }
    
    public double roundValue(double value) {
        return (double) (int) ((value + 0.005) * 100) / 100;
    }
}