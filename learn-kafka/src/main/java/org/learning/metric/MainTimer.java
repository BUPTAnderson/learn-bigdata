package org.learning.metric;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.core.TimerContext;
import com.yammer.metrics.reporting.ConsoleReporter;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Created by anderson on 17-11-20.
 * Timer主要用来统计某一块代码段的执行时间以及其分布情况, 底层是基于Histogram和Meter来实现的
 * 示例结果:
 * 17-11-20 18:19:11 ==============================================================
 * org.learning.metric.MainTimer:
 * testTimer:
 * count = 7
 * mean rate = 2.30 calls/s
 * 1-minute rate = 0.00 calls/s
 * 5-minute rate = 0.00 calls/s
 * 15-minute rate = 0.00 calls/s
 * min = 145.10ms
 * max = 861.10ms
 * mean = 422.37ms
 * stddev = 292.72ms
 * median = 237.07ms
 * 75% <= 735.07ms
 * 95% <= 861.10ms
 * 98% <= 861.10ms
 * 99% <= 861.10ms
 * 99.9% <= 861.10ms
 */
public class MainTimer
{
    private static Timer timer = Metrics.newTimer(MainTimer.class, "testTimer", TimeUnit.MILLISECONDS, TimeUnit.SECONDS);

    public static void main(String[] args)
            throws InterruptedException
    {
        ConsoleReporter.enable(1, TimeUnit.SECONDS);
        Random rn = new Random();
        timer.time();
        System.out.println();
        while (true) {
            TimerContext context = timer.time();
            Thread.sleep(rn.nextInt(1000));
            context.stop();
        }
    }
}
