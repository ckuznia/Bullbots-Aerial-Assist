package org.bullbots.PID;

import edu.wpi.first.wpilibj.PIDOutput;
import edu.wpi.first.wpilibj.PIDSource;
import org.bullbots.component.DriveTrain;

/**
 * @author Clay Kuznia
 */
public class PIDHandler {
    
    private final DriveTrain driveTrain;
    
    public PIDHandler(DriveTrain driveTrain) {
        this.driveTrain = driveTrain;
    }
    
    
    //===================================================================
    //===================================================================
    //===================================================================
    
    
    // Source (Input) for tracking PID
    public class TrackingPIDSource implements PIDSource {
        
        public double pidGet() {
            return driveTrain.getTrackingOffset();
        }
    }
    
    // Output for tracking PID
    public class TrackingPIDOutput implements PIDOutput {
        
        public void pidWrite(double value) {
            driveTrain.setTrackingOffset(value);
        }
    }
    
    
    //===================================================================
    //===================================================================
    //===================================================================
    
    
    // Source (Input) for depth PID
    public class DepthPIDSource implements PIDSource {
        
        public double pidGet() {
            return driveTrain.getDepthOffset();
        }
    }
    
    // Output for depth PID
    public class DepthPIDOutput implements PIDOutput {
        
        public void pidWrite(double value) {
            driveTrain.setDepthOffset(value);
        }
    }
    
    
    //===================================================================
    //===================================================================
    //===================================================================
    
    
    public PIDSource getTrackingSource() {
        return new TrackingPIDSource();
    }
    
    public PIDOutput getTrackingOutput() {
        return new TrackingPIDOutput();
    }
    
    public PIDSource getDepthSource() {
        return new DepthPIDSource();
    }
    
    public PIDOutput getDepthOutput() {
        return new DepthPIDOutput();
    }
}
