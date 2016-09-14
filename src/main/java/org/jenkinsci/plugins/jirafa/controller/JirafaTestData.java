package org.jenkinsci.plugins.jirafa.controller;

import hudson.tasks.junit.CaseResult;
import hudson.tasks.junit.TestAction;
import hudson.tasks.junit.TestObject;
import hudson.tasks.junit.TestResultAction;

import java.util.Collections;
import java.util.List;

/**
 * Test data for Jirafa test action.
 *
 * @author Jiri Holusa (jholusa@redhat.com)
 */
public class JirafaTestData extends TestResultAction.Data {

    private String jiraUrl;
    private String filter;
    private String username;
    private String password;

    public JirafaTestData(String jiraUrl, String filter, String username, String password) {
        this.filter = filter;
        this.jiraUrl = jiraUrl;
        this.password = password;
        this.username = username;
    }

    @Override
    public List<? extends TestAction> getTestAction(TestObject testObject) {
        if (testObject instanceof CaseResult) {
            CaseResult caseResult = (CaseResult) testObject;
            JirafaTestAction jirafaTestAction = new JirafaTestAction(caseResult);
            jirafaTestAction.setJiraUrl(jiraUrl);
            jirafaTestAction.setFilter(filter);
            jirafaTestAction.setUsername(username);;
            jirafaTestAction.setPassword(password);
            return Collections.singletonList(jirafaTestAction);
        }

        return Collections.emptyList();
    }
}
