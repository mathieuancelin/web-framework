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

import cx.ath.mancel01.webframework.WebFramework;
import groovy.text.SimpleTemplateEngine;
import java.io.File;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author mathieuancelin
 */
public class TemplateRenderer {

    private final SimpleTemplateEngine engine;
    private final ConcurrentHashMap<String, groovy.text.Template> templates =
            new ConcurrentHashMap<String, groovy.text.Template>();

    public TemplateRenderer() {
        this.engine = new SimpleTemplateEngine();
    }

    public Writer render(File file, Map<String, Object> context, OutputStream os) throws Exception {
        return renderWithGroovy(file, context, os);
    }

    private Writer renderWithGroovy(File file, Map<String, Object> context, OutputStream os) throws Exception {
        // TODO : if file not exists, return 404
        OutputStreamWriter osw = new OutputStreamWriter(os);
        if (WebFramework.dev) {
            return engine.createTemplate(file).make(context).writeTo(osw);
        } else {
            if (!templates.containsKey(file.getAbsolutePath())) {
                templates.putIfAbsent(file.getAbsolutePath()
                    , engine.createTemplate(file));
            }
            return templates.get(file.getAbsolutePath()).make(context).writeTo(osw);
        }
    }
}