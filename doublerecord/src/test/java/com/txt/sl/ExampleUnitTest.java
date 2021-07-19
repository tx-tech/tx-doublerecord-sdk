package com.txt.sl;

import org.junit.Test;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    private final String hello = "hello";

    @Test
    public void addition_isCorrect() throws Exception {
        getLock1();
        getLock2();
    }

    private void getLock1() {
        Thread thread1 = new Thread(() -> {
            synchronized (hello) {
                System.out.println("ThreadA 拿到了内部锁");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("ThreadA 释放了内部锁");
        });
        thread1.start();
    }

    private void getLock2() {
        Thread thread2 = new Thread(() -> {
            System.out.println("ThreadB 尝试获取内部锁");
            synchronized (hello) {
                System.out.println("ThreadB 拿到了内部锁");
            }
            System.out.println("ThreadB 继续执行");
        });
        thread2.start();
    }
}