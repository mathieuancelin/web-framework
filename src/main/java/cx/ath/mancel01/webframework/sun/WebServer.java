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
package cx.ath.mancel01.webframework.sun;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import cx.ath.mancel01.webframework.Dispatcher;
import cx.ath.mancel01.webframework.WebBinder;
import cx.ath.mancel01.webframework.http.Cookie;
import cx.ath.mancel01.webframework.http.Header;
import cx.ath.mancel01.webframework.http.Request;
import cx.ath.mancel01.webframework.http.Response;
import cx.ath.mancel01.webframework.util.FileUtils.FileGrabber;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
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
    private Dispatcher dispatcher;
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
            System.out.print("starting http server ... ");
            server = HttpServer.create(new InetSocketAddress(host, port), 0);
            server.setExecutor(exec);
            server.createContext(rootContext, new HttpHandler() {
                @Override
                public void handle(HttpExchange he) throws IOException {
                    System.out.println("start processing request ...");
                    long start = System.currentTimeMillis();
                    try {
                        Request req = parseRequest(he);
                        Response res = dispatcher.process(req);
                        copyResponse(req, res, he);
                    } catch (Exception e) {
                        e.printStackTrace();
                        e.getCause().printStackTrace();
                    } finally {
                        he.close();
                        System.out.println("request processed in : " + (System.currentTimeMillis() - start) + " ms.\n");
                        System.out.println("=======================================\n");
                    }
                }
            });
            dispatcher = new Dispatcher(binder.getClass(), rootContext, new FileGrabber() {
                @Override
                public File getFile(String file) {
                    return new File(viewDirectory, file);
                }
            });
            Runtime.getRuntime().addShutdownHook(new Shutdown(dispatcher));
            server.start();
            dispatcher.start();
            System.out.println("done !");
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void stop() {
        System.out.print("stopping http server ... ");
        server.stop(0);
        dispatcher.stop();
        exec.shutdownNow();
        System.out.println("done !");
    }

    private class Shutdown extends Thread {
        private final Dispatcher dispatcher;

        public Shutdown(Dispatcher dispatcher) {
            this.dispatcher = dispatcher;
        }

        @Override
        public void run() {
            dispatcher.stop();
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////

    private void copyResponse(Request request, Response response, HttpExchange he)
            throws IOException {
        if (response.contentType != null) {
            he.getResponseHeaders().add("Content-Type", response.contentType
                    + (response.contentType.startsWith("text/") ? "; charset=utf-8" : ""));
        } else {
            he.getResponseHeaders().add("Content-Type", "text/plain;charset=utf-8");
        }
        if (!response.headers.containsKey("cache-control")) {
            he.getResponseHeaders().add("Cache-Control", "no-cache");
        }
        Map<String, Header> headers = response.headers;
        for (Map.Entry<String, Header> entry : headers.entrySet()) {
            Header hd = entry.getValue();
            String key = entry.getKey();
            for (String value : hd.values) {
                he.getResponseHeaders().add(key, value);
            }
        }
        Map<String, Cookie> cookies = response.cookies;
        for (Cookie cookie : cookies.values()) {
            StringBuilder builder = new StringBuilder();
            builder.append(cookie.name);
            builder.append("=");
            builder.append(cookie.value);
            if (cookie.domain != null) {
                builder.append("; domain=");
                builder.append(cookie.domain);
            }          
            if (cookie.maxAge != null) {
                builder.append("; expires=");
                builder.append(cookie.maxAge);
            }
            if (cookie.path != null) {
                builder.append("; path=");
                builder.append(cookie.path);
            }
            if (cookie.secure) {
                builder.append("; secure");
            }
            if (cookie.httpOnly) {
                builder.append("; HttpOnly");
            }
            he.getResponseHeaders().add("Set-Cookie", builder.toString());
        }
//        he.getResponseHeaders().add("Set-Cookie", "maurice=machin");
//        he.getResponseHeaders().add("Set-Cookie", "john=truc");
        response.out.flush();
        if (response.direct != null && response.direct instanceof File) {
            File file = (File) response.direct;
            he.getResponseHeaders().add("Content-Length", String.valueOf(file.length()));
            he.sendResponseHeaders(response.status, file.length());
            if (!request.method.equals("HEAD")) {
                copyStream(he, new FileInputStream(file));
            } else {
                copyStream(he, new ByteArrayInputStream(new byte[0]));
            }
        } else if (response.direct != null && response.direct instanceof InputStream) {
            copyStream(he, (InputStream) response.direct);
        } else {
            byte[] content = response.out.toByteArray();
            he.getResponseHeaders().add("Content-Length", String.valueOf(content.length));
            he.sendResponseHeaders(response.status, content.length);
            if (!request.method.equals("HEAD")) {
                he.getResponseBody().write(content);
            } else {
                copyStream(he, new ByteArrayInputStream(new byte[0]));
            }
        }
    }

    private static Request parseRequest(HttpExchange he) throws Exception {
        Request request = new Request();
        URI uri = he.getRequestURI();
        request.method = he.getRequestMethod().intern();
        request.path = uri.getPath();
        // TODO : replace tokenizer
        StringTokenizer tokenizer = new StringTokenizer(uri.toString(), "?");
        String queryString = "";
        if (tokenizer.countTokens() > 1) {
            tokenizer.nextToken();
            queryString = tokenizer.nextToken();
        }
        request.querystring = queryString == null ? "" : queryString;
        if (he.getRequestHeaders().getFirst("Content-Type") != null) {
            request.contentType = he.getRequestHeaders().getFirst("Content-Type").split(";")[0].trim().toLowerCase().intern();
        } else {
            request.contentType = "text/html".intern();
        }

        if (he.getRequestHeaders().getFirst("X-HTTP-Method-Override") != null) {
            request.method = he.getRequestHeaders().getFirst("X-HTTP-Method-Override").intern();
        }
        request.body = he.getRequestBody();
        request.url = uri.toString() + (queryString == null ? "" : "?" + queryString);
        request.host = he.getRequestHeaders().getFirst("host");
        if (request.host.contains(":")) {
            request.port = Integer.parseInt(request.host.split(":")[1]);
            request.domain = request.host.split(":")[0];
        } else {
            request.port = 80;
            request.domain = request.host;
        }
        request.remoteAddress = he.getRemoteAddress().getHostName();
        Set<String> headersNames = he.getRequestHeaders().keySet();
        for (String headerName : headersNames) {
            Header hd = new Header();
            hd.name = headerName;
            hd.values = new ArrayList<String>();
            List<String> enumValues = he.getRequestHeaders().get(hd.name);
            for (String value : enumValues) {
                hd.values.add(value);
            }
            request.headers.put(hd.name.toLowerCase(), hd);
        }
        List<String> cookieHeaders = he.getRequestHeaders().get("Cookie");
        if (cookieHeaders != null) {
            for (String cookieHeader : cookieHeaders) {
                String[] tokens = cookieHeader.split("; ");
                Cookie cookie = new Cookie();
                for (String token : tokens) {
                    if (token.startsWith("domain")) {
                        String[] keyVal = token.split("=");
                        cookie.domain = keyVal[1];
                    } else if (token.startsWith("expires")) {
                        // TODO : what to do here ?
                    } else if (token.startsWith("path")) {
                        String[] keyVal = token.split("=");
                        cookie.path = keyVal[1];
                    } else if (token.startsWith("secure")) {
                        cookie.secure = true;
                    } else if (token.startsWith("HttpOnly")) {
                        cookie.httpOnly = true;
                    } else {
                        cookie = new Cookie();
                        String[] keyVal = token.split("=");
                        cookie.name = keyVal[0];
                        cookie.value = keyVal[1];
                    }
                    //System.out.println(cookie);
                    request.cookies.put(cookie.name, cookie);
                }
            }
        }
        return request;
    }

    private void copyStream(HttpExchange he, InputStream is) throws IOException {
        OutputStream os = he.getResponseBody();
        byte[] buffer = new byte[8096];
        int read = 0;
        while ((read = is.read(buffer)) > 0) {
            os.write(buffer, 0, read);
        }
        os.flush();
        is.close();
    }
}
