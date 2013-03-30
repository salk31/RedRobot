package com.redspr.redrobot;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.TargetLocator;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import com.gargoylesoftware.htmlunit.AlertHandler;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;

public class Foo {

    static Set<Class> proxy = new HashSet();
    static {
        proxy.add(TargetLocator.class);
        proxy.add(WebDriver.class);

        new Thread(new PrimeRun()).start();
    }

    // TODO 00 handle collection!

    static Class[] proxy(Object i) {
      //  if (i != null && Proxy.isProxyClass(i.getClass()))
       //     return null;
        if (i instanceof List)
            return new Class[] { List.class };
        if (i instanceof WebElement)
            return new Class[] { WebElement.class };
        if (i instanceof TargetLocator)
            return new Class[] { TargetLocator.class };
        if (i instanceof WebDriver)
            return new Class[] { JavascriptExecutor.class, WebDriver.class };
        return null;
    }

    static LinkedBlockingQueue<Call> todo = new LinkedBlockingQueue();
    static LinkedBlockingQueue<Call> done = new LinkedBlockingQueue();

    static class PrimeRun implements Runnable {
        Map proxyToTarget = new IdentityHashMap();

        Bar alert;

        @Override
        public void run() {
            System.out.println("Thread start");
            while (true) {
                Call call = null;
                try {

                    call = todo.poll(100, TimeUnit.SECONDS);
                    System.out.println("Thread got task " + call.method);

                    Object obj2 = proxyToTarget.get(call.obj);
                    if (obj2 != null) {
                        call.obj = obj2;
                    }

                    Object ret;
                    if (call.obj == null) {
                        proxyToTarget.clear();
                        HtmlUnitDriver d = new HtmlUnitDriver() {
                            @Override
                            protected WebClient modifyWebClient(WebClient client) {
                                client.setAlertHandler(new AlertHandler() {
                                    @Override
                                    public void handleAlert(Page arg0,
                                            String arg1) {
                                        alert = new Bar(arg1);
                                        System.out.println("Got alert " + arg1);
                                        try {
                                            alert.semaphore.poll(100, TimeUnit.SECONDS);
                                        } catch (InterruptedException e) {
                                            // TODO Auto-generated catch block
                                            e.printStackTrace();
                                        }
                                        alert = null;
                                        // TODO block!
                                    }
                                });
                                return client;
                            }
                        };
                        d.setJavascriptEnabled(true);


                        ret = d;

                    } else {
                        ret = call.method.invoke(call.obj, call.args);
                    }

                    if (ret != null) {
                        Class[] clazz = proxy(ret);
                        if (clazz != null) {
                            ret = createProxy(clazz, ret);
                        }

                    }
                    System.out.println("Thread put result ");

                    call.result = ret;

                } catch (Throwable th) {
                    th.printStackTrace();
                    call.throwable = th;
                }
                if (!call.isAsync()) {
                    done.add(call);
                }
            }
        }

        Object createProxy(Class[] clazz, final Object target) {
            if (target instanceof List) {
                List l = (List) target;
                for (int i = 0; i < l.size(); i++) {
                    l.set(i, createProxy(new Class[]{WebElement.class}, l.get(i)));
                }
                return target;
            }


            InvocationHandler handler = new InvocationHandler() {

                @Override
                public Object invoke(Object obj, Method method, Object[] args)
                        throws Throwable {


                if ("alert".equals(method.getName())) {
Thread.sleep(1000);
                    if (alert == null) {
                        throw new NoAlertPresentException();
                    }
                    return alert;
                }


                    Call call = new Call();
                    call.obj = obj;
                    call.method = method;
                    call.args = args;



                    todo.add(call);
                    if (call.isAsync()) {
                        return null;
                    }
                    Call call2 = done.poll(100, TimeUnit.SECONDS);

                    if (call2.throwable != null) {
                        throw call2.throwable;
                    }

                    return call2.result;
                };
            };

            Object p = Proxy.newProxyInstance(WebDriver.class.getClassLoader(),
                    clazz, handler);
            proxyToTarget.put(p, target);
            return p;
        }
    }

    static class Call {
        Object obj;
        Method method;
        Object[] args;
        Object result;
        Throwable throwable;

        boolean isAsync() {
            return method != null && Void.TYPE.equals(method.getReturnType());
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
