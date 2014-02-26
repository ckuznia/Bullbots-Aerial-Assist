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
import org.usfirst.frc1891.AerialAssist.RobotMap;
import edu.wpi.first.wpilibj.*;
import edu.wpi.first.wpilibj.can.CANTimeoutException;
import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.bullbots.core.Winch;
import org.usfirst.frc1891.AerialAssist.Robot;
/**
 *
 */
public class Shooter extends Subsystem {
    // BEGIN AUTOGENERATED CODE, SOURCE=ROBOTBUILDER ID=DECLARATIONS
    DigitalInput loadSwitch = RobotMap.shooterloadSwitch;
    SpeedController shootMotor = RobotMap.shootershootMotor;
    DigitalInput shootSwitch = RobotMap.shootershootSwitch;
    AnalogChannel iRSensor2 = RobotMap.shooterIRSensor2;
    AnalogChannel iRSensor1 = RobotMap.shooterIRSensor1;
    AnalogChannel potentiometer = RobotMap.shooterpotentiometer;
    SpeedController angleMotor = RobotMap.shooterangleMotor;
    // END AUTOGENERATED CODE, SOURCE=ROBOTBUILDER ID=DECLARATIONS
    
    /*
    Shooter:
    1: winch needs to be ready to lock
    2: winch needs to be pulled down until it hits the loadServo (now its locked)
    3: winch needs to be unwound to release rope slack
    4: winch lock needs to be released to fire, as you do this, it should be "re-cocked"
    5: winch can now be pulled back down (start back at #2 is in "ready to lock" position)
    */
    
    private boolean isCalibrated = false, readyToFire = false, shootRequested = false, motorOffSwitch = false, movingShooter = false, isDown;
    private final int shootButton = 1, tiltButton = 2;
    private final double minPotValue = 3.2, maxPotValue = 4.91, midPotValue = (minPotValue + maxPotValue) / 2, potTolerance = 0.05;
    private final double ANGLE_MOTOR_SPEED = 0.5;
    
    protected void initDefaultCommand() {}
    
    public Shooter() {
         System.out.println("midPotValue:" + midPotValue);
         
         // Finding out what position the shooter is in
         isDown = potentiometer.getValue() < midPotValue;
    }
    
    public void update() {
        updateShooting();
        updateTilting();
    }
    
    private void updateShooting() {
        // If shoot button is pressed and shooter is ready to shoot
        if(readyToFire && !movingShooter) {
            // Must use BOTH joysticks to shoot
            if(Robot.oi.joystickController1.isButtonDown(shootButton) && Robot.oi.joystickController2.isButtonDown(shootButton)) {
                readyToFire = false;
                shootRequested = true;
            }
            else shootMotor.set(0.0);
        }
        // Winch lock has been released and needs to be relocked
        else {
            // Shoots the ball, and relocks the winch, if needed
            if(shootRequested) shootAndRelock();
            else {
                System.out.println("prepToFire() code......");
                prepToFire();
            }
        }
    }
    
    private void updateTilting() {
        
        if(!movingShooter) {
            if(Robot.oi.joystickController1.isButtonDown(tiltButton) || Robot.oi.joystickController2.isButtonDown(tiltButton)) {
                movingShooter = true;
            }
        }
        else { // Shooter is moving
            if(isDown) { // Move it up
                // If its in position
                if(potentiometer.getValue() <= minPotValue + potTolerance) {
                    angleMotor.set(0.0);
                    isDown = false;
                    movingShooter = false;
                }
                // Otherwise keep moving the motor
                else angleMotor.set(ANGLE_MOTOR_SPEED);
            }
            else { // Move it down
                // If its in position
                if(potentiometer.getValue() >= maxPotValue - potTolerance) {
                    angleMotor.set(0.0);
                    isDown = true;
                    movingShooter = false;
                }
                // Otherwise keep moving the motor
                else angleMotor.set(-ANGLE_MOTOR_SPEED);
            }
        }
        
        
        
        
        
        // OLD
        
        // Updating tilt
        /*if(!tiltRequested) {
            isDown = (potentiometer.getValue() < midPotValue);
        }
        
        System.out.println("isDown: " + isDown);
        
        // Checking for a tilt request
        if(Robot.oi.joystickController1.isButtonDown(tiltButton) || Robot.oi.joystickController2.isButtonDown(tiltButton)) {
             tiltRequested = true;
        }
        
        // Updating the tilt if there was a tilt request
        if(tiltRequested) {
            System.out.println("Tilt requested...");
            if(isDown) {
                // Tilting up
                if(potentiometer.getValue() <= maxPotValue * potTolerance) angleMotor.set(ANGLE_MOTOR_SPEED);
                else {
                    System.out.println("done tilting up");
                    angleMotor.set(0.0);
                    tiltRequested = false;
                }
            }
            else {
                // Tilting down
                if(potentiometer.getValue() >= minPotValue * 1.25) angleMotor.set(-ANGLE_MOTOR_SPEED);
                else {
                    System.out.println("done tilting down");
                    angleMotor.set(0.0);
                    tiltRequested = false;
                }
            }
        }
        */
    }
    
    private void shootAndRelock() {
        // Waiting until the lock is released (fired)
        if(!motorOffSwitch) {
            if(shootSwitch.get()) shootMotor.set(1.0);
            else motorOffSwitch = true;
        }
        // Now relocking the winch
        else if(shootSwitch.get()) {
            shootMotor.set(0.0);
            shootRequested = false;
        }
    }
    
    public void prepToFire() {
        // So far the loading is the same as the calibrating,
        // so we just call calibrate().... this might be
        // changed later
        RobotMap.winchJags.calibrate();
    }
    
    public void calibrate() {
        RobotMap.winchJags.calibrate();
    }
    public boolean isCalibrated() {
        return isCalibrated;
    }
    
    public void setCalibrated(boolean value) {
       isCalibrated = value;
    }
    
    public void setReadyToFire(boolean value) {
       readyToFire = value;
    }
    
    public boolean isReadyToFire() {
        return readyToFire;
    }
}
