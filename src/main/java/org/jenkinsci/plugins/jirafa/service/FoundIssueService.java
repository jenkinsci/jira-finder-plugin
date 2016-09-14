package org.jenkinsci.plugins.jirafa.service;

import com.google.inject.Inject;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.database.jpa.PersistenceService;
import org.jenkinsci.plugins.jirafa.dao.FoundIssueDao;
import org.jenkinsci.plugins.jirafa.dao.LinkDao;
import org.jenkinsci.plugins.jirafa.dao.TestDao;
import org.jenkinsci.plugins.jirafa.entity.Entity;
import org.jenkinsci.plugins.jirafa.entity.FoundIssue;
import org.jenkinsci.plugins.jirafa.entity.Link;
import org.jenkinsci.plugins.jirafa.entity.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Service layer encapsulating all accesses to database. Found issues are stored in global database.
 *
 * @author Jiri Holusa (jholusa@redhat.com)
 */
public class FoundIssueService {

    @Inject
    private PersistenceService ps;

    private static EntityManager em;
    private static EntityTransaction et;

    private FoundIssueDao foundIssueDao;
    private TestDao testDao;

    public FoundIssueService() {
        Jenkins.getInstance().getInjector().injectMembers(this);

        synchronized (EntityManager.class) { // we need to ensure that entity managers are created sequentially because of matrix jobs
            if (em == null) {
                try {
                    em = ps.getGlobalEntityManagerFactory().createEntityManager();
                    et = em.getTransaction();
                } catch (SQLException ex) {
                    throw new RuntimeException("Error occurred while creating connection to database.", ex);
                } catch (IOException ex) {
                    throw new RuntimeException("Error occurred while creating connection to database.", ex);
                }
            }
        }

        foundIssueDao = new FoundIssueDao(em);
        testDao = new TestDao(em);
    }

    public Test getTest(String name) {
        return testDao.getByName(name);
    }

    public List<FoundIssue> getFoundIssuesForTest(String testName) {
        Test test = testDao.getByName(testName);
        List<FoundIssue> foundIssues = test.getFoundIssues();
        return foundIssues;
    }

    public void saveFoundIssuesForTest(Test test) {
        Test freshTest = testDao.getByName(test.getName());
        if (freshTest == null) {
            freshTest = new Test();
            freshTest.setName(test.getName());
            et.begin();
            freshTest = testDao.create(freshTest);
            et.commit();
        }

        List<FoundIssue> newFoundIssues = new ArrayList<FoundIssue>();
        for (FoundIssue issue: test.getFoundIssues()) {
            FoundIssue freshIssue = foundIssueDao.getByKey(issue.getKey());
            if (freshIssue == null) {
                freshIssue = new FoundIssue();
                freshIssue.setKey(issue.getKey());
                freshIssue.setSummary(issue.getSummary());
                freshIssue.setDescription(issue.getDescription());
                et.begin();
                freshIssue = foundIssueDao.create(freshIssue);
                et.commit();
            }

            List<Test> tests = freshIssue.getTests();
            if (tests == null) {
                tests = new ArrayList<Test>();
            }
            tests.add(freshTest);
            freshIssue.setTests(tests);
            et.begin();
            freshIssue = foundIssueDao.update(freshIssue);
            et.commit();
            newFoundIssues.add(freshIssue);
        }

        List<FoundIssue> foundIssues = freshTest.getFoundIssues();
        if (foundIssues == null) {
            foundIssues = new ArrayList<FoundIssue>();
        }

        for (FoundIssue newIssue: newFoundIssues) {
            if (!foundIssues.contains(newIssue)) {
                foundIssues.add(newIssue);
            }
        }
        freshTest.setFoundIssues(foundIssues);

        et.begin();
        testDao.update(freshTest);
        et.commit();
    }

    public List<FoundIssue> getAllFoundIssues() {
        return foundIssueDao.getAll();
    }
}
