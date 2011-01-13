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

import cx.ath.mancel01.webframework.annotation.Controller;
import cx.ath.mancel01.webframework.http.Cookie;
import cx.ath.mancel01.webframework.http.Response;
import cx.ath.mancel01.webframework.view.Render;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

@Controller
public class SecurityController {

    static final char[] HEX_CHARS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    @Inject
    private Response response;

    @Inject
    private Authenticator authenticator;

    @Path("/security/login")
    @POST
    public void login(@FormParam("user") String user,
            @FormParam("password") String password,
            @FormParam("url") String url, @FormParam("rememberme") String rememberme) {
        if (!authenticator.authenticate(user, password)) {
            Render.redirect("http://www.google.com").go();
        }
        Cookie cookie = new Cookie();
        cookie.name = "username";
        cookie.value = user;
        response.cookies.put("username", cookie);
        if(rememberme.equals("on")) {
            cookie = new Cookie();
            cookie.name = "rememberme";
            cookie.value = user + "-" + sign(user);
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
        Render.redirect("/").go(); // TODO: fix that
    }

    public static String sign(String message) {
        byte[] key = "oiuytredcvbhjnkjhgfdghjkjhgfghjkjhgfghjkjhgf".getBytes();
        if (key.length == 0) {
            return message;
        }
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            SecretKeySpec signingKey = new SecretKeySpec(key, "HmacSHA1");
            mac.init(signingKey);
            byte[] messageBytes = message.getBytes("utf-8");
            byte[] result = mac.doFinal(messageBytes);
            int len = result.length;
            char[] hexChars = new char[len * 2];
            for (int charIndex = 0, startIndex = 0; charIndex < hexChars.length;) {
                int bite = result[startIndex++] & 0xff;
                hexChars[charIndex++] = HEX_CHARS[bite >> 4];
                hexChars[charIndex++] = HEX_CHARS[bite & 0xf];
            }
            return new String(hexChars);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
