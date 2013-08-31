Steps to do a data scrape for a semester.

0. Add the new term to Term.java, change the current Term to it, set the date on DataUtils.java, and update the term param in MitCatalogScraper. Also create a corresponding folder under data/.

1. Run MitCatalogScraper. Revise that it's working correctly. You should have 4 files after the scrape. TODO(ipince): document scraper.

2. ssh into the server, mysqldump the classes, books, and classbooks tables. scp them over to your local machine.
  ssh -p 226 rodrigo@bookspicker.com
  mysqldump -u bp -p bp_production books classes classbooks > bp_prod_books_classes_classbooks_<date>.sql
  (go back to local shell)
  scp -P 226 rodrigo@bookspicker.com:~/bp_prod_books_classes_classbooks_<date>.sql bp_prod_books_classes_classbooks_<date>.sql
  mysql -u bp -p < bp_prod_books_classes_classbooks_<date>.sql

3. Run MitCatalogImporter. This will populate your local db with the new classes and books.

4. Run BP locally and make sure the db is being accessed correctly.

5. mysqldump the local db's classes, books, and classbooks tables. scp the file back to the server and load them into the db.
