package org.learning.metric;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.reporting.ConsoleReporter;

import java.util.concurrent.TimeUnit;

/**
 * Created by anderson on 17-11-20.
 * Meter的最终统计结果有: 请求数总数, 平均每秒的请求数, 以及最近的1, 5, 15分钟的平均TPS
 * 实例:
 * 17-11-20 17:13:09 ==============================================================
 * org.learning.metric.MainMeter:
 * Meter:
 * count = 110
 * mean rate = 2.03 requests/s
 * 1-minute rate = 2.00 requests/s
 * 5-minute rate = 2.00 requests/s
 * 15-minute rate = 2.00 requests/s
 */
public class MainMeter
{
    private static Meter meter = Metrics.newMeter(MainMeter.class, "Meter", "requests", TimeUnit.SECONDS);

    public static void main(String[] args)
            throws InterruptedException
    {
        ConsoleReporter.enable(1, TimeUnit.SECONDS);
        while (true) {
            meter.mark();
            meter.mark();
            Thread.sleep(1000L);
        }
    }
}
