package org.jenkinsci.plugins.jirafa.service;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClientFactory;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import org.jenkinsci.plugins.jirafa.service.to.SearchCriteria;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * This class performs search of the issues in configured JIRA.
 *
 * @author Jiri Holusa (jholusa@redhat.com)
 */
public class JiraFinderService {

    private JiraRestClient client;

    private String jiraUrl;
    private String filter;
    private String authUsername;
    private String authPassword;
    private int maxResults = 5;

    private static final Set<String> SEARCH_FIELDS = new HashSet<String>(
            Arrays.asList( //first 5 are mandatory by definition of JIRA REST client
                    "summary",
                    "issuetype",
                    "created",
                    "updated",
                    "project",
                    "status",
                    "key",
                    "description"
                    ));

    public List<Issue> search(SearchCriteria searchCriteria) {
        String fullName = searchCriteria.getPackageName() + "." + searchCriteria.getTestName() + "." + searchCriteria.getMethodName();
        String testName = searchCriteria.getTestName();
        String testWithMethodName = searchCriteria.getTestName() + "." + searchCriteria.getMethodName();

        String query = "(" + filter + ") AND (text ~ \"" +
                fullName + "\" OR text ~ \"" +
                testName + "\" OR text ~ \"" +
                testWithMethodName + "\")";

        SearchResult result = client.getSearchClient()
                    .searchJql(query, maxResults, 0, SEARCH_FIELDS)
                    .claim();

        List<Issue> issues = new ArrayList<Issue>();
        for (Issue issue: result.getIssues()) {
            issues.add(issue);
        }
        return issues;
    }

    public void connect() {
        if (client == null) {
            JiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();

            URI jiraServerUri;
            try {
                jiraServerUri = new URI(jiraUrl);
            } catch (URISyntaxException ex) {
                throw new IllegalArgumentException("Invalid URI syntax of JIRA server.", ex);
            }

            client = factory.createWithBasicHttpAuthentication(jiraServerUri, authUsername, authPassword);
        }
    }

    public void close() {
        if (client != null) {
            try {
                client.close();
            } catch (IOException ex) {
                throw new RuntimeException("Error occurred while closing the JIRA client.", ex);
            } finally {
                client = null;
            }
        }
    }

    public void setAuthPassword(String authPassword) {
        this.authPassword = authPassword;
    }

    public void setAuthUsername(String authUsername) {
        this.authUsername = authUsername;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public void setJiraUrl(String jiraUrl) {
        this.jiraUrl = jiraUrl;
    }

    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }
}
