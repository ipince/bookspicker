Last updated: 8/15/10

We currently maintain the following databases:

Production:
reipince+bp-prod (for production)

Testing:
reipince+bp-test (test db, but with real data)
reipince+bp-scrap (sandbox to do whatever you want)

Backups (of production dbs):
reipince+bp-backup-<date> (e.g., "reipince+bp-backup-aug5")


The names are pretty self-explanatory. Now, for each of the
three 'important' databases (prod, test, and scrap), we have
a Hibernate configuration file:

(1) hibernate.cfg.xml for bp-prod
(2) hibernate.test.cfg.xml for bp-test
(3) hibernate.test.wipedata.cfg.xml for bp-scrap

What's the difference?
- (1) and (2) use the C3P0 connection pool library,
which is a production-ready system to handle our database
connections for us. In contrast, (3) uses the built-in
Hibernate connection pool, which is only meant to be used
for testing.
- When using (2) and (3), Hibernate will print the SQL
statements it generates, while if using (1), it will not.
- MOST IMPORTANTLY, when using (3), Hibernate DROPS AND
RE-CREATES the database every time it runs. This is useful
if you're changing Hibernate mappings, because Hibernate
will automatically create the tables with the correct
structures. In contrast, (1) and (2) use the database
as it is. Thus, if the Hibernate mappings are no longer valid
(because you changed them but forgot to change the database),
then Hibernate will fail.

NOTE: if you change Hibernate mappings and the database 
table structure changes (very common), then you must make 
sure that bp-test and bp-prod have the correct structure 
if you plan to run your code using those databases!

So... which should I use?
- If you are NOT playing with Hibernate, just use (2).
Otherwise, use (3) until Hibernate is able to map things
appropriately. After that, copy over the structure to
bp-test and keep developing using (2).
Never use the production database when testing unless you're
ABSOLUTELY sure you won't damage it.