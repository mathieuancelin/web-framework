
package app.controller;

import cx.ath.mancel01.webframework.view.View;
import cx.ath.mancel01.webframework.annotation.Controller;
import cx.ath.mancel01.webframework.http.Request;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.Path;

@Controller
public class ${classname} {

    @Inject
    private Request request;

    @Inject
    private EntityManager em;

    public View index() {
        return new View().param("request", request);
    }
}
