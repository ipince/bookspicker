package com.bookspicker.server.data.tools;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.bookspicker.server.data.ClassManager;
import com.bookspicker.server.data.tools.DataUtils.Material;
import com.bookspicker.shared.School;
import com.bookspicker.shared.SchoolClass;
import com.bookspicker.shared.Term;

/**
 * Takes the RAW data extracted from B&N and adds class
 * data to the DB.
 */
public class BnCollegeClassImporter {
	
	private static String mainPath; // set on main() after determining school/term
	private static School school; // set on main()
	private static Term term; // set on main()
	
	public static void main(String[] args) {
		// Choose school and term!
		school = DataUtils.getSchoolFromUser();
		term = DataUtils.getTermFromUser();
		
		mainPath = DataUtils.DATA_PATH + school.toString() + DataUtils.DIR_SEP +
			term.toString() + DataUtils.DIR_SEP + 
			DataUtils.FILE_DATE_PREFIX + 
			DataUtils.DIR_SEP;
		
		new BnCollegeClassImporter().run();
	}
	
	private void run() {
		// Get path
		File dir = new File(mainPath + DataUtils.MAT_RAW_PATH);
		// Filter files by name
		FilenameFilter filter = new FilenameFilter() {
		    public boolean accept(File dir, String name) {
		    	System.out.println(name);
		    	return !name.endsWith("Ids.dat") && !name.endsWith("stats.dat");
		    }
		};
		
		String[] children = dir.list(filter);
		String filename, course;
		
		if (children == null) {
			System.err.println("Pathname does not correspond to a directory");
			return;
		}

		// Process each file
		List<Material> materials = new ArrayList<Material>();
		ClassManager cm = ClassManager.getManager();
		for (int i = 0; i < children.length; i++) {
			// Get filename of file or directory
			filename = children[i];
			course = filename.substring(0,
					filename.indexOf(".dat"));

			// empty material list
			materials.clear();

			// load raw data
			try {
				DataUtils.readRawData(mainPath, filename, materials);
			} catch (IOException e) {
				// skip
				System.err.println("Error processing materials for course " + course);
				continue;
			}
			
			// Save classes to DB
			for (Material mat : materials) {
				String[] parts = mat.section.split(DataUtils.PARTS_SEP);
				
				// Make sure there are 3 parts
				if (parts.length != 3) {
					System.err.println("Not 3 parts: " + mat.section);
					continue; // skip
				}
				
				// Search for class
				SchoolClass clas = cm.getClassByParts(school, term,
						parts[0], parts[1], parts[2]);
				if (clas == null) {
					System.out.println("Class not found for section: " + mat.section + "; adding it");
					clas = new SchoolClass();
					clas.setSchool(school);
					clas.setTerm(term);
					clas.setCourse(parts[0]);
					clas.setClas(parts[1]);
					clas.setSection(parts[2]);
					clas.setCode(school.getClassCode(parts[0], parts[1], parts[2]));
					
					clas = cm.save(clas);
				}
			}
		}
	}
}
