package com.test;
import static java.lang.invoke.MethodHandles.lookup;
import java.lang.invoke.*;

public class MethodHandle {
    static class A {
        public void println(String a){
            System.out.println(a);
        }
    }

    public static void main(String[] args) throws Throwable {
        Object obj = System.currentTimeMillis() % 2 == 0 ? System.out : new A();
        getPrintlnMH(obj).invokeExact("test....");
    }

    private static java.lang.invoke.MethodHandle getPrintlnMH(Object receiver) throws Throwable {
        // first arg is the return type, others are the arguments for the method
        MethodType mt = MethodType.methodType(void.class, String.class);
        return lookup().findVirtual(receiver.getClass(), "println", mt).bindTo(receiver);

    }
}
