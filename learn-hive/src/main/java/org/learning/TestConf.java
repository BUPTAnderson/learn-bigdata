package org.learning;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.common.FileUtils;
import org.apache.hadoop.hive.conf.HiveConf;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.UUID;

/**
 * Created by anderson on 17-7-10.
 */
public class TestConf
{
    private static final String TMP_PREFIX = "_tmp_space.db";

    private static String makeSessionId() {
        return UUID.randomUUID().toString();
    }

    public static void main(String[] args)
            throws InterruptedException, IOException
    {
        Configuration conf = new Configuration();
        Path path = new Path(HiveConf.getVar(conf, HiveConf.ConfVars.LOCALSCRATCHDIR));
        System.out.println(path.toString());

        // session_id
        HiveConf hiveConf = new HiveConf();
        if (StringUtils.isEmpty(hiveConf.getVar(HiveConf.ConfVars.HIVESESSIONID))) {
            hiveConf.setVar(HiveConf.ConfVars.HIVESESSIONID, makeSessionId());
        }
        String sessionId = hiveConf.getVar(HiveConf.ConfVars.HIVESESSIONID);
        System.out.println(sessionId);

        path = new Path(HiveConf.getVar(hiveConf, HiveConf.ConfVars.DOWNLOADED_RESOURCES_DIR));
        System.out.println(path.toString());
        //
        System.out.println("--" + System.getProperty("java.io.tmpdir"));
        System.out.println("++" + System.getProperty("user.name"));
        System.out.println(ManagementFactory.getRuntimeMXBean().getName());
//        Thread.currentThread().sleep(100000L);

        Path localSessionPath = new Path(HiveConf.getVar(conf, HiveConf.ConfVars.LOCALSCRATCHDIR), sessionId);
        System.out.println(localSessionPath.toUri().toString());

        Path rootHDFSDirPath = new Path(HiveConf.getVar(conf, HiveConf.ConfVars.SCRATCHDIR));
        Path p = new Path(rootHDFSDirPath, "anderson");
        Path hdfsSessionPath = new Path(p.toUri().toString(), sessionId);
        Path hdfsTmpTableSpace = new Path(hdfsSessionPath, TMP_PREFIX);
        System.out.println(hdfsTmpTableSpace.toUri().toString());

        File file = createTempFile(hiveConf);
        System.out.println("----------------");
        System.out.println(file.getName());
        System.out.println(file.getAbsolutePath());

        System.out.println("++" + System.getProperty("user.home"));

        System.out.println("------>" + hiveConf.getVar(HiveConf.ConfVars.DOWNLOADED_RESOURCES_DIR));
        hiveConf.set("hive.downloaded.resources.dir", "hahahahahhahahaah");
        System.out.println("------>" + hiveConf.getVar(HiveConf.ConfVars.DOWNLOADED_RESOURCES_DIR));
    }

    private static File createTempFile(HiveConf conf) throws IOException
    {
        String lScratchDir = HiveConf.getVar(conf, HiveConf.ConfVars.LOCALSCRATCHDIR);
        String sessionID = conf.getVar(HiveConf.ConfVars.HIVESESSIONID);

        return FileUtils.createTempFile(lScratchDir, sessionID, ".pipeout");
    }
}
