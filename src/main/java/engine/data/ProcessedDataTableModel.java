package engine.data;

import javax.swing.table.DefaultTableModel;

/**
 * Model for JTable showing PROCESSED engine.data elements.
 * 
 * @author J. M. Colmenar
 */
public class ProcessedDataTableModel extends DefaultTableModel {
    
    public ProcessedDataTableModel() {
        // States headers:
        this.columnIdentifiers.addAll(ProcessedData.getColumnIdentifiers());
    }
 
    public void loadProcessedData() {
        this.dataVector.clear();
        for (int i=0; i<ProcessedData.size(); i++) {
            this.addRow(ProcessedData.getRowElement(i));
        }
    }
    
}
