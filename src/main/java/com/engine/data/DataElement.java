package com.engine.data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

/**
 * Data element to store information of the patient.
 * 
 * @author J. M. Colmenar
 */
public class DataElement implements Comparable {

    private Date date;
    private double gl;
    private double ch;
    private double il;
    private double basalRate;
    private double tempBasalAmount;
    private int tempBasalDurationMinutes;
    private String bolusType = "";
    private double insulinBolus;
    private int progBolusDurationMinutes;
    private double insulinSensitivity;
    private double targetGlucose;
    
    /**
     * Copy constructor
     * 
     * @param d 
     */
    public DataElement(DataElement d) {
        this.date = d.date;
        this.gl = d.gl;
        this.ch = d.ch;
        this.il = d.il;
        this.basalRate = d.basalRate;
        this.tempBasalAmount = d.tempBasalAmount;
        this.tempBasalDurationMinutes = d.tempBasalDurationMinutes;
        this.bolusType = d.bolusType;
        this.insulinBolus = d.insulinBolus;
        this.progBolusDurationMinutes = d.progBolusDurationMinutes;
        this.insulinSensitivity = d.insulinSensitivity;
        this.targetGlucose = d.targetGlucose;
    }
    
    public DataElement(Date date, double gl) {
        this.date = date;
        this.gl = gl;
        // Basal rate is -1 because there is no engine.data there
        this.basalRate = -1;
        this.targetGlucose = gl;
    }
    
    private static final String HEADER = "Index;Date;Time;New Device Time;BG Reading (mg/dL);Linked BG Meter ID;Basal Rate (U/h);Temp Basal Amount;Temp Basal Type;Temp Basal Duration (h:mm:ss);Bolus Type;Bolus Volume Selected (U);Bolus Volume Delivered (U);Programmed Bolus Duration (h:mm:ss);Prime Type;Prime Volume Delivered (U);Alarm;Suspend;Rewind;BWZ Estimate (U);BWZ Target High BG (mg/dL);BWZ Target Low BG (mg/dL);BWZ Carb Ratio (U/Ex);BWZ Insulin Sensitivity (mg/dL/U);BWZ Carb Input (exchanges);BWZ BG Input (mg/dL);BWZ Correction Estimate (U);BWZ Food Estimate (U);BWZ Active Insulin (U);Sensor Calibration BG (mg/dL);Sensor Glucose (mg/dL);ISIG Value;Event Marker";
    private static final int LINE_SIZE = 33;
    private static final int Index = 0;
    private static final int Date = Index + 1;
    private static final int Time = Date + 1;
    private static final int New_Device_Time = Time + 1;
    private static final int BG_Reading_mg_dL = New_Device_Time + 1;
    private static final int Linked_BG_Meter_ID = BG_Reading_mg_dL + 1;
    private static final int Basal_Rate_U_h = Linked_BG_Meter_ID + 1;
    private static final int Temp_Basal_Amount = Basal_Rate_U_h + 1;
    private static final int Temp_Basal_Type = Temp_Basal_Amount + 1;
    private static final int Temp_Basal_Duration_h_mm_ss = Temp_Basal_Type + 1;
    private static final int Bolus_Type = Temp_Basal_Duration_h_mm_ss + 1;
    private static final int Bolus_Volume_Selected_U = Bolus_Type + 1;
    private static final int Bolus_Volume_Delivered_U = Bolus_Volume_Selected_U + 1;
    private static final int Programmed_Bolus_Duration_h_mm_ss = Bolus_Volume_Delivered_U + 1;
    private static final int Prime_Type = Programmed_Bolus_Duration_h_mm_ss + 1;
    private static final int Prime_Volume_Delivered_U = Prime_Type + 1;
    private static final int Alarm = Prime_Volume_Delivered_U + 1;
    private static final int Suspend = Alarm + 1;
    private static final int Rewind = Suspend + 1;
    private static final int BWZ_Estimate_U = Rewind + 1;
    private static final int BWZ_Target_High_BG_mg_dL = BWZ_Estimate_U + 1;
    private static final int BWZ_Target_Low_BG_mg_dL = BWZ_Target_High_BG_mg_dL + 1;
    private static final int BWZ_Carb_Ratio_U_Ex = BWZ_Target_Low_BG_mg_dL + 1;
    private static final int BWZ_Insulin_Sensitivity_mg_dL_U = BWZ_Carb_Ratio_U_Ex + 1;
    private static final int BWZ_Carb_Input_exchanges = BWZ_Insulin_Sensitivity_mg_dL_U + 1;
    private static final int BWZ_BG_Input_mg_dL = BWZ_Carb_Input_exchanges + 1;
    private static final int BWZ_Correction_Estimate_U = BWZ_BG_Input_mg_dL + 1;
    private static final int BWZ_Food_Estimate_U = BWZ_Correction_Estimate_U + 1;
    private static final int BWZ_Active_Insulin_U = BWZ_Food_Estimate_U + 1;
    private static final int Sensor_Calibration_BG_mg_dL = BWZ_Active_Insulin_U + 1;
    private static final int Sensor_Glucose_mg_dL = Sensor_Calibration_BG_mg_dL + 1;
    private static final int ISIG_Value = Sensor_Glucose_mg_dL + 1;
    private static final int Event_Marker = ISIG_Value + 1;    

