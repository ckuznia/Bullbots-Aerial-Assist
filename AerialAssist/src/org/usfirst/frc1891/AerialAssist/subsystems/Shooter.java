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
import edu.wpi.first.wpilibj.command.Subsystem;
import org.usfirst.frc1891.AerialAssist.Robot;
/**
 *
 */
public class Shooter extends Subsystem {
    // BEGIN AUTOGENERATED CODE, SOURCE=ROBOTBUILDER ID=DECLARATIONS
    AnalogChannel potentiometer = RobotMap.shooterpotentiometer;
    SpeedController angleMotor = RobotMap.shooterangleMotor;
    PIDController anglePIDSystem = RobotMap.shooterAnglePIDSystem;
    DigitalInput loadSwitch = RobotMap.shooterloadSwitch;
    SpeedController shootMotor = RobotMap.shootershootMotor;
    AnalogChannel iRSensor1 = RobotMap.shooterIRSensor1;
    DigitalInput shootSwitch = RobotMap.shootershootSwitch;
    AnalogChannel iRSensor2 = RobotMap.shooterIRSensor2;
    // END AUTOGENERATED CODE, SOURCE=ROBOTBUILDER ID=DECLARATIONS
    
    /*
    Shooter:
    1: winch needs to be ready to lock
    2: winch needs to be pulled down until it hits the loadServo (now its locked)
    3: winch needs to be unwound to release rope slack
    4: winch lock needs to be released to fire, as you do this, it should be "re-cocked"
    5: winch can now be pulled back down (start back at #2 is in "ready to lock" position)
    */
    
    private final int shootButton = 1;
    
    private boolean lockingWinch = false;
    
    public Shooter() {
        System.out.println("constructor()");
        
    }
    
    // Put methods for controlling this subsystem
    // here. Call these from Commands.
    public void initDefaultCommand() {
        // BEGIN AUTOGENERATED CODE, SOURCE=ROBOTBUILDER ID=DEFAULT_COMMAND
    // END AUTOGENERATED CODE, SOURCE=ROBOTBUILDER ID=DEFAULT_COMMAND        
    }
    
    public void update() {
        if(lockingWinch) runShootMotor();
        
        if(!loadSwitch.get()) runWinch(5);
        
        if(Robot.oi.joystickController1.isButtonDown(shootButton) ||
                Robot.oi.joystickController2.isButtonDown(shootButton)) {
            lockingWinch = false;
            shoot();
        }
        else lockingWinch = false;
    }
    
    public void runWinch(double RPM) {
        RobotMap.winchJags.driveUsingSpeed(RPM);
    }
    
    public void shoot() {
        // Make sure the robot is ready to shoot
        if(loadSwitch.get()) runShootMotor();
    }
    
    public void runShootMotor() {
        shootMotor.set(0.1);
    }
    
    public void calibrate() {
        RobotMap.winchJags.calibrate();
    }
}
