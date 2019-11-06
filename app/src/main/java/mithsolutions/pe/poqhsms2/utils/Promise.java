package mithsolutions.pe.poqhsms2.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by MITH on 21/10/2019.
 */

public class Promise {
    public static class Executor extends Thread {
        private ConcurrentLinkedQueue<Worker> workers;
        private Callback callback;
        private CountDownLatch latch;

        private Executor(List<Runnable> tasks, Callback callback) {
            super();
            this.callback = callback;
            workers = new ConcurrentLinkedQueue<>();
            latch = new CountDownLatch(tasks.size());

            for (Runnable task : tasks) {
                workers.add(new Worker(task, latch));
            }
        }

        public void execute() {
            start();
        }

        @Override
        public void run() {
            while (true) {
                Worker worker = workers.poll();
                if (worker == null) {
                    break;
                }
                worker.start();
            }

            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (callback != null) {
                callback.onComplete();
            }
        }

        public static class Builder {
            private List<Runnable> tasks = new ArrayList<>();
            private Callback callback;

            public Builder add(Runnable task) {
                tasks.add(task);
                return this;
            }

            public Builder callback(Callback callback) {
                this.callback = callback;
                return this;
            }

            public Executor build() {
                return new Executor(tasks, callback);
            }
        }

        public interface Callback {
            void onComplete();
        }
    }

    public static class Worker implements Runnable {

        private AtomicBoolean started;
        private Runnable task;
        private Thread thread;
        private CountDownLatch latch;

        public Worker(Runnable task, CountDownLatch latch) {
            this.latch = latch;
            this.task = task;
            started = new AtomicBoolean(false);
            thread = new Thread(this);
        }

        public void start() {
            if (!started.getAndSet(true)) {
                thread.start();
            }
        }

        @Override
        public void run() {
            task.run();
            latch.countDown();
        }
    }
}
