package org.bullbots;

import edu.wpi.first.wpilibj.tables.ITable;
import edu.wpi.first.wpilibj.tables.ITableListener;

/**
 * @author Clay Kuznia
 */
public class TableListener implements ITableListener {

    public static boolean isTableUpdated;
    
    public void valueChanged(ITable itable, String string, Object o, boolean bln) {
        isTableUpdated = true;
    }
}
