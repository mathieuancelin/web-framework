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

import com.google.gson.Gson;
import cx.ath.mancel01.webframework.view.TemplateRenderer;
import cx.ath.mancel01.webframework.view.View;
import cx.ath.mancel01.webframework.integration.dependencyshot.WebBinder;
import cx.ath.mancel01.webframework.exception.BreakFlowException;
import cx.ath.mancel01.dependencyshot.DependencyShot;
import cx.ath.mancel01.dependencyshot.graph.Binder;
import cx.ath.mancel01.dependencyshot.graph.Binding;
import cx.ath.mancel01.dependencyshot.injection.InjectorImpl;
import cx.ath.mancel01.webframework.annotation.Controller;
import cx.ath.mancel01.webframework.compiler.CompilationException;
import cx.ath.mancel01.webframework.compiler.WebFrameworkClassLoader;
import cx.ath.mancel01.webframework.http.Request;
import cx.ath.mancel01.webframework.http.Response;
import cx.ath.mancel01.webframework.integration.dependencyshot.DependencyShotIntegrator;
import cx.ath.mancel01.webframework.util.FileUtils.FileGrabber;
import cx.ath.mancel01.webframework.view.Binary;
import cx.ath.mancel01.webframework.view.JSON;
import cx.ath.mancel01.webframework.view.Page;
import cx.ath.mancel01.webframework.view.Redirect;
import cx.ath.mancel01.webframework.view.XML;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

/**
 *
 * @author mathieuancelin
 */
public class FrameworkHandler {

    // see: http://wikis.sun.com/display/Jersey/Overview+of+JAX-RS+1.0+Features

    private static final String DEFAUTL_CONTENT_TYPE = "text/html";
    private InjectorImpl injector;
    private final WebBinder configBinder;
    private final TemplateRenderer renderer;
    private final FileGrabber grabber;
    private Map<String, Class> controllers;
    private Class rootController;
    private boolean started = false;
    private final String contextRoot;
    private final File base;
    //private WebFrameworkClassLoader loader;

    public FrameworkHandler(Class<? extends Binder> binderClass, String contextRoot, FileGrabber grabber) {
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
        configureInjector();
        this.base = grabber.getFile("public");
        //loader = new WebFrameworkClassLoader(getClass().getClassLoader());
    }

    private void configureInjector() {
        this.injector.allowCircularDependencies(true);
        this.injector.registerShutdownHook();
        new DependencyShotIntegrator(injector).registerBindings();
    }

    public void validate() {
        if (rootController == null) {
            throw new RuntimeException("You need to register a root controler");
        } 
    }

    public void start() {
        WebFramework.init();
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
                if ("".endsWith(contextRoot)) {
                    throw new RuntimeException("Can't have an empty context root");
                }
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
        } catch (Throwable t) {
            return null; // TODO : return error page
        }
    }

    private Response render(Class controllerClass, String methodName) throws Exception {
        Binding controllerBinding = null;
        if (WebFramework.dev) {
            //controllerClass = RequestCompiler.getCompiledClass(controllerClass);
            //controllerClass = loader.loadClass(controllerClass.getName());
            //this.injector = DependencyShot.getInjector(configBinder);
            //configureInjector();
            try {
                controllerClass = new WebFrameworkClassLoader().loadClass(controllerClass.getName());
            } catch (Throwable ex) {
                return createErrorResponse(ex);
            }
            controllerBinding = new Binding(null, null, controllerClass, controllerClass, null, null);
        }
        long start = System.currentTimeMillis();
        Object controller = null;
        if (WebFramework.dev) {
            try {
                controller = controllerBinding.getInstance(injector, null);
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
        start = System.currentTimeMillis();
        Response res = new Response();
        res.contentType = DEFAUTL_CONTENT_TYPE;
        res.out = new ByteArrayOutputStream();

        // Render view
        if (ret instanceof View) {
            View view = (View) ret;
            String viewName = view.getViewName();
            if ( viewName == null ) {
                // TODO : add extension based on content type
                viewName = methodName + ".html";
            }
            viewName = "views/" + controllerClass.getSimpleName().toLowerCase() + "/" + viewName;
            renderer.render(grabber.getFile(viewName), view.getContext(), res.out);
            WebFramework.logger.trace("template view rendering : {} ms."
                    , (System.currentTimeMillis() - start));
        } else if (ret instanceof Binary) {
            Binary bin = (Binary) ret;
            res.contentType = bin.getContentType();
            res.direct = bin.getFile();
            WebFramework.logger.trace("binary file rendering : {} ms."
                    , (System.currentTimeMillis() - start));
        } else if (ret instanceof JSON) {
            JSON json = (JSON) ret;
            res.contentType = json.getContentType();
            Gson gson = new Gson();
            String jsonRepresentation = gson.toJson(json.getJsonObject());
            res.out.write(jsonRepresentation.getBytes(), 0, jsonRepresentation.length());
            WebFramework.logger.trace("JSON object rendering : {} ms."
                    , (System.currentTimeMillis() - start));
        } else if (ret instanceof Page) {
            Page page = (Page) ret;
            res.contentType = page.getContentType();
            String message = page.getMessage();
            res.out.write(message.getBytes(), 0, message.length());
            WebFramework.logger.trace("page rendering : {} ms."
                    , (System.currentTimeMillis() - start));
        } else if (ret instanceof XML) {
            XML xml = (XML) ret;
            res.contentType = xml.getContentType();
            JAXBContext context = JAXBContext.newInstance(xml.getXmlObjectClass());
            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            m.marshal(xml.getXmlObject(), res.out);
            WebFramework.logger.trace("XML object rendering : {} ms."
                    , (System.currentTimeMillis() - start));
        } else if (ret instanceof Redirect) {
            Redirect red = (Redirect) ret;
            res.contentType = red.getContentType();
            res.headers.put("Refresh", red.getHeader());
            String message = red.getMessage();
            res.out.write(message.getBytes(), 0, message.length());
            WebFramework.logger.trace("redirection : {} ms."
                    , (System.currentTimeMillis() - start));
        } else {
            throw new RuntimeException("You can't render object of type " + ret.getClass().getName());
        }
        return res;
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
        if (cause == null) {
            cause = original;
        }
        Response res = new Response();
        res.contentType = DEFAUTL_CONTENT_TYPE;
        res.out = new ByteArrayOutputStream();
        String html = "<html><head><title>Compilation error</title></head><body>" + cause.getMessage().replace("\n", "<br/>") + "</body></html>";
        res.out.write(html.getBytes(), 0, html.length());
        return res;
    }
}