    /** Long date format, yyyy/MM/dd. **/
    private static final SimpleDateFormat realDataDateFormatter10 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    /** Short date format, yyyy/MM/dd. **/
    private static final SimpleDateFormat realDataDateFormatter8 = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
    
    private static String[] splitDataLine(String line) {
        // First change "," by "." to obtain correct real numbers.
        line = line.replaceAll(",", ".");
        // By adding a space we make split work with the complete line.
        line+= " ";
        return line.split(";");
    }
    
    public static boolean isValidRealDataLine(String line) {
        boolean res = false;
        String[] s = splitDataLine(line);
        // Good engine.data lines:
        if ( (s.length == LINE_SIZE) && !line.equals(HEADER)) {
              res = true;              
        }   
                
        if (res && s[DataElement.Date].isEmpty() && s[DataElement.Sensor_Glucose_mg_dL].isEmpty() &&
            s[DataElement.Basal_Rate_U_h].isEmpty() && s[DataElement.Temp_Basal_Amount].isEmpty() &&
            s[DataElement.Bolus_Volume_Delivered_U].isEmpty() && s[DataElement.BWZ_Carb_Input_exchanges].isEmpty()) {
            res = false;
        }
        
        // Check if contains engine.data testing date:
        if (res) {
            try {
                Date d = realDataDateFormatter10.parse(s[Date] + " " + s[Time]);
            } catch (ParseException ex) {
                res = false;
            }
        }
            
        return res;
    }
    
    private static double getDoubleValue(String[] data, int index) {
        double v = 0.0;
        if (!data[index].isEmpty()) {
            v = Double.valueOf(data[index]);
        }
        return v;
    }
    
    private static int parseMinutes(String[] data, int index) {
        int v = 0;
        if (!data[index].isEmpty()) {
            String[] tbadur = data[index].split(":");
            v = Integer.valueOf(tbadur[0]) * 60 + Integer.valueOf(tbadur[1]);
        }
        return v;
    }
    
