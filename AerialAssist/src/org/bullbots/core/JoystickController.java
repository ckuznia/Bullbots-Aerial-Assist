package org.bullbots.core;

import edu.wpi.first.wpilibj.Joystick;

/**
 * @author Clay Kuznia
 */
public class JoystickController {
    
    private final Joystick JOYSTICK;
    
    private final double DEADBAND = 0.065;
    
    public JoystickController(Joystick joystick) {
	this.JOYSTICK = joystick;
    }
    
    public double getXAxis(){
	double value = JOYSTICK.getRawAxis(1);
        
        // Only returning the actual value if the value
        // is outside the deadband
	return (Math.abs(value) > DEADBAND ? value : 0);
    }
    
    public double getYAxis(){
	double value = JOYSTICK.getRawAxis(2);
        
        // Only returning the actual value if the value
        // is outside the deadband
	return (Math.abs(value) > DEADBAND ? value : 0);
    }
    
    public boolean isButtonDown(int button){
	return JOYSTICK.getRawButton(button);
    }
}