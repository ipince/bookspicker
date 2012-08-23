package com.bookspicker.test.server.services;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.bookspicker.server.data.ClassManager;
import com.bookspicker.shared.ClassBook;
import com.bookspicker.shared.School;
import com.bookspicker.shared.SchoolClass;
import com.bookspicker.shared.Term;

@SuppressWarnings("serial")
public class TestServlet extends HttpServlet {

	protected void doGet(
			HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		
		try {
			// Begin unit of work
			//HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();

			// Process request and render page
	        // Write HTML header
	        PrintWriter out = response.getWriter();
	        out.println("<html><head><title>Class Lookup</title></head><body>");

	        // Handle actions

	        String classCode = request.getParameter("classCode");

	        if (classCode == null) {
	        	out.println("<b><i>Please enter a class number.</i></b>");
	        } else {
	        	ClassManager mgr = ClassManager.getManager();
	        	SchoolClass theClass = mgr.getClassByCode(School.MIT, Term.SPRING2010, classCode);
	        	if (theClass == null) {
	        		out.println("<b><i>Class number " + classCode + " not found.</i></b>");
	        	} else {
	        		out.println("<b>Class number: " + classCode + ".</i></b><br/>");
	        		out.println("<b>Class title: " + theClass.getTitle() + ".</i></b><br/><br/>");

	        		int i = 1;
	        		for (ClassBook book : (Set<ClassBook>) theClass.getBooks()) {
	        			out.println("Book " + i + ": " + book.getBook().getTitle() + "<br/>");
	        			i++;
	        		}
	        	}
	        }

	        // Print page
	       printEventForm(out);

	       // Write HTML footer
	       out.println("</body></html>");
	       out.flush();
	       out.close();

			// End unit of work
			//HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
		} catch (Exception ex) {
			//HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().rollback();
			if ( ServletException.class.isInstance( ex ) ) {
				throw ( ServletException ) ex;
			} else {
				throw new ServletException( ex );
			}
		}
	}
	
	private void printEventForm(PrintWriter out) {
        out.println("<h2>Lookup books for class:</h2>");
        out.println("(Try 15.01, 24.00, 5.012, or 6.170) <br/><br/>");
        out.println("<form>");
        out.println("Class Number: <input name='classCode' length='50'/> ");
        out.println("<input type='submit' name='action' value='lookup'/>");
        out.println("</form>");
    }
}
