/*
 *  Copyright 2011 mathieu.
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
package cx.ath.mancel01.webframework;

import cx.ath.mancel01.webframework.http.Request;

/**
 *
 * @author mathieu
 */
public class SuspendException extends RuntimeException {

    private final AsyncJob job;
    private final Request req;

    public SuspendException(AsyncJob job, Request req) {
        this.job = job;
        this.req = req;
    }

    public Request getReq() {
        return req;
    }

    public AsyncJob getJob() {
        return job;
    }

    public void waitFor() {
        while (!job.getFuture().isDone()) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ex) {
                WebFramework.logger.error("Thread interruption while waiting for job to end. ", ex);
            }
        }
        req.isNew = false;
        Request.current.set(req);
        AsyncJob.current.set(job);
    }
}
