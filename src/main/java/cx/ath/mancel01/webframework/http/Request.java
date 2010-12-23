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

import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author mathieuancelin
 */
public class Request {

    /**
     * Server host
     */
    public String host;
    /**
     * Request path
     */
    public String path;
    /**
     * QueryString
     */
    public String querystring;
    /**
     * Full url
     */
    public String url;
    /**
     * HTTP method
     */
    public String method;
    /**
     * Server domain
     */
    public String domain;
    /**
     * Client address
     */
    public String remoteAddress;
    /**
     * Request content-type
     */
    public String contentType;
    /**
     * HTTP port
     */
    public Integer port;
    /**
     * is HTTPS ?
     */
    public Boolean secure = false;
    /**
     * HTTP Headers
     */
    public Map<String, Header> headers = new HashMap<String, Header>(16);
    /**
     * HTTP Cookies
     */
    public Map<String, Cookie> cookies = new HashMap<String, Cookie>(16);
    /**
     * Body stream
     */
    public transient InputStream body;
    /**
     * Format (html,xml,json,text)
     */
    public String format = null;
    /**
     * Free space to store your request specific data
     */
    public Map<String, Object> args = new HashMap<String, Object>(16);
    /**
     * When the request has been received
     */
    public Date date = new Date();
    /**
     * New request or already submitted
     */
    public boolean isNew = true;
    /**
     * HTTP Basic User
     */
    public String user;
    /**
     * HTTP Basic Password
     */
    public String password;
    /**
     * Request comes from loopback interface
     */
    public boolean isLoopback;

    public String getControllerName() {
        return "";
    }

    public String getCalledMethod() {
        return "";
    }
}
