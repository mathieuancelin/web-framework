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
        String callback =  Session.current.get().get("callbackUrl");
        if (callback == null) {
            callback = loginModule.defaultCallbackURL();
        }
        Session.current.get().remove("callbackUrl");
        Render.view("login.html").param("callbackUrl", callback).go();
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
        cookie.name = Session.USERNAME;
        cookie.value = user;
        response.cookies.put(Session.USERNAME, cookie);
        if(rememberme.equals("on")) {
            cookie = new Cookie();
            cookie.name = Session.REMEMBERME;
            cookie.value = user + "-" + SecurityUtils.sign(user);
            cookie.maxAge = 2592000; // 30 days
            response.cookies.put(Session.REMEMBERME, cookie);
        }
        Render.redirect(url).go();
    }

    @Path("/security/logout")
    @GET
    public void logout() {
        Cookie username = new Cookie();
        Cookie rememberme = new Cookie();
        Cookie sessionId = new Cookie();
        username.name = Session.USERNAME;
        username.domain = null;
        username.value = "";
        username.path = "/";
        username.maxAge = 0;
        response.cookies.put(Session.USERNAME, username);
        rememberme.name = Session.REMEMBERME;
        rememberme.domain = null;
        rememberme.value = "";
        rememberme.path = "/";
        rememberme.maxAge = 0;
        response.cookies.put(Session.REMEMBERME, rememberme);
        sessionId.name = Session.SESSION_ID;
        sessionId.domain = null;
        sessionId.value = "";
        sessionId.path = "/";
        sessionId.maxAge = 0;
        response.cookies.put(Session.SESSION_ID, sessionId);
        Session.current.get().clear();
        if (!"/".equals(WebFramework.contextRoot)) {
            Render.redirect(WebFramework.contextRoot + loginModule.logoutURL()).go();
        }
        Render.redirect(loginModule.logoutURL()).go();
    }
}
