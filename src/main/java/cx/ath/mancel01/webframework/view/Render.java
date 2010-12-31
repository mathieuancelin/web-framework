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

/**
 *
 * @author mathieuancelin
 */
public class Render implements ParameterizedRender {
    
    private View view;

    private Render() {}

    public static ParameterizedRender page(String name) {
        Render render = new Render();
        render.view = new View(name);
        return render;
    }

    public static ParameterizedRender withParam(String name, Object value) {
        return new Render();
    }

    @Override
    public ParameterizedRender with(String name, Object value) {
        view.param(name, value);
        return this;
    }

    @Override
    public void go() {
        throw new BreakFlowException(view);
    }

    public View getView() {
        return view;
    }
}
