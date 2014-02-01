package org.bullbots.component;

import edu.wpi.first.wpilibj.PIDController;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.bullbots.PID.PIDHandler;
import org.bullbots.Robot;
import org.bullbots.component.core.DualJaguar;
import org.bullbots.component.core.Jaguar;

/**
 * @author Clay Kuznia
 */
public class DriveTrain {
    
    // Normal Driving PID
    private final double P = 0.0;
    private final double I = 0.0;
    private final double D = 0.0;
    
    // Tracking PID (X Axis)
    private final double P_TRACKING = 0.0;
    private final double I_TRACKING = 0.0;
    private final double D_TRACKING = 0.0;
    private final double OPTIMAL_TRACKING_OFFSET = 0.0;
    
    // Depth PID (Z Axis)
    private final double P_DEPTH = 0.0;
    private final double I_DEPTH = 0.0;
    private final double D_DEPTH = 0.0;
    private final double OPTIMAL_DEPTH_OFFSET = 100.0;
    
    // PID Controller Objects
    private final PIDHandler PIDHandler = new PIDHandler(this);
    private final PIDController trackingPIDController;
    private final PIDController depthPIDController;
    
    private final DualJaguar LEFT_DUAL_JAG;
    private final DualJaguar RIGHT_DUAL_JAG;
    
    // For Testing one Jaguar only
    private final Jaguar TEST_JAG;
    private final boolean testingJag = false;
    
    private double trackingOffset, depthOffset;
    
    public DriveTrain(double p, double i, double d) {
        // Setting up PID stuff
        trackingPIDController = new PIDController(P_TRACKING, I_TRACKING, D_TRACKING, PIDHandler.getTrackingSource(), PIDHandler.getTrackingOutput());
        trackingPIDController.setSetpoint(OPTIMAL_TRACKING_OFFSET); // The value that we want it to be
        trackingPIDController.setContinuous(true);
        trackingPIDController.setOutputRange(-0.5, 0.5); // PLAY WITH VALUES
        trackingPIDController.setAbsoluteTolerance(0); // PLAY WITH VALUES
        trackingPIDController.enable();

        depthPIDController = new PIDController(P_DEPTH, I_DEPTH, D_DEPTH, PIDHandler.getDepthSource(), PIDHandler.getDepthOutput());
        depthPIDController.setSetpoint(OPTIMAL_DEPTH_OFFSET);
        depthPIDController.setContinuous(true);
        depthPIDController.setOutputRange(-0.7, 0.7); // PLAY WITH VALUES
        depthPIDController.setAbsoluteTolerance(8); // PLAY WITH VALUES
        depthPIDController.enable();
        
        if(!testingJag) {
            LEFT_DUAL_JAG = new DualJaguar(4, 6, p, i, d);
            RIGHT_DUAL_JAG = new DualJaguar(7, 3, p, i, d);
            TEST_JAG = null;
        }
        else {
            LEFT_DUAL_JAG = RIGHT_DUAL_JAG = null;
            TEST_JAG = new Jaguar(000, p, i, d);
        }
    }
    
    public void trackBall() {
        // Tracking the ball horizontally
        double xOffset = SmartDashboard.getNumber("xdistance");
        trackingOffset = xOffset;
        
        // Don't need negative values since the motors are facing different directions, so it turns the robot for us.
        driveUsingVoltage(trackingOffset, trackingOffset);
        
        
        
        // Tracking ball vertically
        /*double zOffset = table.getNumber("radius"); // THIS IS A RATIO : NEED TO CALCULATE
        driveTrain.setDepthOffset(zOffset);

        driveTrain.driveUsingVoltage(depthOffset, -depthOffset);*/
        
        
        // ==================================================
        // This part below is only used to configure the PIDs
        
        // Getting the numbers from the SmartDashboard, and re-setting the PIDS using those numbers
        trackingPIDController.setPID(SmartDashboard.getNumber("P_TRACKING"), SmartDashboard.getNumber("I_TRACKING"), SmartDashboard.getNumber("D_TRACKING"));
    }
    
    public void driveTestJag(double voltage) {
        TEST_JAG.driveUsingVoltage(voltage);
    }
    
    public void driveUsingVoltage(double leftVoltage, double rightVoltage) {
        LEFT_DUAL_JAG.driveUsingVoltage(leftVoltage);
        RIGHT_DUAL_JAG.driveUsingVoltage(rightVoltage);
    }
    
    public void driveUsingSpeed(double leftRPM, double rightRPM) {
        // MAY NEED A SPEED FACTOR (100)
        LEFT_DUAL_JAG.driveUsingVoltage(leftRPM);
        RIGHT_DUAL_JAG.driveUsingVoltage(rightRPM);
    }
    
    public void driveUsingPosition(double leftRotations, double rightRotations) {
        LEFT_DUAL_JAG.driveUsingVoltage(leftRotations);
        RIGHT_DUAL_JAG.driveUsingVoltage(rightRotations);
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
}
