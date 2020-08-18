package com.example.qrcode.Image.ClipImage;

import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author jett
 * @since 2017-09-04.
 */
public class ThreadPool {

    private static final int CORE_POOL_SIZE = 3;
    private static final int MAX_POOL_SIZE = 20;
    private static final int ALIVE_TIME = 5;

    private static final ThreadPool instance;

    private final ThreadPoolExecutor pool;

    static {
        instance = new ThreadPool();
    }

    private ThreadPool() {
        pool = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, ALIVE_TIME, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(), new MyThreadFactory());
    }

    public static ThreadPool getInstance() {
        return instance;
    }

    public void execute(Runnable r) {
        pool.execute(r);
    }

    public Future<?> submit(Runnable r) {
        return pool.submit(r);
    }

    private class MyThreadFactory implements ThreadFactory {

        private final AtomicInteger mCount = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, "GWThreadPool-" + mCount.getAndIncrement());
            thread.setPriority(Thread.NORM_PRIORITY - 1);
            thread.setDaemon(false);
            return thread;
        }
    }

}
