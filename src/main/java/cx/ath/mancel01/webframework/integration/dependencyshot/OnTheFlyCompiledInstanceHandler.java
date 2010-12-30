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

import cx.ath.mancel01.dependencyshot.api.InjectionPoint;
import cx.ath.mancel01.dependencyshot.graph.Binding;
import cx.ath.mancel01.dependencyshot.injection.InjectorImpl;
import cx.ath.mancel01.dependencyshot.spi.ImplementationValidator;
import cx.ath.mancel01.dependencyshot.spi.InstanceHandler;
import cx.ath.mancel01.dependencyshot.util.ReflectionUtil;
import cx.ath.mancel01.webframework.WebFramework;
import cx.ath.mancel01.webframework.compiler.RequestCompiler;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mathieuancelin
 */
public class OnTheFlyCompiledInstanceHandler extends InstanceHandler {

    private static ThreadLocal<Class<?>> clazzHandler = new ThreadLocal<Class<?>>() {

        @Override
        protected Class<?> initialValue() {
            return null;
        }

    };

    private Method getBinding;

    public OnTheFlyCompiledInstanceHandler() {
        try {
            getBinding =
                    InjectorImpl.class.getDeclaredMethod("getBinding", Class.class, Annotation.class);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public <T extends ImplementationValidator> T getValidator() {
        return (T) new ImplementationValidator() {

            @Override
            public boolean isValid(Object o) {
                return isInstanceValid(o);
            }
        };
    }

    @Override
    public boolean isInstanceValid(Object instance) {
        if (!WebFramework.dev) {
            return false;
        }
        if (clazzHandler.get() != null) {
            return false;
        }
        if (instance.getClass().getName().startsWith("app.")) {
            return true;
        }
        return false;
    }

    @Override
    public Object manipulateInstance(Object instance, Class interf, InjectorImpl injector, InjectionPoint point) {
        System.out.println("handle :)");
        Annotation qualifier = ReflectionUtil.getQualifier(point.getAnnotations());
        Class<?> clazz = RequestCompiler.getCompiledClass(instance.getClass());
        try {
            getBinding.setAccessible(true);
            Binding binding = (Binding) getBinding.invoke(injector, interf, qualifier);
            getBinding.setAccessible(false);
            binding.setTo(clazz);
        } catch (Exception ex) {
            Logger.getLogger(OnTheFlyCompiledInstanceHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        //Binding targetBinding = injector.bindings().get(instance);
        
        clazzHandler.set(clazz);
        Object obj = injector.getInstance(clazz);
        clazzHandler.set(null);
        return obj;
    }

}
