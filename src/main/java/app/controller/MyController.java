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

import app.model.Person;
import app.services.Service;
import app.services.ServiceImpl;
import cx.ath.mancel01.webframework.view.Render;
import cx.ath.mancel01.webframework.view.View;
import cx.ath.mancel01.webframework.annotation.Controller;
import cx.ath.mancel01.webframework.http.Request;
import cx.ath.mancel01.webframework.view.Binary;
import cx.ath.mancel01.webframework.view.JSON;
import cx.ath.mancel01.webframework.view.Page;
import cx.ath.mancel01.webframework.view.Redirect;
import cx.ath.mancel01.webframework.view.XML;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author mathieuancelin
 */
@Controller
public class MyController {

    private Service service = new ServiceImpl();

//    @Inject
//    private Service service;

    @Inject
    private Request request;

    @Inject
    private EntityManager em;

    @Path("/")
    public View index() {
        List<String> numbers = new ArrayList<String>();
        numbers.add("one");
        numbers.add("two");
        numbers.add("three");
        em.persist(new Person("john", "doe", "null"));
        return new View()
                .param("message", service.hello("One eyed Jack"))
                .param("numbers", numbers).param("request", request);
    }

    @Path("/person/all")
    public void findall() {
        List<Person> persons = Person.jpa.all().getResultList();
        Render.view("findall.html").param("persons", persons).go();
    }

    @Path("/get/{id}")
    public void get(@PathParam("id") int id) {
        Render.text("ok : " + id).go();
    }

    @Path("/post")
    public void postValue(@FormParam("value") String value, @FormParam("value2") String value2) {
        Render.text("input was : " + value + " " + value2).go();
    }

    @Path("/put")
    @PUT
    @Consumes({MediaType.TEXT_PLAIN})
    public void putValue(String value) {
        System.out.println("send : " + value);
        Render.text("ok").go();
    }

    public View other() {
        List<String> numbers = new ArrayList<String>();
        numbers.add("four");
        numbers.add("five");
        numbers.add("six");
        return new View("index.html")
                .param("message", service.hello("Anonymous"))
                .param("numbers", numbers).param("request", request);
    }

    public void foo() {
        List<String> numbers = new ArrayList<String>();
        numbers.add("seven");
        numbers.add("height");
        numbers.add("nine");
        Render.view("index.html")
            .param("message", service.hello("foo"))
            .param("numbers", numbers).param("request", request)
            .go();
    }

    public XML personxml() {
        return Render.xml(new Person("john", "smith", "nowhere"));
    }

    public JSON personjson() {
        return Render.json(new Person("john", "smith", "nowhere"));
    }

    public Page text() {
        return Render.text("Hello World!");
    }

    public Binary file() {
        return Render.binary("pom.xml");
    }

    public Redirect test() {
        return Render.redirect("http://www.google.fr");
    }

    public void say() {
        Render.text("Hello billy boy :)").go();
    }
}
