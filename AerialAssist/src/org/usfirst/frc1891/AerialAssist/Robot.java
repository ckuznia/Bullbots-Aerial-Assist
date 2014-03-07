// RobotBuilder Version: 1.0
//
// This file was generated by RobotBuilder. It contains sections of
// code that are automatically generated and assigned by robotbuilder.
// These sections will be updated in the future when you export to
// Java from RobotBuilder. Do not put any code or make any change in
// the blocks indicating autogenerated code or it will be lost on an
// update. Deleting the comments indicating the section will prevent
// it from being updated in the future.
package org.usfirst.frc1891.AerialAssist;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.can.CANTimeoutException;
import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.command.Scheduler;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import org.usfirst.frc1891.AerialAssist.commands.*;
import org.usfirst.frc1891.AerialAssist.subsystems.*;
/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class Robot extends IterativeRobot {
    Command autonomousCommand;
    public static OI oi;
    // BEGIN AUTOGENERATED CODE, SOURCE=ROBOTBUILDER ID=DECLARATIONS
    public static Shooter shooter;
    // END AUTOGENERATED CODE, SOURCE=ROBOTBUILDER ID=DECLARATIONS
    
    private final NetworkTable table = NetworkTable.getTable("visionprocessing");
    
    public static DriveTrain driveTrain;
    
    public static double MAX_RPM = 240.0; // 120 RPMs = ~4 Feet/Second
    
    // Robot Joystick Controls
    public static final int
            SHOOT_BUTTON = 1,
            TILT_BUTTON = 3;
        
    /**
     * This function is run when the robot is first started up and should be
     * used for any initialization code.
     */
    public void robotInit() {
        System.out.println("\n\nROBOT INITIALIZATION HAS BEGUN.");
	RobotMap.init();
        // BEGIN AUTOGENERATED CODE, SOURCE=ROBOTBUILDER ID=CONSTRUCTORS
        shooter = new Shooter(this);
    // END AUTOGENERATED CODE, SOURCE=ROBOTBUILDER ID=CONSTRUCTORS
        driveTrain = new DriveTrain();
        // This MUST be here. If the OI creates Commands (which it very likely
        // will), constructing it during the construction of CommandBase (from
        // which commands extend), subsystems are not guaranteed to be
        // yet. Thus, their requires() statements may grab null pointers. Bad
        // news. Don't move it.
        oi = new OI();
	
        // instantiate the command used for the autonomous period
        // BEGIN AUTOGENERATED CODE, SOURCE=ROBOTBUILDER ID=AUTONOMOUS
        autonomousCommand = new AutonomousCommand(this);
    // END AUTOGENERATED CODE, SOURCE=ROBOTBUILDER ID=AUTONOMOUS
        printSettings();
        System.out.println("\n\nROBOT INITIALIZATION HAS FINISHED.\n\n");
    }
    
    private void printSettings() {
        RobotMap.driveJags1.getMasterJag().printSettings();
        RobotMap.driveJags1.getSlaveJag().printSettings();
        RobotMap.driveJags2.getMasterJag().printSettings();
        RobotMap.driveJags2.getSlaveJag().printSettings();
        RobotMap.winchJags.getMasterJag().printSettings();
        RobotMap.winchJags.getSlaveJag().printSettings();
    }
    
    public void autonomousInit() {
        // schedule the autonomous command (example)
        if (autonomousCommand != null) autonomousCommand.start();
    }
    
    /**
     * This function is called periodically during autonomous
     */
    public void autonomousPeriodic() {
        Scheduler.getInstance().run();
    }
    
    public void teleopInit() {
	// This makes sure that the autonomous stops running when
        // teleop starts running. If you want the autonomous to 
        // continue until interrupted by another command, remove
        // this line or comment it out.
        if (autonomousCommand != null) autonomousCommand.cancel();
        
        // Setting the mode on the table
        table.putString("robotMode", "TELEOP");
        try {
            // Resetting position values for testing
            RobotMap.driveJags1.getMasterJag().enableControl(0.0);
            RobotMap.driveJags2.getMasterJag().enableControl(0.0);
        } catch (CANTimeoutException ex) {
            ex.printStackTrace();
        }
        
        
    }
        
    /**
     * This function is called periodically during operator control
     */
    public void teleopPeriodic() {
        Scheduler.getInstance().run();
        
        // Updating state of drive train
        driveTrain.driveUsingSpeed(Robot.oi.joystickController1.getYAxis() * MAX_RPM, 
                -Robot.oi.joystickController2.getYAxis() * MAX_RPM);
        try {
            //System.out.println("Position of DualJaguar #1:\t" + RobotMap.driveJags1.getMasterJag().getPosition());
            System.out.println("\nPosition of DualJaguar #2:" + RobotMap.driveJags2.getMasterJag().getPosition());
            // Updating the state of the shooter
            //shooter.update(); // remember to use BOTH joysticks to shoot
        } catch (CANTimeoutException ex) {
            ex.printStackTrace();
        }
        
        // Flipping robot driving direction
        if(Robot.oi.joystickController1.isButtonDown(4) || Robot.oi.joystickController2.isButtonDown(4)) {
            MAX_RPM = Math.abs(MAX_RPM);
            System.out.println("a");
        }
        else if(Robot.oi.joystickController1.isButtonDown(5) || Robot.oi.joystickController2.isButtonDown(5)) {
            if(MAX_RPM > 0) MAX_RPM *= -1;
            System.out.println("b");
        }
    }
    
    /**
     * This function called periodically during test mode
     */
    public void testPeriodic() {
        LiveWindow.run();
        
        RobotMap.winchJags.showPosition();
        
        // Continually driving the motors to the setpoint configured in the LiveWindow
        //driveTrain.driveUsingSpeed(RobotMap.driveJags1.setPoint, RobotMap.driveJags2.setPoint);
    }
    
    public void testInit() {
         LiveWindow.setEnabled(true);
    }
    
    public NetworkTable getTable() {
        return table;
    }
}
