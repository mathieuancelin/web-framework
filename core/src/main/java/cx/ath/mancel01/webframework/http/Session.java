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

import cx.ath.mancel01.webframework.util.SecurityUtils;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author mathieuancelin
 */
public class Session {

    private static final Pattern sessionParser
            = Pattern.compile("\u0000([^:]*):([^\u0000]*)\u0000");
    public static ThreadLocal<Session> current 
            = new ThreadLocal<Session>();
    private String sessionId;
    private Map<String, String> data = new HashMap<String, String>();

    public static Session restore() {
        Request req = Request.current.get();
        Response res = Response.current.get();
        Session session = new Session();
        try {
            Cookie cookie = req.cookies.get("webfwk-session");
            if (cookie != null) {
                String value = cookie.value;
                String sign = value.substring(0, value.indexOf("-"));
                String data = value.substring(value.indexOf("-") + 1);
                if (sign.equals(SecurityUtils.sign(data))) {
                    String sessionData = URLDecoder.decode(data, "utf-8");
                    Matcher matcher = sessionParser.matcher(sessionData);
                    while (matcher.find()) {
                        session.put(matcher.group(1), matcher.group(2));
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Corrupted HTTP session from " + Request.current.get().remoteAddress, e);
        }
        if (!req.cookies.containsKey("webfwk-session-id")) {
            session.sessionId = UUID.randomUUID().toString();
            Cookie cookie = new Cookie();
            cookie.name = "webfwk-session-id";
            cookie.value = session.sessionId;
            res.cookies.put("webfwk-session-id", cookie);
        } else {
            session.sessionId = req.cookies.get("webfwk-session-id").value;
        }
        return session;
    }

    public void save() {
        try {
            StringBuilder session = new StringBuilder();
            for (String key : data.keySet()) {
                session.append("\u0000");
                session.append(key);
                session.append(":");
                session.append(data.get(key));
                session.append("\u0000");
            }
            String sessionData = URLEncoder.encode(session.toString(), "utf-8");
            String sign = SecurityUtils.sign(sessionData);
            Cookie cookie = new Cookie();
            cookie.name = "webfwk-session";
            cookie.value = sign + "-" + sessionData;
            Response.current.get().cookies.put(cookie.name, cookie);
        } catch (Exception e) {
            throw new RuntimeException("Session serializationProblem", e);
        }
    }

    public Map<String, String> getData() {
        return data;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String put(String key, String value) {
        return data.put(key, value);
    }

    public void remove(String key) {
        data.remove(key);
    }

    public String get(String key) {
        return data.get(key);
    }

    public void clear() {
        data.clear();
    }

    public boolean contains(String key) {
        return data.containsKey(key);
    }
}
