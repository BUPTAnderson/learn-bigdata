package org.learning;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.UUID;

/**
 * Created by anderson on 17-7-14.
 */
public class TestQueryId
{
    public static String makeQueryId() {
        GregorianCalendar gc = new GregorianCalendar();
        String userid = System.getProperty("user.name");

        return userid
                + "_"
                + String.format("%1$4d%2$02d%3$02d%4$02d%5$02d%6$02d", gc
                .get(Calendar.YEAR), gc.get(Calendar.MONTH) + 1, gc
                .get(Calendar.DAY_OF_MONTH), gc.get(Calendar.HOUR_OF_DAY), gc
                .get(Calendar.MINUTE), gc.get(Calendar.SECOND))
                + "_"
                + UUID.randomUUID().toString();
    }

    public static void main(String[] args)
    {
        String queryId = makeQueryId();
        System.out.println(queryId);
    }
}
