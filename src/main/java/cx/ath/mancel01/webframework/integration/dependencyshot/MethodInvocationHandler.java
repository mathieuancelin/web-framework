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

package cx.ath.mancel01.webframework.integration.dependencyshot;

import cx.ath.mancel01.dependencyshot.aop.annotation.ExcludeInterceptors;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;

/**
 *
 * @author Mathieu ANCELIN
 */
public class MethodInvocationHandler implements MethodFilter, MethodHandler {

    private final Object interceptedBean;

    public MethodInvocationHandler(Object interceptedBean) {
        this.interceptedBean = interceptedBean;
    }

    @Override
    public boolean isHandled(Method method) {
        return true;
    }

    @Override
    public Object invoke(Object self, Method method, Method procced, Object[] args) throws Throwable {
        return method.invoke(interceptedBean, args);
    }
}
