package org.jenkinsci.plugins.jirafa.controller;

/**
 * Transfer object between controllers ({@link JirafaTestAction}) and views.
 *
 * @author Jiri Holusa (jholusa@redhat.com)
 */
public class IssueTO {

    private String key;
    private String summary;
    private String description;

    public IssueTO(String key, String summary, String description) {
        this.description = description;
        this.key = key;
        this.summary = summary;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}
