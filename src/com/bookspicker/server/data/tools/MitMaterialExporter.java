package com.bookspicker.server.data.tools;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import com.bookspicker.server.data.ClassManager;
import com.bookspicker.shared.ClassBook;
import com.bookspicker.shared.SchoolClass;
import com.bookspicker.shared.Term;

public class MitMaterialExporter {

	public static final String PATH =
		System.getProperty("user.dir") + 
		System.getProperty("file.separator") + 
		"data" + 
		System.getProperty("file.separator") +
		"materials-export" + 
		System.getProperty("file.separator");

	public static void main(String[] args) {
		
		ClassManager cm = ClassManager.getManager();

		List<SchoolClass> classes = (List<SchoolClass>) cm.listClasses();

		String filename = "classbookdata.txt";
		System.out.println("Writing data to " + filename);
		try {
			FileWriter fstream = new FileWriter(PATH + filename);
			BufferedWriter out = new BufferedWriter(fstream);
			int count = 0;
			for (SchoolClass clas : classes) {
				if (!clas.getTerm().equals(Term.SPRING2010))
					continue;
				
				for (ClassBook cb : clas.getBooks()) {
					
					if (cb.getBook().getIsbn() == null ||
							cb.getBook().getIsbn().isEmpty())
						continue;
					
					out.write(clas.getCode()); // class
					out.write(DataUtils.SEP);
					out.write("2010SP"); // term
					out.write(DataUtils.SEP);
					out.write(cb.getBook().getIsbn()); // isbn
					out.write(DataUtils.SEP);
					out.write(cb.getNecessity().toString()); // necessity
					out.write(DataUtils.SEP);
					out.write("0"); // earliest edition
					out.write(DataUtils.SEP);
					out.write("bookspicker"); // added by
					out.write(DataUtils.SEP);
					out.write(""); // comment
					out.write(DataUtils.SEP);
					out.write(""); // modified
					
					out.newLine();
					count++;
					
				}
			}
			out.close();
			System.out.println("Wrote " + count + " records to " + filename);
		} catch (IOException e) {
			System.err.println("Error writing file: " + e.getMessage());
		}
	}
	
}
