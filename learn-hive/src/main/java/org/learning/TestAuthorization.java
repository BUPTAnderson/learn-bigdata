package org.learning;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hive.common.JavaUtils;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.security.HiveAuthenticationProvider;
import org.apache.hadoop.hive.ql.security.SessionStateUserAuthenticator;
import org.apache.hadoop.hive.ql.security.authorization.DefaultHiveAuthorizationProvider;
import org.apache.hadoop.hive.ql.security.authorization.HiveAuthorizationProvider;
import org.apache.hadoop.hive.ql.security.authorization.plugin.HiveAuthorizerFactory;
import org.apache.hadoop.hive.ql.security.authorization.plugin.sqlstd.SQLStdHiveAuthorizerFactory;
import org.apache.hadoop.util.ReflectionUtils;

/**
 * Created by anderson on 17-7-14.
 */
public class TestAuthorization
{
    public static HiveAuthorizationProvider getAuthorizeProviderManager(
            Configuration conf, String authzClassName,
            HiveAuthenticationProvider authenticator, boolean nullIfOtherClass) throws HiveException
    {
        HiveAuthorizationProvider ret = null;
        try {
            Class<? extends HiveAuthorizationProvider> cls = null;
            if (authzClassName == null || authzClassName.trim().equals("")) {
                cls = DefaultHiveAuthorizationProvider.class;
            } else {
                Class<?> configClass = Class.forName(authzClassName, true, JavaUtils.getClassLoader());
                // 这里判断HiveAuthorizationProvider类是否是configClass的父类 如果这里configClass是SQLStdConfOnlyAuthorizerFactory, isAssignableFrom返回false
                if (nullIfOtherClass && !HiveAuthorizationProvider.class.isAssignableFrom(configClass)) {
                    return null;
                }
                cls = (Class<? extends HiveAuthorizationProvider>) configClass;
            }
            if (cls != null) {
                ret = ReflectionUtils.newInstance(cls, conf);
            }
        } catch (Exception e) {
            throw new HiveException(e);
        }
        ret.setAuthenticator(authenticator);
        return ret;
    }

    public static HiveAuthorizerFactory getAuthorizerFactory(
            Configuration conf, HiveConf.ConfVars authorizationProviderConfKey)
            throws HiveException {
        Class<? extends HiveAuthorizerFactory> cls = conf.getClass(authorizationProviderConfKey.varname,
                SQLStdHiveAuthorizerFactory.class, HiveAuthorizerFactory.class);

        if (cls == null) {
            //should not happen as default value is set
            throw new HiveException("Configuration value " + authorizationProviderConfKey.varname
                    + " is not set to valid HiveAuthorizerFactory subclass");
        }

        HiveAuthorizerFactory authFactory = ReflectionUtils.newInstance(cls, conf);
        return authFactory;
    }

    public static void main(String[] args)
            throws ClassNotFoundException, HiveException
    {
        HiveConf hiveConf = new HiveConf();
        HiveAuthorizationProvider hiveAuthorizationProvider = getAuthorizeProviderManager(hiveConf, "org.apache.hadoop.hive.ql.security.authorization.plugin.sqlstd.SQLStdConfOnlyAuthorizerFactory", new SessionStateUserAuthenticator(), true);
        System.out.println(hiveAuthorizationProvider == null);
        hiveConf.set("hive.security.authorization.manager", "org.apache.hadoop.hive.ql.security.authorization.plugin.sqlstd.SQLStdConfOnlyAuthorizerFactory");
        HiveAuthorizerFactory hiveAuthorizerFactory = getAuthorizerFactory(hiveConf, HiveConf.ConfVars.HIVE_AUTHORIZATION_MANAGER);
        System.out.println(hiveAuthorizerFactory.getClass().getName());
        System.out.println(hiveConf.get("hive.internal.ss.authz.settings.applied.marker", "").equals(Boolean.TRUE.toString()));
    }
}
