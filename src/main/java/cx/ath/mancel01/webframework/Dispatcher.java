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
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author mathieuancelin
 */
public class Dispatcher {

    private static final String DEFAUTL_CONTENT_TYPE = "text/html";
    private final InjectorImpl injector;
    private final WebBinder configBinder;
    private final TemplateRenderer renderer;
    private final FileGrabber grabber;
    private Map<String, Class> controllers;
    private Class rootController;
    private boolean started = false;
    private final String contextRoot;

    public Dispatcher(Class<? extends Binder> binderClass, String contextRoot, FileGrabber grabber) {
        controllers = new HashMap<String, Class>();
        renderer = new TemplateRenderer();
        this.contextRoot = contextRoot;
        this.grabber = grabber;
        try {
            this.configBinder = (WebBinder) binderClass.newInstance();
            this.configBinder.setDispatcher(this);
            this.injector = DependencyShot.getInjector(configBinder);
        } catch (Exception e) {
            throw new RuntimeException("Error at injector creation", e);
        }
        this.injector.allowCircularDependencies(true);
        this.injector.registerShutdownHook();
    }

    public void validate() {
        if (rootController == null) {
            throw new RuntimeException("You need to register a root controler");
        } 
    }

    public void start() {
        this.started = true;
    }

    public void stop() {
        this.started = false;
        this.injector.triggerLifecycleDestroyCallbacks();
    }

    public void setRootController(Class rootController) {
        if (rootController.isAnnotationPresent(Controller.class)) {
            this.rootController = rootController;
            registrerController(rootController);
        } else {
            throw new RuntimeException("You can't register a controller without @Controller annotation");
        }
    }

    public synchronized void registrerController(Class<?> clazz) {
        if (clazz.isAnnotationPresent(Controller.class)) {

            // TODO : find JAX-RS annotations
            
            Controller controller = clazz.getAnnotation(Controller.class);
            String name = controller.value();
            if (name.contains("/")) {
                throw new RuntimeException("You can't use / in controller name");
            }
            if ("".equals(name)) {
                name = clazz.getSimpleName().toLowerCase();
            }
            this.controllers.put(name, clazz);
        } else {
            throw new RuntimeException("You can't register a controller without @Controller annotation");
        }
    }

    public Response process(Request request) throws Exception {
        if (started) {
            Response res = new Response();
            String path = request.path;
            if ("".endsWith(contextRoot)) {
                throw new RuntimeException("Can't have an empty context root");
            }
            if (!"/".equals(contextRoot)) {
                path = path.replace(contextRoot, "");
            }
            String[] tokens = path.split("/");
            // if no corresponding @Path on controller, try to find it hte old way
            if (tokens.length >= 2) {
                String firstToken = tokens[1];
                String secondToken = "index";
                if (tokens.length >= 3) {
                    secondToken = tokens[2];
                }
                if (controllers.containsKey(firstToken)) {
                    res = render(controllers.get(firstToken), secondToken);
                } else {
                    throw new RuntimeException("Controller " + firstToken + " does not exist.");
                    // TODO : return 404
                }
            } else {
                if (rootController != null) {
                    res = render(rootController, "index");
                } else {
                    throw new RuntimeException("You need to register a root controler");
                }
            }
            return res;
        } else {
            throw new RuntimeException("Framework not started ...");
        }
    }

    private Response render(Class controllerClass, String methodName) throws Exception {
        long start = System.currentTimeMillis();
        Object controller = injector.getInstance(controllerClass);
        System.out.println("controller injection : " + (System.currentTimeMillis() - start) + " ms.");
        start = System.currentTimeMillis();

        // TODO : find methods with param if querystring not empty
        Method method = controller.getClass().getMethod(methodName);
        // TODO : if no param method, send on default
        RenderView view = (RenderView) method.invoke(controller);

        System.out.println("controller method invocation : " + (System.currentTimeMillis() - start) + " ms.");
        start = System.currentTimeMillis();
        Response res = new Response();
        res.contentType = DEFAUTL_CONTENT_TYPE;
        res.out = new ByteArrayOutputStream();
        renderer.render(grabber.getFile(view.getViewName()), view.getContext(), res.out);
        System.out.println("template view rendering : " + (System.currentTimeMillis() - start) + " ms.");
        start = System.currentTimeMillis();
        return res;
    }
}
