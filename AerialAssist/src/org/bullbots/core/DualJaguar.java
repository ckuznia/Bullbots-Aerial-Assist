package org.bullbots.core;

import edu.wpi.first.wpilibj.CANJaguar;
import edu.wpi.first.wpilibj.can.CANTimeoutException;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import edu.wpi.first.wpilibj.livewindow.LiveWindowSendable;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.tables.ITable;
import edu.wpi.first.wpilibj.tables.ITableListener;

/**
 * @author Clay Kuznia
 */
public class DualJaguar implements LiveWindowSendable {
    
    private Jaguar MASTER_JAG, SLAVE_JAG;
    private ITable table; // Table for configuring PIDs
    
    public double setPoint = 0.0;
    
    public DualJaguar(int MASTER_ID, int SLAVE_ID, double P, double I, double D) {
        try {
            MASTER_JAG = new Jaguar(MASTER_ID, P, I, D);
            SLAVE_JAG = new Jaguar(SLAVE_ID);
        } catch (CANTimeoutException ex) {
            ex.printStackTrace();
        }
    }
    
    public void driveUsingVoltage(double votage) {
	MASTER_JAG.driveUsingVoltage(votage);
	SLAVE_JAG.driveUsingVoltage(votage);
    }
    
    public void driveUsingSpeed(double value) {
	try {
            MASTER_JAG.setControlMode(CANJaguar.ControlMode.kSpeed);
            MASTER_JAG.getSpeedReference();
            
            System.out.println("Set value: " + value + "\tActual value: " + MASTER_JAG.getX());
        
            // Updating the speed of the master jag
            if(MASTER_JAG.getX() != value) {
                MASTER_JAG.driveUsingSpeed(value);
                System.out.println("Master jag was updated.");
            }

            // Setting them in current mode in order to get the current
            SLAVE_JAG.setControlMode(CANJaguar.ControlMode.kCurrent);

            // If the slave current does not match the master, then update the slave's current to the master's
            if(SLAVE_JAG.getOutputCurrent() != MASTER_JAG.getOutputCurrent()) {
                SLAVE_JAG.setX(MASTER_JAG.getOutputCurrent());

                System.out.println("Slave jag was updated.");
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        
    }
    
    public void driveUsingPosition(double rotations) {
	MASTER_JAG.driveUsingPosition(rotations);
	SLAVE_JAG.driveUsingPosition(rotations);
    }
    
    public void stop() {
        MASTER_JAG.stop();
        SLAVE_JAG.stop();
    }
    
    public double getSpeed() {
        try {
            return MASTER_JAG.getSpeed();
        } catch (CANTimeoutException ex) {
            ex.printStackTrace();
        }
        return 0.0;
    }

    public String getSmartDashboardType(){
        return "PIDController";
    }

    private ITableListener listener = new ITableListener() {
        public void valueChanged(ITable table, String key, Object value, boolean isNew) {
            System.out.println("Key: " + key+"\tValue: "+value);
            
            
            try {
                MASTER_JAG.setPID(table.getNumber("p"), table.getNumber("i"), table.getNumber("d"));
                setPoint = table.getNumber("setpoint");
                if (table.getBoolean("enabled")) MASTER_JAG.disableControl();
                else MASTER_JAG.enableControl();
                
                /*if (key.equals("p") || key.equals("i") || key.equals("d")) {
                    if (MASTER_JAG.getP() != table.getNumber("p", 0.0) || MASTER_JAG.getI() != table.getNumber("i", 0.0) || 
                            MASTER_JAG.getD() != table.getNumber("d", 0.0)) {
                        System.out.println("PIDS were updated.");
                        MASTER_JAG.setPID(table.getNumber("p", 0.0), table.getNumber("i", 0.0), table.getNumber("d", 0.0));
                    } else if(key.equals("Setpoint")) {
                        System.out.println("Setpoint updated.");
                        driveUsingSpeed(table.getNumber("Setpoint"));
                    } else if (key.equals("enabled")) {
                        if (MASTER_JAG.isAlive() != table.getBoolean("Enabled")) {
                            System.out.println("Jag control was updated");
                            
                        }
                    } else {
                        System.out.println("KEY: " + key + "\tVALUE: " + value);
                    }
                }*/
            }
            catch(Exception e ) {
                e.printStackTrace();
            }
                
        }
    };
    
   
    public void initTable(ITable table) {
        System.out.println("initTable() called");
        if(this.table!=null)
            this.table.removeTableListener(listener);
        this.table = table;
        if(table!=null) {
            try {
                table.putNumber("p", MASTER_JAG.getP());
                table.putNumber("i", MASTER_JAG.getI());
                table.putNumber("d", MASTER_JAG.getD());
                table.putNumber("setpoint", MASTER_JAG.getX());
                table.putBoolean("enabled", MASTER_JAG.isAlive());
                table.addTableListener(listener, false);
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public ITable getTable(){
        return table;
    }

    /**
     * {@inheritDoc}
     */
    public void updateTable() {}

    /**
     * {@inheritDoc}
     */
    public void startLiveWindowMode() {
        
        try {
            // Disabling the jags for safety
            MASTER_JAG.disableControl();
        } catch (CANTimeoutException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void stopLiveWindowMode() {}
}