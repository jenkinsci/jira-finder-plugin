package org.jenkinsci.plugins.jirafa.entity;

import org.jenkinsci.plugins.database.jpa.GlobalTable;
import org.jenkinsci.plugins.database.jpa.PerItemTable;

import javax.persistence.*;
import java.util.List;

/**
 * Entity representing issue in JIRA. Stored in global database.
 *
 * @author Jiri Holusa (jholusa@redhat.com)
 */
@GlobalTable
@NamedQueries({
        @NamedQuery(name = FoundIssue.GET_BY_KEY, query = "SELECT foundIssue FROM FoundIssue foundIssue WHERE foundIssue.key = :key"),
        @NamedQuery(name = FoundIssue.GET_ALL, query = "SELECT foundIssue FROM FoundIssue foundIssue")
})
@javax.persistence.Entity
public class FoundIssue implements Entity {

    public static final String GET_ALL = "FoundIssue.getAll";
    public static final String GET_BY_KEY = "FoundIssue.getByKey";

    @Id
    @SequenceGenerator(name = "FOUND_ISSUE_ID_GENERATOR", sequenceName = "FOUND_ISSUE_SEQUENCE", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "FOUND_ISSUE_ID_GENERATOR")
    private Long id;

    @Column
    private String key;

    @Column
    private String summary;

    @Column
    private String description;

    @ManyToMany(mappedBy = "foundIssues")
    private List<Test> tests;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public List<Test> getTests() {
        return tests;
    }

    public void setTests(List<Test> tests) {
        this.tests = tests;
    }

    @Override
    public String toString() {
        return "FoundIssue{" +
                "id=" + id +
                ", key='" + key + '\'' +
                ", summary='" + summary + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FoundIssue that = (FoundIssue) o;

        return key != null ? key.equals(that.key) : that.key == null;
    }

    @Override
    public int hashCode() {
        return key != null ? key.hashCode() : 0;
    }

}
