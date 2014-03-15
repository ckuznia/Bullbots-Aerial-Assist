package org.bullbots.core;

import edu.wpi.first.wpilibj.can.CANTimeoutException;
import org.usfirst.frc1891.AerialAssist.Robot;
import org.usfirst.frc1891.AerialAssist.RobotMap;

/**
 * @author Clay Kuznia
 */
public class Winch extends DualJaguar {
    
    private final double 
            SPEED = -1,
            UNWIND_SPEED = -SPEED,
            UNWIND_AMOUNT = 2.14, // Positive is more unwinding, negative is less
            PRE_UNWIND_DELAY = 200;
    public static boolean 
            isLocked = false,   // If the winch is in locked position
            sleeped = false,    // If the delay after unwinding was implemented
            isDown = false;     // If the shooter sled is down and locked
    private double startPos;
    
    public Winch(int MASTER_ID, int SLAVE_ID, double P, double I, double D) {
        super(MASTER_ID, SLAVE_ID, P, I, D);
    }
    
    public void calibrate() {
        try {
            // Setting state values
            Robot.shooter.setLoaded(false);
            Robot.shooter.setLoading(true);
            
            /*
            Rounding the position value to 2 decimal places, that 
            way we are not overloading the cRIO by checking a lot 
            of floating point values.
            */
            double roundedPosition = Jaguar.roundValue(MASTER_JAG.getPosition());
            
            // Relocking the winch in order to ensure it is locked
            if(!isLocked && Robot.shooter.fireAndLock()) isLocked = true;
            
            // If the loadswitch has not been hit yet, keep driving the winch down
            if(!RobotMap.shooterloadSwitch.get() && !isDown) this.driveUsingVoltage(SPEED);
            // Load switch was hit
            else {
                // Shooter is now down
                isDown = true;
                
                // Putting a delay before the winch is driven the other way to unwind
                if(!sleeped) {
                    // Stopping everything
                    this.stop();
                    
                    // Then waiting...
                    Thread.sleep((int) PRE_UNWIND_DELAY);
                    sleeped = true;
                    
                    // Resetting the position of the encoder, that way we count up
                    // until we reach a certian point, and then stop undwinding
                    startPos = Jaguar.roundValue(MASTER_JAG.getPosition());
                }
                
                /*
                Unwinding the winch, once finished, then shooter is ready to fire
                */
                if(roundedPosition < startPos + UNWIND_AMOUNT) this.driveUsingVoltage(UNWIND_SPEED);
                // Robot is fully loaded and ready to fire
                else {
                    // Stopping everything
                    this.stop();
                    
                    // Setting state values
                    isDown = isLocked = sleeped = false;
                    Robot.shooter.setLoading(false);
                    Robot.shooter.setLoaded(true);
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
