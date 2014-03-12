package org.bullbots.core;

import edu.wpi.first.wpilibj.CANJaguar;
import edu.wpi.first.wpilibj.can.CANTimeoutException;

/**
 * @author Clay Kuznia
 */
public class Jaguar extends CANJaguar {
    
    private final int ID;
    private double P, I, D;
    private final double LOWEST_ACCEPTABLE_VOLTAGE = 11.5;
    private int voltCheckCount = 0;
    private final boolean hasEncoder;
    private static final ControlMode DEFAULT_CONTROL_MODE = ControlMode.kPercentVbus;
        
    public Jaguar(final int ID, final double P, final double I, final double D) throws CANTimeoutException {
	super(ID, DEFAULT_CONTROL_MODE);
        System.out.println("Found Jaguar #" + ID + ", Jaguar configuration will require an encoder");
        
        // Initializing values
        this.ID = ID;
        hasEncoder = true;
        this.P = P;
        this.I = I;
        this.D = D;
        
        // Initializing jaguar
        configureJaguarWithEncoder();
        checkIncomingVoltage();
    }
    
    public Jaguar(int ID) throws CANTimeoutException {
        super(ID, DEFAULT_CONTROL_MODE);
        System.out.println("Found Jaguar #" + ID);
        
        // Initializing values
        this.ID = ID;
        hasEncoder = false;
        
        // Initializing jaguar
        configureJaguar();
        checkIncomingVoltage();
    }
    
    public void printSettings() {
        try {
            // Showing information about the Jagaur's settings
            System.out.println("\nJagaur #" + ID + " Settings:");
            System.out.println("\t\tBus Voltage: " + this.getBusVoltage());
            System.out.println("\t\tSmartDashboard Type: " + this.getSmartDashboardType());
            System.out.println("\t\tControl Mode: " + this.getControlMode().value + 
                    "\n\t\t\t" + CANJaguar.ControlMode.kPercentVbus.value + " -> kPercentVbus" +
                    "\n\t\t\t" + CANJaguar.ControlMode.kCurrent.value + " -> kCurrent" +
                    "\n\t\t\t" + CANJaguar.ControlMode.kSpeed.value + " -> kSpeed" +
                    "\n\t\t\t" + CANJaguar.ControlMode.kPosition.value +" -> kPosition" +
                    "\n\t\t\t" + CANJaguar.ControlMode.kVoltage.value + " -> kVoltage");
            
            ControlMode originalMode = this.getControlMode();
            
            // Switching to a mode that uses PIDs, otherwise the
            // getP(), getI(), and getD() will all show up as 0. 
            changeControlMode(CANJaguar.ControlMode.kSpeed);
            
            System.out.println("\t\tPID Values:\n\t\t\tP: " + this.getP() + "\n\t\t\tI: " + this.getI() + "\n\t\t\tD: " + this.getD());
            
            // Returning the Jaguar back to its original control mode
            changeControlMode(originalMode);
            
            // Continuing to information about the Jagaur's settings
            System.out.println("\t\tFirmware Version: " + this.getFirmwareVersion());
            System.out.println("\t\tHardware Version: " + this.getHardwareVersion());
            System.out.println("\t\tPosition Reference: " + this.getPositionReference().value +
                    "\n\t\t\t" + CANJaguar.PositionReference.kNone.value + " -> kNone" +
                    "\n\t\t\t" + CANJaguar.PositionReference.kQuadEncoder.value + " -> kQuadEncoder" +
                    "\n\t\t\t" + CANJaguar.PositionReference.kPotentiometer.value + " -> kPotentiometer");
            System.out.println("\t\tSpeed Reference: " + this.getSpeedReference().value +
                    "\n\t\t\t" + CANJaguar.SpeedReference.kNone.value + " -> kNone" +
                    "\n\t\t\t" + CANJaguar.SpeedReference.kEncoder.value + " -> kEncoder" +
                    "\n\t\t\t" + CANJaguar.SpeedReference.kInvEncoder.value + " -> kInvEncoder" +
                    "\n\t\t\t" + CANJaguar.SpeedReference.kQuadEncoder.value + " -> kQuadEncoder");
            System.out.println("\t\tTemperature: " + this.getTemperature());
            System.out.println("\t\tgetX() Value: " + this.getX());
            System.out.println("\t\tisAlive() Value: " + this.isAlive());            
            
        } catch (CANTimeoutException ex) {
            ex.printStackTrace();
            System.out.println("Error using printSettings on Jaguar #" + ID);
        }
    }
    
    public void setX(double value) {
        try {
            // Rounding the value in order not to overload the cRIO
            double roundedValue = roundValue(value);
            
            // Only updating the jaguar if the value has changed
            if(roundedValue != this.getX()) {
                super.setX(roundedValue);
                // Making sure the voltage isn't getting to low
                checkIncomingVoltage();
            }
        } catch (CANTimeoutException ex) {
            ex.printStackTrace();
        }
    }
    
