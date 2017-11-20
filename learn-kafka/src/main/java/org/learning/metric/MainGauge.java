package org.learning.metric;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.reporting.ConsoleReporter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by anderson on 17-11-20.
 * Gauge是Metrics中最简单的度量类型, 它用于记录瞬时值, 只有一个简单的返回值,
 * 示例:
 * 17-11-20 17:16:27 ==============================================================
 * org.learning.metric.MainGauge:
 * testGauge:
 * value = 11
 */
public class MainGauge
{
    public static void main(String[] args)
            throws InterruptedException
    {
        final List<String> list = new ArrayList<String>();
        // 每隔5秒钟在控制台输出一次
        ConsoleReporter.enable(5, TimeUnit.SECONDS);
        // 定义一个名为"testGauge"的Gauge, 用于度量list集合的长度
        Gauge<Integer> g = Metrics.newGauge(MainGauge.class, "testGauge", new Gauge<Integer>() {
            @Override
            public Integer value()
            {
                return list.size();
            }
        });
        while (true) {
            list.add("s");
            Thread.sleep(1000L);
        }
    }
}
