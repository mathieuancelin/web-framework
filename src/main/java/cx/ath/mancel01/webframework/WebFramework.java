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

import java.io.IOException;
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
    public static boolean dev;

    static {
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