    public void driveUsingVoltage(double voltage) {
	// Voltage range is from -1.0 to 1.0
        changeControlMode(CANJaguar.ControlMode.kPercentVbus);
        setX(voltage);
    }
    
    public void driveUsingSpeed(double RPM) {
        // Speed unit is in RPMs
        // PIDS are REQUIRED in order to use this mode
        changeControlMode(CANJaguar.ControlMode.kSpeed);
        setX(RPM);
    }
    
    public void driveUsingPosition(double rotations) {
	// Position unit is in rotations
        // PIDS are REQUIRED in order to use this mode
        changeControlMode(CANJaguar.ControlMode.kPosition);
        setX(rotations);
    }
    
    public void driveUsingCurrent(double current) {
        // Current unit is in Amps
        // PIDS are REQUIRED in order to use this mode
        changeControlMode(CANJaguar.ControlMode.kCurrent);
        setX(current);
    }
    
    public void changeControlMode(CANJaguar.ControlMode mode) {
	try {
            // Only changing the control mode if needed
	    if(!this.getControlMode().equals(mode)) {
		super.changeControlMode(mode);
                
                // Since the control mode was changed, the
                // CANJaguar's settings MUST be reset
		if(hasEncoder) configureJaguarWithEncoder();
                else configureJaguar();
	    }
	}
        catch(CANTimeoutException e){
	    e.printStackTrace();
	    System.out.println("Error setting the control mode " + mode + " on Jaguar #" + ID);
	}
    }
    
    private void configureJaguarWithEncoder() {
	try {
            // Initialize PID and encoder settings
            this.setSpeedReference(CANJaguar.SpeedReference.kQuadEncoder);
            this.setPositionReference(CANJaguar.PositionReference.kQuadEncoder);
            this.configEncoderCodesPerRev(1440);
            this.setPID(P, I, D);
            
            // Configuring the rest of the settings
            configureJaguar();
	}
	catch(CANTimeoutException e) {
	    e.printStackTrace();
	    System.out.println("Error configuring Jaguar #" + ID);
	}
    }
    
    private void configureJaguar() {
        try {
            this.setVoltageRampRate(0);
            this.configMaxOutputVoltage(12);
            this.enableControl(0);
        }
        catch(CANTimeoutException e) {
	    e.printStackTrace();
	    System.out.println("Error configuring Jaguar #" + ID);
	}
    }
    
    public void setPID(double P, double I, double D) {
        try {
            this.P = P;
            this.I = I;
            this.D = D;
            super.setPID(P, I, D);
        } catch (CANTimeoutException ex) {
            ex.printStackTrace();
            System.out.println("Error configuring PIDs on Jaguar #" + ID);
        }
    }
    
    private void checkIncomingVoltage() {
	try {
	    /*
            Only check for voltage after a set number of iterations.
            
            *** Later we should only print out the warning message
            if the voltage has been low for so many consecutive iterations.
            that way we are not printing out every time the voltage drops,
            becuase voltage dips can occur (Even with capacitors).
            */
	    if(voltCheckCount >= 60) {
		voltCheckCount = 0;
		double incomingVoltage = this.getBusVoltage();
                
		// If battery voltage is too low
		if(incomingVoltage <= LOWEST_ACCEPTABLE_VOLTAGE) {
		    System.out.println("WARNING! Incoming voltage is at " + incomingVoltage + "v on Jaguar #" + ID);
		}
	    }
            else voltCheckCount++;
	}
	catch(CANTimeoutException e) {
	    e.printStackTrace();
	    System.out.println("Error checking the incoming voltage of Jaguar #" + ID);
	}
    }
    
    public void stop() {
        try {
            ControlMode originalMode = this.getControlMode();
            driveUsingVoltage(0.0);
            
            /*
            Setting the control mode back to what it orginally
            was, that way the user can assume the control mode
            is unchanged when they stop the motor.
            */
            changeControlMode(originalMode);
        } catch (CANTimeoutException ex) {
            ex.printStackTrace();
            System.out.println("Error stopping Jaguar #" + ID);
        }
    }
    
    public void showVoltage() {
        try {
            System.out.println("Jag #" + ID + "Output Voltage: " + this.getOutputVoltage());
        }
        catch (CANTimeoutException ex) {
            ex.printStackTrace();
        }
    }
    
    public void showCurrent() {
        try {
            System.out.println("Jag #" + ID + "Output Current: " + this.getOutputCurrent());
        }
        catch (CANTimeoutException ex) {
            ex.printStackTrace();
        }
    }
    
    public static double roundValue(double value) {
        return (double) (int) ((value + 0.005) * 100) / 100;
    }
}