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
import cx.ath.mancel01.webframework.compiler.WebFrameworkClassLoader;
import java.lang.annotation.Annotation;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;
import javax.inject.Named;
import javax.inject.Provider;

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
        if (!WebFramework.proxyInjectionForCompilation) {
            return false;
        }
        if (!WebFramework.dev) {
            return false;
        }
        if (clazzHandler.get() != null) {
            return false;
        }
        if (instance.getClass().getName().contains("_javassist_")) {
            return false;
        }
        if (instance.getClass().getName().startsWith("app.")) {
            return true;
        }
        return false;
    }

    @Override
    public Object manipulateInstance(Object instance, Class interf, InjectorImpl injector, InjectionPoint point) {
        System.out.println("handle " + instance.getClass().getName() + " :)");
        Class<?> clazz = null;
        try {
            clazz = new WebFrameworkClassLoader().loadClass(instance.getClass().getName());
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
        if (clazz != null) {
            clazzHandler.set(clazz);
            Annotation qualifier = null;
            if (point != null)
                qualifier = ReflectionUtil.getQualifier(point.getAnnotations());
            Named named = null;
            if (point != null)
                named = ReflectionUtil.getNamed(point.getAnnotations());
            Annotation annotation = qualifier;
            String name = null;
            if (named != null) {
                annotation = named;
                name = named.value();
            }
            Binding b = injector.bindings().get(Binding.lookup(clazz, annotation));
            Provider p = null;
            if (b != null) {
                p = b.getProvider();
            }
            Class qualif = null;
            if (qualifier != null) {
                qualif = qualifier.annotationType();
            }
            Object obj = new Binding(qualif, name, clazz, clazz, p, null)
                    .getInstance(injector, point);
            MethodInvocationHandler handler = new MethodInvocationHandler(obj);
            ProxyFactory fact = new ProxyFactory();
            Class from = interf;
            Class to = clazz;
            if (from.isInterface()) {
                fact.setInterfaces(new Class[] {from});
            } 
            fact.setSuperclass(to);
            fact.setFilter(handler);
            Class newBeanClass = fact.createClass();
            Object scopedObject = null;
            try {
                scopedObject = newBeanClass.cast(newBeanClass.newInstance());
            } catch (Exception ex) {
                throw new IllegalStateException("Unable to create proxy for object " + from.getSimpleName(), ex);
            }
            ((ProxyObject) scopedObject).setHandler(handler);
            clazzHandler.set(null);
            return scopedObject;
        } else {
            return instance;
        }
    }
}
