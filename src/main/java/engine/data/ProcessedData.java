package engine.data;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

/**
 * Stores processed engine.data values.
 * 
 * @author J. M. Colmenar
 */
public class ProcessedData {
  
    private static final ArrayList<Date> dates = new ArrayList<Date>(); // Glucose
    private static final ArrayList<Double> glReal = new ArrayList<Double>(); // Glucose
    private static final ArrayList<Double> ch = new ArrayList<Double>(); // Carbohidrates
    private static final ArrayList<Double> is = new ArrayList<Double>(); // Insulin - short acting
    private static final ArrayList<Double> il = new ArrayList<Double>(); // Insulin - long acting

    private static double[] glRealArr = null;
    private static double[] chArr = null;
    private static double[] isArr = null;
    private static double[] ilArr = null;
    
    // Insulin types
    private static final int SHORT_EFFECT = 0;
    private static final int LONG_EFFECT = 1;

    // Basal insulin type
    private static int basalType = SHORT_EFFECT;    
    
    /**
     * States basal insulin type as short.
     */
    public static void setBasalInsulinTypeShort() {
        basalType = SHORT_EFFECT;
    }

    /**
     * States basal insulin type as long.
     */
    public static void setBasalInsulinTypeLong() {
        basalType = LONG_EFFECT;
    }    
    
    public static ArrayList<Date> getDates() {
        return dates;
    }

    public static ArrayList<Double> getGlReal() {
        return glReal;
    }

    public static ArrayList<Double> getCh() {
        return ch;
    }

    public static ArrayList<Double> getIs() {
        return is;
    }

    public static ArrayList<Double> getIl() {
        return il;
    }

    private static double[] toArray(ArrayList<Double> lista) {
        double[] arr = new double[lista.size()];
        for (int i=0; i< arr.length; i++) {
            arr[i] = lista.get(i);
        }
        return arr;
    }
    
    public static void generateArrays() {
        glRealArr = toArray(glReal);
        chArr = toArray(ch);
        isArr = toArray(is);
        ilArr = toArray(il);
    }
    
    public static double[] getGlRealArray() {
        return glRealArr;
    }

    public static double[] getChArray() {
        return chArr;
    }

    public static double[] getIsArray() {
        return isArr;
    }

    public static double[] getIlArray() {
        return ilArr;
    }
    
    private static final String[] headerCols = {"Date","Hour","Glucose","Ch.","Ins. Short Units", "Ins. Long Units"};

    
    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat(ConstantsDataMngmt.DATE_EXPORT_FORMAT); 

    private static String getRowElementToStringNoDate(int index) {
        String str = "";
        str += String.valueOf(glReal.get(index))+";";
        str += String.valueOf(ch.get(index))+";";
        str += String.valueOf(is.get(index));
        if (il.get(index) != null) {
            str += ";"+String.valueOf(il.get(index));
        }
        return str;
    }
    
    private static String getRowElementToString(int index) {
        String str = "";
        if (dates.get(index) == null) {
            str += "-;-;";  // Date;Hour;
        } else {
            str += dateFormatter.format(dates.get(index))+";";
        }
        str += getRowElementToStringNoDate(index);
        return str;
    }
    
    public static String[] getRowElement(int index) {
        return getRowElementToString(index).split(";");
    }
    
    public static Collection getColumnIdentifiers() {
        ArrayList<String> cols = new ArrayList<String>();
        cols.addAll(Arrays.asList(headerCols));       
        return cols;
    }

    /**
     * Clears all the previously processed engine.data.
     */
    public static void clear() {
        dates.clear();
        glReal.clear();
        ch.clear();
        is.clear();
        il.clear();
        
        glRealArr = null;
        chArr = null;
        isArr = null;
        ilArr = null;        
    }
    
    public static void addElement(Date datev, double glRealv, double chv, double isv, double ilv, double basalv) {
        // Distribute basal value.
        switch (basalType) {
            case SHORT_EFFECT:
                isv += basalv;
                break;
            case LONG_EFFECT:
                ilv += basalv;
        }
        dates.add(datev);
        glReal.add(glRealv);
        ch.add(chv);
        is.add(isv);
        il.add(ilv);
    }

    public static int size() {
        // Any of the arrays will provide array size
        return glReal.size();
    }

    /**
     * Export processed engine.data to dataFile overwritting if previously exists.
     */
    public static void exportToFile(String dataFile) {
        FileWriter f;
        BufferedWriter writer = null;
        try {
            f = new FileWriter(dataFile, false);

            writer = new BufferedWriter(f);

            // Write file format header:
            writer.write(ConstantsDataMngmt.DATA_FILE_HEADERS[ConstantsDataMngmt.PROCESSED_DATA_FORMAT]+"\n");
            // Write column headers:
            writer.write("# ");
            for (String s : headerCols) {
                writer.write(s+";");
            }
            writer.write("\n");
            
            // Write engine.data
            for (int i=0; i<glReal.size(); i++) {
                writer.write(getRowElementToString(i));
                writer.write("\n");
            }

            if (writer != null) {
                writer.close();
            }
        } catch (IOException ex) {
            System.err.println("Error writting file: " + ex.getLocalizedMessage());
        }
    }

    /**
     * Exports pancreas engine.data using Temporal Models Format: first, the real glucose
     * value; the rest of the variables are put next.
     * 
     * @param dataFile Destination file.
     */
    public static void exportToFileWithTemporalModelsFormat(String dataFile) {
        FileWriter f;
        BufferedWriter writer = null;
        try {
            f = new FileWriter(dataFile, false);

            writer = new BufferedWriter(f);

            // Write file format header:
            writer.write("# Real glucose;All the other variables...\n");
            
            // Write engine.data
            for (int i=0; i<glReal.size(); i++) {
                writer.write(getRowElementToStringNoDate(i));
                writer.write("\n");
            }

            if (writer != null) {
                writer.close();
            }
        } catch (IOException ex) {
            System.err.println("Error writting file: " + ex.getLocalizedMessage());
        }
    }
    
}
