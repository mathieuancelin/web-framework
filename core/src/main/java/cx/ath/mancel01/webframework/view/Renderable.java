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

package cx.ath.mancel01.webframework.view;

import cx.ath.mancel01.webframework.exception.BreakFlowException;
import cx.ath.mancel01.webframework.http.Response;
import cx.ath.mancel01.webframework.http.StatusCodes;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author mathieuancelin
 */
public abstract class Renderable {

    protected int statusCode = StatusCodes.OK;
    protected String contentType = MediaType.APPLICATION_OCTET_STREAM;

    public int getStatusCode() {
        return statusCode;
    }

    public String getContentType() {
        return contentType;
    }

    public abstract Response render();

    public void go() {
        throw new BreakFlowException(this);
    }

}
