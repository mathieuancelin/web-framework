
package app.model;

import cx.ath.mancel01.webframework.data.DataHelper;
import cx.ath.mancel01.webframework.data.Model;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@XmlRootElement
public class Task extends Model<Task> {

    @Transient
    public transient static DataHelper<Task> jpa = DataHelper.forType(Task.class);

    @Id @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private String title;
    private Boolean done;

    public Task() {
    }

    public Task(String title) {
        this.title = title;
    }

    public Boolean getDone() {
        return done;
    }

    public void setDone(Boolean done) {
        this.done = done;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}

