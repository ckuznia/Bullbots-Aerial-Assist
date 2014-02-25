package org.bullbots.core;

import edu.wpi.first.wpilibj.CANJaguar;
import edu.wpi.first.wpilibj.can.CANTimeoutException;
import edu.wpi.first.wpilibj.livewindow.LiveWindowSendable;
import edu.wpi.first.wpilibj.tables.ITable;
import edu.wpi.first.wpilibj.tables.ITableListener;

/**
 * @author Clay Kuznia
 */
public class DualJaguar implements LiveWindowSendable {
    
    protected Jaguar MASTER_JAG, SLAVE_JAG;
    protected ITable table; // Table for configuring PIDs
    
    public double setPoint = 0.0; // For tunning PIDs
    
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
            double roundedSetValue = MASTER_JAG.roundValue(RPM);
            double roundedGetValue = MASTER_JAG.roundValue(MASTER_JAG.getSpeed());            
            
            // Updating the speed of the master jag
            if(roundedGetValue != roundedSetValue) MASTER_JAG.driveUsingSpeed(roundedSetValue);
            matchSlaveWithMaster();
        }
        catch(CANTimeoutException e) {
            e.printStackTrace();
        }
    }
    
    public void driveUsingPosition(double rotations) {
        MASTER_JAG.driveUsingPosition(rotations);
        matchSlaveWithMaster();
    }
    
    public void driveUsingCurrent(double current) {
        try {
            // Rounding values to 2 decimal places, in order not to overload the cRIO
            double roundedSetValue = MASTER_JAG.roundValue(current);
            double roundedGetValue = MASTER_JAG.roundValue(MASTER_JAG.getOutputCurrent());
            
            // Updating the speed of the master jag
            if(roundedGetValue != roundedSetValue) MASTER_JAG.driveUsingCurrent(roundedSetValue);
            matchSlaveWithMaster();
        }
        catch(CANTimeoutException e) {
            e.printStackTrace();
        }
    }
    
    private void matchSlaveWithMaster() {
        try {
            // If the slave's voltage value does not match the master's, then update the slave's voltage value to the master's
            if(SLAVE_JAG.getOutputVoltage()!= MASTER_JAG.getOutputVoltage()) {
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
                if (table.getBoolean("enabled")) MASTER_JAG.disableControl();
                else MASTER_JAG.enableControl();
            }
            catch(Exception e ) {
                e.printStackTrace();
            }
        }
    };
    
   
    public void initTable(ITable table) {
        System.out.println("initTable() called");
        if(this.table!=null) this.table.removeTableListener(listener);
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
    
    public ITable getTable(){
        return table;
    }
    
    public void updateTable() {}
    
    public void startLiveWindowMode() {
        try {
            // Disabling the jags for safety
            MASTER_JAG.disableControl();
        } catch (CANTimeoutException e) {
            e.printStackTrace();
        }
    }
    
    public void stopLiveWindowMode() {}
    
    public Jaguar getMasterJag() {
        return MASTER_JAG;
    }
    
    public Jaguar getSlaveJag() {
        return SLAVE_JAG;
    }
}