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

import cx.ath.mancel01.webframework.http.Header;

/**
 *
 * @author mathieuancelin
 */
public class Redirect extends Renderable {

    private final String url;

    public Redirect(String url) {
        this.url = url;
        this.contentType = "text/html";
    }

    public Header getHeader() {
        return new Header("Refresh", "0; url=" + url);
    }

    public String getMessage() {
        return "<html><body>please follow <a href=\"" + url + "\">" + url + "</a></body></html>";
    }

}
