
package app.controller;

import app.model.Task;
import cx.ath.mancel01.webframework.annotation.Controller;
import cx.ath.mancel01.webframework.view.Render;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

@Controller
public class Todo {

    @Path("/")
    public void index() {
        Render.view("index.html").param("tasks", Task.jpa.all().getResultList()).go();
    }

    @POST
    public void createTask(@FormParam("title") String title) {
        Task task = new Task(title).save();
        Render.json(task).go();
    }

    @POST
    public void change(@FormParam("id") Long id, @FormParam("done") Boolean done) {
        Task task = Task.jpa.findById(id);
        task.setDone(!done);
        Render.json(task.save()).go();
    }
}
