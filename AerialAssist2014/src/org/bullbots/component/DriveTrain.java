package org.bullbots.component;

import edu.wpi.first.wpilibj.PIDController;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.bullbots.PID.PIDHandler;
import org.bullbots.TableListener;
import org.bullbots.component.core.DualJaguar;

/**
 * @author Clay Kuznia
 */
public class DriveTrain {
    /* Helpful PID Stuff:
        Setpoint -> Value at which you want it to be
        P -> Speed at which to move to the setpoint
        I -> Not always used, for fine tuning
        D -> The amount of damping when arriving at the setpoint
    */
    
    // Normal Driving PIDs
    private final double P_JOY1 = 0.25;
    private final double I_JOY1 = 0.25;
    private final double D_JOY1 = 0.25;
    
    private final double P_JOY2 = 0.25;
    private final double I_JOY2 = 0.25;
    private final double D_JOY2 = 0.25;
    
    private final double JAG_TOLERANCE = 0.01;
    
    // Tracking PID (X Axis)
    private final double P_TRACKING = 0.25;
    private final double I_TRACKING = 0.25;
    private final double D_TRACKING = 0.25;
    private final double OPTIMAL_TRACKING_OFFSET = 0.0;
    
    // Depth PID (Z Axis)
    private final double P_DEPTH = 0.25;
    private final double I_DEPTH = 0.25;
    private final double D_DEPTH = 0.25;
    private final double OPTIMAL_DEPTH_OFFSET = 0.0;
    
    // PID Controller Objects
    private final PIDHandler PIDHandler;
    private final PIDController joystick1PIDController;
    private final PIDController joystick2PIDController;
    private final PIDController trackingPIDController;
    private final PIDController depthPIDController;
    
    private final DualJaguar LEFT_DUAL_JAG;
    private final DualJaguar RIGHT_DUAL_JAG;
    
    private double trackingOffset, depthOffset;
    
    private final NetworkTable table;
    
    public DriveTrain(NetworkTable table) {
        this.table = table;
        
        PIDHandler = new PIDHandler(this);
        
        // Joystick1 PID setup
        joystick1PIDController = new PIDController(P_JOY1, I_JOY1, D_JOY1, PIDHandler.getJoystick1PIDSource(), PIDHandler.getJoystick1PIDOutput());
        joystick1PIDController.setSetpoint(0.0);
        joystick1PIDController.setContinuous(false);
        joystick1PIDController.setInputRange(-1, 1);
        joystick1PIDController.setOutputRange(-1, 1);
        joystick1PIDController.setAbsoluteTolerance(JAG_TOLERANCE);
        joystick1PIDController.enable();
        LiveWindow.addActuator("PIDS", "Joystick 1", joystick1PIDController);
         
        // Joystick2 PID setup
        joystick2PIDController = new PIDController(P_JOY2, I_JOY2, D_JOY2, PIDHandler.getJoystick2PIDSource(), PIDHandler.getJoystick2PIDOutput());
        joystick2PIDController.setSetpoint(0.0);
        joystick2PIDController.setContinuous(false);
        joystick2PIDController.setInputRange(-1, 1);
        joystick2PIDController.setOutputRange(-1, 1);
        joystick2PIDController.setAbsoluteTolerance(JAG_TOLERANCE);
        joystick2PIDController.enable();
        LiveWindow.addActuator("PIDS", "Joystick 2", joystick2PIDController);
        
        // Tracking PID setup
        trackingPIDController = new PIDController(P_TRACKING, I_TRACKING, D_TRACKING, PIDHandler.getTrackingSource(), PIDHandler.getTrackingOutput());
        trackingPIDController.setSetpoint(OPTIMAL_TRACKING_OFFSET);
        trackingPIDController.setContinuous(false);
        trackingPIDController.setInputRange(-320, 320); // 320 = camera width / 2
        trackingPIDController.setOutputRange(-1, 1);
        trackingPIDController.setAbsoluteTolerance(5); // pixel tolerance
        trackingPIDController.enable();
        
        // Depth PID setup
        depthPIDController = new PIDController(P_DEPTH, I_DEPTH, D_DEPTH, PIDHandler.getDepthSource(), PIDHandler.getDepthOutput());
        depthPIDController.setSetpoint(OPTIMAL_DEPTH_OFFSET);
        depthPIDController.setContinuous(false);
        depthPIDController.setInputRange(0, 0);     // NEED TO CONFIGURE
        depthPIDController.setOutputRange(-1, 1);
        depthPIDController.setAbsoluteTolerance(3);     // NEED TO CONFIGURE
        depthPIDController.enable();
        //LiveWindow.addActuator("PIDS", "Depth", depthPIDController);
        
        // Putting the numbers on the dashboard
        SmartDashboard.putNumber("P_JOY1", P_JOY1);
        SmartDashboard.putNumber("I_JOY1", I_JOY1);
        SmartDashboard.putNumber("D_JOY1", D_JOY1);
        
        SmartDashboard.putNumber("P_JOY2", P_JOY2);
        SmartDashboard.putNumber("I_JOY2", I_JOY2);
        SmartDashboard.putNumber("D_JOY2", D_JOY2);
        
        SmartDashboard.putNumber("P_TRACKING", P_TRACKING);
        SmartDashboard.putNumber("I_TRACKING", I_TRACKING);
        SmartDashboard.putNumber("D_TRACKING", D_TRACKING);
        
        SmartDashboard.putNumber("P_DEPTH", P_DEPTH);
        SmartDashboard.putNumber("I_DEPTH", I_DEPTH);
        SmartDashboard.putNumber("D_DEPTH", D_DEPTH);
        
        // Setting up the Jaguars
        LEFT_DUAL_JAG = new DualJaguar(4, 6, P_JOY1, I_JOY1, D_JOY1);
        RIGHT_DUAL_JAG = new DualJaguar(7, 3, P_JOY2, I_JOY2, D_JOY2);
    }
    
