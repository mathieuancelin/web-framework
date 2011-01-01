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

import java.util.ArrayList;
import java.util.List;
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
            if (url.matches(replaceParamsWithWildcard(urlRoute))) {
                Param param = new Param(urlRoute, matcher);
                System.out.print("key : " + param.name() + ", ");
                System.out.println("value : " + param.value(url));
            } else {
                System.out.println("given url doesn't match route url !");
            }
        }
    }

    @Test
    public void findQueryParam() {
        String url = "person/info/important?id=1234&type=civic";
        System.out.println("key : id, values : " + getParamValues("id", url));
        System.out.println("key : type, values : " + getParamValues("type", url));
    }

    private static List<String> getParamValues(String name, String url) {
        List<String> values = new ArrayList<String>();
        Pattern pattern = Pattern.compile("[\\?&]" + name + "=([^&#]*)");
        Matcher matcher = pattern.matcher(url);
        while (matcher.find()) {
            values.add(matcher.group().replaceAll("[\\?&]" + name + "=", ""));
        }
        return values;
    }

    private class Param {

        private final String name;
        private String prefix;
        private String suffix;

        public Param(String urlRoute, Matcher matcher) {
            this.name = matcher.group().replaceAll("\\{|\\}", "");
            prefix = urlRoute.substring(0, matcher.start());
            suffix = urlRoute.substring(matcher.end());
            prefix = replaceParamsWithWildcard(prefix);
            suffix = replaceParamsWithWildcard(suffix);
        }

        public String value(String url) {
            return url.replaceAll(prefix, "").replaceAll(suffix, "");
        }

        public String name() {
            return name;
        }
    }
    
    private static String replaceParamsWithWildcard(String value) {
        return value.replaceAll("\\{[a-zA-Z0-9]+\\}", "[a-zA-Z0-9]+");
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
