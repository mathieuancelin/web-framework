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
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

/**
 *
 * @author mathieuancelin
 */
public class TemplateRenderer {

    private final SimpleTemplateEngine engine;

    public TemplateRenderer() {
        this.engine = new SimpleTemplateEngine();
    }

    public String render(String fileName, Map<String, Object> context) throws Exception {
        return render(new File(fileName), context);
    }

    public Writer render(File file, Map<String, Object> context, OutputStream os) throws Exception {
        OutputStreamWriter osw = new OutputStreamWriter(os);
        return engine.createTemplate(file)
                .make(context).writeTo(osw);
    }

    public String render(File file, Map<String, Object> context) throws Exception {
        StringWriter builder = new StringWriter();
        engine.createTemplate(file)
                .make(context).writeTo(builder);
        return builder.toString();
    }
}
