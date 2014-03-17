// RobotBuilder Version: 1.0
//
// This file was generated by RobotBuilder. It contains sections of
// code that are automatically generated and assigned by robotbuilder.
// These sections will be updated in the future when you export to
// Java from RobotBuilder. Do not put any code or make any change in
// the blocks indicating autogenerated code or it will be lost on an
// update. Deleting the comments indicating the section will prevent
// it from being updated in the future.

package org.usfirst.frc1891.AerialAssist.subsystems;

import edu.wpi.first.wpilibj.AnalogChannel;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.SpeedController;
import edu.wpi.first.wpilibj.can.CANTimeoutException;
import org.usfirst.frc1891.AerialAssist.RobotMap;
import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.bullbots.core.Jaguar;
import org.usfirst.frc1891.AerialAssist.Robot;

/**
 * @author Clay Kuznia
 */
public class Shooter extends Subsystem {
    // BEGIN AUTOGENERATED CODE, SOURCE=ROBOTBUILDER ID=DECLARATIONS
    DigitalInput loadSwitch = RobotMap.shooterloadSwitch;
    SpeedController shootMotor = RobotMap.shootershootMotor;
    DigitalInput shootSwitch = RobotMap.shootershootSwitch;
    AnalogChannel IRSensor = RobotMap.shooterIRSensor1;
    AnalogChannel potentiometer = RobotMap.shooterpotentiometer;
    SpeedController angleMotor = RobotMap.shooterangleMotor;
    // END AUTOGENERATED CODE, SOURCE=ROBOTBUILDER ID=DECLARATIONS
    
    /*
    Shooter:
    1: winch needs to be ready to lock
    2: winch needs to be pulled down until it hits the loadServo (now its locked)
    3: winch needs to be unwound to spool out the rope
    4: winch lock needs to be released to fire, as you do this, it should be "relocked"
    5: winch can now be pulled back down (re-loaded)
    */
    
    /*
    Bugs:
    + May be a bug with shooter motor
    */
    
    /*
    Do this sometime:
    
    public void setSomeState(boolean value) {
    
        someState = value
        if(someState) {
            otherState = false;
            otherState2 = false;
            ...
        }
    }
    */
    
    // For knowing what mode the robot is in (Teleop or Autonomous)
    private final Robot robot;
    
    private boolean 
            // Shooter states
            isLoaded = false,
            isShooting = false,
            isLoading = false,
            isDown, // The robots tilt position will be checked and assigned on startup
            isTiltingShooter = false, 
            hasFired = false;
    private final double 
            // Potentiometer
            UP_POT_VALUE = 2.46, // Always the smaller value
            DOWN_POT_VALUE = 4.47, // Always the bigger value
            MID_POT_VALUE = (UP_POT_VALUE + DOWN_POT_VALUE) / 2,
            UP_POT_TOLERANCE = 0.03,
            DOWN_POT_TOLERANCE = 0.1,
            
            // Angle(tilt) motor
            ANGLE_MOTOR_SPEED = 0.8,
            
            // Shoot motor
            SHOOT_MOTOR_SPEED = 0.4,
            
            // Delays
            POST_SHOOT_DELAY = 1200, //1200
            
            // IR Values
            MIN_IR_VALUE = 1.95, // 2.0 is the actual spot
            MAX_IR_VALUE = 2.1,
            IR_TOLERANCE = 0.1;
    private long startTime;
        
    public Shooter(Robot robot) {
        this.robot = robot;
        
        // Finding the tilt position of the shooter
        isDown = potentiometer.getVoltage() > MID_POT_VALUE;
    }
    
    /**
     * Used to update the shooter during Tele-operation or Autonomous.
     */
    public void update() {
        updateState();
        updateTilting();
    }
    
    private void updateState() {
        // Shooter is loaded and ready to fire
        if(isLoaded) updateLoaded();
        
        // Shooter is in the middle of the shooting process
        else if(isShooting) updateShooting();
        
        // Shooter is loading
        else if(isLoading) updateLoading();
        
        // Shooter is ready to load (in it's idle state)
        else {
            // Checking if we are in teleop or autonomous mode...
            
            // If in Autonomous
            if(robot.isAutonomous()) updateIdleAutonomous();
            // Otherwise, In Teleop mode
            else updateIdleTeleop();
        }
    }
    
    private void updateLoaded() {
        SmartDashboard.putBoolean("inrange", autoInRange());
        
        System.out.println("Shooter State: Loaded");
        // Must use BOTH joysticks to shoot, only active when shooter is not tilting and is up
        if(!isTiltingShooter && 
                !isDown &&
                Robot.oi.joystickController1.isButtonDown(Robot.SHOOT_BUTTON) && 
                Robot.oi.joystickController2.isButtonDown(Robot.SHOOT_BUTTON)) {
            isLoaded = false;
            isShooting = true;
            System.out.println("FIRED");
        }
        System.out.println("isTiltingShooter: " + isTiltingShooter + " isDown" + isDown);
        
        
    }
    
