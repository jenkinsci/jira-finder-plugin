package org.jenkinsci.plugins.jirafa.entity;

import org.jenkinsci.plugins.database.jpa.GlobalTable;
import org.jenkinsci.plugins.database.jpa.PerItemTable;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

/**
 * Entity representing test failure.
 *
 * @author Jiri Holusa (jholusa@redhat.com)
 */
@GlobalTable
@NamedQueries({
        @NamedQuery(name = Test.GET_BY_NAME, query = "SELECT test FROM Test test WHERE test.name = :name"),
        @NamedQuery(name = Test.GET_ALL, query = "SELECT test FROM Test test")
})
@javax.persistence.Entity
public class Test implements Entity {

    public static final String GET_ALL = "Test.getAll";
    public static final String GET_BY_NAME = "Test.getByName";

    @Id
    @SequenceGenerator(name = "TEST_ID_GENERATOR", sequenceName = "TEST_SEQUENCE", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "TEST_ID_GENERATOR")
    private Long id;

    @Column
    private String name;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "test_found_issue",
            joinColumns = {@JoinColumn(name = "test_id", nullable = false, updatable = false)},
            inverseJoinColumns = {@JoinColumn(name = "found_issue_id", nullable = false, updatable = false)}
    )
    private List<FoundIssue> foundIssues;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<FoundIssue> getFoundIssues() {
        return foundIssues;
    }

    public void setFoundIssues(List<FoundIssue> foundIssues) {
        this.foundIssues = foundIssues;
    }

    @Override
    public String toString() {
        return "Test{" +
                "foundIssues=" + foundIssues +
                ", id=" + id +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Test test = (Test) o;

        return name != null ? name.equals(test.name) : test.name == null;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
