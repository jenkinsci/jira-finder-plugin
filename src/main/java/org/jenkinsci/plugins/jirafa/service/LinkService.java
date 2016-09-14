package org.jenkinsci.plugins.jirafa.service;

import com.google.inject.Inject;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.database.jpa.PersistenceService;
import org.jenkinsci.plugins.jirafa.dao.LinkDao;
import org.jenkinsci.plugins.jirafa.entity.Link;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Service layer encapsulating all accesses to database. Links are stored on per-job databases.
 *
 * @author Jiri Holusa (jholusa@redhat.com)
 */
public class LinkService {

    @Inject
    private PersistenceService ps;

    private static EntityManager em;
    private static EntityTransaction et;

    private LinkDao linkDao;

    public LinkService(String jobName) {
        Jenkins.getInstance().getInjector().injectMembers(this);

        if (em == null) {
            try {
                em = ps.getPerItemEntityManagerFactory(Jenkins.getInstance().getItemMap().get(jobName)).createEntityManager();
                et = em.getTransaction();
            } catch (SQLException ex) {
                throw new RuntimeException("Error occurred while creating connection to database.", ex);
            } catch (IOException ex) {
                throw new RuntimeException("Error occurred while creating connection to database.", ex);
            }
        }

        linkDao = new LinkDao(em);
    }

    public void linkIssueToTest(String issue, String summary, String test, String stacktrace) {
        Link link = new Link();
        link.setIssue(issue);
        link.setTest(test);
        link.setSummary(summary);
        link.setStacktrace(stacktrace);

        et.begin();
        linkDao.create(link);
        et.commit();
    }

    public List<Link> findIssuesByTestAndStacktrace(String test, String stacktrace) {
        return linkDao.findByTestAndStacktrace(test, stacktrace);
    }

    public Link get(Long id) {
        return linkDao.get(id);
    }

    public void delete(Long id) {
        Link link = get(id);
        et.begin();
        linkDao.delete(link);
        et.commit();
    }

}
