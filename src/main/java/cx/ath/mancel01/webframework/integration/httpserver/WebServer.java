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
package cx.ath.mancel01.webframework.integration.httpserver;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import cx.ath.mancel01.webframework.FrameworkHandler;
import cx.ath.mancel01.webframework.integration.dependencyshot.WebBinder;
import cx.ath.mancel01.webframework.WebFramework;
import cx.ath.mancel01.webframework.http.Request;
import cx.ath.mancel01.webframework.http.Response;
import cx.ath.mancel01.webframework.util.FileUtils.FileGrabber;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Developpement embeddable http server for the web-framework.
 *
 * @author mathieuancelin
 */
public class WebServer {

    private static final int NTHREADS = 100;
    private static final ExecutorService exec = Executors.newFixedThreadPool(NTHREADS);
    private HttpServer server;
    private final String host;
    private final int port;
    private final String rootContext;
    private FrameworkHandler dispatcher;
    private final WebBinder binder;
    private final File viewDirectory;

    public WebServer(String host,
            int port, String rootContext,
            WebBinder binder, File viewDirectory) {
        this.host = host;
        this.port = port;
        this.rootContext = rootContext;
        this.binder = binder;
        this.viewDirectory = viewDirectory;
    }

    public void start() {
        try {
//            WebFramework.logger.info("starting http server ... ");
            server = HttpServer.create(new InetSocketAddress(host, port), 0);
            server.setExecutor(exec);
            server.createContext(rootContext, new HttpHandler() {
                @Override
                public void handle(HttpExchange he) throws IOException {
                    WebFramework.logger.trace("start processing request ...");
                    long start = System.currentTimeMillis();
                    try {
                        Request req = InOutBinder.extractRequest(he);
                        Request.current.set(req);
                        Response res = dispatcher.process(req);
                        Response.current.set(res);
                        InOutBinder.flushResponse(req, res, he);
                    } catch (Exception e) {
                        e.printStackTrace(); // TODO : print something useless here
                        e.getCause().printStackTrace();
                    } finally {
                        he.close();
                        WebFramework.logger.trace("request processed in {} ms.\n"
                                , (System.currentTimeMillis() - start));
                        WebFramework.logger.trace("=======================================\n");
                    }
                }
            });
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
        server.stop(0);
        dispatcher.stop();
        exec.shutdownNow();
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
