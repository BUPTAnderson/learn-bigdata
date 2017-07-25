package org.learning;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * Created by anderson on 17-7-24.
 */
public class TestExecutionId
{
    public static String generateExecutionId() {
        Random rand = new Random();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss_SSS");
        String executionId = "hive_" + format.format(new Date()) + "_"
                + Math.abs(rand.nextLong());
        return executionId;
    }

    public static void main(String[] args)
    {
        System.out.println(generateExecutionId());
    }
}
