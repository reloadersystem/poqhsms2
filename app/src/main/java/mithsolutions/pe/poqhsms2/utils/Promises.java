package mithsolutions.pe.poqhsms2.utils;

/**
 * Created by MITH on 21/10/2019.
 */

/*
 * CRAWLINK Networks Pvt Ltd. CONFIDENTIAL
 * Copyright (c) 2017 All rights reserved.
 *
 * The source code contained or described herein and all documents
 * related to the source code ("Material") are owned by CRAWLINK
 * Networks Pvt Ltd. No part of the Material may be used, copied,
 * reproduced, modified, published, uploaded,posted, transmitted,
 * distributed, or disclosed in any way without CRAWLINK Networks
 * Pvt Ltd. prior written permission.
 *
 * No license under any patent, copyright, trade secret or other
 * intellectual property right is granted to or conferred upon you
 * by disclosure or delivery of the Materials, either expressly, by
 * implication, inducement, estoppel or otherwise. Any license
 * under such intellectual property rights must be express and
 * approved by CRAWLINK Networks Pvt Ltd. in writing.
 *
 */



        import android.os.Handler;
        import android.os.Looper;
        import android.util.Log;

        import java.util.ArrayList;
        import java.util.List;

/**
 * The Promises object represents the eventual completion (or failure)
 * of an asynchronous operation, and its resulting value.
 * <p>
 * A Promises is a proxy for a value not necessarily known when
 * the Promises is created. It allows you to associate handlers
 * with an asynchronous action's eventual success value or failure reason.
 * This lets asynchronous methods return values like synchronous methods:
 * instead of immediately returning the final value,
 * the asynchronous method returns a Promises to supply the value
 * at some point in the future.
 * <p>
 * For more information on Javascript Promises
 * please visit the official Mozilla Promises documentation
 *
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promises ">
 * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promises </a>
 * <p>
 * This Android Promises Library slightly different than the native Javascript Promises .
 * This Promises object has two imprtant method i.e. `resolve()` and `reject()`,
 * whenevey you done withe your process just call resolve or reject
 * function based on your state.
 * The resultant value will be automaticall passed as argument to the
 * followng `then()` or `error()` function.
 * <p>
 * You can write `n` numbers of `then()` chain.
 * <p>
 * It supports above JAVA 1.8
 */

public class Promises {

    private static final String TAG = "Promises ";
    private Handler handler;
    private OnSuccessListener onSuccessListener;
    private OnErrorListener onErrorListener;
    private Promises child;
    private boolean isResolved;
    private Object resolvedObject;
    private boolean isRejected;
    private Object rejectedObject;
    private Object tag;


    public Promises () {
        // Trigger all Promises calls in UI thread
        // if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
        //     this.handler = new Handler();
        // }

        this.handler = new Handler(Looper.getMainLooper());
    }

    public static Promises chain(Object obj) {
        Promises p = new Promises ();
        p.resolve(obj);
        return p;
    }

    public static Promises all(Promises ... list) {
        Promises p = new Promises ();
        int size = 0;
        if (list != null) {
            size = list.length;
        }
        Object results[] = new Object[size];
        if (list == null || list.length <= 0) {
            Log.w(TAG, "Promises list should not be empty!");
            p.resolve(results);
            return p;
        }

        if (list != null && list.length > 0) {
            new Runnable() {
                int completedCount = 0;

                @Override
                public void run() {
                    for (int i = 0; i < list.length; i++) {
                        Promises Promises = list[i];
                        Promises .setTag(i);
                        Promises .then(res -> {
                            results[(int) Promises .getTag()] = res;
                            completed(null);
                            return res;
                        }).error(err -> {
                            completed(err);
                        });
                    }
                }

                private void completed(Object err) {
                    completedCount++;
                    if (err != null) {
                        p.reject(err);
                    } else if (completedCount == list.length) {
                        p.resolve(results);
                    }
                }
            }.run();
        } else {
            Log.w(TAG, "Promises should not be empty!");
            p.resolve(results);
        }


        return p;
    }

