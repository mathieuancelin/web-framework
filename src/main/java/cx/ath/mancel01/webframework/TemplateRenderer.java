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

import freemarker.template.Configuration;
import groovy.text.SimpleTemplateEngine;
import java.io.File;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

/**
 *
 * @author mathieuancelin
 */
public class TemplateRenderer {

    private static final int VELOCITY = 1;
    private static final int GROOVY = 2;
    private static final int FREEMARKER = 3;
    private static final int ENGINE = VELOCITY;

    private final SimpleTemplateEngine engine;
    private final VelocityEngine ve;

    public TemplateRenderer() {
        this.engine = new SimpleTemplateEngine();
        this.ve = new VelocityEngine();
        this.ve.init();
    }

    public Writer render(File file, Map<String, Object> context, OutputStream os) throws Exception {
        if (ENGINE == VELOCITY) {
            return renderWithVelocity(file, context, os);
        } else if (ENGINE == GROOVY) {
            return renderWithGroovy(file, context, os);
        } else if (ENGINE == FREEMARKER) {
            return renderWithFreemarker(file, context, os);
        } else {
            throw new RuntimeException("You nedd to use a render template");
        }
    }

    private Writer renderWithGroovy(File file, Map<String, Object> context, OutputStream os) throws Exception {
        OutputStreamWriter osw = new OutputStreamWriter(os);
        return engine.createTemplate(file)
                .make(context).writeTo(osw);
    }

    private Writer renderWithVelocity(File file, Map<String, Object> context, OutputStream os) throws Exception {
        OutputStreamWriter writer = new OutputStreamWriter(os);
        Template template = ve.getTemplate(file.getPath());
        template.merge(new VelocityContext(context), writer);
        writer.flush();
        return writer;
    }
    
    private Writer renderWithFreemarker(File file, Map<String, Object> context, OutputStream os) throws Exception {
        OutputStreamWriter writer = new OutputStreamWriter(os);
        Configuration cfg = new Configuration();
        freemarker.template.Template tpl = cfg.getTemplate(file.getPath());
        tpl.process(context, writer);
        return writer;
    }

//    public String render(String fileName, Map<String, Object> context) throws Exception {
//        return render(new File(fileName), context);
//    }
//    public String render(File file, Map<String, Object> context) throws Exception {
//        StringWriter builder = new StringWriter();
//        engine.createTemplate(file)
//                .make(context).writeTo(builder);
//        return builder.toString();
//    }

}
