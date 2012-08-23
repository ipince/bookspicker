package com.bookspicker.server.data.tools;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import com.bookspicker.server.data.ClassManager;
import com.bookspicker.shared.SchoolClass;
import com.bookspicker.shared.Term;

/**
 * Exports data to a file that can be uploaded
 * to a different database.
 * 
 * @author Rodrigo Ipince
 *
 */
public class MitClassExporter {

	public static final String PATH =
		System.getProperty("user.dir") + 
		System.getProperty("file.separator") + 
		"data" + 
		System.getProperty("file.separator") +
		"class-export" + 
		System.getProperty("file.separator");

	public static void main(String[] args) {
		
		ClassManager cm = ClassManager.getManager();

		List<SchoolClass> classes = (List<SchoolClass>) cm.listClasses();

		String filename = "classdata.txt";
		System.out.println("Writing data to " + filename);
		try {
			FileWriter fstream = new FileWriter(PATH + filename);
			BufferedWriter out = new BufferedWriter(fstream);
			int count = 0;
			for (SchoolClass clas : classes) {
				if (!clas.getTerm().equals(Term.SPRING2010))
					continue;

				out.write("2010SP");
				out.write(DataUtils.SEP);
				out.write(clas.getCode());
				out.write(DataUtils.SEP);
				out.write(clas.getTitle());

				out.write(DataUtils.SEP);
				out.write(clas.getCode());

				out.write(DataUtils.SEP);
				out.write(DataUtils.SEP);
				out.write(DataUtils.SEP);
				out.write(DataUtils.SEP);
				out.write(DataUtils.SEP);
				out.newLine();
				count++;
			}
			out.close();
			System.out.println("Wrote " + count + " records to " + filename);
		} catch (IOException e) {
			System.err.println("Error writing file: " + e.getMessage());
		}
	}

}
