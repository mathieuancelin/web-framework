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

import app.MyBinder;
import cx.ath.mancel01.webframework.sun.WebServer;
import java.io.File;
import java.io.IOException;
import org.junit.Test;

/**
 *
 * @author mathieuancelin
 */
public class TemplateTest {

    @Test
    public void template() throws Exception {
        
    }

    @Test
    public void httpServerTest() throws Exception {
        WebServer dispatcher =
                new WebServer("localhost", 8080,
                "/", new MyBinder(), new File("src/main/webapp"));
        dispatcher.start();
        //Thread.sleep(30000);
        dispatcher.stop();
    }

    public static void main(String... args) {
        try {
            WebServer dispatcher 
                    = new WebServer("localhost", 8080,
                    "/", new MyBinder(), new File("src/main/webapp"));
            dispatcher.start();
            System.out.println("press return key or Ctrl-C to stop the http server ...");
            char c = '\0';
            while ((c = (char) System.in.read()) !='\n') {
                System.out.println("return press catched !");
            }
            dispatcher.stop();
            System.exit(0);
        } catch (IOException ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }
}
