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

import cx.ath.mancel01.webframework.http.Request;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ws.rs.FormParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

/**
 *
 * @author mathieuancelin
 */
public class Param {

    public enum ParamType {
        PATH, QUERY, FORM
    }

    public static final Pattern PATH_PARAM_DECLARATION = Pattern.compile("\\{[a-zA-Zàáâãäåçèéêëìíîïðòóôõöùúûüýÿ0-9]+\\}");
    private final String name;
    private String prefix;
    private String suffix;
    private final String urlRoute;
    private final ParamType paramType;
    private final Class<?> type;

    public Param(Annotation annotation, String urlRoute, Class<?> type) {
        if (annotation instanceof PathParam) {
            this.name = ((PathParam) annotation).value();
            this.paramType = ParamType.PATH;
        } else if (annotation instanceof QueryParam) {
            this.name = ((QueryParam) annotation).value();
            this.paramType = ParamType.QUERY;
        } else if (annotation instanceof FormParam) {
            this.name = ((FormParam) annotation).value();
            this.paramType = ParamType.FORM;
        } else {
            this.name = ""; // TODO : remove that
            this.paramType = ParamType.QUERY;
        }
        this.urlRoute = urlRoute;
        this.type = type;
    }

    public void setPathParamName(Matcher matcher) {
        //this.name = matcher.group().replaceAll("\\{|\\}", "");
        prefix = urlRoute.substring(0, matcher.start());
        suffix = urlRoute.substring(matcher.end());
        prefix = replaceParamsWithWildcard(prefix);
        suffix = replaceParamsWithWildcard(suffix);
    }

    public String value(Request req) {
        if(this.paramType.equals(ParamType.PATH)) {
            if (matchesRoute(req.getPath(), urlRoute)) {
                return req.getPath().replaceAll(prefix, "").replaceAll(suffix, "");
            }
            throw new RuntimeException("url " + req.getPath() + "doesn't match route url pattern" + urlRoute);
        } else if (this.paramType.equals(ParamType.QUERY)) {
            return getQueryParam(this.name, req);
        } else {
            throw new RuntimeException("can't manage params of type : " + paramType);
        }
    }

    private String getQueryParam(String name, Request req) {
        List<String> values = Param.getQueryValues(name, "?" + req.querystring);
        if (!values.isEmpty()) {
            return values.iterator().next();
        }
        return "";
    }

    public String name() {
        return name;
    }

    public Class<?> type() {
        return type;
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
        return value.replaceAll("\\{[a-zA-Zàáâãäåçèéêëìíîïðòóôõöùúûüýÿ0-9]+\\}", "[a-zàáâãäåçèéêëìíîïðòóôõöùúûüýÿA-Z0-9]+");
    }
}