    public static Promises series(List<?> list, OnSuccessListener listener) {
        Promises p = new Promises ();
        int size = 0;
        if (list != null) {
            size = list.size();
        }
        ArrayList<Object> results = new ArrayList<>(size);
        if (list == null || listener == null || list.size() <= 0) {
            Log.e(TAG, "Arguments should not be NULL!");
            p.resolve(results);
            return p;
        }

        new Runnable() {
            int index = -1;
            int completedCount = 0;

            @Override
            public void run() {
                index++;
                if (index < list.size()) {
                    handleSuccess(index, list.get(index));
                } else {
                    p.resolve(results);
                }
            }

            private void handleSuccess(int index, Object object) {
                Object res = listener.onSuccess(object);
                results.add(index, res);
                if (res instanceof Promises ) {
                    Promises pro = (Promises ) res;
                    pro.setTag(index);
                    pro.then(r -> {
                        results.set((int) pro.getTag(), r);
                        if (!completed(null)) {
                            run();
                        }
                        return r;
                    }).error(err -> completed(err));
                } else if (!completed(null)) {
                    run();
                }

            }

            private boolean completed(Object err) {
                completedCount++;
                if (err != null) {
                    p.reject(err);
                } else if (completedCount == list.size()) {
                    p.resolve(results);
                    return true;
                }
                return false;
            }

        }.run();

        return p;
    }

    public static Promises parallel(List<?> list, OnSuccessListener listener) {
        Promises p = new Promises ();
        int size = 0;
        if (list != null) {
            size = list.size();
        }
        ArrayList<Object> results = new ArrayList<>(size);
        if (list == null || listener == null || list.size() <= 0) {
            Log.e(TAG, "Arguments should not be NULL!");
            p.resolve(results);
            return p;
        }

        new Runnable() {
            int completedCount = 0;

            @Override
            public void run() {
                if (list.size() > 0) {
                    for (int i = 0; i < list.size(); i++) {
                        handleSuccess(i, list.get(i));
                    }
                } else {
                    p.resolve(results);
                }
            }

            private void handleSuccess(int index, Object object) {
                Object res = listener.onSuccess(object);
                results.add(index, res);
                if (res instanceof Promises ) {
                    Promises pro = (Promises ) res;
                    pro.setTag(index);
                    pro.then(r -> {
                        results.set((int) pro.getTag(), r);
                        completed(null);
                        return r;
                    }).error(err -> completed(err));
                } else {
                    completed(null);
                }

            }

            private void completed(Object err) {
                completedCount++;
                if (err != null) {
                    p.reject(err);
                } else if (completedCount == list.size()) {
                    p.resolve(results);
                }
            }

        }.run();

        return p;
    }

    /**
     * When you want to execute some operations parallelly with some parallel excution limit,
     * then you can use this function.
     *
     * @param list     List over which you want to execute operation.
     * @param limit    this is limit to your parallel execution.
     * @param listener
     * @return This will return the result after completion of entire execution.
     */
    public static Promises parallelWithLimit(List<?> list, int limit, OnSuccessListener listener) {

        Promises p = new Promises ();
        if (list == null || listener == null || list.size() <= 0) {
            Log.e(TAG, "Arguments should not be NULL!");
            return null;
        }

        if (limit <= 0) {
            Log.e(TAG, "Limit argument should not be <= ZERO!");
            return null;
        }

        int[] count = new int[1];
        int[] iteration = new int[1];
        ArrayList childList = new ArrayList<>();

        Promises
                .series(list, object -> {
                    Promises pro = new Promises ();
                    count[0]++;
                    iteration[0]++;
                    Log.i(TAG, "parallelWithLimit: iteration " + iteration[0]);
                    if (count[0] < limit) {
                        childList.add(object);
                        if (iteration[0] == list.size()) {
                            parallel(childList, listener)
                                    .then(res -> {
                                        pro.resolve(res);
                                        count[0] = 0;
                                        childList.clear();
                                        return res;
                                    });
                        } else {
                            pro.resolve(true);
                        }

                    } else if (count[0] == limit) {
                        Log.i(TAG, "parallelWithLimit: Executiong group paralely");
                        Log.i(TAG, "parallelWithLimit: ========================");
                        childList.add(object);
                        parallel(childList, listener)
                                .then(res -> {
                                    pro.resolve(res);
                                    count[0] = 0;
                                    childList.clear();
                                    return res;
                                });
                    }

                    return pro;

                })
                .then(res -> {
                    p.resolve(res);
                    return res;
                });

        return p;
    }