    /**
     * Creates a DataElement starting from a real engine.data file line.
     * 
     * @param dataLine 
     */
    public DataElement(String dataLine) {
        String[] data = splitDataLine(dataLine);
        // Date
        String strDate = data[Date] + " " + data[Time];
        SimpleDateFormat df;
        if (data[Date].length() == 10) {
            df = realDataDateFormatter10;
        } else {
            df = realDataDateFormatter8;
        }
        try {
            this.date = df.parse(strDate);
        } catch (ParseException ex) {
            System.err.println("Date could not be parsed: " + ex.getLocalizedMessage());
        }

        this.date.getTime();

        // Glucose: always take sensor value:
        this.gl = getDoubleValue(data,DataElement.Sensor_Glucose_mg_dL);
        // -- If still without value, states -1 because it will be merged later.
        if (this.gl == 0.0) {
            this.gl = -1;
        }
        this.targetGlucose = this.gl;
        
        // Basal insuline: if no engine.data we store -1 because 0 means basal set to 0.
        if (data[DataElement.Basal_Rate_U_h].isEmpty()) {
            this.basalRate = -1;
        } else {
            this.basalRate = getDoubleValue(data,DataElement.Basal_Rate_U_h);
        }
                
        // Temp basal amount
        this.tempBasalAmount = getDoubleValue(data,DataElement.Temp_Basal_Amount);
        
        // Temp basal amount in minutes
        this.tempBasalDurationMinutes = parseMinutes(data,DataElement.Temp_Basal_Duration_h_mm_ss);
                
        // Bolus type
        this.bolusType = data[DataElement.Bolus_Type];
        
        // Insulin bolus
        this.insulinBolus = getDoubleValue(data,DataElement.Bolus_Volume_Delivered_U);
        
        // Programmed bolus duration in minutes
        this.progBolusDurationMinutes = parseMinutes(data,DataElement.Programmed_Bolus_Duration_h_mm_ss);
        
        // Carbohidrates
        this.ch = getDoubleValue(data,DataElement.BWZ_Carb_Input_exchanges);
        
        // Insulin sensitivity: how much the glucose decreases for each insulin unit
        this.insulinSensitivity = getDoubleValue(data,DataElement.BWZ_Insulin_Sensitivity_mg_dL_U);
        
    }
    
    
    public DataElement(String strDate, double gl, double ch, double is, double il) {
        // Date format exported by glUCModel
        if (strDate != null) {
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            try {
                this.date = formatter.parse(strDate);
            } catch (ParseException ex) {
                System.err.println("Date could not be parsed: " + ex.getLocalizedMessage());
            }
        }
        this.gl = gl;
        this.targetGlucose = this.gl;
        this.ch = ch;
        this.insulinBolus = is;
        this.il = il;
    }
    
    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public double getGl() {
        return gl;
    }

    public void setGl(double gl) {
        this.gl = gl;
    }

    public double getCh() {
        return ch;
    }

    public void setCh(double ch) {
        this.ch = ch;
    }

    public double getInsulinBolus() {
        return insulinBolus;
    }

    public void setInsulinBolus(double insulinBolus) {
        this.insulinBolus = insulinBolus;
    }

    public double getIl() {
        return il;
    }

    public void setIl(double il) {
        this.il = il;
    }

    public double getTargetGlucose() {
        return targetGlucose;
    }

    public void setTargetGlucose(double targetGlucose) {
        this.targetGlucose = targetGlucose;
    }
    
    

    public void setAllButGlucoseAndDate(DataElement d) {
        
        this.gl = d.gl;
        this.targetGlucose = this.gl;
        this.ch = d.ch;
        this.il = d.il;
        this.basalRate = d.basalRate;
        this.tempBasalAmount = d.tempBasalAmount;
        this.tempBasalDurationMinutes = d.tempBasalDurationMinutes;
        this.bolusType = d.bolusType;
        this.insulinBolus = d.insulinBolus;
        this.progBolusDurationMinutes = d.progBolusDurationMinutes;
        this.insulinSensitivity = d.insulinSensitivity;

    }

    @Override
    public String toString() {
        return "DataElement{" + "date=" + date + ", gl=" + gl + ", targetGluc= "+targetGlucose+", ch=" + ch + ", il=" + il + ", basalRate=" + basalRate + ", tempBasalAmount=" + tempBasalAmount + ", tempBasalDurationMinutes=" + tempBasalDurationMinutes + ", bolusType=" + bolusType + ", insulinBolus=" + insulinBolus + ", progBolusDurationMinutes=" + progBolusDurationMinutes + ", insulinSensitivity=" + insulinSensitivity + '}';
    }

    @Override
    public int compareTo(Object o) {
        return this.date.compareTo(((DataElement) o).getDate());
    }

