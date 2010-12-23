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

import java.io.Serializable;

/**
 *
 * @author mathieuancelin
 */
public class Cookie implements Serializable {

    /**
     * Cookie name
     */
    public String name;
    /**
     * Cookie domain
     */
    public String domain;
    /**
     * Cookie path
     */
    public String path = "/";
    /**
     * for HTTPS ?
     */
    public boolean secure = false;
    /**
     * Cookie value
     */
    public String value;
    /**
     * Cookie max-age
     */
    public Integer maxAge;
    /**
     * Don't use
     */
    public boolean sendOnError = false;
    /**
     * See http://www.owasp.org/index.php/HttpOnly
     */
    public boolean httpOnly = false;
}
