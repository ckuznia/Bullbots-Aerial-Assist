package org.bullbots.core;

import edu.wpi.first.wpilibj.can.CANTimeoutException;
import org.usfirst.frc1891.AerialAssist.Robot;
import org.usfirst.frc1891.AerialAssist.RobotMap;

/**
 *
 * @author Bullbots
 */
public class Winch extends DualJaguar {
    
    private final double 
            SPEED = -1, 
            APPROACH_SPEED = -0.35, 
            UNWIND_SPEED = -SPEED;
    private final float SLOW_POINT = (float) -1.45;
    public static boolean 
            isLocked = false, 
            sleeped = false;
    
    public Winch(int MASTER_ID, int SLAVE_ID, double P, double I, double D) {
        super(MASTER_ID, SLAVE_ID, P, I, D);
    }
    
    public void calibrate() {
        try {
            Robot.shooter.setReadyToFire(false);
            
            // Rounding the position value to 2 decimal places, that 
            // way we are not checking a lot of floating point values
            double roundedPosition = Jaguar.roundValue(MASTER_JAG.getPosition());
            
            // Callibrating the winch...
            if(!RobotMap.shooterloadSwitch.get() && !isLocked) {
                // If it is away from the limit switch
                if(roundedPosition > SLOW_POINT) this.driveUsingVoltage(SPEED);
                // It is approaching the limit switch, slow down
                else this.driveUsingVoltage(APPROACH_SPEED);
            }
            else {
                // Setting locked to true, that way if it gets off the limit switch
                // (Besides shooting) it will not try to lock again
                isLocked = true;
                
                // Stopping everything
                this.stop();
                
                // Putting a delay before the winch is driven the other way to unwind
                if(!sleeped) {
                    Thread.sleep(200);
                    sleeped = true;
                }
                
                // Unwinding the winch, once finished, then shooter is ready to fire
                if(roundedPosition < 0.0) this.driveUsingVoltage(UNWIND_SPEED);
                else {
                    Robot.shooter.setReadyToFire(true);
                    Robot.shooter.setReadyToLoad(false);
                    Robot.shooter.setWaited(false);
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
