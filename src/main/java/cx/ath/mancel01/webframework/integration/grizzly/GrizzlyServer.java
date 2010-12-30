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
package cx.ath.mancel01.webframework.integration.grizzly;

import com.sun.grizzly.http.embed.GrizzlyWebServer;
import com.sun.grizzly.tcp.http11.GrizzlyAdapter;
import com.sun.grizzly.tcp.http11.GrizzlyRequest;
import com.sun.grizzly.tcp.http11.GrizzlyResponse;
import cx.ath.mancel01.webframework.FrameworkHandler;
import cx.ath.mancel01.webframework.integration.dependencyshot.WebBinder;
import cx.ath.mancel01.webframework.WebFramework;
import cx.ath.mancel01.webframework.http.Request;
import cx.ath.mancel01.webframework.http.Response;
import cx.ath.mancel01.webframework.util.FileUtils.FileGrabber;
import java.io.File;
import java.io.IOException;

/**
 * Developpement embeddable http server for the web-framework.
 *
 * @author mathieuancelin
 */
public class GrizzlyServer {

    // see: http://download.java.net/maven/2/com/sun/grizzly/grizzly-webserver/
    // see : http://jfarcand.wordpress.com/2009/01/08/extending-the-grizzly-http-runtime-part-v-programatically-configuring-servlet-and-grizzlyadapter/

    private static final int NTHREADS = 100;
    private GrizzlyWebServer server;
    private final int port;
    private final String rootContext;
    private FrameworkHandler dispatcher;
    private final WebBinder binder;
    private final File viewDirectory;

    public GrizzlyServer(
            int port, String rootContext,
            WebBinder binder, File viewDirectory) {
        this.port = port;
        this.rootContext = rootContext;
        this.binder = binder;
        this.viewDirectory = viewDirectory;
    }

    public void start() {
        try {
            server = new GrizzlyWebServer(port);
            GrizzlyAdapter adapter = new GrizzlyAdapter() {
                @Override
                public void service(GrizzlyRequest request, GrizzlyResponse response) {
                    WebFramework.logger.debug("start processing request ...");
                    long start = System.currentTimeMillis();
                    try {
                        Request req = GrizzlyBinder.extractRequest(request);
                        Request.current.set(req);
                        Response res = dispatcher.process(req);
                        Response.current.set(res);
                        GrizzlyBinder.flushResponse(req, res, response);
                    } catch (Exception e) {
                        e.printStackTrace(); // TODO : print something useless here
                        e.getCause().printStackTrace();
                    } finally {
                        // response..close();
                        WebFramework.logger.debug("request processed in {} ms.\n", (System.currentTimeMillis() - start));
                        WebFramework.logger.debug("=======================================\n");
                    }
                }
            };
            server.setMaxThreads(NTHREADS);
            server.addGrizzlyAdapter(adapter, new String[]{rootContext});
            dispatcher = new FrameworkHandler(binder.getClass(), rootContext, new FileGrabber() {
                @Override
                public File getFile(String file) {
                    return new File(viewDirectory, file);
                }
            });
            Runtime.getRuntime().addShutdownHook(new Shutdown(dispatcher));
            server.start();
            dispatcher.start();
            WebFramework.logger.info("starting http server ... done !");
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void stop() {
        server.stop();
        dispatcher.stop();
        WebFramework.logger.info("stopping http server ... done !");
    }

    private class Shutdown extends Thread {

        private final FrameworkHandler dispatcher;

        public Shutdown(FrameworkHandler dispatcher) {
            this.dispatcher = dispatcher;
        }

        @Override
        public void run() {
            dispatcher.stop();
        }
    }
}
