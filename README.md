# bookspicker

Code running on bookspicker.com

## Developer Setup

The easiest way to set up the project (at least for now) is to use Eclipse. I plan on adding
Maven so everything can be built from the command line, but for now, Eclipse remains easiest.

1. Download and install (unpack) Eclipse.
2. Download GWT 2.5+. The easiest way to do this is to follow these instructions on how to
[install the Google Eclipse Plugin](https://developers.google.com/web-toolkit/usingeclipse).
    - Note your eclipse version (e.g. Juno is 4.2), and use <version> as opposed to 3.7 on
      the Google plugin link.
    - You don't need to install everything available in the list. "Google Plugin for Eclipse"
      and SDKs -> Google Web Toolkit SDK 2.5.0 are enough.
3. Go to the package explorer. Right click -> Import -> Existing project -> select the bookspicker
   main directory. Import.
4. You may (or may not see) a build error that reads:

    > The GWT SDK JAR gwt-servlet.jar is missing in the WEB-INF/lib directory	lib	/BooksPicker 2.0/war/WEB-INF	Unknown	Google Web Toolkit Problem

    To remove that error, you must "trigger" a copying of the jar into the WEB-INF/lib dir. An easy
    way to do this is to go to Eclipse -> Preferences -> Google. Look at the settings, and click
    Apply. No need to change any of them. After exiting the Preferences dialog, Eclipse should trigger
    some GWT SDK command that auto-copies the desired jar into the right directory. Horrible hacky
    solution? Yes. But it's the easiest workaround I've found so far (if you want, search on google
    for a solution... they all suck :-/  ).
5. You're done.

To restart the tomcat server on rackspace:
$ ssh -p 226 <username>@bookspicker.com
$ cd /usr/local/tomcat
$ sudo ./bin/startup.sh
 
