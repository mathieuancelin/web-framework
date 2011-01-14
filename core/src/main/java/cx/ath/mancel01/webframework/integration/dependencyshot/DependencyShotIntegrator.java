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

package cx.ath.mancel01.webframework.integration.dependencyshot;

import cx.ath.mancel01.webframework.http.Cookie;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.inject.Qualifier;
import cx.ath.mancel01.dependencyshot.graph.Binding;
import cx.ath.mancel01.dependencyshot.graph.BindingBuilder;
import cx.ath.mancel01.dependencyshot.injection.InjectorImpl;
import cx.ath.mancel01.webframework.WebFramework;
import cx.ath.mancel01.webframework.cache.CacheService;
import cx.ath.mancel01.webframework.data.JPAService;
import cx.ath.mancel01.webframework.data.JPAService.TxManager;
import cx.ath.mancel01.webframework.http.Request;
import cx.ath.mancel01.webframework.http.Response;
import cx.ath.mancel01.webframework.http.Session;
import java.sql.Connection;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.sql.DataSource;
import org.slf4j.Logger;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 *
 * @author mathieuancelin
 */
public class DependencyShotIntegrator {

    @Target({PARAMETER, FIELD })
    @Retention(RUNTIME)
    @Documented
    @Qualifier
    public @interface LoggedUsername {}

    @Target({PARAMETER, FIELD })
    @Retention(RUNTIME)
    @Documented
    @Qualifier
    public @interface SessionId {}

    private final InjectorImpl injector;

    public DependencyShotIntegrator(InjectorImpl injector) {
        this.injector = injector;
    }

    public void registerBindings() {
        Binding requestBinding =
            BindingBuilder.prepareBindingThat().bind(Request.class).providedBy(new Provider<Request>() {
                @Override
                public Request get() {
                    return Request.current.get();
                }
            }).build();
        Binding responseBinding =
            BindingBuilder.prepareBindingThat().bind(Response.class).providedBy(new Provider<Response>() {
                @Override
                public Response get() {
                    return Response.current.get();
                }
            }).build();
        Binding cacheBinding =
            BindingBuilder.prepareBindingThat().bind(CacheService.class).providedBy(new Provider<CacheService>() {
                @Override
                public CacheService get() {
                    return CacheService.getInstance();
                }
            }).build();
        Binding loggerBinding =
            BindingBuilder.prepareBindingThat().bind(Logger.class).providedBy(new Provider<Logger>() {
                @Override
                public Logger get() {
                    return WebFramework.logger;
                }
            }).build();
        Binding emBinding =
            BindingBuilder.prepareBindingThat().bind(EntityManager.class).providedBy(new Provider<EntityManager>() {
                @Override
                public EntityManager get() {
                    return JPAService.currentEm.get();
                }
            }).build();
        Binding datasourceBinding =
            BindingBuilder.prepareBindingThat().bind(DataSource.class).providedBy(new Provider<DataSource>() {
                @Override
                public DataSource get() {
                    return JPAService.getInstance().getDataSource();
                }
            }).build();
        Binding connectionBinding =
            BindingBuilder.prepareBindingThat().bind(Connection.class).providedBy(new Provider<Connection>() {
                @Override
                public Connection get() {
                    return JPAService.getInstance().getConnection();
                }
            }).build();
        Binding txManagerBinding =
            BindingBuilder.prepareBindingThat().bind(TxManager.class).providedBy(new Provider<TxManager>() {
                @Override
                public TxManager get() {
                    return JPAService.getInstance().getTxManager();
                }
            }).build();
        Binding sessionBinding =
            BindingBuilder.prepareBindingThat().bind(Session.class).providedBy(new Provider<Session>() {
                @Override
                public Session get() {
                    return Session.current.get();
                }
            }).build();
        Binding loggedUsernameBinding =
            BindingBuilder.prepareBindingThat().bind(String.class).annotatedWith(LoggedUsername.class).providedBy(new Provider<String>() {
                @Override
                public String get() {
                    Cookie cookie = Request.current.get().cookies.get("username");
                    if (cookie != null) {
                        return cookie.value;
                    }
                    return "anonymous";
                }
            }).build();
        Binding sessionIdBinding =
            BindingBuilder.prepareBindingThat().bind(String.class).annotatedWith(SessionId.class).providedBy(new Provider<String>() {
                @Override
                public String get() {
                    Cookie cookie = Request.current.get().cookies.get("webfwk-session-id");
                    if (cookie != null) {
                        return cookie.value;
                    }
                    return "none";
                }
            }).build();
        injector.bindings().put(cacheBinding, cacheBinding);
        injector.bindings().put(responseBinding, responseBinding);
        injector.bindings().put(requestBinding, requestBinding);
        injector.bindings().put(loggerBinding, loggerBinding);
        injector.bindings().put(emBinding, emBinding);
        injector.bindings().put(datasourceBinding, datasourceBinding);
        injector.bindings().put(connectionBinding, connectionBinding);
        injector.bindings().put(txManagerBinding, txManagerBinding);
        injector.bindings().put(sessionBinding, sessionBinding);
        injector.bindings().put(loggedUsernameBinding, loggedUsernameBinding);
        injector.bindings().put(sessionIdBinding, sessionIdBinding);
    }

}
