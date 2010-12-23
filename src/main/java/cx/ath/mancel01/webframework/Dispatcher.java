/*
 *  Copyright 2010 mathieuancelin.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package cx.ath.mancel01.webframework;

import cx.ath.mancel01.dependencyshot.DependencyShot;
import cx.ath.mancel01.dependencyshot.graph.Binder;
import cx.ath.mancel01.dependencyshot.injection.InjectorImpl;
import cx.ath.mancel01.webframework.annotation.Controller;
import cx.ath.mancel01.webframework.http.Request;
import cx.ath.mancel01.webframework.http.Response;
import cx.ath.mancel01.webframework.util.FileUtils.FileGrabber;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author mathieuancelin
 */
public class Dispatcher {

    private final InjectorImpl injector;
    private final WebBinder configBinder;
    private final TemplateRenderer renderer;
    private final FileGrabber grabber;

    private Map<String, Class> controllers;

    public Dispatcher(Class<? extends Binder> binderClass, FileGrabber grabber) {
        controllers = new HashMap<String, Class>();
        renderer = new TemplateRenderer();
        this.grabber = grabber;
        try {
            this.configBinder = (WebBinder) binderClass.newInstance();
            this.configBinder.setDispatcher(this);
            this.injector = DependencyShot.getInjector(configBinder);
        } catch (Exception e) {
            throw new RuntimeException("Error at injector creation", e);
        }
    }

    public synchronized void registrerController(Class<?> clazz) {
        if(clazz.isAnnotationPresent(Controller.class)) {
            Controller controller = clazz.getAnnotation(Controller.class);
            String name = controller.value();
            if ("".equals(name)) {
                name = clazz.getSimpleName().substring(0, 1).toLowerCase()
                        + clazz.getSimpleName().substring(1);
            }
            this.controllers.put(name, clazz);
        } else {
            throw new RuntimeException("You can't register a controller without @Controller annotation");
        }
    }

    public Response process(Request context) throws Exception {
        // on regarde l'url et on trouve le controleur
        Object controller = injector.getInstance(controllers.get("myController"));
        Method method = controller.getClass().getMethod("index");
        RenderView view = (RenderView) method.invoke(controller);
        Response res = new Response();
        res.contentType = "text/html";
        res.out = new ByteArrayOutputStream();
        renderer.render(grabber.getFile(view.getViewName()), view.getContext(), res.out);

        // si pas de méthode, on cherche une methode index
        // sinon on cherche méthode via url
        // si ok on appelle et on récup le render
        // on fabrique le template
        // on fabrique la response
        // on retourne


        return res;
    }
}
