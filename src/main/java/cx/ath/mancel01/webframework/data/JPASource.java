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
package cx.ath.mancel01.webframework.data;

import app.model.Person;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import org.hibernate.ejb.Ejb3Configuration;
import org.hsqldb.Server;

/**
 *
 * @author mathieuancelin
 */
public class JPASource {

    private Server hsqlServer = null;

    public void launchTestServer() {
        try {
            File target = new File("target");
            if (!target.exists()) {
                target.mkdir();
            }
            File db = new File(target, "db");
            if (!db.exists()) {
                db.mkdir();
            }
            hsqlServer = new Server();
            hsqlServer.setLogWriter(null);
            hsqlServer.setSilent(true);
            hsqlServer.setDatabaseName(0, "webframeworkdb");
            hsqlServer.setDatabasePath(0, "file:target/db/webframeworkdb");
            hsqlServer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopTestServer() {
        if (hsqlServer != null) {
            hsqlServer.stop();
        }
    }

    public void launchJPA() throws Exception {
        System.setProperty("com.mchange.v2.log.MLog", "com.mchange.v2.log.FallbackMLog");
        System.setProperty("com.mchange.v2.log.FallbackMLog.DEFAULT_CUTOFF_LEVEL", "OFF");
        Ejb3Configuration cfg = new Ejb3Configuration();
        cfg.setProperty("hibernate.hbm2ddl.auto", "create-drop");
        cfg.setProperty("javax.persistence.transactionType", "RESOURCE_LOCAL");
        cfg.setProperty("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
        cfg.addAnnotatedClass(Person.class);
        ComboPooledDataSource source = new ComboPooledDataSource();
        source.setDriverClass("org.hsqldb.jdbcDriver");
        source.setJdbcUrl("jdbc:hsqldb:hsql://localhost/webframeworkdb");
        source.setUser("sa");
        source.setPassword("");
        source.setAcquireRetryAttempts(10);
        source.setCheckoutTimeout(5000);
        source.setBreakAfterAcquireFailure(false);
        source.setMaxPoolSize(30);
        source.setMinPoolSize(1);
        source.setIdleConnectionTestPeriod(10);
        source.setTestConnectionOnCheckin(true);
        cfg.setDataSource(source);
        EntityManagerFactory programmaticEmf = cfg.buildEntityManagerFactory();
        EntityManager manager = programmaticEmf.createEntityManager();
        Person person = new Person("john", "doe", "null");
        EntityTransaction tx = manager.getTransaction();
        tx.begin();
        manager.persist(person);
        manager.flush();
        tx.commit();
        manager.close();
        programmaticEmf.close();
        source.close();
    }

    public void testConnection() {
        Connection connection = null;
        try {
            Class.forName("org.hsqldb.jdbcDriver");
            connection = DriverManager.getConnection(
                    "jdbc:hsqldb:hsql://localhost/webframeworkdb", "sa", "");
            try {
                connection.prepareStatement("drop table testtable;").execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
            connection.prepareStatement(
                    "create table testtable ( id INTEGER, "
                    + "name VARCHAR);").execute();
            connection.prepareStatement(
                    "insert into testtable(id, name) "
                    + "values (1, 'testvalue');").execute();
            ResultSet rs = connection.prepareStatement(
                    "select * from testtable;").executeQuery();
            rs.next();
            System.out.println("Id: " + rs.getInt(1) + " Name: "
                    + rs.getString(2));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Closing the connection
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
