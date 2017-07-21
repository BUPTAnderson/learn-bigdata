package org.learning.rpc;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.retry.RetryPolicies;
import org.apache.hadoop.io.retry.RetryPolicy;
import org.apache.hadoop.io.retry.RetryUtils;
import org.apache.hadoop.mapred.JobTrackerNotYetInitializedException;
import org.apache.hadoop.mapred.SafeModeException;

import java.util.concurrent.TimeUnit;

import static org.apache.hadoop.ipc.Client.IPC_CLIENT_CONNECT_MAX_RETRIES_DEFAULT;
import static org.apache.hadoop.ipc.Client.IPC_CLIENT_CONNECT_MAX_RETRIES_KEY;
import static org.apache.hadoop.mapred.JobClient.MAPREDUCE_CLIENT_RETRY_POLICY_ENABLED_DEFAULT;
import static org.apache.hadoop.mapred.JobClient.MAPREDUCE_CLIENT_RETRY_POLICY_ENABLED_KEY;
import static org.apache.hadoop.mapred.JobClient.MAPREDUCE_CLIENT_RETRY_POLICY_SPEC_DEFAULT;
import static org.apache.hadoop.mapred.JobClient.MAPREDUCE_CLIENT_RETRY_POLICY_SPEC_KEY;

/**
 * Created by anderson on 17-7-9.
 */
public class TestRetryPolicy
{
    public static void main(String[] args)
    {
        Configuration conf = new Configuration();
        RetryPolicy retryPolicy = RetryUtils.getMultipleLinearRandomRetry(
                conf,
                MAPREDUCE_CLIENT_RETRY_POLICY_ENABLED_KEY,
                MAPREDUCE_CLIENT_RETRY_POLICY_ENABLED_DEFAULT,
                MAPREDUCE_CLIENT_RETRY_POLICY_SPEC_KEY,
                MAPREDUCE_CLIENT_RETRY_POLICY_SPEC_DEFAULT
        );
        // 默认值为null
        System.out.println(retryPolicy);

        if (retryPolicy == null) {
            final int max = conf.getInt(
                    IPC_CLIENT_CONNECT_MAX_RETRIES_KEY,
                    IPC_CLIENT_CONNECT_MAX_RETRIES_DEFAULT);
            retryPolicy = RetryPolicies.retryUpToMaximumCountWithFixedSleep(
                    max, 1, TimeUnit.SECONDS);
        }

        System.out.println(retryPolicy.getClass().getName()); // org.apache.hadoop.io.retry.RetryPolicies$RetryUpToMaximumCountWithFixedSleep

        RetryPolicy defaultPolicy =
                RetryUtils.getDefaultRetryPolicy(
                        conf,
                        MAPREDUCE_CLIENT_RETRY_POLICY_ENABLED_KEY,
                        MAPREDUCE_CLIENT_RETRY_POLICY_ENABLED_DEFAULT,
                        MAPREDUCE_CLIENT_RETRY_POLICY_SPEC_KEY,
                        MAPREDUCE_CLIENT_RETRY_POLICY_SPEC_DEFAULT,
                        JobTrackerNotYetInitializedException.class,
                        SafeModeException.class
                );
        System.out.println(defaultPolicy.getClass().getName());
    }
}