    /**
     * Call this function with your resultant value, it will be available
     * in following `then()` function call.
     *
     * @param object your resultant value (any type of data you can pass as argument
     *               e.g. int, String, List, Map, any Java object)
     * @return This will return the resultant value you passed in the function call
     */
    public Object resolve(Object object) {
        if (!isResolved) {
            isResolved = true;
            resolvedObject = object;
            if (onSuccessListener != null) {
                if (handler != null) {
                    handler.post(() -> handleSuccess(child, resolvedObject));
                } else {
                    new Thread(() -> handleSuccess(child, resolvedObject)).start();
                }
            }
        } else {
            Log.e(TAG, "The Promises already resolved, you can not resolve same Promises multiple time!");
        }
        return object;
    }

    /**
     * Call this function with your error value, it will be available
     * in following `error()` function call.
     *
     * @param object your error value (any type of data you can pass as argument
     *               e.g. int, String, List, Map, any Java object)
     * @return This will return the error value you passed in the function call
     */
    public Object reject(Object object) {
        if (!isRejected) {
            isRejected = true;
            rejectedObject = object;
            if (onErrorListener != null) {
                if (handler != null) {
                    handler.post(() -> handleError(onErrorListener));
                } else {
                    new Thread(() -> handleError(rejectedObject)).start();
                }
            } else {
                if (child != null) {
                    child.reject(object);
                }
            }
        } else {
            Log.e(TAG, "The Promises already rejected, you can not reject same Promises multiple time!");
        }
        return object;
    }

    /**
     * After executing asyncronous function the result will be available in the success listener
     * as argument.
     *
     * @param listener OnSuccessListener
     * @return It returns a Promises for satisfying next chain call.
     */

    public Promises then(OnSuccessListener listener) {
        onSuccessListener = listener;
        child = new Promises ();
        if (isResolved) {
            if (handler != null) {
                handler.post(() -> handleSuccess(child, resolvedObject));
            } else {
                new Thread(() -> handleSuccess(child, resolvedObject)).start();
            }
        }
        return child;
    }

    /**
     * This function must call at the end of the `then()` cain, any `reject()` occurs in
     * previous execution this function will be called.
     *
     * @param listener
     */
    public void error(OnErrorListener listener) {
        onErrorListener = listener;
        if (isRejected) {
            if (handler != null) {
                handler.post(() -> handleError(onErrorListener));
            } else {
                new Thread(() -> handleError(rejectedObject)).start();
            }
        }
    }

    private void handleSuccess(Promises child, Object object) {
        if (onSuccessListener != null) {
            Object res = onSuccessListener.onSuccess(object);
            if (res != null) {
                if (res instanceof Promises ) {
                    if (child != null) {
                        Promises p = (Promises ) res;
                        p.onSuccessListener = child.onSuccessListener;
                        p.onErrorListener = child.onErrorListener;
                        p.child = child.child;

                        if (p.isResolved) {
                            p.handleSuccess(p.child, p.resolvedObject);
                        } else if (p.isRejected) {
                            p.handleError(p.rejectedObject);
                        }
                    }
                } else if (child != null) {
                    child.resolve(res);
                }
            } else {
                if (child != null) {
                    child.resolve(res);
                }
            }
        }
    }

    private void handleError(Object object) {
        if (onErrorListener != null) {
            onErrorListener.onError(object);
        } else if (child != null) {
            child.reject(object);
        }
    }

    private Object getTag() {
        return tag;
    }

    private void setTag(Object tag) {
        this.tag = tag;
    }

    public interface OnSuccessListener {
        Object onSuccess(Object object);
    }

    public interface OnErrorListener {
        void onError(Object object);
    }


}