    private void updateShooting() {
        System.out.println("Shooter State: Shooting");
        
        // Checking to see if we are finished firing and locking the winch
        if(fireAndLock()) {
            //System.out.println("\tJust started waiting...");
            try {
                Thread.sleep((int) POST_SHOOT_DELAY);
            }
            catch(InterruptedException e) {
                e.printStackTrace();
            }
            isShooting = false;
            //System.out.println("\tJust ENDED waiting...");
            
            
            
            // Starting the timer
            //startTime = System.currentTimeMillis();
        }
        /*// Delay before moving onto the next state
        else {
            if(System.currentTimeMillis() >= startTime + POST_SHOOT_DELAY) isShooting = false;
        }*/
    }
    
    private void updateLoading() {
        System.out.println("Shooter State: Loading");
        load();
    }
    
    private void updateIdleTeleop() {
        System.out.println("Shooter State: Ready to Load - Teleop");
        
        // Resetting encoders
        resetEncoderPos();
        
        // Loading robot instantly
        load();
    }
    
    private void updateIdleAutonomous() {
        System.out.println("Shooter State: Ready to Load - Autonomous");
        
        // Resetting encoders
        resetEncoderPos();
        
        // Loading the robot instantly
        load();
    }
    
    private void updateTilting() {
        if(!isTiltingShooter) { // Awaiting a request to tilt
            if(Robot.oi.joystickController1.isButtonDown(Robot.TILT_BUTTON) || 
                    Robot.oi.joystickController2.isButtonDown(Robot.TILT_BUTTON)) {
                isTiltingShooter = true;
            }
        }
        else { // Shooter is moving
            // Rounding value to 2 decimal places in order
            // not to overload the cRIO
            double roundedPotVoltage = Jaguar.roundValue(potentiometer.getVoltage());
            
            if(isDown) { // Move it up
                // If its in position
                if(roundedPotVoltage <= UP_POT_VALUE + UP_POT_TOLERANCE) {
                    angleMotor.set(0.0);
                    isDown = false;
                    isTiltingShooter = false;
                }
                // Otherwise keep moving the motor
                else angleMotor.set(-ANGLE_MOTOR_SPEED);
            }
            else { // Move it down
                // If its in position
                if(roundedPotVoltage >= DOWN_POT_VALUE - DOWN_POT_TOLERANCE) {
                    angleMotor.set(0.0);
                    isDown = true;
                    isTiltingShooter = false;
                }
                // Otherwise keep moving the motor
                else angleMotor.set(ANGLE_MOTOR_SPEED);
            }
        }
    }
    
    public boolean fireAndLock() {
        //System.out.println("\tfireAndLock()");
        shootMotor.set(SHOOT_MOTOR_SPEED);
        
        // Waiting until the lock is released (fired)
        if(!hasFired) {
            hasFired = !shootSwitch.get();
            //System.out.println("\t\tNot Fired");
        }
        
        // Now waiting until the motor is back on the
        // switch, then stopping the motor (locked)
        else {
            //System.out.println("\t\tFIRED! (Winch unlocked)");
            if(shootSwitch.get()) {
                //System.out.println("\t\tLocked Winch.. done with fireAndLock()");
                shootMotor.set(0.0);
                hasFired = false;
                return true;
            }
        }
        //System.out.println("\t\tHasFired = " + hasFired);
        
        // Returning false until the robot has fired
        // and the winch has been relocked
        return false;
    }
    
    private void resetEncoderPos() {
        try {
            // Resetting the 0 point on the robot (Re-calibrating)
            RobotMap.winchJags.getMasterJag().enableControl(0.0);
        } catch (CANTimeoutException ex) {
            ex.printStackTrace();
        }
    }
    
    public void load() {
        // So far the loading is the same as the calibrating,
        // so we just call calibrate() in order to load...
        // this might be changed later
        RobotMap.winchJags.calibrate();
    }
    
    public boolean inRange() {
        // Checking if we are within shooting range
        //System.out.println(IRSensor.getVoltage());
        return (IRSensor.getVoltage()>= MIN_IR_VALUE && IRSensor.getVoltage() <= MAX_IR_VALUE);
    }
    
    public boolean autoInRange() {
        return (IRSensor.getVoltage() >= MIN_IR_VALUE && IRSensor.getVoltage() <= MAX_IR_VALUE + IR_TOLERANCE);
    }
    
    public void calibrate() {
        RobotMap.winchJags.calibrate();
    }
    
    protected void initDefaultCommand() {}
    
    public boolean isLoaded() {
        return isLoaded;
    }
    
    public void setLoaded(boolean value) {
       isLoaded = value;
    }
    
    public void setShooting(boolean value) {
        isShooting = value;
    }
    
    public void setLoading(boolean value) {
        isLoading = value;
    }
}
