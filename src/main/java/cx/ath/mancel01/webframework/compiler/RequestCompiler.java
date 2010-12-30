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
package cx.ath.mancel01.webframework.compiler;

import cx.ath.mancel01.webframework.WebFramework;
import cx.ath.mancel01.webframework.util.FileUtils;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

/**
 *
 * @author mathieuancelin
 */
public class RequestCompiler {
//
//    public void init() {
//        JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
//        //int rc = javac.run(null, null, null, args);
//    }

    public static Class<?> compile(Class<?> clazz) {
        try {
            long start = System.currentTimeMillis();
            String path = clazz.getName().replace(".", "/");
            File oldClass = new File("target/compclasses/" + path + ".class");
            File source = new File("src/main/java/" + path + ".java");
            if (oldClass.exists()) {
                oldClass.delete();
            }
            String command = WebFramework.compile;
            Process p = Runtime.getRuntime().exec(command.replace("{3}", source.getAbsolutePath()));
            p.waitFor();
            ClassLoader loader = new ClassLoader(RequestCompiler.class.getClassLoader()) {

                @Override
                public Class<?> loadClass(String name) throws ClassNotFoundException {
                    if (name.startsWith("app.")) {
                        return findClass(name);
                    } else {
                        return super.loadClass(name);
                    }
                }
                
                @Override
                protected Class<?> findClass(String name) throws ClassNotFoundException {
                    if (name.startsWith("app.")) {
                        String path = name.replace(".", "/");
                        File clazz = new File("target/compclasses/" + path + ".class");
                        byte[] b = loadClassData(clazz);
                        return defineClass(name, b, 0, b.length);
                    } else {
                        return super.findClass(name);
                    }
                }

                private byte[] loadClassData(File file) {
                    return FileUtils.readFileAsString(file).getBytes();
                }
            };
            Class<?> loadedClazz = loader.loadClass(clazz.getName());
            WebFramework.logger.debug("controller compilation : {} ms."
                , (System.currentTimeMillis() - start));
            return loadedClazz;
        } catch (Exception ex) {
            Logger.getLogger(RequestCompiler.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
    }
}
