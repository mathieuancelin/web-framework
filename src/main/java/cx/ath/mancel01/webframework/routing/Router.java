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

import cx.ath.mancel01.webframework.annotation.Controller;
import cx.ath.mancel01.webframework.http.Request;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import javax.ws.rs.FormParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

/**
 *
 * @author mathieuancelin
 */
public class Router {

    private Map<String, WebMethod> registeredControllers;
    private WebMethod rootController;

    public WebMethod route(Request request) {
        return null;
    }

    public void registrerController(Class<?> clazz) {
        if (clazz.isAnnotationPresent(Controller.class)) {
            String prefix = "";
            boolean isRootController = false;
            if (clazz.isAnnotationPresent(Path.class)) {
                Path path = clazz.getAnnotation(Path.class);
                prefix = path.value().trim();
                if (!prefix.startsWith("/")) {
                    prefix = "/" + prefix;
                }
                if ("/".equals(prefix)) {
                    isRootController = true;
                }
            } else {
                prefix = clazz.getSimpleName().toLowerCase();
            }
            for (Method method : clazz.getMethods()) {
                if (method.isAnnotationPresent(Path.class)) {
                    Path methodPath = method.getAnnotation(Path.class);
                    String value = methodPath.value().trim();
                    if (value.startsWith("/")) {
                        registerRoute(value, clazz, method, isRootController);
                    } else {
                        String url = "";
                        if (prefix.endsWith("/")) {
                            url = prefix + value;
                        } else {
                            url = prefix + "/" + value;
                        }
                        registerRoute(url, clazz, method, isRootController);
                        }
                } else {
                    String url = "";
                    if (prefix.endsWith("/")) {
                        url = prefix + method.getName();
                    } else {
                        url = prefix + "/" + method.getName();
                    }
                    registerRoute(url, clazz, method, isRootController);
                }
            }
        } else {
            throw new RuntimeException("You can't register a controller without @Controller annotation");
        }
    }

    private void registerRoute(String url, Class clazz, Method method, boolean isRoot) {
        WebMethod webMethod = new WebMethod();
        webMethod.setClazz(clazz);
        webMethod.setFullUrl(url);
        webMethod.setMethod(method);
        webMethod.setComparisonUrl(Param.replaceParamsWithWildcard(url));
        Annotation[][] annotations = method.getParameterAnnotations();
        if (annotations != null) {
            List<PathParam> pathParams = new ArrayList<PathParam>();
            List<QueryParam> queryParams = new ArrayList<QueryParam>();
            List<FormParam> formParams = new ArrayList<FormParam>();
            for (int i = 0; i < annotations.length; i++) {
                for (int j = 0; j < annotations[i].length; j++) {
                    Annotation annotation = annotations[i][j];
                    if (annotation instanceof PathParam) {
                        pathParams.add((PathParam) annotation);
                    }
                    if (annotation instanceof QueryParam) {
                        queryParams.add((QueryParam) annotation);
                    }
                    if (annotation instanceof FormParam) {
                        formParams.add((FormParam) annotation);
                    }
                }
            }
            for (QueryParam queryParam : queryParams) {
                webMethod.getQueryParamsNames().add(queryParam.value());
            }
            for (FormParam formParam : formParams) {
                webMethod.getFormParamsNames().add(formParam.value());
            }
            Matcher matcher = Param.PATH_PARAM_DECLARATION.matcher(url);
            while (matcher.find()) {
                Param param = new Param(url, matcher);
                webMethod.getPathParams().put(param.name(), param);
            }
            if (webMethod.getPathParams().size() != pathParams.size()) {
                throw new RuntimeException("the method " + method.getName()
                        + " doesn't have the right number of path params.");
            }
        }
        registeredControllers.put(Param.replaceParamsWithWildcard(url), webMethod);
        if (isRoot && method.getName().equals("index")) {
            rootController = webMethod;
        }
    }
}
