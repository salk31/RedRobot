package com.redspr.redrobot;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.Alert;
import org.openqa.selenium.Beta;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.TargetLocator;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.security.Credentials;

public class Foo {

    static Set<Class> proxy = new HashSet();
    static {
        proxy.add(TargetLocator.class);
        proxy.add(WebDriver.class);

        new Thread(new PrimeRun()).start();
    }

    static Class[] proxy(Object i) {
        if (i != null && Proxy.isProxyClass(i.getClass())) return null;
        if (i instanceof TargetLocator) return new Class[]{TargetLocator.class};
        if (i instanceof WebDriver) return new Class[]{JavascriptExecutor.class, WebDriver.class};
        return null;
    }

    static LinkedBlockingQueue<Call> todo = new LinkedBlockingQueue();
    static LinkedBlockingQueue<Call> done = new LinkedBlockingQueue();


    static class PrimeRun implements Runnable {
 Map proxyToTarget = new IdentityHashMap();
        @Override
        public void run() {
            System.out.println("Thread start");
            while (true) {
                try {


                    Call call = todo.poll(100, TimeUnit.SECONDS);
                    System.out.println("Thread got task " + call.method);

Object obj2 = proxyToTarget.get(call.obj);
if (obj2 != null) {
    call.obj = obj2;
}


                    Object  ret;
                    if (call.obj == null) {
                        proxyToTarget.clear();
                        HtmlUnitDriver d = new HtmlUnitDriver();
                        d.setJavascriptEnabled(true);
                        ret = d;
                    }

                    else if ("alert".equals(call.method.getName())) {
                        ret = new Alert() {

                            @Override
                            public void dismiss() {
                                // TODO Auto-generated method stub

                                //throw new RuntimeException("not Alert.dismiss implemented");

                            }

                            @Override
                            public void accept() {
                                // TODO Auto-generated method stub

                                throw new RuntimeException("not Alert.accept implemented");

                            }

                            @Override
                            public String getText() {
                                // TODO Auto-generated method stub

                                throw new NoAlertPresentException();

                            }

                            @Override
                            public void sendKeys(String keysToSend) {
                                // TODO Auto-generated method stub

                                throw new RuntimeException("not Alert.sendKeys implemented");

                            }

                            @Override
                            @Beta
                            public void authenticateUsing(Credentials credentials) {
                                // TODO Auto-generated method stub

                                throw new RuntimeException("not Alert.authenticateUsing implemented");

                            }

                        };
                    } else {


                    ret = call.method.invoke(call.obj, call.args);

                    }

                    if (ret != null) {
                        Class[] clazz = proxy(ret);
                        if (clazz != null) {
                            ret = createProxy(clazz, ret);
                        }
                    }
                    System.out.println("Thread put result " );

                    if (!call.isAsync()) {
                    call.result = ret;

                    done.add(call);
                    }

                } catch (Throwable th) {
                    th.printStackTrace();
                }

                }
        }

        Object createProxy(Class[] clazz, final Object target) {
            if (target != null && Proxy.isProxyClass(target.getClass())) {
                throw new RuntimeException("Proxy a proxy!");
        }

            InvocationHandler handler = new InvocationHandler() {

                @Override
                public Object invoke(Object obj, Method method, Object[] args)
                        throws Throwable {
                    Call call = new Call();
                    call.obj = obj;
                    call.method = method;
                    call.args = args;

                    todo.add(call);

                    Call call2 = done.poll(100, TimeUnit.SECONDS);



                    return call2.result;
                };
            };

            Object p = Proxy.newProxyInstance(
                    WebDriver.class.getClassLoader(),
                     clazz , handler);
proxyToTarget.put(p, target);
return p;
        }
    }

    static class Call {
        Object obj;
        Method method;
        Object[] args;
        Object result;

        boolean isAsync() {
            return method != null && Void.class.equals(method.getReturnType());
        }
    }

    static WebDriver foo() {




        Call call = new Call();
        todo.add(call);
if (!call.isAsync()) {
        try {
            done.poll(100, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
}
        return (WebDriver) call.result;
    };


}
