package org.learning;

import org.apache.hadoop.fs.permission.FsPermission;

/**
 * Created by anderson on 17-7-12.
 */
public class TestPermission
{
    public static void main(String[] args)
    {
        System.out.println((short) 00733);
        FsPermission writableHDFSDirPermission = new FsPermission((short) 00733);
        System.out.println(writableHDFSDirPermission.toShort());
    }
}
