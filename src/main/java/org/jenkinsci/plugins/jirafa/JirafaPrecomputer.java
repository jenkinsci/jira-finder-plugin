package org.jenkinsci.plugins.jirafa;

import com.atlassian.jira.rest.client.api.domain.Issue;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Descriptor;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.junit.*;
import org.jenkinsci.plugins.jirafa.controller.JirafaTestData;
import org.jenkinsci.plugins.jirafa.entity.FoundIssue;
import org.jenkinsci.plugins.jirafa.entity.Test;
import org.jenkinsci.plugins.jirafa.service.FoundIssueService;
import org.jenkinsci.plugins.jirafa.service.JiraFinderService;
import org.jenkinsci.plugins.jirafa.service.to.SearchCriteria;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Post-build step that goes through the test results and pre-searches the issues and stores the results into database,
 * so it doesn't have to be computed when viewing the report.
 *
 * @author Jiri Holusa (jholusa@redhat.com)
 */
public class JirafaPrecomputer extends TestDataPublisher {

    private String jiraUrl;
    private String filter;
    private String username;
    private String password;

    // we want to aggregate the results during the matrix jobs, hence we need to determine, if the test data contribution
    // is done withing just a one configuration, or if it's a new build.
    private static Integer lastBuildNumber = -1;
    private volatile static Set<String> failedTestsToProcess;

    @Override
    public TestResultAction.Data contributeTestData(Run<?, ?> run, @Nonnull FilePath workspace, Launcher launcher, TaskListener listener, final TestResult testResult) throws IOException, InterruptedException {
        synchronized (lastBuildNumber) {
            if (lastBuildNumber != run.getNumber()) {
                failedTestsToProcess = new HashSet<String>();
                lastBuildNumber = run.getNumber();
            }
        }

        FoundIssueService foundIssueService = new FoundIssueService();
        JiraFinderService jiraFinderService = new JiraFinderService();
        jiraFinderService.setJiraUrl(jiraUrl);
        jiraFinderService.setFilter(filter);
        jiraFinderService.setAuthUsername(username);
        jiraFinderService.setAuthPassword(password);

        jiraFinderService.connect();

        for (CaseResult caseResult: testResult.getFailedTests()) {
            synchronized (failedTestsToProcess) {
                if (!failedTestsToProcess.contains(caseResult.getFullDisplayName())) {
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

                    failedTestsToProcess.add(caseResult.getFullDisplayName());
                }
            }
        }

        jiraFinderService.close();
        return new JirafaTestData(jiraUrl, filter, username, password);
    }

    @DataBoundConstructor
    public JirafaPrecomputer(String jiraUrl, String filter, String username, String password) {
        this.jiraUrl = jiraUrl;
        this.filter = filter;
        this.username = username;
        this.password = password;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<TestDataPublisher> {

        static {
            System.setProperty("hibernate.dialect", "com.enigmabridge.hibernate.dialect.SQLiteDialect");
        }

        @Override
        public String getDisplayName() {
            return "Jirafa extension";
        }
    }

    private static final TestResultAction.Data EMPTY_CONTRIBUTION = new TestResultAction.Data() {
        @Override
        public List<? extends TestAction> getTestAction(TestObject testObject) {
            return Collections.emptyList();
        }
    };

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public String getFilter() {
        return filter;
    }

    public String getJiraUrl() {
        return jiraUrl;
    }
}
