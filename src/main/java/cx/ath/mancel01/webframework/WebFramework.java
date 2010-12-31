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
package cx.ath.mancel01.webframework;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.FileAppender;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mathieuancelin
 */
public class WebFramework {

    public static final Logger logger = LoggerFactory.getLogger(WebFramework.class);
    public static final Properties config = new Properties();
    public static final File WEB_SOURCES = new File("src/main/webapp");
    public static final File JAVA_SOURCES = new File("src/main/java");
    public static final File RESOURCES = new File("src/main/resources");
    public static final File TARGET = new File("target");
    public static final File MVN_COMPILED_CLASSES_PATH = new File("target/classes");
    public static final File FWK_COMPILED_CLASSES_PATH = new File("target/compclasses");
    public static boolean dev = false;
    public static boolean proxyInjectionForCompilation = false;
    public static boolean recompileServices = true;
    public static String classpath = "";

    public static void init() {

        initClasspath();
        initConfig();
        initLogger();
        if (dev) {
            if (!FWK_COMPILED_CLASSES_PATH.exists()) {
                FWK_COMPILED_CLASSES_PATH.mkdir();
            }
        }
    }
    
    private static void initClasspath() {
        ClassLoader sysClassLoader = ClassLoader.getSystemClassLoader();
        URL[] urls = ((URLClassLoader) sysClassLoader).getURLs();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < urls.length; i++) {
            builder.append(urls[i].getFile());
            builder.append(":");
        }
        classpath = builder.toString();
        if (classpath.endsWith(":")) {
            classpath = classpath.substring(0, classpath.length() - 1);
        }
    }

    private static void initConfig() {
        try {
            config.load(WebFramework.class.getClassLoader().getResourceAsStream("config.properties"));
            String mode = config.getProperty("framework.mode", "dev");
            if (mode.equals("dev")) {
                dev = true;
            } else {
                dev = false;
            }
            String recServ = config.getProperty("framework.recompile.services", "false");
            if (recServ.equals("true")) {
                recompileServices = true;
            } else {
                recompileServices = false;
            }
        } catch (IOException e) {
            logger.error("Error while loading configuration file", e);
        }
    }

    private static void initLogger() {
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        lc.stop(); lc.reset();
        ch.qos.logback.classic.Logger backLogger = (ch.qos.logback.classic.Logger) logger;
        ConsoleAppender consoleAppender = new ConsoleAppender();
        consoleAppender.setContext(lc);
        PatternLayout pl = new PatternLayout();
        pl.setPattern("%d{HH:mm:ss.SSS} %-5level - %msg%n");
        pl.setContext(lc);
        pl.start();
        consoleAppender.setLayout(pl);
        consoleAppender.start();
        backLogger.detachAndStopAllAppenders();
        if (!WebFramework.dev) {
            FileAppender fileAppender = new FileAppender();
            fileAppender.setContext(lc);
            pl = new PatternLayout();
            pl.setPattern("%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n");
            pl.setContext(lc);
            pl.start();
            fileAppender.setFile(TARGET.getAbsolutePath());
            fileAppender.setLayout(pl);
            fileAppender.start();
            backLogger.addAppender(fileAppender);
        }
        backLogger.addAppender(consoleAppender);
        backLogger.setLevel(Level.valueOf(config.getProperty("framework.logger.level", "info")));
        lc.start();
    }

    //public static String compile = "javac -encoding utf-8 -source 1.6 -target 1.6 -d {1} -classpath {2} {3}";
    //public static String compile = "-encoding utf-8 -source 1.6 -target 1.6 -d {1} -classpath {2}";
    //compile = compile.replace("{2}", classpath);
    //compile = compile.replace("{1}", new File("target/compclasses").getAbsolutePath());
}
