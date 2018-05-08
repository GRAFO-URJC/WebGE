package com.engine.data;

/**
 * Constants for engine.data management.
 * 
 * @author J. M. Colmenar
 */
public class ConstantsDataMngmt {
    
    public static int SAMPLING_RATE_MINUTES = 1;
    public static final int PUMP_BASAL_RATE = 1;
    
    // Date format for exporting
    public static String DATE_EXPORT_FORMAT = "yyyy/MM/dd;HH:mm:ss";

    // File format constants:
    public static final int AIDA_FORMAT = 0;
    public static final int GLUCMODEL_FORMAT = 1;
    public static final int PROCESSED_DATA_FORMAT = 2;
    public static final int PANCREAS_TOOL_FORMAT = 3;
    public static final int AUSTRIA_FORMAT = 4;
    public static final int CUSTOM_FORMAT = 5;    
    public static final int CUSTOM_FORMAT_2 = 6;    
    public static final int MEDTRONIC_CARE_LINK_FORMAT = 7;    // Leave as last !!
    
    
    public static final String[] DATA_FILE_FORMATS = {
        "AIDA",                 // AIDA
        "glUCModel",            // Real engine.data (glUCModel)
        "Processed Data (PancreasGE)",    // Previously processed engine.data
        "Pancreas Model Tools", // Pancreas Model Tools
        "Austria's pre-process",    // Pre-process by Austrian colleagues
        "Custom format",    // Custom format
        "Custom format 2"   // The same as the other but two columns changed 
    };
    
    public static final String[] DATA_FILE_HEADERS = {
        "#Hour,GL,CH,IS,IL",         // AIDA
        "#Date;Hour;GL;CH;IB;IN",    // Real engine.data (glUCModel)
        "# Processed Data (Pancreas GE)",
        "# Glucose;Ch.;Ins. Short Units",  // Pancreas model tools
        "PatientID,Glucose,Glucose_Interpol,CH,Insuline,DateStr,Minutes,HourMin,ID,Code,Glucose_target", // Austrian...
        "\"Glucose\",\"Insuline\",\"CH\",\"Date\",\"Minutes\",\"ID\"", // Custom
        "\"Glucose\",\"CH\",\"Insuline\",\"Date\",\"Minutes\",\"HourMin\",\"ID\"" // Custom 2
    };    
    
    public static final String MEDTRONIC_FILE_HEADER = "Last Name;First Name;Patient ID;Start Date;End Date;;Device;";
    
}
