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

import cx.ath.mancel01.webframework.WebFramework;
import cx.ath.mancel01.webframework.http.Response;
import java.io.ByteArrayOutputStream;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

/**
 *
 * @author mathieuancelin
 */
public class XML extends Renderable {

    private final Object xmlObject;

    public XML(Object xmlObject) {
        this.xmlObject = xmlObject;
        this.contentType = MediaType.APPLICATION_XML;
    }

    public Object getXmlObject() {
        return xmlObject;
    }

    public Class<?> getXmlObjectClass() {
        return xmlObject.getClass();
    }

    @Override
    public Response render() {
        try {
            long start = System.currentTimeMillis();
            Response res = Response.current.get();
            res.out = new ByteArrayOutputStream();
            res.contentType = this.getContentType();
            JAXBContext context = JAXBContext.newInstance(this.getXmlObjectClass());
            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            m.marshal(this.getXmlObject(), res.out);
            WebFramework.logger.trace("XML object rendering : {} ms.", System.currentTimeMillis() - start);
            return res;
        } catch (JAXBException ex) {
            throw new RuntimeException(ex);
        }
    }

}
