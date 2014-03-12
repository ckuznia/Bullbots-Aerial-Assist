package org.bullbots.core;

import edu.wpi.first.wpilibj.can.CANTimeoutException;
import edu.wpi.first.wpilibj.livewindow.LiveWindowSendable;
import edu.wpi.first.wpilibj.tables.ITable;
import edu.wpi.first.wpilibj.tables.ITableListener;
import edu.wpi.first.wpilibj.tables.TableKeyNotDefinedException;

/**
 * @author Clay Kuznia
 */
public class DualJaguar implements LiveWindowSendable {
    
    protected Jaguar MASTER_JAG, SLAVE_JAG;
    // Table for configuring PIDs
    protected ITable table;
    
    // For tunning PIDs
    public double setPoint = 0.0;
    
    public DualJaguar(int MASTER_ID, int SLAVE_ID, double P, double I, double D) {
        try {
            MASTER_JAG = new Jaguar(MASTER_ID, P, I, D);
            SLAVE_JAG = new Jaguar(SLAVE_ID);
        } catch (CANTimeoutException ex) {
            ex.printStackTrace();
        }
    }
    
    public void driveUsingVoltage(double voltage) {
	MASTER_JAG.driveUsingVoltage(voltage);
	SLAVE_JAG.driveUsingVoltage(voltage);
    }
    
    public void driveUsingSpeed(double RPM) {
	try {
            // Rounding values to 2 decimal places, in order not to overload the cRIO
            double roundedSetValue = Jaguar.roundValue(RPM);
            double roundedGetValue = Jaguar.roundValue(MASTER_JAG.getSpeed());            
            
            // Updating the speed of the master jag, if needed
            if(roundedGetValue != roundedSetValue) MASTER_JAG.driveUsingSpeed(roundedSetValue);
            
            // Updating the speed of the slave jaguar
            matchSlaveWithMaster();
        }
        catch(CANTimeoutException e) {
            e.printStackTrace();
        }
    }
    
    public void driveUsingPosition(double rotations) {
        // This method is incomplete, need to look up how position mode
        // acutally works and then go from there...
        
        MASTER_JAG.driveUsingPosition(rotations);
        
        /*
        The slave motor is just dragged along with
        the master, so in order to move you must
        overcome more friction than usual.
        */
        
    }
    
    public void driveUsingCurrent(double current) {
        try {
            // Rounding values to 2 decimal places, in order not to overload the cRIO
            double roundedSetValue = Jaguar.roundValue(current);
            double roundedGetValue = Jaguar.roundValue(MASTER_JAG.getOutputCurrent());
            
            // Updating the speed of the master jaguar
            if(roundedGetValue != roundedSetValue) MASTER_JAG.driveUsingCurrent(roundedSetValue);
            
            // Updating the speed of the slave jaguar
            matchSlaveWithMaster();
        }
        catch(CANTimeoutException e) {
            e.printStackTrace();
        }
    }
    
    private void matchSlaveWithMaster() {
        try {
            // Rounding values to 2 decimal places, in order not to overload the cRIO
            double roundedSlaveVoltage = Jaguar.roundValue(SLAVE_JAG.getOutputVoltage());
            double roundedMasterVoltage = Jaguar.roundValue(MASTER_JAG.getOutputVoltage());
            
            // If the slave's voltage value does not match the master's, then update the slave's voltage value to the master's
            if(roundedSlaveVoltage != roundedMasterVoltage) {
                SLAVE_JAG.driveUsingVoltage(MASTER_JAG.getOutputVoltage() / MASTER_JAG.getBusVoltage());
            }
        }
        catch(CANTimeoutException e) {
            e.printStackTrace();
        }
    }
    
    public void stop() {
        MASTER_JAG.stop();
        SLAVE_JAG.stop();
    }

    public String getSmartDashboardType(){
        return "PIDController";
    }

    protected ITableListener listener = new ITableListener() {
        public void valueChanged(ITable table, String key, Object value, boolean isNew) {
            try {
                // Updating values
                MASTER_JAG.setPID(table.getNumber("p"), table.getNumber("i"), table.getNumber("d"));
                setPoint = table.getNumber("setpoint");
                
                // Enabling or disabling the CANJaguars' control
                if (table.getBoolean("enabled")) {
                    MASTER_JAG.enableControl();
                    SLAVE_JAG.enableControl();
                }
                else {
                    MASTER_JAG.disableControl();
                    SLAVE_JAG.disableControl();
                }
            }
            catch(CANTimeoutException e ) {
                e.printStackTrace();
            } catch (TableKeyNotDefinedException e) {
                e.printStackTrace();
            }
        }
    };
   
    public void initTable(ITable newTable) {
        // Removing listeners from the old table
        if(this.table != null) this.table.removeTableListener(listener);
        
        // Assigning the new table to the old table
        this.table = newTable;
        
        // Initializing the new table
        if(newTable != null) {
            try {
                newTable.putNumber("p", MASTER_JAG.getP());
                newTable.putNumber("i", MASTER_JAG.getI());
                newTable.putNumber("d", MASTER_JAG.getD());
                newTable.putNumber("setpoint", MASTER_JAG.getX());
                /*
                It is unknown at the moment what 'isAlive()' actually
                refers to (the 2013-2014 API does not specify), but 
                we are assuming that it has to do with whether the 
                CANJaguar is enabled or not.
                */
                newTable.putBoolean("enabled", MASTER_JAG.isAlive());
                newTable.addTableListener(listener, false);
            }
            catch(CANTimeoutException e) {
                e.printStackTrace();
            }
        }
    }
    
    public ITable getTable() {
        return table;
    }
    
    public void updateTable() {}
    
    public void startLiveWindowMode() {
        // Stopping the CANJaguar just before the
        // LiveWindow starts for safety purposes
        stop();
    }
    
    public void stopLiveWindowMode() {}
    
    public void showVoltage() {
        MASTER_JAG.showVoltage();
        SLAVE_JAG.showVoltage();
    }
    
    public void showCurrent() {
        MASTER_JAG.showCurrent();
        SLAVE_JAG.showCurrent();
    }
    
    public Jaguar getMasterJag() {
        return MASTER_JAG;
    }
    
    public Jaguar getSlaveJag() {
        return SLAVE_JAG;
    }
}