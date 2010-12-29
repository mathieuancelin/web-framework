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

package cx.ath.mancel01.webframework;

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

    private static final int VELOCITY = 1;
    private static final int GROOVY = 2;
    private static final int ENGINE = GROOVY;

    private final boolean devMode;
    private final SimpleTemplateEngine engine;
//    private final VelocityEngine ve;
    private final ConcurrentHashMap<String, groovy.text.Template> templates =
            new ConcurrentHashMap<String, groovy.text.Template>();

    public TemplateRenderer() {
        this.engine = new SimpleTemplateEngine();
//        this.ve = new VelocityEngine();
//        this.ve.init();
        // TODO : change to read conf file
        this.devMode = true;
    }

    public Writer render(File file, Map<String, Object> context, OutputStream os) throws Exception {
//        if (ENGINE == VELOCITY) {
//            return renderWithVelocity(file, context, os);
//        } else if (ENGINE == GROOVY) {
            return renderWithGroovy(file, context, os);
//        } else {
//            // should never append :)
//            throw new RuntimeException("You need to use a render engine");
//        }
    }

    private Writer renderWithGroovy(File file, Map<String, Object> context, OutputStream os) throws Exception {
        // TODO : if file not exists, return 404
        OutputStreamWriter osw = new OutputStreamWriter(os);
        if (devMode) {
            return engine.createTemplate(file).make(context).writeTo(osw);
        } else {
            if (!templates.containsKey(file.getAbsolutePath())) {
                templates.putIfAbsent(file.getAbsolutePath()
                    , engine.createTemplate(file));
            }
            return templates.get(file.getAbsolutePath()).make(context).writeTo(osw);
        }
    }

//    private Writer renderWithVelocity(File file, Map<String, Object> context, OutputStream os) throws Exception {
//        OutputStreamWriter writer = new OutputStreamWriter(os);
//        Template template = ve.getTemplate(file.getPath());
//        template.merge(new VelocityContext(context), writer);
//        writer.flush();
//        return writer;
//    }
    
//    public String render(File file, Map<String, Object> context) throws Exception {
//        StringWriter builder = new StringWriter();
//        engine.createTemplate(file)
//                .make(context).writeTo(builder);
//        return builder.toString();
//    }

}

//
//<!--#foreach( $number in $numbers )
//      <div>${number}</div>
//      #end-->