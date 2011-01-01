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

import cx.ath.mancel01.webframework.exception.BreakFlowException;
import cx.ath.mancel01.webframework.http.Request;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author mathieuancelin
 */
public class WebMethod {

    private Class<?> clazz;
    private Method method;
    private String fullUrl;
    private String comparisonUrl;
    private List<String> queryParamsNames = new ArrayList<String>();
    private List<String> formParamsNames = new ArrayList<String>();
    private Map<String, Param> pathParams = new HashMap<String, Param>();

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

    public List<String> getFormParamsNames() {
        return formParamsNames;
    }

    public void setFormParamsNames(List<String> formParamsNames) {
        this.formParamsNames = formParamsNames;
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

    public Map<String, Param> getPathParams() {
        return pathParams;
    }

    public void setPathParams(Map<String, Param> pathParams) {
        this.pathParams = pathParams;
    }

    public List<String> getQueryParamsNames() {
        return queryParamsNames;
    }

    public void setQueryParamsNames(List<String> queryParamsNames) {
        this.queryParamsNames = queryParamsNames;
    }

    public Object invoke(Request req, Object controller) {
        Object ret = null;
        try {
            //ret = method.invoke(controller);
            ret = controller.getClass().getMethod(method.getName()).invoke(controller);
        } catch (Exception ex) {
            if (ex.getCause() instanceof BreakFlowException) {
                BreakFlowException br = (BreakFlowException) ex.getCause();
                ret = br.getRenderable();
            } else {
                throw new RuntimeException(ex);
            }
        }
        return ret;
    }

}
