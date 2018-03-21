package engine.data;

import java.util.ArrayList;
import javax.swing.table.DefaultTableModel;

/**
 * Model for JTable showing engine.data elements.
 * 
 * @author J. M. Colmenar
 */
public class DataElementTableModel extends DefaultTableModel {
    
    public DataElementTableModel() {
        // States headers:
        this.columnIdentifiers.addAll(DataElement.getColumnIdentifiers());
    }
    
    public void addData(DataElement d) {
        this.addRow(d.toColumn());
    }

    void addAllData(ArrayList<DataElement> allData) {
        this.dataVector.clear();
        
        for (DataElement d : allData) {
            addData(d);
        }
    }
    
}
