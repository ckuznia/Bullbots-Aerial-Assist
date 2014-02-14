package org.bullbots.core;

import edu.wpi.first.wpilibj.Joystick;

/**
 * @author Clay Kuznia
 */
public class JoystickController {
    
    private final Joystick JOYSTICK;
    
    private final double DEADBAND = 0.05;
    
    public JoystickController(Joystick joystick) {
	this.JOYSTICK = joystick;
    }
    
    public double getXAxis(){
	double value = JOYSTICK.getRawAxis(1);
	if(Math.abs(value) > DEADBAND) return value;
	return 0;
    }
    
    public double getYAxis(){
	double value = JOYSTICK.getRawAxis(2);
	if(Math.abs(value) > DEADBAND) return value;
	return 0;
    }
    
    public boolean isButtonDown(int button){
	return JOYSTICK.getRawButton(button);
    }
}