    /**
     * Merges this engine.data with the incoming one setting the value from d
     * wherever the attribute was empty.
     * 
     * @param d
     */
    public void combine(DataElement d) {
        // Glucose
        if (this.gl < 0) {
            this.gl = d.gl;
        }
        
        this.targetGlucose = d.targetGlucose;

        // Basal insuline
        if (this.basalRate <= 0.0) {
            this.basalRate = d.basalRate;
        }

        // Temp basal amount
        if (this.tempBasalAmount <= 0.0) {
            this.tempBasalAmount = d.tempBasalAmount;
        }

        // Temp basal amount in minutes
        if (this.tempBasalDurationMinutes <= 0) {
            this.tempBasalDurationMinutes = d.tempBasalDurationMinutes;
        }

        // Bolus type
        if (this.bolusType.isEmpty()) {
            this.bolusType = d.bolusType;
        }

        // Insulin bolus
        if (this.insulinBolus <= 0.0) {
            this.insulinBolus = d.insulinBolus;
        }

        // Programmed bolus duration in minutes
        if (this.progBolusDurationMinutes <= 0) {
            this.progBolusDurationMinutes = d.progBolusDurationMinutes;
        }

        // Carbohidrates
        if (this.ch <= 0.0) {
            this.ch = d.ch;
        }

        // Insulin sensitivity: how much the glucose decreases for each insulin unit
        if (this.insulinSensitivity <= 0.0) {
            this.insulinSensitivity = d.insulinSensitivity;
        }
    }

    public Double getBasalRate() {
        return this.basalRate;
    }

    private static String[] headerCols = {"Date","Glucose","Target Glucose","Basal Rate","Ch.","Ins. Bolus","Bolus Type","Ins. Long","Sensitivity"};
    
    public static Collection getColumnIdentifiers() {
        ArrayList<String> cols = new ArrayList<String>();
        cols.addAll(Arrays.asList(headerCols));       
        return cols;
    }
    
    public String[] toColumn() {
        String[] cols = new String[headerCols.length];
        int i = 0;
        cols[i++] = (this.date != null ? this.date.toString() : "-");
        cols[i++] = String.valueOf(this.gl);
        cols[i++] = String.valueOf(this.targetGlucose);
        cols[i++] = String.valueOf(this.basalRate);
        cols[i++] = String.valueOf(this.ch);
        cols[i++] = String.valueOf(this.insulinBolus);
        cols[i++] = this.bolusType;
        cols[i++] = String.valueOf(this.il);
        cols[i++] = String.valueOf(this.insulinSensitivity);
        return cols;
    }

    
    public boolean equalsButDate(DataElement other) {
        if (other == null) {
            return false;
        }
        
        if (Double.doubleToLongBits(this.gl) != Double.doubleToLongBits(other.gl)) {
            return false;
        }
        if (Double.doubleToLongBits(this.targetGlucose) != Double.doubleToLongBits(other.targetGlucose)) {
            return false;
        }
        if (Double.doubleToLongBits(this.ch) != Double.doubleToLongBits(other.ch)) {
            return false;
        }
        if (Double.doubleToLongBits(this.il) != Double.doubleToLongBits(other.il)) {
            return false;
        }
        if (Double.doubleToLongBits(this.basalRate) != Double.doubleToLongBits(other.basalRate)) {
            return false;
        }
        if (Double.doubleToLongBits(this.tempBasalAmount) != Double.doubleToLongBits(other.tempBasalAmount)) {
            return false;
        }
        if (this.tempBasalDurationMinutes != other.tempBasalDurationMinutes) {
            return false;
        }
        if ((this.bolusType == null) ? (other.bolusType != null) : !this.bolusType.equals(other.bolusType)) {
            return false;
        }
        if (Double.doubleToLongBits(this.insulinBolus) != Double.doubleToLongBits(other.insulinBolus)) {
            return false;
        }
        if (this.progBolusDurationMinutes != other.progBolusDurationMinutes) {
            return false;
        }
        if (Double.doubleToLongBits(this.insulinSensitivity) != Double.doubleToLongBits(other.insulinSensitivity)) {
            return false;
        }
        return true;
    }

    public double getInsulinSensitivity() {
        return insulinSensitivity;
    }

    public void setInsulinSensitivity(double value) {
        this.insulinSensitivity = value;
    }

    public boolean inSameSamplingInterval(DataElement d) {
        long diffMiliseconds = Math.abs(d.date.getTime()-date.getTime());
        
        return (diffMiliseconds <= (60000 * ConstantsDataMngmt.SAMPLING_RATE_MINUTES));
    }
    
}
