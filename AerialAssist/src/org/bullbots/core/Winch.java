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
            UNWIND_SPEED = -SPEED,
            UNWIND_OFFSET = 0.01, // Robot unwinds more than it winds down
            PRE_UNWIND_DELAY = 200,
            SLOW_POINT = -1.4; // -1.45
    public static boolean 
            isLocked = false,   // If the winch is in locked position
            sleeped = false,    // If the delay after unwinding was implemented
            isDown = false;     // If the shooter sled is down and locked
    
    public Winch(int MASTER_ID, int SLAVE_ID, double P, double I, double D) {
        super(MASTER_ID, SLAVE_ID, P, I, D);
    }
    
    public void calibrate() {
        try {
            System.out.println("\tcalibrate()");
            Robot.shooter.setLoaded(false);
            Robot.shooter.setLoading(true);
            
            // Rounding the position value to 2 decimal places, that 
            // way we are not checking a lot of floating point values
            double roundedPosition = Jaguar.roundValue(MASTER_JAG.getPosition());
            
            // Relocking the winch in order to make sure it is locked
            if(!isLocked && Robot.shooter.fireAndLock()) {
                System.out.println("\t\tWinch is in locked position");
                isLocked = true;
            }
            
            // Callibrating the winch...
            if(!RobotMap.shooterloadSwitch.get() && !isDown) {
                // If it is away from the limit switch
                //if(roundedPosition > SLOW_POINT) {
                    System.out.println("\t\tPulling shooter down... FAST");
                    this.driveUsingVoltage(SPEED);
                //}
                /*// It is approaching the limit switch, slow down
                else {
                    System.out.println("\t\tPulling shooter down... SLOW");
                    this.driveUsingVoltage(APPROACH_SPEED);
                }*/
            }
            else {
                // Load switch was hit, so shooter is down
                isDown = true;
                
                // Putting a delay before the winch is driven the other way to unwind
                if(!sleeped) {
                    // Stopping everything
                    this.stop();
                    System.out.println("\t\tBefore sleeping...");
                    
                    // Then waiting...
                    Thread.sleep((int) PRE_UNWIND_DELAY);
                    sleeped = true;
                    System.out.println("\t\tAFTER sleeping...");
                }
                
                // Unwinding the winch, once finished, then shooter is ready to fire
                // (The 0.0 is redundant, but it makes it more clear that we want
                // the shooter to be back at the 0 position with a minor tolerance,
                // that tolerance being UNWIND_OFFSET.
                if(roundedPosition < 0.0 + UNWIND_OFFSET) {
                    System.out.println("\t\tUnwinding cable...");
                    this.driveUsingVoltage(UNWIND_SPEED);
                }
                else {
                    // Stopping everything
                    this.stop();
                    Robot.shooter.setLoaded(true);
                    Robot.shooter.setLoading(false);
                    isDown = isLocked = sleeped = false;
                    System.out.println("\t\tFinished calibrate();");
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
