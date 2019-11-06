package mithsolutions.pe.poqhsms2.utils;

/**
 * Created by MITH on 20/10/2019.
 */

public class  Callback{
    @FunctionalInterface
    public interface Void <T>{
        void call(T t) throws Exception;
    }
    @FunctionalInterface
    public interface Function <T>{
        T call() throws Exception;
    }
}

