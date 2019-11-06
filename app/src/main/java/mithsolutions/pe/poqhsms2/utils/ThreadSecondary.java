package mithsolutions.pe.poqhsms2.utils;

/**
 * Created by MITH on 20/10/2019.
 */

public abstract class ThreadSecondary extends Thread {
    public abstract void accion();
    @Override
    public void run() {
        accion();
    }
}