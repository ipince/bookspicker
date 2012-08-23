package com.bookspicker;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;


/**
 * This class initializes the Log4J.
 * Inspired by: http://whatwouldnickdo.com/wordpress/186/gwt-hosted-mode-and-log4j/
 * @author Jonathan
 *
 */

public class Log4JInitServlet extends HttpServlet {
  private static final long serialVersionUID = 6568022043936788992L;
  public static final Logger logger = Logger.getLogger(Log4JInitServlet.class);
 
  public void init() throws ServletException {
    System.out.println("Log4JInitServlet init() starting.");
    String log4jfile = getInitParameter("log4j-properties");
    System.out.println("log4jfile: "+log4jfile);
    if (log4jfile != null) {
      String propertiesFilename = getServletContext().getRealPath(log4jfile);
      PropertyConfigurator.configure(propertiesFilename);
      logger.info("logger configured.");
    }else{
      System.out.println("Error setting up logger.");
    }
      System.out.println("Log4JInitServlet init() done.");
  }
}