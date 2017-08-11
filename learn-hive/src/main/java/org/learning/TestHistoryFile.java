package org.learning;

import org.apache.hadoop.fs.FSDataInputStream;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Created by anderson on 17-7-31.
 */
public class TestHistoryFile
{
    public static void main(String[] args)
    {
        System.out.println(new File("/home/anderson/.beeline/history").isFile());

        InputStream inputStream = null;
        System.out.println(inputStream instanceof FileInputStream || inputStream instanceof FSDataInputStream);

        ByteArrayOutputStream hist = new ByteArrayOutputStream();

        if (new File("/tmp/history").isFile()) {
            try {
                // save the current contents of the history buffer. This gets
                // around a bug in JLine where setting the output before the
                // input will clobber the history input, but setting the
                // input before the output will cause the previous commands
                // to not be saved to the buffer.
                try (FileInputStream historyIn = new FileInputStream("/tmp/history")) {
                    int n;
                    while ((n = historyIn.read()) != -1) {
                        hist.write(n);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        System.out.println(hist.toString());
    }
}
