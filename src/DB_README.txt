This readMe has two parts:
1. Running BP locally.
2. Updating the DB on production
 


1. Running BP locally:
----------------------

1. You will need to install mysql (on Mac use macports and install mysql5-server-devel, on PC I don't give a crap)
2. Setup the root password and create the bp user with the password "<password>".
3. Load bp_production to your local db:
	a. go into mysql : mysql -u root
	b. create the db: create database bp_production
	c. provide access to bp:
		1.  grant usage on *.* to bp@localhost identified by '<password>';
		2. grant all privileges on bp_production.* to bp@localhost ; 
	d. go back to the command line and load the dump file:  mysql -u root --password="password" bp_production < db/bp_production.sql
	e. Test your db setting: mysql -u bp -p'<password>' -D bp_production

** if you want to load a new dump drop the database and load it from scratch...


 If it works you are all set.
 
 
 2. Updating the DB on production
 ---------------------------------
1. Every schema change should be written into db/uprade/file.sql
2. Then the update should be commited and loaded on the QA_DB in production and if everything is good it should be loaded to produciton.
3. If we need to redump the production db, we can do it on slice under /home/bp/bp_recent/Bookspicker-2.0/db/
	 run the following command: sudo  mysqldump -u bp --password=<password> bp_production > bp_production.sql  
4. commit via svn
