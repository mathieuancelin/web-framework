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
import cx.ath.mancel01.webframework.http.Cookie;
import cx.ath.mancel01.webframework.http.Request;
import cx.ath.mancel01.webframework.http.Response;
import cx.ath.mancel01.webframework.http.Session;
import cx.ath.mancel01.webframework.util.SecurityUtils;
import cx.ath.mancel01.webframework.view.Render;
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
        if (req.cookies.containsKey(Session.USERNAME)) {
            return mi.proceed();
        } else if (req.cookies.containsKey(Session.REMEMBERME)) {
            String cookieValue = req.cookies.get(Session.REMEMBERME).value;
            String username = cookieValue.split("-")[0];
            String sign = cookieValue.split("-")[1];
            if (SecurityUtils.sign(username).equals(sign)) {
                Cookie cookie = new Cookie();
                cookie.name = Session.USERNAME;
                cookie.value = username;
                Response.current.get().cookies.put(Session.USERNAME, cookie);
                return mi.proceed();
            } else {
                Session.current.get().put("callbackUrl", req.path);
                if (!"/".equals(WebFramework.contextRoot)) {
                    Render.redirect(WebFramework.contextRoot
                            + "/security/loginpage").go();
                }
                Render.redirect("/security/loginpage").go();
            }
        } else {
            Session.current.get().put("callbackUrl", req.path);
            if (!"/".equals(WebFramework.contextRoot)) {
                Render.redirect(WebFramework.contextRoot
                        + "/security/loginpage").go();
            }
            Render.redirect("/security/loginpage").go();
        }
        // should never append
        return null;
    }
}
