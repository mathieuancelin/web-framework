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
import cx.ath.mancel01.webframework.http.Header;
import cx.ath.mancel01.webframework.http.Response;
import java.io.ByteArrayOutputStream;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author mathieuancelin
 */
public class Redirect extends Renderable {

    private final String url;

    public Redirect(String url) {
        this.url = url;
        this.contentType = MediaType.TEXT_HTML;
    }

    public Header getHeader() {
        return new Header("Refresh", "0; url=" + url);
    }

    public String getMessage() {
        return "<html><body>please follow <a href=\"" + url + "\">" + url + "</a></body></html>";
    }

    @Override
    public Response render() {
        long start = System.currentTimeMillis();
        Response res = Response.current.get();
        res.out = new ByteArrayOutputStream();
        res.contentType = this.getContentType();
        res.headers.put("Refresh", this.getHeader());
        String message = this.getMessage();
        res.out.write(message.getBytes(), 0, message.length());
        WebFramework.logger.trace("redirection : {} ms."
                , (System.currentTimeMillis() - start));
        return res;
    }

}
