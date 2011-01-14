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

import cx.ath.mancel01.webframework.http.Cookie;
import cx.ath.mancel01.webframework.http.Request;
import cx.ath.mancel01.webframework.http.Response;
import cx.ath.mancel01.webframework.util.SecurityUtils;
import cx.ath.mancel01.webframework.view.FrameworkPage;
import javax.inject.Singleton;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 *
 * @author mathieu
 */
@Singleton
public class SecurityInterceptor implements MethodInterceptor {

    @Override
    public final Object invoke(MethodInvocation mi) throws Throwable {
        Request req = Request.current.get();
        String form = "<form method=\"POST\" action=\"@{'/security/login'}\" >"
                    + "<table><tr><td>"
                    + "Username </td><td>"
                    + "<input type=\"text\" name=\"user\"></td></tr><tr><td>"
                    + "Password </td><td><input type=\"password\" name=\"password\">"
                    + "<input type=\"hidden\" name=\"url\" value=\"" + req.path + "\">"
                    + "</td></tr><tr><td></td></td></tr><tr><td>"
                    + "<input type=\"submit\" value=\"Login\" align=\"center\">"
                    + "</td><td>Remember me <input type=\"checkbox\" name=\"rememberme\"/>"
                    + "</td></tr></table>"
                    + "</form>";
        if (req.cookies.containsKey("username")) {
            return mi.proceed();
        } else if (req.cookies.containsKey("rememberme")) {
            String cookieValue = req.cookies.get("rememberme").value;
            String username = cookieValue.split("-")[0];
            String sign = cookieValue.split("-")[1];
            if (SecurityUtils.sign(username).equals(sign)) {
                Cookie cookie = new Cookie();
                cookie.name = "username";
                cookie.value = username;
                Response.current.get().cookies.put("username", cookie);
                return mi.proceed();
            } else {
                new FrameworkPage("Connection", form).go();
            }
        } else {
            new FrameworkPage("Connection", form).go();
        }
        // should never append
        return null;
    }
}
