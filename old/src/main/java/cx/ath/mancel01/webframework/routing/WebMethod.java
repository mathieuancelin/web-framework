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

import cx.ath.mancel01.webframework.data.JPAService;
import cx.ath.mancel01.webframework.exception.BreakFlowException;
import cx.ath.mancel01.webframework.http.Request;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;

/**
 *
 * @author mathieuancelin
 */
public class WebMethod {

    private Class<?> clazz;
    private Method method;
    private String fullUrl;
    private String comparisonUrl;
    private Map<String, Param> params = new HashMap<String, Param>();

    public Class<?> getClazz() {
        return clazz;
    }

    public void setClazz(Class<?> clazz) {
        this.clazz = clazz;
    }

    public String getComparisonUrl() {
        return comparisonUrl;
    }

    public void setComparisonUrl(String comparisonUrl) {
        this.comparisonUrl = comparisonUrl;
    }

    public String getFullUrl() {
        return fullUrl;
    }

    public void setFullUrl(String fullUrl) {
        this.fullUrl = fullUrl;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Map<String, Param> getParams() {
        return params;
    }

    public void setParams(Map<String, Param> params) {
        this.params = params;
    }

    public Object invoke(Request req, Object controller) {
        Object ret = null;
        try {
            List<Class<?>> types = new ArrayList<Class<?>>();
            List<Object> objects = new ArrayList<Object>();
            for(Param p : params.values()) {
                types.add(p.type());
                objects.add(p.value(req));
            }
            Object[] args = new Object[objects.size()];
            Class<?>[] argsTypes = new Class<?>[types.size()];
            args = objects.toArray(args);
            argsTypes = types.toArray(argsTypes);
            Method m = controller.getClass().getMethod(method.getName(), argsTypes);
            String pattern = "(";
            if (m.isAnnotationPresent(GET.class)) {
                pattern += "GET|";
            }
            if (m.isAnnotationPresent(POST.class)) {
                pattern += "POST|";
            }
            if (m.isAnnotationPresent(PUT.class)) {
                pattern += "PUT|";
            }
            if (m.isAnnotationPresent(DELETE.class)) {
                pattern += "DELETE";
            }
            pattern += ")";
            if (!pattern.equals("()")) {
                if (req.method.matches(pattern)) {
                    ret = m.invoke(controller, args);
                } else {
                    throw new RuntimeException("can't invoke "
                            + controller.getClass().getSimpleName()
                            + "." + m.getName() + " with a "
                            + req.method + " http method.");
                }
            } else {
                ret = m.invoke(controller, args);
            }
        } catch (Exception ex) {
            if (ex.getCause() instanceof BreakFlowException) {
                BreakFlowException br = (BreakFlowException) ex.getCause();
                ret = br.getRenderable();
            } else {
                JPAService.getInstance().stopTx(true);
                throw new RuntimeException(ex);
            }
        }
        return ret;
    }
}
