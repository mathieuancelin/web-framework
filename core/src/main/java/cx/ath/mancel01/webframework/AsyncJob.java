package cx.ath.mancel01.webframework;

import cx.ath.mancel01.webframework.http.Request;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 *
 * @author mathieu
 */
public abstract class AsyncJob<T> implements Callable<T> {

    public static ThreadLocal<AsyncJob> current
            = new ThreadLocal<AsyncJob>();

    static final ExecutorService exec = Executors.newFixedThreadPool(5);

    private Future<T> future;

    public static <T> void invokeAndWaitFor(AsyncJob<T> job) {
        Request request = Request.current.get();
        if (request.isNew) {
            job.invokeNow();
            job.waitFor();
        }
    }

    public static Object getCurrentJobValue() {
        return AsyncJob.current.get().get();
    }

    public static <T> T getCurrentJobValue(Class<T> type) {
        return type.cast(AsyncJob.current.get().get());
    }

    public void invokeNow() {
        this.future = exec.submit(this);
    }

    public final void waitFor() {
        throw new SuspendException(this, Request.current.get());
    }

    public final T get() {
        T get = null;
        try {
            get = future.get();
        } catch (Exception e) {
            WebFramework.logger.error("", e);
        }
        return get;
    }

    Future<T> getFuture() {
        return future;
    }
}
