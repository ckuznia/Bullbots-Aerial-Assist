package org.bullbots.core;

import edu.wpi.first.wpilibj.can.CANTimeoutException;
import org.usfirst.frc1891.AerialAssist.Robot;
import org.usfirst.frc1891.AerialAssist.RobotMap;

/**
 *
 * @author Bullbots
 */
public class Winch extends DualJaguar {
    
    private final double SPEED = -0.5, APPROACH_SPEED = -0.35, UNWIND_SPEED = 0.5;
    private final float SLOW_POINT = (float) -2.0; // -2.6
    
    public static boolean isLocked = false, sleeped = false;
    
    public Winch(int MASTER_ID, int SLAVE_ID, double P, double I, double D) {
        super(MASTER_ID, SLAVE_ID, P, I, D);
    }
    
    public void calibrate() {
        try {
            Robot.shooter.setReadyToFire(false);
            
            // Callibrating the winch...
            if(!RobotMap.shooterloadSwitch.get() && !isLocked) {
                // If it is away from the limit switch
                if(MASTER_JAG.getPosition() > SLOW_POINT) {
                    System.out.println("driving fast:"+MASTER_JAG.getPosition());
                    this.driveUsingVoltage(SPEED);
                }
                else { // It is close to the limit switch
                    System.out.println("driving slow:"+MASTER_JAG.getPosition());
                    this.driveUsingVoltage(APPROACH_SPEED);
                }
            }
            else {
                // Setting locked to true, that way if it gets off the limit switch
                // (Besides shooting) it will not try to lock again
                isLocked = true;
                
                // Stopping everything
                this.stop();
                System.out.println("LOCKED IN PLACE!!!");
                
                // Putting a delay before the winch is driven the other way
                if(!sleeped) {
                    Thread.sleep(200);
                    sleeped = true;
                }
                
                // Unwinding the winch to put slack in the line
                if(MASTER_JAG.getPosition() < 0.0) this.driveUsingVoltage(UNWIND_SPEED);
                else {
                    Robot.shooter.setReadyToFire(true);
                    System.out.println("Ready to fire...");
                }
            }
        }
        catch(CANTimeoutException e) {
            e.printStackTrace();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
    
    public void showPosition() {
        try {
            System.out.println("Position = " + MASTER_JAG.getPosition());
        } catch (CANTimeoutException ex) {
            ex.printStackTrace();
        }
    }
}
