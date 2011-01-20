
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
public class ${classname} extends Model {

    @Transient
    public transient static DataHelper jpa = DataHelper.forType(${classname}.class);

    @Id @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;

    public ${classname}() {
    }

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    @Override
    public String toString() {
        return "Person{" + "id=" + id + " }";
    }
}
