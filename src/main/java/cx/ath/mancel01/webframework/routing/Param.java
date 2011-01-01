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
package cx.ath.mancel01.webframework.routing;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author mathieuancelin
 */
public class Param {

    public static final Pattern PATH_PARAM_DECLARATION = Pattern.compile("\\{[a-zA-Z0-9]+\\}");
    private final String name;
    private String prefix;
    private String suffix;
    private final String urlRoute;

    public Param(String urlRoute, Matcher matcher) {
        this.urlRoute = urlRoute;
        this.name = matcher.group().replaceAll("\\{|\\}", "");
        prefix = urlRoute.substring(0, matcher.start());
        suffix = urlRoute.substring(matcher.end());
        prefix = replaceParamsWithWildcard(prefix);
        suffix = replaceParamsWithWildcard(suffix);
    }

    public String value(String url) {
        if (matchesRoute(url, urlRoute)) {
            return url.replaceAll(prefix, "").replaceAll(suffix, "");
        }
        throw new RuntimeException("url " + url + "doesn't match route url pattern" + urlRoute);
    }

    public String name() {
        return name;
    }

    public static List<String> getQueryValues(String name, String url) {
        List<String> values = new ArrayList<String>();
        Pattern pattern = Pattern.compile("[\\?&]" + name + "=([^&#]*)");
        Matcher matcher = pattern.matcher(url);
        while (matcher.find()) {
            values.add(matcher.group().replaceAll("[\\?&]" + name + "=", ""));
        }
        return values;
    }

    public static boolean matchesRoute(String url, String route) {
        return url.matches(Param.replaceParamsWithWildcard(route));
    }

    public static String replaceParamsWithWildcard(String value) {
        return value.replaceAll("\\{[a-zA-Z0-9]+\\}", "[a-zA-Z0-9]+");
    }
}
