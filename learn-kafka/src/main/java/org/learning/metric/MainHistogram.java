package org.learning.metric;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Histogram;
import com.yammer.metrics.reporting.ConsoleReporter;

import java.util.concurrent.TimeUnit;

/**
 * Created by anderson on 17-11-20.
 * Histogram可以用于度量最大值, 最小值, 平均值, 方差, 中位值, 百分比数据(例如75%, 90%, 98%, 99%的数据在哪个范围内), 实例如下:
 * 示例结果:
 * 17-11-20 18:12:19 ==============================================================
 * org.learning.metric.MainHistogram:
 * testHistogram:
 * min = 0.00
 * max = 36.00
 * mean = 18.00
 * stddev = 10.82
 * median = 18.00
 * 75% <= 27.50
 * 95% <= 35.10
 * 98% <= 36.00
 * 99% <= 36.00
 * 99.9% <= 36.00
 */
public class MainHistogram
{
    // 创建Histogram对象
    private static Histogram histo = Metrics.newHistogram(MainHistogram.class, "testHistogram");

    public static void main(String[] args)
            throws InterruptedException
    {
        ConsoleReporter.enable(1, TimeUnit.SECONDS);;
        int i = 0;
        while (true) {
            // 更新Histogram度量数据
            histo.update(i++);
            Thread.sleep(1000L);
        }
    }
}
