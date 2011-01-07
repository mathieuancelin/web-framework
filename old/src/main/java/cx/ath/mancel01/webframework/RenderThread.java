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

package cx.ath.mancel01.webframework;

import cx.ath.mancel01.webframework.http.Request;
import cx.ath.mancel01.webframework.http.Response;
import cx.ath.mancel01.webframework.routing.WebMethod;
import cx.ath.mancel01.webframework.view.HtmlPage;

/**
 *
 * @author mathieuancelin
 */
public class RenderThread extends Thread {

    private final FrameworkHandler handler;
    private final Request request;
    private final WebMethod webMethod;
    private Response response;

    public RenderThread(FrameworkHandler handler, Request request, WebMethod webMethod) {
        this.handler = handler;
        this.request = request;
        this.webMethod = webMethod;
    }

    @Override
    public void run() {
        try {
            Request.current.set(request);
            response = handler.render(request, webMethod);
            Response.current.set(response);
        } catch (Exception ex) {
            ex.printStackTrace();
            response = new HtmlPage("Error",
                    "<h1>Ooops, an error occured : "
                    + ex.getMessage() + "</h1>").render();
        }
    }

    public Response getResponse() {
        try {
            this.join();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
            response = new HtmlPage("Error",
                    "<h1>Ooops, an error occured : "
                    + ex.getMessage() + "</h1>").render();
        }
        return response;
    }
}
