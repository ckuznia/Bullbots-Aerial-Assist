package org.bullbots.core;

import edu.wpi.first.wpilibj.CANJaguar;
import edu.wpi.first.wpilibj.can.CANTimeoutException;
import org.usfirst.frc1891.AerialAssist.RobotMap;

/**
 *
 * @author Bullbots
 */
public class Winch extends DualJaguar {
    
    private final double SPEED = -60, APPROACH_SPEED = -15;
    private final float SLOW_POINT = (float) -3.82;
    
    public Winch(int MASTER_ID, int SLAVE_ID, double P, double I, double D) {
        super(MASTER_ID, SLAVE_ID, P, I, D);
    }
    
    public void calibrate() {
        try {
            // Callibrating the winch...
            
            MASTER_JAG.setControlMode(CANJaguar.ControlMode.kPosition);
            MASTER_JAG.enableControl(0.0);
            
            // Running the winch at high speed until it is approaching the limit switch
            do {
                System.out.println("driving fast");
                MASTER_JAG.driveUsingSpeed(SPEED);
            }
            // Making sure the limit switch is not pressed as a safety feature
            while(MASTER_JAG.getPosition() > SLOW_POINT && !RobotMap.shooterloadSwitch.get());
            
            // Now running the winch at low speed until the switch is triggered
            do {
                System.out.println("driving slow");
                MASTER_JAG.driveUsingSpeed(APPROACH_SPEED);
            }
            while(!RobotMap.shooterloadSwitch.get());
            
            // Stopping everything
            this.stop();
            System.out.println("DONE");
        }
        catch(CANTimeoutException e) {
            e.printStackTrace();
        }
    }
}
