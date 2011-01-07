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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author mathieuancelin
 */
public class Response {

    public Integer status = 200;
    public String contentType;
    public Map<String, Header> headers = new HashMap<String, Header>(16);
    public Map<String, Cookie> cookies = new HashMap<String, Cookie>(16);
    public ByteArrayOutputStream out;
    public Object direct;

    public static ThreadLocal<Response> current = new ThreadLocal<Response>();

    public String getHeader(String name) {
        for (String key : headers.keySet()) {
            if (key.toLowerCase().equals(name.toLowerCase())) {
                if (headers.get(key) != null) {
                    return headers.get(key).value();
                }
            }
        }
        return null;
    }

    public void setHeader(String name, String value) {
        Header h = new Header();
        h.name = name;
        h.values = new ArrayList<String>(1);
        h.values.add(value);
        headers.put(name, h);
    }

    public void print(Object o) {
        try {
            out.write(o.toString().getBytes("utf-8"));
        } catch (IOException ex) {
            throw new RuntimeException("UTF-8 problem ?", ex);
        }
    }

    public void reset() {
        out.reset();
    }
}
