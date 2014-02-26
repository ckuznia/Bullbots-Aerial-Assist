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
        
    public Jaguar(final int ID, final double P, final double I, final double D) throws CANTimeoutException {
	super(ID);
        System.out.println("Found Jaguar #" + ID + ", Jaguar configuration will require an encoder");
        this.P = P;
        this.I = I;
        this.D = D;
        
        this.ID = ID;
        hasEncoder = true;
        // Initializing jaguar
        configureJaguarWithEncoder();
        changeControlMode(CANJaguar.ControlMode.kPercentVbus);
        checkIncomingVoltage();
    }
    
    public Jaguar(int ID) throws CANTimeoutException {
        super(ID);
        System.out.println("Found Jaguar #" + ID);
        
        this.ID = ID;
        hasEncoder = false;
        // Initializing jaguar
        configureJaguar();
        changeControlMode(CANJaguar.ControlMode.kPercentVbus);
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
    
    private void setValue(double value) {
        try {
            // Rounding the value in order not to overload the cRIO
            double roundedValue = roundValue(value);
            
            // Only updating the jaguar if the value has changed
            if(roundedValue != this.getX()) {
                // Making sure the voltage isn't getting to low
                checkIncomingVoltage();
                this.setX(roundedValue);
            }
        } catch (CANTimeoutException ex) {
            ex.printStackTrace();
        }
    }
    
    public void driveUsingVoltage(double voltage) {
	// Voltage range is from -1.0 to 1.0
        changeControlMode(CANJaguar.ControlMode.kPercentVbus);
        setValue(voltage);
    }
    
    public void driveUsingSpeed(double RPM) {
        // Speed unit is in RPMs
        // PIDS are REQUIRED in order to use this mode
        changeControlMode(CANJaguar.ControlMode.kSpeed);
        setValue(RPM);
    }
    
    public void driveUsingPosition(double rotations) {
	// Position unit is in rotations
        // PIDS are REQUIRED in order to use this mode
        changeControlMode(CANJaguar.ControlMode.kPosition);
        setValue(rotations);
    }
    
    public void driveUsingCurrent(double current) {
        // Current unit is in Amps
        // PIDS are REQUIRED in order to use this mode
        changeControlMode(CANJaguar.ControlMode.kCurrent);
        setValue(current);
    }
    
    public void changeControlMode(CANJaguar.ControlMode mode) {
	try {
	    if(!this.getControlMode().equals(mode)) {
		super.changeControlMode(mode);
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
            // Initialize PID and encoder stuff
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
            this.enableControl(0.0);
        }
        catch(CANTimeoutException e) {
	    e.printStackTrace();
	    System.out.println("Error configuring Jaguar #" + ID);
	}
    }
    
    public void setPID(double P, double I, double D) {
        try {
            super.setPID(P, I, D);
            this.P = P;
            this.I = I;
            this.D = D;
        } catch (CANTimeoutException ex) {
            ex.printStackTrace();
            System.out.println("Error configuring PIDs on Jaguar #" + ID);
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
        driveUsingVoltage(0.0);
    }
    
    public double roundValue(double value) {
        return (double) (int) ((value + 0.005) * 100) / 100;
    }
}