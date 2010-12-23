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
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author mathieuancelin
 */
public class Header implements Serializable {

    /**
     * Header name
     */
    public String name;
    /**
     * Header value
     */
    public List<String> values;

    public Header() {
        this.values = new ArrayList<String>(5);
    }

    public Header(String name, String value) {
        this.name = name;
        this.values = new ArrayList<String>(5);
        this.values.add(value);
    }

    public Header(String name, List<String> values) {
        this.name = name;
        this.values = values;
    }

    /**
     * First value
     * @return The first value
     */
    public String value() {
        return values.get(0);
    }

    @Override
    public String toString() {
        return values.toString();
    }
}
