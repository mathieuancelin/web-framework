/*
 *  Copyright 2011 mathieuancelin.
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
import cx.ath.mancel01.webframework.http.Response;
import cx.ath.mancel01.webframework.http.StatusCodes;
import cx.ath.mancel01.webframework.util.FileUtils.FileGrabber;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;

/**
 *
 * @author mathieuancelin
 */
public class FrameworkPage extends Page {

    private static final TemplateRenderer renderer = new TemplateRenderer();
    private final String html;
    private final String title;

    public FrameworkPage(String title, String html) {
        this.html = html;
        this.title = title;
        renderer.grabber = new FileGrabber() {
            @Override
            public File getFile(String file) {
                return new File(WebFramework.VIEWS, file);
            }
        };
    }

    @Override
    public Response render() {
        try {
            long start = System.currentTimeMillis();
            Response res = new Response();
            res.out = new ByteArrayOutputStream();
            res.contentType = this.getContentType();
            String message = this.getMessage();
            //res.out.write(message.getBytes(), 0, message.length());
            renderer.render(message, new HashMap<String, Object>(), res.out);
            WebFramework.logger.trace("html page rendering : {} ms.", (System.currentTimeMillis() - start));
            return res;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public int getStatusCode() {
        return StatusCodes.OK;
    }

    @Override
    public String getContentType() {
        return "text/html";
    }

    @Override
    public String getMessage() {
        return  "#{extends 'main.html' /}" +
                "#{set title:'" + title + "' /}" +
                "<h2 class=\"title\">" + title + "</h2>" +
                "<div class=\"entry\">" +
                "   <p>" + html + "</p>" +
                "</div>" +
                "<div class=\"byline\" />";
    }
}
