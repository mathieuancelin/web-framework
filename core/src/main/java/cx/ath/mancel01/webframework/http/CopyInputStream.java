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
package cx.ath.mancel01.webframework.http;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author mathieuancelin
 */
public class CopyInputStream {

    private InputStream inputStream;
    private ByteArrayOutputStream copyStream = new ByteArrayOutputStream();

    public CopyInputStream(InputStream is) {
        inputStream = is;
        try {
            copy();
        } catch (IOException ex) {
            // do nothing
        }
    }

    private int copy() throws IOException {
        int read = 0;
        int chunk = 0;
        byte[] data = new byte[4096];
        while (-1 != (chunk = inputStream.read(data))) {
            read += data.length;
            copyStream.write(data, 0, chunk);
        }
        return read;
    }

    public InputStream getCopy() {
        return (InputStream) new ByteArrayInputStream(copyStream.toByteArray());
    }
}
