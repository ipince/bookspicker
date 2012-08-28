This README has two parts:
1. Running BP locally.
2. Updating the DB on production


1. Running BP locally:
----------------------

1. You will need to install mysql and run mysqld.
2. Setup the root password and create the "bp" user with the password "<password>".
    a. mysql -u root
    b. create user 'bp'@'localhost' identified by '<password>';
3. Load bp_qa to your local db:
    a. go into mysql : mysql -u root
    b. create the db: create database bp_qa
    c. provide access to bp: grant all privileges on bp_qa.* to bp@localhost identified by '<password>';
    d. go back to the command line and load the dump file:  mysql -u root --password="password" bp_qa < db/bp_schema.sql
    e. Test your db setting: mysql -u bp -p'<password>' -D bp_qa

** if you want to load a new dump drop the database and load it from scratch...


If it works you are all set.


TODO(rodrigo): update
2. Updating the DB on production
---------------------------------

1. Every schema change should be written into db/upgrade/file.sql
2. Then the update should be committed and loaded on the QA_DB in production and if everything is good it should be loaded to production.
3. If we need to redump the production db, we can do it on slice under /home/bp/bp_recent/Bookspicker-2.0/db/
	 run the following command: sudo  mysqldump -u bp --password=<password> bp_production > bp_production.sql  
4. commit via svn
