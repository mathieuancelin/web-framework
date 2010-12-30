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
    public static boolean dev = false;
    public static String classpath = "";
    public static String compile = "javac -encoding utf-8 -source 1.6 -target 1.6 -d {1} -classpath {2} {3}";

    public static void init() {
        ClassLoader sysClassLoader = ClassLoader.getSystemClassLoader();
        URL[] urls = ((URLClassLoader)sysClassLoader).getURLs();
        StringBuilder builder = new StringBuilder();
        for(int i=0; i< urls.length; i++) {
            builder.append(urls[i].getFile());
            builder.append(":");
        }
        classpath = builder.toString();
        if (classpath.endsWith(":")) {
            classpath = classpath.substring(0, classpath.length() - 1);
        }
        compile = compile.replace("{2}", classpath);
        compile = compile.replace("{1}", new File("target/compclasses").getAbsolutePath());
        try {
            config.load(WebFramework.class.getClassLoader()
                    .getResourceAsStream("config.properties"));
            String mode = config.getProperty("framework.mode", "dev");
            if (mode.equals("dev")) {
                dev = true;
            } else {
                dev = false;
            }
        } catch (IOException e) {
            logger.error("Error while loading configuration file", e);
        }
    }

}
