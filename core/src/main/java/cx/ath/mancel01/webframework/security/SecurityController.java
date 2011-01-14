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

package cx.ath.mancel01.webframework.security;

import cx.ath.mancel01.webframework.WebFramework;
import cx.ath.mancel01.webframework.annotation.Controller;
import cx.ath.mancel01.webframework.http.Cookie;
import cx.ath.mancel01.webframework.http.Response;
import cx.ath.mancel01.webframework.http.Session;
import cx.ath.mancel01.webframework.util.SecurityUtils;
import cx.ath.mancel01.webframework.view.Render;
import javax.inject.Inject;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

@Controller
public class SecurityController {

    @Inject
    private Response response;

    @Inject
    private LoginModule loginModule;

    @Path("/security/loginpage")
    @GET
    public void loginPage() {
        //Render.viewWithParam("url", this)
    }

    @Path("/security/login")
    @POST
    public void login(
            @FormParam("user") String user,
            @FormParam("password") String password,
            @FormParam("url") String url, 
            @FormParam("rememberme") String rememberme) {
        
        if (!loginModule.authenticate(user, password)) {
            if (!"/".equals(WebFramework.contextRoot)) {
                Render.redirect(WebFramework.contextRoot
                        + loginModule.authenticationFailURL()).go();
            }
            Render.redirect(loginModule.authenticationFailURL()).go();
        }
        Cookie cookie = new Cookie();
        cookie.name = "username";
        cookie.value = user;
        response.cookies.put("username", cookie);
        if(rememberme.equals("on")) {
            cookie = new Cookie();
            cookie.name = "rememberme";
            cookie.value = user + "-" + SecurityUtils.sign(user);
            cookie.maxAge = 2592000; // 30 days
            response.cookies.put("rememberme", cookie);
        }
        Render.redirect(url).go();
    }

    @Path("/security/logout")
    @GET
    public void logout() {
        Cookie username = new Cookie();
        Cookie rememberme = new Cookie();
        Cookie sessionId = new Cookie();
        username.name = "username";
        username.domain = null;
        username.value = "";
        username.path = "/";
        username.maxAge = 0;
        response.cookies.put("username", username);
        rememberme.name = "rememberme";
        rememberme.domain = null;
        rememberme.value = "";
        rememberme.path = "/";
        rememberme.maxAge = 0;
        response.cookies.put("rememberme", rememberme);
        sessionId.name = "webfwk-session-id";
        sessionId.domain = null;
        sessionId.value = "";
        sessionId.path = "/";
        sessionId.maxAge = 0;
        response.cookies.put("webfwk-session-id", sessionId);
        Session.current.get().clear();
        if (!"/".equals(WebFramework.contextRoot)) {
            Render.redirect(WebFramework.contextRoot + loginModule.logoutURL()).go();
        }
        Render.redirect(loginModule.logoutURL()).go();
    }
}
