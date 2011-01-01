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

import cx.ath.mancel01.webframework.view.TemplateRenderer;
import cx.ath.mancel01.webframework.integration.dependencyshot.WebBinder;
import cx.ath.mancel01.webframework.exception.BreakFlowException;
import cx.ath.mancel01.dependencyshot.DependencyShot;
import cx.ath.mancel01.dependencyshot.graph.Binder;
import cx.ath.mancel01.dependencyshot.graph.Binding;
import cx.ath.mancel01.dependencyshot.injection.InjectorImpl;
import cx.ath.mancel01.webframework.annotation.Controller;
import cx.ath.mancel01.webframework.compiler.CompilationException;
import cx.ath.mancel01.webframework.compiler.RequestCompiler;
import cx.ath.mancel01.webframework.compiler.WebFrameworkClassLoader;
import cx.ath.mancel01.webframework.http.Request;
import cx.ath.mancel01.webframework.http.Response;
import cx.ath.mancel01.webframework.integration.dependencyshot.DependencyShotIntegrator;
import cx.ath.mancel01.webframework.util.FileUtils.FileGrabber;
import cx.ath.mancel01.webframework.view.HtmlPage;
import cx.ath.mancel01.webframework.view.Render;
import cx.ath.mancel01.webframework.view.Renderable;
import cx.ath.mancel01.webframework.view.View;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author mathieuancelin
 */
public class FrameworkHandler {

    // see: http://wikis.sun.com/display/Jersey/Overview+of+JAX-RS+1.0+Features

    private static final String DEFAUTL_CONTENT_TYPE = "text/html";
    private InjectorImpl injector;
    private WebBinder configBinder;
    private final TemplateRenderer renderer;
    private final FileGrabber grabber;
    private Map<String, Class> controllers;
    private Class rootController;
    private boolean started = false;
    private final String contextRoot;
    private final File base;
    private Class<? extends Binder> binderClass;
    private String binderClassName;

    public FrameworkHandler(String binderClassName, String contextRoot, FileGrabber grabber) {
        this.binderClassName = binderClassName;
        controllers = new HashMap<String, Class>();
        renderer = new TemplateRenderer();
        if ("".equals(contextRoot)) {
            throw new RuntimeException("Can't have an empty context root");
        }
        this.contextRoot = contextRoot;
        this.grabber = grabber;
        WebFramework.init();
        try {
            if (!WebFramework.dev) {
                this.binderClass =
                    (Class<? extends Binder>)
                        Class.forName(binderClassName);
            } else {
                this.binderClass =
                    (Class<? extends Binder>)
                        new WebFrameworkClassLoader(getClass().getClassLoader())
                            .loadClass(binderClassName);
            }
            this.configBinder = (WebBinder) this.binderClass.newInstance();
            this.configBinder.setDispatcher(this);
            this.injector = DependencyShot.getInjector(configBinder);
            configureInjector(this.injector);
        } catch (Exception e) {
            throw new RuntimeException("Error at injector creation", e);
        }
        this.base = grabber.getFile("public");
    }

    private void configureInjector(InjectorImpl inj) {
        inj.allowCircularDependencies(true);
        inj.registerShutdownHook();
        new DependencyShotIntegrator(inj).registerBindings();
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

    public Response process(Request request) {
        try {
            if (started) {
                WebFramework.logger.trace("asked resource => {}", request.path);
                Response res = new Response();
                String path = request.path;
                if (!"/".equals(contextRoot)) {
                    path = path.replace(contextRoot, "");
                }
                if (path.endsWith("favicon.ico")) {
                    path = "/public/img/favicon.ico";
                }
                if (path.startsWith("/public/")) {
                    File asked = new File(base, path.replace("/public/", ""));
                    res.direct = asked;
                    res.out = new ByteArrayOutputStream();
                    return res;
                }
                String[] tokens = path.split("/");
                // TODO find controller the JAX-RS way
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
                        WebFramework.logger.error("Controller {} does not exist.", firstToken);
                        return Render.notFound().render();
                    }
                } else {
                    if (rootController != null) {
                        res = render(rootController, "index");
                    } else {
                        return new HtmlPage("Error",
                                "<h1>You need to register a root controller</h1>")
                                .render();
                    }
                }
                return res;
            } else {
                throw new RuntimeException("Framework not started ...");
            }
        } catch (Throwable t) {
            final Throwable ex = t;
            return new HtmlPage("Error"
                    , "<h1>Ooops, can't render an object of type : "
                    + ex.getMessage() + "</h1>").render();
        }
    }

    private Response render(Class controllerClass, String methodName) throws Exception {
        Binding devControllerBinding = null;
        InjectorImpl devInjector = null;
        if (WebFramework.dev) {
            try {
                Class devBinderClass = new WebFrameworkClassLoader().loadClass(binderClassName);
                WebBinder devBinder = (WebBinder) devBinderClass.newInstance();
                devBinder.setDispatcher(this);
                devInjector = DependencyShot.getInjector(devBinder);
                controllerClass = new WebFrameworkClassLoader().loadClass(controllerClass.getName());
            } catch (Throwable ex) {
                return createErrorResponse(ex);
            }
            devControllerBinding = new Binding(null, null, controllerClass, controllerClass, null, null);
        }
        long start = System.currentTimeMillis();
        Object controller = null;
        if (WebFramework.dev) {
            try {
                controller = devControllerBinding.getInstance(devInjector, null);
                //controller = controllerBinding.getInstance(injector, null);
            } catch (Throwable ex) {
                return createErrorResponse(ex);
            }
        } else {
            controller = injector.getInstance(controllerClass);
        }
        WebFramework.logger.trace("controller injection : {} ms."
                , (System.currentTimeMillis() - start));
        start = System.currentTimeMillis();
        // TODO : find methods with param if querystring not empty
        Method method = controller.getClass().getMethod(methodName);
        // TODO : if no param method, send on default
        Object ret = null;
        try {
            ret = method.invoke(controller);
        } catch (InvocationTargetException ex) {
            if (ex.getCause() instanceof BreakFlowException) {
                BreakFlowException br = (BreakFlowException) ex.getCause();
                ret = br.getRenderable();
            } else {
                throw ex;
            }
        }
        WebFramework.logger.trace("controller method invocation : {} ms."
                , (System.currentTimeMillis() - start));
        if (ret instanceof Renderable) {
            Renderable renderable = (Renderable) ret;
            if (renderable instanceof View) { // ok that's not really OO but what the hell !
                return ((View) renderable).render(methodName, controllerClass, grabber);
            }
            return renderable.render();
        } else {
            return new HtmlPage("Can't render", "<h1>Ooops, can't render an object of type : "
                    + ret.getClass().getName() + "</h1>").render();
        }
    }

    private Response createErrorResponse(Throwable t) {
        Throwable original = t;
        Throwable cause = null;
        while (cause == null) {
            t = t.getCause();
            if (t == null) {
                break;
            }
            if (t instanceof CompilationException) {
                cause = t;
            }
        }
        if (cause == null) { // TODO : error page with stacktrace
            return new HtmlPage("Error"
                , "<h1>Error :</h1><br/>"
                + original.getMessage().replace("\n", "<br/>")).render();
        }
        return new HtmlPage("Compilation error"
                , "<h1>Compilation error :</h1><br/>"
                + cause.getMessage().replace("\n", "<br/>")).render();
    }
}
