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
            APPROACH_SPEED = -0.66, 
            UNWIND_SPEED = -SPEED;
    private final float SLOW_POINT = (float) -1.45;
    public static boolean 
            isLocked = false, 
            sleeped = false,
            isDown = false;
    private double unwindOffset = -0.075;
    
    public Winch(int MASTER_ID, int SLAVE_ID, double P, double I, double D) {
        super(MASTER_ID, SLAVE_ID, P, I, D);
    }
    
    public void calibrate() {
        try {
            Robot.shooter.setLoaded(false);
            Robot.shooter.setLoading(true);
            
            // Rounding the position value to 2 decimal places, that 
            // way we are not checking a lot of floating point values
            double roundedPosition = Jaguar.roundValue(MASTER_JAG.getPosition());
            
            // Relocking the winch in order to make sure it is locked
            if(!isLocked && Robot.shooter.fireAndLock()) isLocked = true;
            
            // Callibrating the winch...
            if(!RobotMap.shooterloadSwitch.get() && !isDown) {
                // If it is away from the limit switch
                System.out.println("roundedPos: " + roundedPosition + " slow point: " + SLOW_POINT);
                if(roundedPosition > SLOW_POINT) this.driveUsingVoltage(SPEED);
                // It is approaching the limit switch, slow down
                else this.driveUsingVoltage(APPROACH_SPEED);
            }
            else {
                // Stopping everything
                this.stop();
                
                // Load switch was hit, so shooter is down
                isDown = true;
                
                // Putting a delay before the winch is driven the other way to unwind
                if(!sleeped) {
                   
                    Thread.sleep(200);
                    sleeped = true;
                }
                
                // Unwinding the winch, once finished, then shooter is ready to fire
                if(roundedPosition < unwindOffset) this.driveUsingVoltage(UNWIND_SPEED);
                else {
                    Robot.shooter.setLoaded(true);
                    Robot.shooter.setLoading(false);
                    isDown = isLocked = sleeped = false;
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
