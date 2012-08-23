Last updated: 8/15/10

Follow these instructions before you deploy! Read this EVERY
TIME YOU DEPLOY since it's VERY EASY to forget things!!!

0. Before you start, make sure you have the following things:
   0.1. A checked out version of BooksPicker 2.0 in Eclipse.
   0.2. A checked out version of BP-Release in Eclipse.
   0.3. A checked out version of BP-Release in the slice (in
        your account home). To do so, ssh to the slice and run
        'svn co file:///home/bp/repo/bookspicker-2.0' in a
        folder of your choosing. That will check out both
        BooksPicker 2.0 and BP-Release.
        
Now you can deploy:

1. If your code changes involved changes to the database, make sure
   the production database (bp-prod) has the correct structure!
   If it does not, you can copy over the structure from bp-test
   or bp-scrap (see README_DB_INFO.txt), but be VERY CAREFUL!
2. Make sure HibernateUtil has testDb, liveTest, and wipeData set to FALSE.
   Otherwise the deployed code will communicate with a test database
   instead of the production database!
3. Make sure that LoginHandler.FORCE_FB_TEST_CALL=false and LIVE_TEST=false.
   Questions? Ask Rodrigo.
4. Make sure SuggestionServiceImpl.REFRESH_FROM_DB = false;
5. Compile 'bookspicker' module (NOT bookspicker_dev). Those two modules
   are essentially the same, except 'bookspicker' compiles for ALL
   browsers, while 'bookspicker_dev' only compiles for gecko (to save
   time when developing).
6. Copy the contents on the 'war' folder from the BooksPicker 2.0
   project to BP-Release. DO NOT COPY .svn FOLDERS!! Since both projects
   are under version control, copying those (hidden) folders over will
   screw up SVN on BP-Release. I (Rodrigo) have a BAT script (in
   Windows) that does this for me, so it's pretty fast. I recommend
   you do the same, both for speed and to minimize human error.
7. Refresh the Package Explorer view on Eclipse and commit on BP-Release.
8. SSH into slice, go to your checked out copy of BP-Release and
   run bp_deploy.sh. The script requires you to have root access. We
   all do, just remember your password!
   
You're done! Test that everything is in order and fall back if something
is not. The script has instructions on what to do!