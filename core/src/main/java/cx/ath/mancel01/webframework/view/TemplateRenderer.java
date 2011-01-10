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
import cx.ath.mancel01.webframework.util.FileUtils;
import cx.ath.mancel01.webframework.util.FileUtils.FileGrabber;
import groovy.text.SimpleTemplateEngine;
import java.io.File;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author mathieuancelin
 */
public class TemplateRenderer {

    private static final Pattern listPattern = Pattern.compile("\\#\\{list items:[^/]+, as:'[^']+'\\}");
    private static final Pattern extendsPatter = Pattern.compile("\\#\\{extends '[^']+' /\\}");
    private static final Pattern setPattern = Pattern.compile("\\#\\{set [^/]+:'[^']+' /\\}");
    private static final Pattern getPattern = Pattern.compile("\\#\\{get [^/]+ /\\}");
    private static final Pattern linkPattern = Pattern.compile("\\@\\{'[^']+'\\}");
    private final SimpleTemplateEngine engine;
    private final ConcurrentHashMap<String, groovy.text.Template> templates =
            new ConcurrentHashMap<String, groovy.text.Template>();
    public FileGrabber grabber;

    public TemplateRenderer() {
        this.engine = new SimpleTemplateEngine();
    }

    public Writer render(File file, Map<String, Object> context, OutputStream os) throws Exception {
        return renderWithGroovy(file, context, os);
    }

    public Writer render(String source, Map<String, Object> context, OutputStream os) throws Exception {
        return renderWithGroovy(source, context, os);
    }

    private Writer renderWithGroovy(String source, Map<String, Object> context, OutputStream os) throws Exception {
         OutputStreamWriter osw = new OutputStreamWriter(os);
         String enhanced = enhanceCode(source, grabber);
         return engine.createTemplate(enhanced).make(context).writeTo(osw);
    }

    private Writer renderWithGroovy(File file, Map<String, Object> context, OutputStream os) throws Exception {
        // TODO : if file not exists, return 404
        OutputStreamWriter osw = new OutputStreamWriter(os);
        if (WebFramework.dev) {
            String code = FileUtils.readFileAsString(file);
            return engine.createTemplate(enhanceCode(code, grabber)).make(context).writeTo(osw);
            //return engine.createTemplate(file).make(context).writeTo(osw);
        } else {
            if (!templates.containsKey(file.getAbsolutePath())) {
                String code = FileUtils.readFileAsString(file);
                templates.putIfAbsent(file.getAbsolutePath()
                    , engine.createTemplate(enhanceCode(code, grabber)));
//                templates.putIfAbsent(file.getAbsolutePath()
//                    , engine.createTemplate(file));
            }
            return templates.get(file.getAbsolutePath()).make(context).writeTo(osw);
        }
    }

    public static String enhanceCode(String code, FileGrabber grabber) {
        // TODO : custom tags, links, optimize :)
        List<String> before = new ArrayList<String>();
        String custom = code
            .replace("%{", "<%")
            .replace("}%", "%>")
            .replace("$.", "\\$.")
            .replace("$(", "\\$(")
            .replace("#{/list}", "<% } %>")
            .replace("#{/list }", "<% } %>");
        Matcher matcher = listPattern.matcher(custom);
        while(matcher.find()) {
            String group = matcher.group();
            String list = group;
            list = list.replace("#{list items:", "<% ")
                .replace(", as:'", ".each { ")
                .replace(",as:'", ".each { ")
                .replace("'}", " -> %>")
                .replace("' }", " -> %>");
            custom = custom.replace(group, list);
        }
        Matcher setMatcher = setPattern.matcher(custom);
        while(setMatcher.find()) {
            String group = setMatcher.group();
            String name = group;
            name = name.replace("#{set ", "").replaceAll(":'[^']+' /\\}", "");
            String value = group;
            value = group.replaceAll("\\#\\{set [^/]+:'", "").replace("' /}", "");
            //custom = custom.replace(group, "<% " + name + " = '" + value + "' %>");
            custom = custom.replace(group, "");
            before.add("<% " + name + " = '" + value + "' %>\n");
        }
        Matcher getMatcher = getPattern.matcher(custom);
        while(getMatcher.find()) {
            String group = getMatcher.group();
            String name = group;
            name = name.replace("#{get ", "").replace(" /}", "");
            custom = custom.replace(group, "${" + name + "}");
        }
        Matcher linkMatcher = linkPattern.matcher(custom);
        while(linkMatcher.find()) {
            String group = linkMatcher.group();
            String link = group;
            link = link.replace("@{'", "").replace("'}", "");
            if (!"/".equals(WebFramework.contextRoot)) {
                link = WebFramework.contextRoot + link;
            }
            custom  = custom.replace(group, link);
        }
        Matcher extendsMatcher = extendsPatter.matcher(custom);
        custom = custom.replaceAll("\\#\\{extends '[^']+' /\\}", "");
        while(extendsMatcher.find()) {
            String group = extendsMatcher.group();
            String fileName = group;
            fileName = fileName.replace("#{extends '", "").replace("' /}", "");
            File file = grabber.getFile(fileName);
            String parentCode = FileUtils.readFileAsString(file);
            String parentCustomCode = enhanceCode(parentCode, grabber);
            String[] parts = parentCustomCode.split("\\#\\{doLayout /\\}");
            if (parts.length > 2) {
                throw new RuntimeException("Can't have #{doLayout /} more than one time in a template.");
            }
            String finalCode = parts[0] + custom + parts[1];
            for (String bef : before) {
                finalCode = bef + finalCode;
            }
            return finalCode;
        }
        for (String bef : before) {
            custom = bef + custom;
        }
        return custom;
    }
}