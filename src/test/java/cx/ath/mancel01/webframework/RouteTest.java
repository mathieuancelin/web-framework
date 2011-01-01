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

import cx.ath.mancel01.webframework.routing.Param;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Test;

/**
 *
 * @author mathieuancelin
 */
public class RouteTest {

    @Test
    public void findPathParam() {
        String urlRoute = "/person/{id}/info/{type}/important";
        String url = "/person/1234/info/civic/important";
        Pattern pattern = Pattern.compile("\\{[a-zA-Z0-9]+\\}");
        Matcher matcher = pattern.matcher(urlRoute);
        while (matcher.find()) {
            Param param = new Param(urlRoute, matcher);
            System.out.print("key : " + param.name() + ", ");
            System.out.println("value : " + param.value(url));
        }
    }

    @Test
    public void findQueryParam() {
        String url = "person/info/important?id=1234&type=civic";
        System.out.println("key : id, values : " + Param.getQueryValues("id", url));
        System.out.println("key : type, values : " + Param.getQueryValues("type", url));
    }
    
    private static void showParams(String url) {
        String[] urlParts = url.split("\\?");
        if (urlParts.length > 1) {
            String queryString = urlParts[1];
            if (!queryString.contains("?")) {
                String[] params = queryString.split("&");
                if (params != null) {
                    for (String param : params) {
                        String[] parts = param.split("=");
                        System.out.print("key : " + parts[0] + ", ");
                        if (parts.length > 1) {
                            System.out.println("value : " + parts[1]);
                        } else {
                            System.out.println("value :");
                        }
                    }
                }
            }
        }
    }
}
