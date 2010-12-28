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
 * A cookie :)
 * 
 * @author mathieuancelin
 */
public class Cookie implements Serializable {
    public String name;
    public String domain;
    public String path = "/";
    public boolean secure = false;
    public String value;
    public Integer maxAge;
    public boolean httpOnly = false;

    @Override
    public String toString() {
        return "Cookie{" + "name=" + name + ", domain=" + domain + ", path="
                + path + ", secure=" + secure + ", value=" + value + ", maxAge="
                + maxAge + ", httpOnly=" + httpOnly + '}';
    }
}
