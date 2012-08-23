package com.bookspicker.server.data.tools;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.bookspicker.server.data.ClassManager;
import com.bookspicker.server.data.tools.DataUtils.ClassBean;
import com.bookspicker.shared.School;
import com.bookspicker.shared.SchoolClass;
import com.bookspicker.shared.Term;

public class MitClassImporter {
	
	private static String filePrefix;
	private static School school;
	
	public static void main(String[] args) {
		
		// Choose school and term!
		school = DataUtils.getSchoolFromUser();
		Term term = DataUtils.getTermFromUser();
		
		filePrefix = school.toString() + "_" + term.toString() + "_" + DataUtils.FILE_DATE_PREFIX + "_";
		
		if (!DataUtils.checkPathAndFilePrefix(filePrefix))
			System.exit(1);
		
		importData();
	}

	private static void importData() {
		
		// Read class data from file and sort classes
		List<ClassBean> beanClasses = new ArrayList<ClassBean>();
		readClasses(filePrefix + "cis_course_catalog.dat", beanClasses);
		Collections.sort(beanClasses, new Comparator<ClassBean>() {
			@Override
			public int compare(ClassBean bean1, ClassBean bean2) {
				if (bean1.term.ordinal() != bean2.term.ordinal())
					return bean1.term.ordinal() - bean2.term.ordinal();
				else
					return bean1.classCode.compareTo(bean2.classCode);
			}
		});
		
		// Add/update classes in DB -- careful about not repeating classes!
		ClassManager cm = ClassManager.getManager();
		int processed = 0;
		int added = 0;
		int updated = 0;
		SchoolClass clas, dbClass;
		boolean changed;
		for (ClassBean bean : beanClasses) {
			
			// Check to see if class is already in DB
			dbClass = cm.getClassByCode(school, bean.term, bean.classCode);
			if (dbClass == null) {
				// If not in DB, add new class
				clas = new SchoolClass(bean.classCode, bean.title, bean.term);
				if (!bean.jointSubjects.isEmpty()) {
					clas.setJointSubjects(bean.jointSubjects.replaceAll(" ", ""));
				}
				clas.setLastActivityDate(bean.lastActivityDate);
				clas.setWarehouseLoadDate(bean.warehouseLoadDate);
				cm.save(clas);
				added++;
			} else {
				// Class is already in DB, so let's update it with the new data
				// Note: do not update if nothing has changed!
				changed = false;
				if (!dbClass.getTitle().equals(bean.title)) {
					dbClass.setTitle(bean.title);
					changed = true;
				}
				// Note that bean must have a non-null joint subjects
				if (!bean.jointSubjects.equals(dbClass.getJointSubjects()) && !bean.jointSubjects.isEmpty()) {
					dbClass.setJointSubjects(bean.jointSubjects.replaceAll(" ", ""));
					changed = true;
				}
				if (!dbClass.getLastActivityDate().equals(bean.lastActivityDate)) {
					dbClass.setLastActivityDate(bean.lastActivityDate);
					changed = true;
				}
				if (!dbClass.getWarehouseLoadDate().equals(bean.warehouseLoadDate)) {
					dbClass.setWarehouseLoadDate(bean.warehouseLoadDate);
					changed = true;
				}
				if (changed) {
					cm.updateClass(dbClass);
					updated++;
				}
			}
			processed++;
		}
		
		System.out.println("Processed " + processed + " records in total");
		System.out.println("Added " + added + " records to database");
		System.out.println("Updated " + updated + " records in database");
	}
	
	private static void readClasses(String filename, List<ClassBean> classes) {
		try {
			//  Create a stream for reading.
			FileInputStream fis = new FileInputStream(DataUtils.CLASS_PROCESSED_PATH + filename);

			//  Next, create an object that can read from that file.
			ObjectInputStream inStream = new ObjectInputStream(fis);

			// Retrieve the Serializable object.
			ClassBean classBean;
			while ((classBean = (ClassBean) inStream.readObject()) != null) {
				classes.add(classBean);
			}
		} catch (EOFException eof) {
			// file ended - ok
		} catch (IOException ioe) {
			System.err.println("Error reading books");
			ioe.printStackTrace();
		} catch (ClassNotFoundException cnfe) {
			System.err.println("Error reading books2: " + cnfe.getMessage());
		}
	}

}
