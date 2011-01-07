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
package cx.ath.mancel01.webframework.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author mathieuancelin
 */
public class Request {

    public String host;
    public String path;
    public String querystring;
    public String url;
    public String method;
    public String domain;
    public String remoteAddress;
    public String contentType;
    public String contextRoot;
    public Integer port;
    public Boolean secure = false;
    public Map<String, Header> headers = new HashMap<String, Header>(16);
    public Map<String, Cookie> cookies = new HashMap<String, Cookie>(16);
    public transient InputStream body;
    public String format = null;
    public Map<String, Object> args = new HashMap<String, Object>(16);
    public Date date = new Date();
    public boolean isNew = true;
    public String user;
    public String password;
    public boolean isLoopback;
    public static ThreadLocal<Request> current = new ThreadLocal<Request>();
    private String bodyValue;

    public String getPath() {
        String tmppath = path;
        if (!"/".equals(contextRoot)) {
            path = path.replace(contextRoot, "/");
        }
        return tmppath;
    }

    public String body() {
        if (bodyValue == null) {
            CopyInputStream cis = new CopyInputStream(body);
            bodyValue = slurpBody(cis.getCopy());
        }
        return bodyValue;
    }

    public boolean isAjax() {
        if (!headers.containsKey("x-requested-with")) {
            return false;
        }
        return "XMLHttpRequest".equals(headers.get("x-requested-with").value());
    }

    private static String slurpBody(InputStream body) {
        if (body == null)
            return "null";
        try {
            StringBuilder out = new StringBuilder();
            byte[] b = new byte[4096];
            for (int n; (n = body.read(b)) != -1;) {
                out.append(new String(b, 0, n));
            }
            return out.toString();
        } catch (IOException ex) {
            return "IO/error";
        }
    }
}
