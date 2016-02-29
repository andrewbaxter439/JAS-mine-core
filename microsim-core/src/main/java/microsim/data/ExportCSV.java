package microsim.data;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.Transient;

import org.apache.log4j.Logger;

import microsim.data.db.DatabaseUtils;
import microsim.data.db.PanelEntityKey;
import microsim.engine.SimulationEngine;

/**
 * 
 * @author Ross Richardson
 *
 */
public class ExportCSV {

	private static Logger log = Logger.getLogger(DatabaseUtils.class);
	public static Long autoincrementSeed = 1000000L;
	
	//Fields for exporting tables to output .csv files 
	final static String newLine = "\n";
	final static String delimiter = ","; 
	final static String directory = SimulationEngine.getInstance().getCurrentExperiment().getOutputFolder() + File.separator + "csv";
	
	Set<String> fieldsForExport;
	FileWriter fileWriter = null;
	
	Collection<?> target;

	public ExportCSV(Collection<?> targetCollection) {
        try { 
        	target = targetCollection;
        	//Checks whether a file with the same filename already exists - if not, then creates one.  Useful for MultiRun case. 
        	Object obj = target.iterator().next();
        	String filename = obj.getClass().getSimpleName();
        	File f = new File(directory + File.separator + filename + ".csv");
        	if(!f.exists())
        	{
				File dir = new File(directory);
				dir.mkdir();
				f.createNewFile();
        	    fileWriter = new FileWriter(f);

        	    final Field idField = obj.getClass().getDeclaredField("id");
        	    
    			if (idField != null)
    				idField.setAccessible(true);

    			if (idField == null
    					|| !idField.getType().equals(PanelEntityKey.class))
    				throw new IllegalArgumentException("Object of type "
    						+ Object.class + " cannot be snapped!");
        	    
    			List<Field> declaredFields = new ArrayList<Field>();
    
        	    List<Field> allFields = ExportCSV.getAllFields(declaredFields, obj.getClass());
        	    
        	    TreeSet<String> nonTransientFieldNames = new TreeSet<String>();
        	    
        	    for(Field field : allFields) {
        	    	Transient trans = field.getAnnotation(Transient.class);
        	    	if(trans == null) {			//Ignore the field if it has the 'Transient' annotation, just like when exporting the data to the output database
        	    		if(field.getType().isPrimitive() || field.getType().equals(String.class)|| field.isEnumConstant()) {
        	    			nonTransientFieldNames.add(field.getName());	//Exclude references to general Objects, including PanelEntityKeys (handle id separately)
        	    		}
        	    	}
        	    }

        	  	fileWriter.append("run" + delimiter + "time" + delimiter + "id" + delimiter);
        	  	
        	    fieldsForExport = new LinkedHashSet<String>();
        	    
        	    for(String fieldNames : nonTransientFieldNames) {		//Iterated in correct order
        	    	fieldsForExport.add(fieldNames);
        	    	fileWriter.append(fieldNames + delimiter);	
        	    }
        	    
        	} else {
                fileWriter = new FileWriter(f);        		
        	}
        	        	        	
	    } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	

	/**
	 * 
	 * Creates a comma-seperated value file of data provided by arrays
	 * 
	 * @param filename - the name of the file.  If a file with the same name already exists, this method will create a new file with an incremented index
	 * @param fileHeader - the headings for the table columns
	 * @param dataArrays - the arrays carrying the data
	 * 
	 * @author Ross Richardson
	 * 
	 */
	public void dumpToCSV() {
 
		try {
			String run = SimulationEngine.getInstance().getCurrentExperiment().id.toString();
			String time = ((Double)SimulationEngine.getInstance().getTime()).toString();
            	
        	for(Object obj : target) {

                fileWriter.append(newLine);
        		fileWriter.append(run + delimiter + time + delimiter);
        		
        		Field idField = obj.getClass().getDeclaredField("id");
        		idField.setAccessible(true);
				fileWriter.append(((PanelEntityKey)idField.get(obj)).getId().toString() + delimiter);
				
        		for(String fieldName : fieldsForExport) {
        			Field thisField = obj.getClass().getDeclaredField(fieldName);
        			thisField.setAccessible(true);
   					fileWriter.append(thisField.get(obj).toString());    					 
	            	fileWriter.append(delimiter);
        		}
        	}
            
        } catch (IOException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
        finally {
        	try { 
        		fileWriter.flush(); 
//        		fileWriter.close(); 
        	} catch (IOException e) { 
//        			System.out.println("Error while flushing/closing fileWriter."); 
        			e.printStackTrace(); 
        	} 
        }
	}	
	
	//Recursive method to get all fields of a class, including inherited ones
		private static List<Field> getAllFields(List<Field> fields, Class<?> type) {
		    fields.addAll(Arrays.asList(type.getDeclaredFields()));

		    if (type.getSuperclass() != null) {
		        fields = getAllFields(fields, type.getSuperclass());
		    }

		    return fields;
		}
		
}
