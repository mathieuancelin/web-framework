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

package app.controller;

import cx.ath.mancel01.webframework.view.View;
import cx.ath.mancel01.webframework.annotation.Controller;
import cx.ath.mancel01.webframework.http.Request;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.Path;

@Controller
public class MyController {

    @Inject
    private Request request;

    @Inject
    private EntityManager em;

    @Path("/")
    public View index() {
        return new View().param("request", request);
    }
}
