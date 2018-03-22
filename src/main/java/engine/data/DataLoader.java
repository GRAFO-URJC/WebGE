package engine.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;


/**
 * Data loader for glucose information.
 * 
 * @author J. M. Colmenar
 */
public class DataLoader {

    // In process engine.data
    protected static ArrayList<DataElement> allData = new ArrayList<>();
    
    // Raw loaded engine.data
    protected static ArrayList<DataElement> allRawData = new ArrayList<>();

    
    public static String lastErrorStr = "";

    /**
     * Maximum engine.data size. Cannot use previous values in a window wider than
     * this length.
     */
    protected static int maxDataSize = 100;
   
    
    /**
     * Loads patient's engine.data from file:
     * 
     * @param inputData 
     * @param begin 
     * @param end 
     */
    public static void loadData(String inputData, Date begin, Date end, int numData) {
        /* Detecting file format:
        
            - AIDA simulator format:        #Hour,GL,CH,IS,IL
                
            - Exported Real engine.data format:    #Date;Hour;GL;CH;IB;IN
        
            - Medtronic's Care Link:        Last Name;First Name;Patient ID;...
        
            - Pancreas Model Tools:         # Glucose;Ch.;Ins. Short Units
        
            - Manel's format                "Glucose","Insuline","CH","Date","Minutes","ID"
        
            - Austria's format              PatientID,Glucose,Glucose_Interpol,CH,Insuline,DateStr,Minutes,HourMin,ID,Code,Glucose_target
        */
        
        int fileFormat = identifyFileFormat(inputData);
        
        // Obtain engine.data list and filter by date if necessary.
        ArrayList<DataElement> rawData = null;
        switch (fileFormat) {
            case ConstantsDataMngmt.AIDA_FORMAT:
                rawData = loadDataAida(inputData);
                break;
            case ConstantsDataMngmt.GLUCMODEL_FORMAT:
                rawData = loadDataGlucmodel(inputData);
                break;
            case ConstantsDataMngmt.PROCESSED_DATA_FORMAT:
                rawData = loadProcessedData(inputData);
                break;    
            case ConstantsDataMngmt.MEDTRONIC_CARE_LINK_FORMAT:
                rawData = loadRealDataFromPump(inputData,begin,end);
                break;    
            case ConstantsDataMngmt.PANCREAS_TOOL_FORMAT:
                rawData = loadPancreasToolData(inputData);
                break;  
            case ConstantsDataMngmt.AUSTRIA_FORMAT:
                rawData = loadPreProcessedData(inputData,numData);
                break;
            case ConstantsDataMngmt.CUSTOM_FORMAT:
                rawData = loadCustomFormatData(inputData);
                break;
            default:
                
                System.err.println("ERROR: Unrecognized file format. Options:");
                for (int i = 0; i < ConstantsDataMngmt.DATA_FILE_HEADERS.length; i++) {
                    System.out.println(" -- Format: " + ConstantsDataMngmt.DATA_FILE_FORMATS[i] + "\n -- First line of file: " + ConstantsDataMngmt.DATA_FILE_HEADERS[i]+"\n");
                }
                return;
        }

        if ((fileFormat == ConstantsDataMngmt.PROCESSED_DATA_FORMAT) || 
            (fileFormat == ConstantsDataMngmt.PANCREAS_TOOL_FORMAT) ||
            (fileFormat == ConstantsDataMngmt.CUSTOM_FORMAT) ||
            (fileFormat == ConstantsDataMngmt.AUSTRIA_FORMAT)    ) {

            // No need for all the stuff in the else branch
            ProcessedData.clear();
            for (DataElement d : rawData) {
                ProcessedData.addElement(d.getDate(), d.getGl(), d.getCh(), d.getInsulinBolus(), d.getIl(), 0.0);
            }

            allData = rawData;
            allRawData = rawData;
            
        } else {

            // Merge insulin and glucose measures because insulin engine.data may not have glucose value.
            allRawData.clear();

            int i = 0, j = 0;
            boolean purge = false;
            while (i < rawData.size()) {
                // Purge equal engine.data:
                if ((i > 0) && rawData.get(i).equalsButDate(rawData.get(i - 1))) {
                    purge = true;
                } else {
                    purge = false;
                }
                if (!purge) {
                    /* If glucose measure exists with no info about ch and insulines, 
                     all the line goes to the final engine.data */
                    if (rawData.get(i).getGl() > 0) {
                        allRawData.add(rawData.get(i));
                        j++;
                    } else {
                        // Compare previous and next rawData.
                        DataElement prev = null;
                        DataElement next = null;
                        long difPrev = Long.MAX_VALUE;
                        long difNext = Long.MAX_VALUE;
                        if (j > 0) {
                            // prev is the already processed
                            prev = allRawData.get(j - 1);
                            difPrev = rawData.get(i).getDate().getTime() - prev.getDate().getTime();
                        }
                        if (i < (rawData.size() - 1)) {
                            // next is the engine.data to be processed next.
                            next = rawData.get(i + 1);
                            difNext = next.getDate().getTime() - rawData.get(i).getDate().getTime();
                        }
                        if (difPrev < difNext) {
                            // Measures go to the previous element (allData), but glucose.
                            prev.setAllButGlucoseAndDate(rawData.get(i));

                        } else {
                            // Insulin measures go to the next element to be processed (engine.data)
                            next.setAllButGlucoseAndDate(rawData.get(i));
                            // Wich is now added.
                            allRawData.add(next);
                            i++;
                            j++;
                        }
                    }
                }

                i++;
            }

            // Fusion elements in the same minute:
            ArrayList<DataElement> newAllRawData = new ArrayList<>();
            newAllRawData.add(allRawData.get(0));
            int i2 = 0;

            for (int k = 1; k < allRawData.size(); k++) {
                // If current and previous engine.data are in the same minute, fuse them and forward.
                DataElement prev = newAllRawData.get(i2);
                if (prev.inSameSamplingInterval(allRawData.get(k))) {
                    prev.combine(allRawData.get(k));
                } else {
                    newAllRawData.add(allRawData.get(k));
                    i2++;
                }
            }

            // Reasign
            allRawData = newAllRawData;

            /*
             * From raw to processed:
             - Uniform space between events: 1 minute rate.
             - Apply temporal changes in rates if needed.
             */
            allData.clear();
            GregorianCalendar indexDate = null;
            GregorianCalendar nextDate = null;
            int index = 0;
            while (index < allRawData.size()) {
                DataElement d = allRawData.get(index);

                // Dates are taken into account when format is not AIDA:
                if (fileFormat != ConstantsDataMngmt.AIDA_FORMAT) {

                    if (indexDate == null) {
                        // First date (seconds are set to 0)
                        indexDate = new GregorianCalendar();
                        indexDate.setTime(d.getDate());
                        indexDate.set(Calendar.SECOND, 0);
                        d.setDate(indexDate.getTime());
                        // Advance
                        indexDate.add(Calendar.MINUTE, ConstantsDataMngmt.SAMPLING_RATE_MINUTES);
                        // and next date in sampling
                        nextDate = new GregorianCalendar();
                        nextDate.setTime(d.getDate());
                        nextDate.add(Calendar.MINUTE, ConstantsDataMngmt.SAMPLING_RATE_MINUTES);
                        // Add engine.data
                        allData.add(new DataElement(d));
                        index++;
                    } else {
                        DataElement nd = null;
                        if ((indexDate.getTime().equals(d.getDate()))
                                || (indexDate.getTime().before(d.getDate()) && nextDate.getTime().after(d.getDate()))) {
                            // If engine.data is within dates, then is included with index date.
                            nd = new DataElement(allRawData.get(index));
                            nd.setDate(indexDate.getTime());
                            index++;
                        } else {
                            // Data is later than nextDate: the current engine.data glucose is copied again with index date
                            nd = new DataElement(indexDate.getTime(), allRawData.get(index - 1).getGl());
                        }
                        allData.add(new DataElement(nd));
                        // Adds minutes to index date and next date
                        indexDate.setTime(nextDate.getTime());
                        nextDate.add(Calendar.MINUTE, ConstantsDataMngmt.SAMPLING_RATE_MINUTES);
                    }
                } else {
                    // AIDA does not uses dates:
                    allData.add(new DataElement(d));
                    index++;
                }

            }

            /*
             * From raw to processed:
             - Change insulin values to rates, etc.
             */
            // Always fill in plain engine.data vectors with final values:
            ProcessedData.clear();
            Iterator<DataElement> iter = allData.iterator();
            int counterBasal = -1;
            double basalValue = -1;
            double currBasalValue = 0;

            while (iter.hasNext()) {

                // Basal insulin:
                DataElement d = iter.next();
                if (d.getBasalRate() > 0) {
                    /* Units are distributed into little injections. 
                     60 are minutes because rates are per hour. */
                    basalValue = d.getBasalRate() / (60 / ConstantsDataMngmt.PUMP_BASAL_RATE);
                    counterBasal = ConstantsDataMngmt.PUMP_BASAL_RATE - 1;
                } else {
                    if (d.getBasalRate() == 0) {
                        // Basal rate is 0. Basal must be stopped.
                        basalValue = -1;
                        counterBasal = -1;
                    }
                }
                currBasalValue = 0;
                if (counterBasal >= 0) {
                    if ((ConstantsDataMngmt.PUMP_BASAL_RATE - counterBasal - 1) == 0) {
                        counterBasal = 0;
                        currBasalValue = basalValue;
                    } else {
                        counterBasal++;
                    }
                }

                // Add engine.data
                ProcessedData.addElement(d.getDate(), d.getGl(), d.getCh(), d.getInsulinBolus(), d.getIl(), currBasalValue);
            }

        }
                
        // Generate arrays
        ProcessedData.generateArrays();
        
        System.out.println("... done.\n");
                
        System.out.println(allData.size()+" records were processed.\n\n");
        
    }

    
    /**
     * Loads previously processed engine.data, with format: # Date;Hour;Glucose;Ch;Ins Short Units;Ins Long Units;.
     * 
     * @param inputData 
     */
    private static ArrayList<DataElement> loadProcessedData(String inputData) {
        ArrayList<DataElement> data = new ArrayList<>();
        System.out.println("\nLoading PancreasGE processed engine.data...\n");
        try {
            BufferedReader datReader = new BufferedReader(new FileReader(new File(inputData)));
            String line;
            // PancreasGE Date format
            SimpleDateFormat formatter = new SimpleDateFormat(ConstantsDataMngmt.DATE_EXPORT_FORMAT);
            while ((line = datReader.readLine()) != null) {
                if (!line.isEmpty() && !line.startsWith("#")) {
                    String[] parts = line.split(";");
                    double vGl = Double.valueOf(parts[2]);
                    double vCh = Double.valueOf(parts[3]);
                    double vIs = Double.valueOf(parts[4]);
                    double vIl = Double.valueOf(parts[5]);
                    DataElement d = new DataElement(null, vGl, vCh, vIs, vIl);
                    // State date and hour
                    String strDate = parts[0]+";"+parts[1];
                    try {
                        d.setDate(formatter.parse(strDate));
                    } catch (ParseException ex) {
                        System.err.println("Date could not be parsed: " + ex.getLocalizedMessage());
                    }                    
                    data.add(d);                   
                }
            }
            System.out.println("... done.\n");
        } catch (FileNotFoundException e) {
            System.err.println(e.getLocalizedMessage());
        } catch (IOException e) {
            System.err.println(e.getLocalizedMessage());
        }        
        return data;
    }    
    
    
    /**
     * Loads engine.data following AIDA format: #Hour,GL,CH,IS,IL
     * 
     * @param inputData 
     */
    private static ArrayList<DataElement> loadDataAida(String inputData) {
        ArrayList<DataElement> data = new ArrayList<>();
        System.out.println("\nLoading engine.data (AIDA format)...\n");
        try {
            BufferedReader datReader = new BufferedReader(new FileReader(new File(inputData)));
            String line;
            while ((line = datReader.readLine()) != null) {
                if (!line.isEmpty() && !line.startsWith("#")) {
                    String[] parts = line.split(",");
                    double vGl = Double.valueOf(parts[1]);
                    double vCh = Double.valueOf(parts[2]);
                    double vIs = Double.valueOf(parts[3]);
                    double vIl = Double.valueOf(parts[4]);
                    data.add(new DataElement(null, vGl, vCh, vIs, vIl));
                }
            }
            System.out.println("... done.\n");
        } catch (FileNotFoundException e) {
            System.err.println(e.getLocalizedMessage());
        } catch (IOException e) {
            System.err.println(e.getLocalizedMessage());
        }        
        return data;
    }

    
    /**
     * Loads engine.data following glUCModel format: #Date;Hour;GL;CH;IB (basal, long effect);IN (normal, short effect)
     * 
     * @param inputData Data filename
     */    
    private static ArrayList<DataElement> loadDataGlucmodel(String inputData) {
        System.out.println("\nLoading engine.data (glUCModel format)...\n");
        
        System.out.println(" --- Assuming first measure has glucose value\n");
        
        // 1.- Read file and merge insulin measures (no periodical, less frequent)
        //     with glucose measures (more frequent)
        ArrayList<DataElement> data = new ArrayList<DataElement>();
        
        try {
            BufferedReader datReader = new BufferedReader(new FileReader(new File(inputData)));
            String line;
            while ((line = datReader.readLine()) != null) {
                if (!line.isEmpty() && !line.startsWith("#")) {
                    String[] parts = line.split(";");
                    // Format for DataElement comes from file: dd/MM/yyyy HH:mm:ss
                    String dateHour = parts[0]+" "+parts[1];
                    // If no glucose engine.data ("-" value), set -1.
                    double gl = -1;
                    if (!parts[2].equals("-")) {
                        gl = Double.valueOf(parts[2]);
                    }
                    // If no ch, then value is 0:
                    double ch = 0;
                    if (!parts[3].equals("-")) {
                        ch = Double.valueOf(parts[3]);
                    }
                    // If no insulins, then value is 0:
                    double is = 0, il = 0;
                    if (!parts[5].equals("-")) {
                        is = Double.valueOf(parts[5]);  // IN, normal, bolus.
                    }
                    if (!parts[4].equals("-")) {
                        il = Double.valueOf(parts[4]);  // IB, basal, cuold be long-efect.
                    }
                    data.add(new DataElement(dateHour,gl,ch,is,il));
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println(e.getLocalizedMessage());
        } catch (IOException e) {
            System.err.println(e.getLocalizedMessage());
        }     
        
        return data;

    }
    
    /**
     * Processes engine.data dumps from real Medtronics insulin pumps.
     * 
     * @param fileName Name of the CSV file exported by Medtronics pumps.
     * @param begin Date from which engine.data are recorded.
     * @param end Date till engine.data are recorded.
     */
    private static ArrayList<DataElement> loadRealDataFromPump(String fileName, Date begin, Date end) {
        System.out.println("\nLoading engine.data (Medtronic Care Link format)...\n");
        FileReader f;
	BufferedReader reader = null;
	String line;
        
        ArrayList<DataElement> data = new ArrayList<DataElement>();
        
	try {
		f = new FileReader(fileName);
		reader = new BufferedReader(f);
		while ( (line = reader.readLine()) != null ) {
                        // Only work with engine.data lines:
                        if (DataElement.isValidRealDataLine(line)) {
                            data.add(new DataElement(line));
                            
                        }
                }
	} catch (IOException e) {
		System.err.println("Error on engine.data file: "+e.getLocalizedMessage());
	}

	try {
		if (reader != null ) reader.close();
	} catch (IOException e) {
		System.err.println("Error closing engine.data file: "+e.getLocalizedMessage());
	}        
        
        // Sort by date
        Collections.sort(data);
        
        ArrayList<DataElement> finalData = new ArrayList<DataElement>();
        
        int i = 0;
        double lastGl = -1;
        // Filter by date
        if (begin != null) {
            while ((i < data.size()) && (data.get(i).getDate().before(begin))) {
                if (data.get(i).getGl() > 0) {
                    lastGl = data.get(i).getGl();
                }
                i++;
            }
        }
        
        // Recall the last previous glucose value
        if ((i < data.size()) && (data.get(i).getGl() < 0)) {
            if (lastGl < 0) {
                System.err.println("\nERROR: there is no glucose value for the first engine.data in the interval, and no previous values are available.\n");
            } else {
                data.get(i).setGl(lastGl);
            }
        }
        
        DataElement lastData = null;
        if (end != null) {
            while ((i < data.size()) && (data.get(i).getDate().before(end))) {
                DataElement d = data.get(i);
                if (d.getGl() < 0) {
                    d.setGl(lastGl);
                }
                if ((lastData != null) && (lastData.getDate().equals(d.getDate()))) {
                    lastData.combine(d);
                } else {
                    finalData.add(d);
                    lastGl = d.getGl();
                    lastData = d;
                }
                i++;
            }
        }      
        
        return finalData;
        
    }

    /**
     * Loads engine.data following the format of the Pancreas tool:
     *   # Glucose;Ch.;Ins Short Units
     * 
     * @param inputData
     * @return 
     */
    private static ArrayList<DataElement> loadPancreasToolData(String inputData) {
        ArrayList<DataElement> data = new ArrayList<>();
        System.out.println("\nLoading Pancreas Model Tools processed engine.data...\n");
        try {
            BufferedReader datReader = new BufferedReader(new FileReader(new File(inputData)));
            String line;
            while ((line = datReader.readLine()) != null) {
                if (!line.isEmpty() && !line.startsWith("#")) {
                    String[] parts = line.split(";");
                    double vGl = Double.valueOf(parts[0]);
                    double vCh = Double.valueOf(parts[1]);
                    double vIs = Double.valueOf(parts[2]);
                    DataElement d = new DataElement(null, vGl, vCh, vIs, 0.0);                   
                    data.add(d);
                }
            }
            System.out.println("... done.\n");
        } catch (FileNotFoundException e) {
            System.err.println(e.getLocalizedMessage());
        } catch (IOException e) {
            System.err.println(e.getLocalizedMessage());
        }        
        return data;
    }

    /**
     * Loads engine.data using custom format:
     * "Glucose","Insuline","CH","Date","Minutes","ID"
     * 
     * @param inputData
     * @return 
     */
    private static ArrayList<DataElement> loadCustomFormatData(String inputData) {
        ArrayList<DataElement> data = new ArrayList<>();
        System.out.println("\nLoading Custom Format engine.data...\n");
        try {
            BufferedReader datReader = new BufferedReader(new FileReader(new File(inputData)));
            String line;
            while ((line = datReader.readLine()) != null) {
                if (!line.isEmpty() && !line.startsWith("\"")) {
                    String[] parts = line.split(",");
                    double vGl = Double.valueOf(parts[0]);
                    double vIs = Double.valueOf(parts[1]);
                    double vCh = Double.valueOf(parts[2]);                    
                    DataElement d = new DataElement(null, vGl, vCh, vIs, 0.0);                   
                    data.add(d);
                }
            }
            System.out.println("... done.\n");
        } catch (FileNotFoundException e) {
            System.err.println(e.getLocalizedMessage());
        } catch (IOException e) {
            System.err.println(e.getLocalizedMessage());
        }        
        return data;
    }

    public static ArrayList<DataElement> loadPreProcessedData(String inputData, int numData) {
        ArrayList<DataElement> data = new ArrayList<>();
        if (numData == 0) {
            numData = Integer.MAX_VALUE;
        }
        System.out.println("\nLoading pre-processed engine.data (Austrian style)...\n");
        System.out.println("--> Glucose to be read is the interpoled one.");
        System.out.println("--> Insulin is always short effect.");
        try {
            BufferedReader datReader = new BufferedReader(new FileReader(new File(inputData)));
            String line;
            while ((numData > 0) && ((line = datReader.readLine()) != null)) {
                if (!line.isEmpty() && !line.startsWith("#") && !line.startsWith("Patient")) {
                    String[] parts = line.split("\\,");
                    double vGl = Double.valueOf(parts[2]);
                    double vCh = Double.valueOf(parts[3]);
                    double vIs = Double.valueOf(parts[4]);
                    double vIl = 0.0;
                    DataElement d = new DataElement(null, vGl, vCh, vIs, vIl);
                    if (line.trim().endsWith(",")) {
                        d.setTargetGlucose(-1.0);
                    }                    
                    data.add(d);   
                    numData--;
                }
            }
            System.out.println("... done.\n");
        } catch (FileNotFoundException e) {
            System.err.println(e.getLocalizedMessage());
        } catch (IOException e) {
            System.err.println(e.getLocalizedMessage());
        }        
        return data;
    }

    public static ArrayList<DataElement> load2ndCustomFormatData(String inputData, int numData) {
        ArrayList<DataElement> data = new ArrayList<>();
        if (numData == 0) {
            numData = Integer.MAX_VALUE;
        }
        System.out.println("\nLoading custom format engine.data ...\n");
        System.out.println("--> Insulin is always short effect.");
        try {
            BufferedReader datReader = new BufferedReader(new FileReader(new File(inputData)));
            String line;
            while ((numData > 0) && ((line = datReader.readLine()) != null)) {
                if (!line.isEmpty() && !line.startsWith("\"")) {
                    String[] parts = line.split("\\,");
                    double vGl = Double.valueOf(parts[0]);
                    double vCh = Double.valueOf(parts[1]);
                    double vIs = Double.valueOf(parts[2]);
                    double vIl = 0.0;
                    DataElement d = new DataElement(null, vGl, vCh, vIs, vIl);
                    data.add(d);   
                    numData--;
                }
            }
            System.out.println("... done.\n");
        } catch (FileNotFoundException e) {
            System.err.println(e.getLocalizedMessage());
        } catch (IOException e) {
            System.err.println(e.getLocalizedMessage());
        }        
        return data;
    }
    
    
    public static int identifyFileFormat(String inputData) {
        int fileFormat = -1;
        
        System.out.println("\nDetecting file format ...\n");
        String line = "";
        try {
            BufferedReader datReader = new BufferedReader(new FileReader(new File(inputData)));
            do {
                line = datReader.readLine();
            } while ((line == null) && (line.isEmpty()));
            datReader.close();
        } catch (FileNotFoundException e) {
            System.err.println(e.getLocalizedMessage());
        } catch (IOException e) {
            System.err.println(e.getLocalizedMessage());
        }        
        
        line = line.trim();
        // Check if Medtronics format:
        if (line.startsWith(ConstantsDataMngmt.MEDTRONIC_FILE_HEADER)) {
            fileFormat = ConstantsDataMngmt.MEDTRONIC_CARE_LINK_FORMAT;
        } else {
            // Check other formats
            int i = 0;
            while ((i < ConstantsDataMngmt.DATA_FILE_HEADERS.length)
                    && (!line.equals(ConstantsDataMngmt.DATA_FILE_HEADERS[i]))) {
                i++;
            }
            fileFormat = i;
        }
        
        return fileFormat;
    }


}
