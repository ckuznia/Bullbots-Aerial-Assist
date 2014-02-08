/*----------------------------------------------------------------------------*/
/* Copyright (c) FIRST 2008. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package org.bullbots;

import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;
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
    
    public static JoystickController joystick, joystick2;
    private DriveTrain driveTrain;
    private NetworkTable table;
    public static TableListener tableListener;
    
    /**
     * This function is run when the robot is first started up and should be
     * used for any initialization code.
     */
    public void robotInit() {
        System.out.println("\n\n>>Robot Initialization Has BEGUN...");
        try {
            joystick = new JoystickController(1);
            joystick2 = new JoystickController(2);
            table =  NetworkTable.getTable("balltable");
            tableListener = new TableListener();
            table.addTableListener(tableListener);
            driveTrain = new DriveTrain(table);
        }
        catch(Exception e) {
            e.printStackTrace();
            UserDebug.print("Error initializing robot.");
        }
        System.out.println("\n\n>> Robot Initialization Has FINISHED...\n\n");
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
            /*PIDController joy1PIDController = driveTrain.getJoystick1PIDController();
            joy1PIDController.setSetpoint(joystick.getYAxis());
            System.out.println("Here:"+joy1PIDController.getError());
            joy1PIDController.setPID(SmartDashboard.getNumber("P_JOY1"), SmartDashboard.getNumber("I_JOY1"), SmartDashboard.getNumber("D_JOY1"));
            
            // Joystick 2 updates
            PIDController joy2PIDController = driveTrain.getJoystick2PIDController();
            joy2PIDController.setSetpoint(joystick2.getYAxis());
            joy2PIDController.setPID(SmartDashboard.getNumber("P_JOY2"), SmartDashboard.getNumber("I_JOY2"), SmartDashboard.getNumber("D_JOY2"));
            
            //System.out.println("joy: " + joystick.getYAxis());
            System.out.println("get(): " + joy1PIDController.get() + "\n");
            //driveTrain.driveUsingVoltage(joy1PIDController.get(), joy2PIDController.get());
            */ 
            //System.out.println("joy1: " + joystick.getYAxis());
            //System.out.println("joy2: " + joystick2.getYAxis());
            
            
            
            driveTrain.driveUsingSpeed(-joystick.getYAxis(), joystick2.getYAxis());
        }
    }
    
    public void testInit() {
        System.out.println("\n\n>>Robot TEST Initialization Has BEGUN...");
        try {
            // Forcing the motors to be put into speed mode
            driveTrain.driveUsingSpeed(0.0, 0.0);
        }
        catch(Exception e) {
            e.printStackTrace();
            UserDebug.print("Error TEST initializing robot.");
        }
        System.out.println("\n\n>> Robot Initialization Has FINISHED...\n\n");
    }
    
    public void testPeriodic() {
        LiveWindow.run();
    }
}
