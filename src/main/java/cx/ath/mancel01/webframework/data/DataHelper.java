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

import java.util.Collection;
import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 *
 * @author mathieuancelin
 */
public class DataHelper<T> {

    private final Class<T> clazz;
    private final String name;

    private EntityManager em() {
        return JPAService.currentEm.get();
    }

    private DataHelper(Class<T> clazz) {
        this.clazz = clazz;
        this.name = clazz.getSimpleName();
    }

    public static <T> DataHelper<T> forType(Class<T> clazz) {
        return new DataHelper<T>(clazz);
    }

    public Query all() {
       return em().createQuery("select o from " + name + " o");
    }

    public long count() {
        return Long.parseLong(em().createQuery("select count(e) from " + name + " e").getSingleResult().toString());
    }

    public int deleteAll() {
        return em().createQuery("delete from " + name).executeUpdate();
    }

    public T delete(Object o) {
        em().remove(o);
        return (T) o;
    }

    public T findById(Object primaryKey) {
        return em().find(clazz, primaryKey);
    }

    public Query execDo(String criteria, Object... args) {
        final String queryName = name + "." + criteria;
        final Query query = em().createNamedQuery(queryName);
        for (int i = 0; i < args.length; i++) {
            query.setParameter(i + 1, args[i]);
        }
        return query;
    }

    public T refresh(Object o) {
        em().refresh(o);
        return (T) o;
    }

    public T save(Object o) {
        em().persist(o);
        return (T) o;
    }

    public T merge(Object o) {
        return (T) em().merge(o);
    }

}
