package com.bookspicker.server.data;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;

import com.bookspicker.shared.Book;
import com.bookspicker.shared.ClassBook;
import com.bookspicker.shared.School;
import com.bookspicker.shared.SchoolClass;
import com.bookspicker.shared.Term;

public class ClassManager {

    private static final ClassManager MANAGER = new ClassManager();

    private ClassManager() {}

    public static ClassManager getManager() {
        return MANAGER;
    }

    @SuppressWarnings("unchecked")
    public List<SchoolClass> listClasses() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        List<SchoolClass> result = session.createQuery("from SchoolClass").list();
        System.out.println("listClasses is being called.");
        session.getTransaction().commit();
        session.close();
        return result;
    }

    @SuppressWarnings("unchecked")
    public List<SchoolClass> listClasses(Term term) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        List<SchoolClass> allClasses = session.createQuery("from SchoolClass").list();
        session.getTransaction().commit();
        session.close();

        List<SchoolClass> termClasses = new ArrayList<SchoolClass>();
        for (SchoolClass sc : allClasses) {
            if (sc.getTerm().equals(term))
                termClasses.add(sc);
        }

        return termClasses;
    }

    public void updateClass(Long classId, Long[] bookIds) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();

        SchoolClass theClass = (SchoolClass) session.load(SchoolClass.class, classId);
        for (int i = 0; i < bookIds.length; i++) {
            ClassBook book = (ClassBook) session.load(Book.class, bookIds[i]);
            //theClass.addBook(book);
        }
        session.update(theClass);
        session.getTransaction().commit();
        session.close();
    }

    public SchoolClass updateClass(SchoolClass clas) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        session.update(clas);
        session.getTransaction().commit();
        session.close();
        return clas;
    }

    public SchoolClass updateClass(SchoolClass clas, Session session) {
        session.beginTransaction();
        session.update(clas);
        session.getTransaction().commit();
        session.close();
        return clas;
    }

    public SchoolClass save(SchoolClass c) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        session.save(c);
        session.getTransaction().commit();
        session.close();

        return c;
    }

    public SchoolClass getClassByCode(School school, Term term, String classCode) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        List classes = session.createQuery("from SchoolClass where code='" + classCode + "'").list();
        session.getTransaction().commit();
        //        session.close();
        for (Object c : classes) {
            SchoolClass aClass = (SchoolClass) c;
            if (!aClass.getTerm().equals(term) || !aClass.getSchool().equals(school))
                continue;
            //System.out.println("Class: " + aClass.getTitle());
            if (aClass.getCode().equals(classCode)) {

                replaceBookListImpl(aClass);
                // session.close();
                return aClass;
            }
        }
        session.close();
        return null;
    }
    
    public SchoolClass getClassByCode(School school, Term term, String classCode, Session session) {
        session.beginTransaction();
        List classes = session.createQuery("from SchoolClass where code='" + classCode + "'").list();
        session.getTransaction().commit();
        for (Object c : classes) {
            SchoolClass aClass = (SchoolClass) c;
            if (!aClass.getTerm().equals(term) || !aClass.getSchool().equals(school))
                continue;
            //System.out.println("Class: " + aClass.getTitle());
            if (aClass.getCode().equals(classCode)) {

                replaceBookListImpl(aClass);
                return aClass;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public SchoolClass getClassByParts(School school, Term term,
            String coursePart, String classPart, String sectionPart) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        List<SchoolClass> candidates = session.createQuery("from SchoolClass where course='" + coursePart + "' and clas='" + classPart + "' and section='" + sectionPart + "'").list();
        session.getTransaction().commit();
        //        session.close();

        // Filter out classes not matching school/term
        // This is kind of a hack because I don't know how to match enums in the
        // hibernate query. Maybe we can use the enum's ordinal?
        List<SchoolClass> matchingCandidates = new ArrayList<SchoolClass>();
        for (SchoolClass aClass : candidates) {
            if (aClass.getTerm().equals(term) && aClass.getSchool().equals(school))
                matchingCandidates.add(aClass);
        }

        // We should be left with only 1 matching class
        if (matchingCandidates.size() >= 1) {
            if (matchingCandidates.size() > 1) {
                // TODO change this to a log
                System.err.println("More than one match found for a class; picking first match");
            }
            SchoolClass clas = matchingCandidates.get(0);
            replaceBookListImpl(clas);
            session.close();
            return clas;
        } else {
            // No match found
            session.close();
            return null;
        }
    }

    /**
     * Hibernate returns a SchoolClass whose List of ClassBooks is
     * a PersistentList or something like that. Unfortunately, that
     * list implementation is not serializable, so here we replace it
     * with a regular ArrayList so that it can be transmitted to the
     * client.
     * 
     * This is kind of a nasty hack that we should fix. TODO
     * 
     * @modifies the given SchoolClass
     */
    private void replaceBookListImpl(SchoolClass clas) {
        List<ClassBook> books = new ArrayList<ClassBook>();
        for (ClassBook cb : clas.getBooks())
            books.add(cb);
                clas.setBooks(books);
    }

    public SchoolClass getClassById(String id) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        List classes = session.createQuery("from SchoolClass where id='" + id + "'").list();
        session.getTransaction().commit();
        //        session.close();
        for (Object c : classes) {
            SchoolClass aClass = (SchoolClass) c;
            //System.out.println("Class: " + aClass.getTitle());
            //TOOD(rodrigo - from Jonathan) - do we really need this check?
            if (aClass.getId().equals(Long.valueOf(id))) {
                replaceBookListImpl(aClass);
                session.close();
                return aClass;
            }
        }
        session.close();
        return null;
    }

}
