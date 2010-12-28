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

package app.binder;

import app.controller.MyController;
import app.services.Service;
import app.services.ServiceImpl;
import cx.ath.mancel01.webframework.WebBinder;

/**
 *
 * @author mathieuancelin
 */
public class MyBinder extends WebBinder {

    @Override
    public void configureBindings() {
        bind(Service.class).to(ServiceImpl.class);
        registerRootController(MyController.class);
    }

}
