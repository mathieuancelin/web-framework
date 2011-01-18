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
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

/**
 *
 * @author mathieuancelin
 */
public class RequestCompiler {

    private static JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
    private static Map<File, Long> sourceFiles = new HashMap<File, Long>();

    public static boolean compile(String clazz) {
        return compileClassIfChanged(clazz);
    }

    private static boolean compileClassIfChanged(String path) {
        long start = System.currentTimeMillis();
        File oldClass = new File(WebFramework.FWK_COMPILED_CLASSES_PATH, path + ".class");
        File source = new File(WebFramework.JAVA_SOURCES, path + ".java");
        boolean ret = false;
        if (!sourceFiles.containsKey(source)) {
            compile(source);
            sourceFiles.put(source, source.lastModified());
            ret = true;
        } else {
            long knownSourceModif = sourceFiles.get(source);
            if (knownSourceModif != source.lastModified()) {
                compile(source);
                sourceFiles.put(source, source.lastModified());
                ret = true;
            } else {
                if (!oldClass.exists()) {
                    compile(source);
                    sourceFiles.put(source, source.lastModified());
                    ret = true;
                }
                ret = false;
            }
        }
        if (ret) {
            WebFramework.logger.trace("class {} compilation : {} ms.", path.replace("/", "."), System.currentTimeMillis() - start);
        }
        return ret;
    }

    public static void compileSources(List<String> classes) {
        try {
            ArrayList<String> args = new ArrayList<String>();
            args.add("-encoding");
            args.add("utf-8");
            args.add("-source");
            args.add("1.6");
            args.add("-target");
            args.add("1.6");
            args.add("-Xlint:none");
            args.add("-d");
            args.add(WebFramework.FWK_COMPILED_CLASSES_PATH.getAbsolutePath());
            args.add("-classpath");
            args.add(WebFramework.classpath);
            args.addAll(classes);
            String[] argsTab = new String[args.size()];
            argsTab = args.toArray(argsTab);
            ErrorOutputStream err = new ErrorOutputStream();
            javac.run(null, null, err, argsTab);
            if (!err.toString().isEmpty()) {
                if (!err.toString().contains("Note: Recompile with -Xlint")) {// TODO : real analysis
                    throw new CompilationException(err.toString());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void findClasses(List<String> builder, File file) {
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

    private static void compile(File source) {
        try {
            ErrorOutputStream err = new ErrorOutputStream();
            javac.run(null, null, err, "-encoding", "utf-8", "-source", "1.6", "-target", "1.6", "-Xlint:none", "-d", WebFramework.FWK_COMPILED_CLASSES_PATH.getAbsolutePath(), "-sourcepath", WebFramework.JAVA_SOURCES.getAbsolutePath(), "-classpath", WebFramework.classpath, source.getAbsolutePath());
            if (!err.toString().isEmpty()) {
                if (!err.toString().contains("Note: Recompile with -Xlint")) // TODO : real analysis
                {
                    throw new CompilationException(err.toString());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
