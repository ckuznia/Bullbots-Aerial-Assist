/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2008. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package org.bullbots;

import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.networktables.NetworkTable;

import org.bullbots.component.DriveTrain;
import org.bullbots.controller.JoystickController;
import org.bullbots.util.UserDebug;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class Robot extends IterativeRobot {
    
    private DriveTrain driveTrain;
    private JoystickController joystick, joystick2;
    public static NetworkTable table = NetworkTable.getTable("balltable");
    
    /**
     * This function is run when the robot is first started up and should be
     * used for any initialization code.
     */
    public void robotInit() {
        try {
            
        }
        catch(Exception e) {
            e.printStackTrace();
            UserDebug.print("Error initializing robot.");
        }
    }

    /**
     * This function is called periodically during autonomous
     */
    public void autonomousPeriodic() {}

    /**
     * This function is called periodically during operator control
     */
    public void teleopPeriodic() {
        int trackButton = 1;
        if(joystick.isButtonDown(trackButton) || joystick2.isButtonDown(trackButton)) driveTrain.trackBall();
        else driveTrain.driveUsingVoltage(joystick.getYAxis(), joystick2.getYAxis());
    }
}
