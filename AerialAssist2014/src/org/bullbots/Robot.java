/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2008. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package org.bullbots;

import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.PIDController;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

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
    public static JoystickController joystick, joystick2;
    private NetworkTable table;
    public static TableListener tableListener;
    
    /**
     * This function is run when the robot is first started up and should be
     * used for any initialization code.
     */
    public void robotInit() {
        System.out.println("\n>> Robot Initialization Has Begun...");
        try {
            table =  NetworkTable.getTable("balltable");
            tableListener = new TableListener();
            table.addTableListener(tableListener);
            driveTrain = new DriveTrain(table);
            joystick = new JoystickController(1);
            joystick2 = new JoystickController(2);
        }
        catch(Exception e) {
            e.printStackTrace();
            UserDebug.print("Error initializing robot.");
        }
        System.out.println("\n>> Robot Initialization Has Finished...");
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
        else {
            // Joystick 1 updates
            PIDController joy1PIDController = driveTrain.getJoystick1PIDController();
            joy1PIDController.setSetpoint(joystick.getYAxis());
            joy1PIDController.setPID(SmartDashboard.getNumber("P_JOY1"), SmartDashboard.getNumber("I_JOY1"), SmartDashboard.getNumber("D_JOY1"));
            
            // Joystick 2 updates
            PIDController joy2PIDController = driveTrain.getJoystick2PIDController();
            joy2PIDController.setSetpoint(joystick2.getYAxis());
            joy2PIDController.setPID(SmartDashboard.getNumber("P_JOY2"), SmartDashboard.getNumber("I_JOY2"), SmartDashboard.getNumber("D_JOY2"));
            
            driveTrain.driveUsingSpeed(joy1PIDController.get(), joy2PIDController.get());
            //driveTrain.driveUsingVoltage(-joystick.getYAxis(), joystick2.getYAxis());
        }
    }
}
