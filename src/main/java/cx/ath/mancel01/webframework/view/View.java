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

package cx.ath.mancel01.webframework.view;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author mathieuancelin
 */
public class View extends Renderable {

    private static final String TYPE = "text/html";
    private final String viewName;
    private final Map<String, Object> context;

    public View() {
        this.contentType = TYPE;
        this.viewName = null;
        this.context = new HashMap<String, Object>();
    }

    public View(String viewName) {
        this.contentType = TYPE;
        this.viewName = viewName;
        this.context = new HashMap<String, Object>();
    }

    public View(String viewName, Map<String, Object> context) {
        this.contentType = TYPE;
        this.viewName = viewName;
        this.context = context;
    }

    public View(String viewName, NamedAttribute... attributes) {
        this.contentType = TYPE;
        this.viewName = viewName;
        this.context = new HashMap<String, Object>();
        for (NamedAttribute attribute : attributes) {
            context.put(attribute.name, attribute.value);
        }
    }

    public View param(String name, Object value) {
        this.context.put(name, value);
        return this;
    }

    public Map<String, Object> getContext() {
        return context;
    }

    public String getViewName() {
        return viewName;
    }
}