    public void trackBall() {
        // Updating values if they need to be updated
        if(TableListener.isTableUpdated) {
            TableListener.isTableUpdated = false;
            
            trackingOffset = table.getNumber("xdistance");
            System.out.println("Raw Table Value: " + table.getNumber("xdistance"));
            depthOffset = table.getNumber("diameter");
        }
        
        // Tracking the ball horizontally
        // Don't need negative values since the motors are facing different directions, so it turns the robot for us.
        trackingOffset = table.getNumber("xdistance");
        double getValue = trackingPIDController.get();
        System.out.println("get() value: " + getValue + "     Raw Table Value: " + trackingOffset + "\n");
        driveUsingVoltage(getValue, getValue);
        
        // For depth
        //driveTrain.driveUsingVoltage(depthOffset, -depthOffset);*/
        
        // ==================================================
        // This part below is only used to configure the PIDs
        
        // Getting the numbers from the SmartDashboard, and re-setting the PIDS using those numbers
        trackingPIDController.setPID(SmartDashboard.getNumber("P_TRACKING"), SmartDashboard.getNumber("I_TRACKING"), SmartDashboard.getNumber("D_TRACKING"));
    }
    
    public void driveUsingVoltage(double leftVoltage, double rightVoltage) {
        LEFT_DUAL_JAG.driveUsingVoltage(leftVoltage);
        RIGHT_DUAL_JAG.driveUsingVoltage(rightVoltage);
    }
    
    public void driveUsingSpeed(double leftRPM, double rightRPM) {
        LEFT_DUAL_JAG.driveUsingSpeed(leftRPM);
        RIGHT_DUAL_JAG.driveUsingSpeed(rightRPM);
    }
    
    public void driveUsingPosition(double leftRotations, double rightRotations) {
        LEFT_DUAL_JAG.driveUsingPosition(leftRotations);
        RIGHT_DUAL_JAG.driveUsingPosition(rightRotations);
    }
    
    public double getTrackingOffset() {
        return trackingOffset;
    }
    
    public void setTrackingOffset(double value) {
        trackingOffset = value;
    }
    
    public double getDepthOffset() {
        return depthOffset;
    }
    
    public void setDepthOffset(double value) {
        depthOffset = value;
    }
    
    public PIDController getJoystick1PIDController() {
        return joystick1PIDController;
    }
    
    public PIDController getJoystick2PIDController() {
        return joystick2PIDController;
    }
    
    public DualJaguar getLeftDualJag() {
        return LEFT_DUAL_JAG;
    }
    
    public DualJaguar getRightDualJag() {
        return RIGHT_DUAL_JAG;
    }
}
