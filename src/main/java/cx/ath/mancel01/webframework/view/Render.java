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
import cx.ath.mancel01.webframework.http.StatusCodes;
import java.io.File;

/**
 *
 * @author mathieuancelin
 */
public class Render implements ParameterizedRender {
    
    private View view;

    private Render() {}

    public static Redirect redirect(String url) {
        return new Redirect(url);
    }

    public static Page text(final String text) {
        return new Page() {

            @Override
            public int getStatusCode() {
                return StatusCodes.OK;
            }

            @Override
            public String getContentType() {
                return "text/plain";
            }

            @Override
            public String getMessage() {
                return text;
            }

        };
    }

    public static Binary binary(String file) {
        return new Binary(file);
    }

    public static Binary binary(File file) {
        return new Binary(file);
    }

    public static JSON json(Object json) {
        return new JSON(json);
    }

    public static XML xml(Object xml) {
        return new XML(xml);
    }

    public static Page notFound() {
        return new Page() {

            @Override
            public int getStatusCode() {
                return StatusCodes.NOT_FOUND;
            }

            @Override
            public String getContentType() {
                return "text/html";
            }

            @Override
            public String getMessage() {
                return "<html><head><title>Page not found</title></head>"
                        + "<body><h1>Page not found</h1></body></html>";
            }

        };
    }

    public static Page badRequest() {
        return new Page() {

            @Override
            public int getStatusCode() {
                return StatusCodes.BAD_REQUEST;
            }

            @Override
            public String getContentType() {
                return "text/html";
            }

            @Override
            public String getMessage() {
                return "<html><head><title>Bad request</title></head>"
                        + "<body><h1>Bad request</h1></body></html>";
            }

        };
    }

    public static Page error() {
        return new Page() {

            @Override
            public int getStatusCode() {
                return StatusCodes.INTERNAL_ERROR;
            }

            @Override
            public String getContentType() {
                return "text/html";
            }

            @Override
            public String getMessage() {
                return "<html><head><title>Error</title></head>"
                        + "<body><h1>Error</h1></body></html>";
            }

        };
    }

    public static Page accesDenied() {
        return new Page() {

            @Override
            public int getStatusCode() {
                return StatusCodes.FORBIDDEN;
            }

            @Override
            public String getContentType() {
                return "text/html";
            }

            @Override
            public String getMessage() {
                return "<html><head><title>Acces denied</title></head>"
                        + "<body><h1>Access Denied</h1></body></html>";
            }

        };
    }

    public static Page todo() {
        return new Page() {

            @Override
            public int getStatusCode() {
                return StatusCodes.NOT_IMPLEMENTED;
            }

            @Override
            public String getContentType() {
                return "text/html";
            }

            @Override
            public String getMessage() {
                return "<html><head><title>TODO</title></head>"
                        + "<body><h1>Page not yet implemented</h1></body></html>";
            }

        };
    }

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
