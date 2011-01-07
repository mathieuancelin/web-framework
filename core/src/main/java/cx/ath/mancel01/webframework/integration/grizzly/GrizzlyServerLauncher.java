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

package cx.ath.mancel01.webframework.integration.grizzly;

import cx.ath.mancel01.webframework.WebFramework;
import cx.ath.mancel01.webframework.integration.dependencyshot.WebBinder;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author mathieuancelin
 */
public class GrizzlyServerLauncher {

    public static void main(String... args) {
        GrizzlyServer server = null;
        try {
            if (args.length < 1) {
                throw new RuntimeException("Can't work without the path of the app.");
            }
            long start = System.currentTimeMillis();
            server
                    = new GrizzlyServer(8080,
                    "/", WebBinder.BINDER_DEFAULT_NAME, new File(args[0]));
            server.start();
            WebFramework.logger.info("running the application in dev mode");
            WebFramework.logger.info("listening for HTTP on port 8080");
            WebFramework.logger.info("web-framework bootstraped in {} ms"
                    , (System.currentTimeMillis() - start));
            WebFramework.logger.info("press Ctrl-C to stop the http server ...\n\n");
            while(true) {
                Thread.sleep(500);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            if (server != null)
                server.stop();
            System.exit(1);
        }
    }
}
