package org.learning.metric;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Counter;
import com.yammer.metrics.reporting.ConsoleReporter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by anderson on 17-11-20.
 * Counter与Gauge类似, 也用来度量数字值, 但是可以通过inc(), dec(), clear()等方法修改其度量值, 一般用来记录某实际的发生次数或是请求个数.Counter底层是通过AtomicLong实现的
 * 示例:
 * 17-11-20 18:07:56 ==============================================================
 * org.learning.metric.MainCounter:
 * testCounter:
 * count = 4
 */
public class MainCounter
{
    // 创建Counter对象
    private final Counter testCounter = Metrics.newCounter(MainCounter.class, "testCounter");
    private final List<String> list = new ArrayList<String>();

    public void add(String str) {
        // 增加Counter度量的值
        testCounter.inc();
        list.add(str);
    }

    public String take() {
        // 减小Counter度量的值
        testCounter.dec();
        return list.remove(0);
    }
    public static void main(String[] args)
            throws InterruptedException
    {
        MainCounter tc = new MainCounter();
        // 每一秒输出一次
        ConsoleReporter.enable(1, TimeUnit.SECONDS);
        while (true) {
            tc.add("s");
            Thread.sleep(1000L);
        }
    }
}
