package org.jenkinsci.plugins.jirafa.controller;

import com.atlassian.jira.rest.client.api.domain.Issue;
import hudson.matrix.MatrixConfiguration;
import hudson.model.Job;
import hudson.tasks.junit.CaseResult;
import hudson.tasks.junit.TestAction;
import org.jenkinsci.plugins.jirafa.entity.FoundIssue;
import org.jenkinsci.plugins.jirafa.entity.Link;
import org.jenkinsci.plugins.jirafa.entity.Test;
import org.jenkinsci.plugins.jirafa.service.FoundIssueService;
import org.jenkinsci.plugins.jirafa.service.JiraFinderService;
import org.jenkinsci.plugins.jirafa.service.LinkService;
import org.jenkinsci.plugins.jirafa.service.to.SearchCriteria;
import org.kohsuke.stapler.bind.JavaScriptMethod;

import java.util.ArrayList;
import java.util.List;

/**
 * Test action that handles rendering of Jirafa extra information.
 *
 * @author Jiri Holusa (jholusa@redhat.com)
 */
public class JirafaTestAction extends TestAction {

    private CaseResult caseResult;
    private List<Link> linkedIssues;
    private List<FoundIssue> foundIssues;
    private LinkService linkService;
    private FoundIssueService foundIssueService;

    private String jiraUrl;
    private String filter;
    private String username;
    private String password;

    public JirafaTestAction(CaseResult caseResult) {
        this.caseResult = caseResult;

        String jobName;
        Job job = caseResult.getRun().getParent();
        if (job instanceof MatrixConfiguration) {
            jobName = job.getParent().getDisplayName();
        } else {
            jobName = job.getName();
        }

        linkService = new LinkService(jobName);
        foundIssueService = new FoundIssueService();

        linkedIssues = linkService.findIssuesByTestAndStacktrace(caseResult.getFullName(), caseResult.getErrorStackTrace());
        if (linkedIssues == null || linkedIssues.isEmpty()) {
            foundIssues = foundIssueService.getFoundIssuesForTest(caseResult.getFullName());
        }
    }

    @JavaScriptMethod
    public void searchIssues() {
        JiraFinderService jiraFinderService = new JiraFinderService();
        jiraFinderService.setJiraUrl(jiraUrl);
        jiraFinderService.setFilter(filter);
        jiraFinderService.setAuthUsername(username);
        jiraFinderService.setAuthPassword(password);

        jiraFinderService.connect();

        Test test = new Test();
        test.setName(caseResult.getFullDisplayName());

        List<Issue> retrievedIssues = jiraFinderService.search(new SearchCriteria(caseResult.getName(), caseResult.getSimpleName(), caseResult.getPackageName()));
        List<FoundIssue> foundIssues = new ArrayList<FoundIssue>();
        for (Issue issue : retrievedIssues) {
            FoundIssue foundIssue = new FoundIssue();
            foundIssue.setKey(issue.getKey());
            foundIssue.setSummary(issue.getSummary());
            foundIssue.setDescription(issue.getDescription());
            foundIssues.add(foundIssue);
        }
        test.setFoundIssues(foundIssues);
        foundIssueService.saveFoundIssuesForTest(test);

        jiraFinderService.close();
    }

    @JavaScriptMethod
    public List<IssueTO> getFoundIssueTOs() {
        List<IssueTO> issues = new ArrayList<IssueTO>();
        if (foundIssues != null) {
            for (FoundIssue issue : foundIssues) {
                IssueTO newIssue = new IssueTO(issue.getKey(), issue.getSummary(), issue.getDescription());
                issues.add(newIssue);
            }
        }
        return issues;
    }

    @JavaScriptMethod
    public void linkIssues(List<String> issueKeys) {
        for (FoundIssue issue: foundIssues) {
            if (issueKeys.contains(issue.getKey())) {
                linkService.linkIssueToTest(
                        issue.getKey(),
                        issue.getSummary(),
                        caseResult.getFullDisplayName(),
                        caseResult.getErrorStackTrace()
                );
            }
        }
    }

    @JavaScriptMethod
    public void deleteLinks(Long[] ids) {
        for (Long id: ids) {
            linkService.delete(id);
        }
    }

    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public String getUrlName() {
        return null;
    }

    public CaseResult getCaseResult() {
        return caseResult;
    }

    public List<Link> getLinkedIssues() {
        return linkedIssues;
    }

    public List<FoundIssue> getFoundIssues() {
        return foundIssues;
    }

    public String getJiraUrl() {
        return jiraUrl + "browse/";
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public void setJiraUrl(String jiraUrl) {
        this.jiraUrl = jiraUrl;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
