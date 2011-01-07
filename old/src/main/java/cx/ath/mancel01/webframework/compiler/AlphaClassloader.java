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

package cx.ath.mancel01.webframework.compiler;

import cx.ath.mancel01.webframework.WebFramework;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mathieuancelin
 */
public class AlphaClassloader extends ClassLoader {

    private Map<String, Class<?>> applicationClasses;

    public AlphaClassloader() {
        super(AlphaClassloader.class.getClassLoader());
        applicationClasses = new HashMap<String, Class<?>>();
        findApplicationClasses();

    }

    public Collection<Class<?>> getApplicationClasses() {
        return applicationClasses.values();
    }

    @Override
    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        System.out.println("ask for " + name);
        Class<?> c = findLoadedClass(name);
        if (c != null) {
            return c;
        }
        if (!isCompilable(name)) {
            return super.loadClass(name, resolve);
        }
        System.out.println("compile for " + name);
        Class<?> clazz = loadApplicationClass(name);
        if (clazz != null) {
            if (resolve) {
                resolveClass(clazz);
            }
            return clazz;
        }
        return super.loadClass(name, resolve);
    }

    private boolean isCompilable(String name) {
        if(name.startsWith("app.model"))
            return false;
        return applicationClasses.containsKey(name);
    }

    private Class<?> loadApplicationClass(String name) {
        String path = name.replace(".", "/");
        RequestCompiler.compile(path);
        File clazzFile = new File(WebFramework.FWK_COMPILED_CLASSES_PATH, path + ".class");
        byte[] b = getClassDefinition(clazzFile);
        Class<?> clazz = defineClass(name, b, 0, b.length);
        resolveClass(clazz);
        return clazz;
    }

    private byte[] getClassDefinition(File file) {
        InputStream is = null;
        try {
            is = new FileInputStream(file);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(RequestCompiler.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (is == null) {
            return null;
        }
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int count;
            while ((count = is.read(buffer, 0, buffer.length)) > 0) {
                os.write(buffer, 0, count);
            }
            return os.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    private void findApplicationClasses() {
        List<String> classesNames = new ArrayList<String>();
        findClasses(classesNames, new File(WebFramework.JAVA_SOURCES, "app")); // TODO : remove app after maven refactoring
        for(String className : classesNames) {
            String name = className.replace(WebFramework.JAVA_SOURCES.getAbsolutePath() + "/", "").replace("/", ".").replace(".java", "");
            applicationClasses.put(name, null);
        }
        for(String name : applicationClasses.keySet()) {
            try {
                //String name = className.replace(WebFramework.JAVA_SOURCES.getAbsolutePath() + "/", "").replace("/", ".").replace(".java", "");
                //System.out.println("found " + name);
                applicationClasses.put(name, this.loadClass(name));
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void findClasses(List<String> builder, File file) {
        final File[] children = file.listFiles();
        if (children != null) {
            for (File f : children) {
                if (f.isDirectory()) {
                    findClasses(builder, f);
                }
                if (f.isFile()) {
                    if (f.getName().endsWith(".java")) {
                        builder.add(f.getAbsolutePath());
                    }
                }
            }
        }
    }
}
