package org.learning;

import org.apache.commons.codec.binary.Base64;
import org.apache.hive.jdbc.JdbcUriParseException;
import org.apache.hive.jdbc.Utils;
import org.apache.hive.jdbc.ZooKeeperHiveClientException;
import org.apache.hive.service.auth.HiveAuthFactory;
import org.apache.hive.service.cli.HandleIdentifier;

import java.net.URI;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by anderson on 17-7-27.
 */
public class TestParseUrl
{
    public static final String URL_PREFIX = "jdbc:hive2://";
    private static final String URI_JDBC_PREFIX = "jdbc:";
    private static final String HIVE_VAR_PREFIX = "hivevar:";
    private static final String HIVE_CONF_PREFIX = "hiveconf:";
    private static final Pattern kvPattern = Pattern.compile("([^=;]*)=([^;]*)[;]?");

    static Utils.JdbcConnectionParams parseURL(String uri, Properties info) throws JdbcUriParseException,
            SQLException, ZooKeeperHiveClientException
    {
        Utils.JdbcConnectionParams connParams = new Utils.JdbcConnectionParams();

        if (!uri.startsWith(URL_PREFIX)) {
            throw new JdbcUriParseException("Bad URL format: Missing prefix " + URL_PREFIX);
        }

        // For URLs with no other configuration
        // Don't parse them, but set embedded mode as true
        // 如果uri为 "jdbc:hive2://", 则设置嵌入式模式为true
        if (uri.equalsIgnoreCase(URL_PREFIX)) {
            connParams.setEmbeddedMode(true);
            return connParams;
        }

        // The JDBC URI now supports specifying multiple host:port if dynamic service discovery is
        // configured on HiveServer2 (like: host1:port1,host2:port2,host3:port3)
        // We'll extract the authorities (host:port combo) from the URI, extract session vars, hive
        // confs & hive vars by parsing it as a Java URI.
        // To parse the intermediate URI as a Java URI, we'll give a dummy authority(dummy:00000).
        // Later, we'll substitute the dummy authority for a resolved authority.
        String dummyAuthorityString = "dummyhost:00000";
        String suppliedAuthorities = getAuthorities(uri, connParams);
        if ((suppliedAuthorities == null) || (suppliedAuthorities.isEmpty())) {
            // Given uri of the form:
            // jdbc:hive2:///dbName;sess_var_list?hive_conf_list#hive_var_list
            connParams.setEmbeddedMode(true);
        } else {
            System.out.println("Supplied authorities: " + suppliedAuthorities);
            String[] authorityList = suppliedAuthorities.split(",");
            connParams.setSuppliedAuthorityList(authorityList);
            uri = uri.replace(suppliedAuthorities, dummyAuthorityString);
        }

        // Now parse the connection uri with dummy authority
        URI jdbcURI = URI.create(uri.substring(URI_JDBC_PREFIX.length()));

        // key=value pattern
        Pattern pattern = Pattern.compile("([^;]*)=([^;]*)[;]?");

        // dbname and session settings
        String sessVars = jdbcURI.getPath();
        if ((sessVars != null) && !sessVars.isEmpty()) {
            String dbName = "";
            // removing leading '/' returned by getPath()
            sessVars = sessVars.substring(1);
            if (!sessVars.contains(";")) {
                // only dbname is provided
                dbName = sessVars;
            } else {
                // we have dbname followed by session parameters
                dbName = sessVars.substring(0, sessVars.indexOf(';'));
                sessVars = sessVars.substring(sessVars.indexOf(';') + 1);
                if (sessVars != null) {
                    Matcher sessMatcher = pattern.matcher(sessVars);
                    while (sessMatcher.find()) {
                        String key = sessMatcher.group(1);
                        String value = sessMatcher.group(2);
                        if (connParams.getSessionVars().put(key, value) != null) {
                            throw new JdbcUriParseException("Bad URL format: Multiple values for property "
                                    + sessMatcher.group(1));
                        }
                    }
                }
            }
            if (!dbName.isEmpty()) {
                connParams.setDbName(dbName);
            }
        }

        // parse hive conf settings
        String confStr = jdbcURI.getQuery();
        if (confStr != null) {
            Matcher confMatcher = pattern.matcher(confStr);
            while (confMatcher.find()) {
                String key = confMatcher.group(1);
                String value = confMatcher.group(2);
                connParams.getHiveConfs().put(key, value);
            }
        }

        // parse hive var settings
        String varStr = jdbcURI.getFragment();
        if (varStr != null) {
            Matcher varMatcher = pattern.matcher(varStr);
            while (varMatcher.find()) {
                connParams.getHiveVars().put(varMatcher.group(1), varMatcher.group(2));
            }
        }

        // Apply configs supplied in the JDBC connection properties object
        for (Map.Entry<Object, Object> kv : info.entrySet()) {
            if ((kv.getKey() instanceof String)) {
                String key = (String) kv.getKey();
                if (key.startsWith("hivevar:")) {
                    connParams.getHiveVars().put(
                            key.substring("hivevar:".length()), info.getProperty(key));
                } else if (key.startsWith("hiveconf:")) {
                    connParams.getHiveConfs().put(
                            key.substring("hiveconf:".length()), info.getProperty(key));
                }
            }
        }
        // Extract user/password from JDBC connection properties if its not supplied
        // in the connection URL
        if (!connParams.getSessionVars().containsKey(Utils.JdbcConnectionParams.AUTH_USER)) {
            if (info.containsKey(Utils.JdbcConnectionParams.AUTH_USER)) {
                connParams.getSessionVars().put(Utils.JdbcConnectionParams.AUTH_USER,
                        info.getProperty(Utils.JdbcConnectionParams.AUTH_USER));
            }
            if (info.containsKey(Utils.JdbcConnectionParams.AUTH_PASSWD)) {
                connParams.getSessionVars().put(Utils.JdbcConnectionParams.AUTH_PASSWD,
                        info.getProperty(Utils.JdbcConnectionParams.AUTH_PASSWD));
            }
        }

        if (info.containsKey(Utils.JdbcConnectionParams.AUTH_TYPE)) {
            connParams.getSessionVars().put(Utils.JdbcConnectionParams.AUTH_TYPE,
                    info.getProperty(Utils.JdbcConnectionParams.AUTH_TYPE));
        }

        // Handle all deprecations here:
        String newUsage;
        String usageUrlBase = "jdbc:hive2://<host>:<port>/dbName;";
        // Handle deprecation of AUTH_QOP_DEPRECATED
        newUsage = usageUrlBase + Utils.JdbcConnectionParams.AUTH_QOP + "=<qop_value>";
        handleParamDeprecation(connParams.getSessionVars(), connParams.getSessionVars(),
                Utils.JdbcConnectionParams.AUTH_QOP_DEPRECATED, Utils.JdbcConnectionParams.AUTH_QOP, newUsage);

        // Handle deprecation of TRANSPORT_MODE_DEPRECATED
        newUsage = usageUrlBase + Utils.JdbcConnectionParams.TRANSPORT_MODE + "=<transport_mode_value>";
        handleParamDeprecation(connParams.getHiveConfs(), connParams.getSessionVars(),
                "hive.server2.transport.mode", Utils.JdbcConnectionParams.TRANSPORT_MODE,
                newUsage);

        // Handle deprecation of HTTP_PATH_DEPRECATED
        newUsage = usageUrlBase + Utils.JdbcConnectionParams.HTTP_PATH + "=<http_path_value>";
        handleParamDeprecation(connParams.getHiveConfs(), connParams.getSessionVars(),
                "hive.server2.thrift.http.path", Utils.JdbcConnectionParams.HTTP_PATH, newUsage);
        // Extract host, port
        if (connParams.isEmbeddedMode()) {
            // In case of embedded mode we were supplied with an empty authority.
            // So we never substituted the authority with a dummy one.
            connParams.setHost(jdbcURI.getHost());
            connParams.setPort(jdbcURI.getPort());
        } else {
            // Configure host, port and params from ZooKeeper if used,
            // and substitute the dummy authority with a resolved one
            configureConnParams(connParams);
            // We check for invalid host, port while configuring connParams with configureConnParams()
            String authorityStr = connParams.getHost() + ":" + connParams.getPort();
            System.out.println("Resolved authority: " + authorityStr);
            uri = uri.replace(dummyAuthorityString, authorityStr);
            connParams.setJdbcUriString(uri);
        }
        return connParams;
    }

    private static void handleParamDeprecation(Map<String, String> fromMap, Map<String, String> toMap,
            String deprecatedName, String newName, String newUsage) {
        if (fromMap.containsKey(deprecatedName)) {
            System.out.println("***** JDBC param deprecation *****");
            System.out.println("The use of " + deprecatedName + " is deprecated.");
            System.out.println("Please use " + newName + " like so: " + newUsage);
            String paramValue = fromMap.remove(deprecatedName);
            toMap.put(newName, paramValue);
        }
    }

    private static String getAuthorities(String uri, Utils.JdbcConnectionParams connParams)
            throws JdbcUriParseException {
        String authorities;
        /**
         * For a jdbc uri like:
         * jdbc:hive2://<host1>:<port1>,<host2>:<port2>/dbName;sess_var_list?conf_list#var_list
         * Extract the uri host:port list starting after "jdbc:hive2://",
         * till the 1st "/" or "?" or "#" whichever comes first & in the given order
         * Examples:
         * jdbc:hive2://host1:port1,host2:port2,host3:port3/db;k1=v1?k2=v2#k3=v3
         * jdbc:hive2://host1:port1,host2:port2,host3:port3/;k1=v1?k2=v2#k3=v3
         * jdbc:hive2://host1:port1,host2:port2,host3:port3?k2=v2#k3=v3
         * jdbc:hive2://host1:port1,host2:port2,host3:port3#k3=v3
         */
        int fromIndex = Utils.URL_PREFIX.length();
        int toIndex = -1;
        ArrayList<String> toIndexChars = new ArrayList<String>(Arrays.asList("/", "?", "#"));
        for (String toIndexChar : toIndexChars) {
            toIndex = uri.indexOf(toIndexChar, fromIndex);
            if (toIndex > 0) {
                break;
            }
        }
        if (toIndex < 0) {
            authorities = uri.substring(fromIndex);
        } else {
            authorities = uri.substring(fromIndex, toIndex);
        }
        return authorities;
    }

    private static void configureConnParams(Utils.JdbcConnectionParams connParams)
            throws JdbcUriParseException, ZooKeeperHiveClientException {
        String serviceDiscoveryMode =
                connParams.getSessionVars().get(Utils.JdbcConnectionParams.SERVICE_DISCOVERY_MODE);
        if ((serviceDiscoveryMode != null)
                && ("zooKeeper"
                .equalsIgnoreCase(serviceDiscoveryMode))) {
            // Set ZooKeeper ensemble in connParams for later use
            connParams.setZooKeeperEnsemble(joinStringArray(connParams.getAuthorityList(), ","));
            // Configure using ZooKeeper
            ZooKeeperHiveClientHelper.configureConnParams(connParams);
        } else {
            String authority = connParams.getAuthorityList()[0];
            URI jdbcURI = URI.create("hive2:" + "//" + authority);
            // Check to prevent unintentional use of embedded mode. A missing "/"
            // to separate the 'path' portion of URI can result in this.
            // The missing "/" common typo while using secure mode, eg of such url -
            // jdbc:hive2://localhost:10000;principal=hive/HiveServer2Host@YOUR-REALM.COM
            if (jdbcURI.getAuthority() != null) {
                String host = jdbcURI.getHost();
                int port = jdbcURI.getPort();
                if (host == null) {
                    throw new JdbcUriParseException("Bad URL format. Hostname not found "
                            + " in authority part of the url: " + jdbcURI.getAuthority()
                            + ". Are you missing a '/' after the hostname ?");
                }
                // Set the port to default value; we do support jdbc url like:
                // jdbc:hive2://localhost/db
                if (port <= 0) {
                    port = Integer.parseInt("10000");
                }
                connParams.setHost(jdbcURI.getHost());
                connParams.setPort(jdbcURI.getPort());
            }
        }
    }

    private static String joinStringArray(String[] stringArray, String seperator) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int cur = 0, end = stringArray.length; cur < end; cur++) {
            if (cur > 0) {
                stringBuilder.append(seperator);
            }
            stringBuilder.append(stringArray[cur]);
        }
        return stringBuilder.toString();
    }

    private static void applyConfs(String serverConfStr)
            throws Exception {
        Matcher matcher = kvPattern.matcher(serverConfStr);
        while (matcher.find()) {
            // Have to use this if-else since switch-case on String is supported Java 7 onwards
            if ((matcher.group(1) != null)) {
                if ((matcher.group(2) == null)) {
                    throw new Exception("Null config value for: " + matcher.group(1)
                            + " published by the server.");
                }
                // Set host
                if (matcher.group(1).equals("hive.server2.thrift.bind.host")) {
                    System.out.println("-->" + matcher.group(2));
                }
                // Set transportMode
                if (matcher.group(1).equals("hive.server2.transport.mode")) {
                    System.out.println(matcher.group(2));
                }
                // Set port
                if (matcher.group(1).equals("hive.server2.thrift.port")) {
                    System.out.println(matcher.group(2));
                }
                if (matcher.group(1).equals("hive.server2.thrift.http.port")) {
                    System.out.println(matcher.group(2));
                }
                // Set sasl qop
                if (matcher.group(1).equals("hive.server2.thrift.sasl.qop")) {
                    System.out.println(matcher.group(2));
                }
                // Set http path
                if (matcher.group(1).equals("hive.server2.thrift.http.path")) {
                    System.out.println(matcher.group(2));
                }
                // Set SSL
                if ((matcher.group(1) != null) && (matcher.group(1).equals("hive.server2.use.SSL"))) {
                    System.out.println(matcher.group(2));
                }
                /**
                 * Note: this is pretty messy, but sticking to the current implementation.
                 * Set authentication configs. Note that in JDBC driver, we have 3 auth modes: NOSASL,
                 * Kerberos (including delegation token mechanism) and password based.
                 * The use of JdbcConnectionParams.AUTH_TYPE==JdbcConnectionParams.AUTH_SIMPLE picks NOSASL.
                 * The presence of JdbcConnectionParams.AUTH_PRINCIPAL==<principal> picks Kerberos.
                 * If principal is absent, the presence of
                 * JdbcConnectionParams.AUTH_TYPE==JdbcConnectionParams.AUTH_TOKEN uses delegation token.
                 * Otherwise password based (which includes NONE, PAM, LDAP, CUSTOM)
                 */
                if (matcher.group(1).equals("hive.server2.authentication")) {
                    // NOSASL
                    if (matcher.group(2).equalsIgnoreCase("NOSASL")) {
                        System.out.println("noSasl");
                    }
                }
                // KERBEROS
                // If delegation token is passed from the client side, do not set the principal
                if (matcher.group(1).equalsIgnoreCase("hive.server2.authentication.kerberos.principal")) {
                    System.out.println(matcher.group(2));
                }
            }
        }
    }

    public static void main(String[] args)
            throws Exception
    {
        String url = "jdbc:hive2://bds-test-001:10000/default";
        // default;serviceDiscoveryMode=zooKeeper;zooKeeperNamespace=hiveserver2_zk是session参数,
        // 即直接更在ip和端口后面的是session参数, 第一个要是数据库名, 其它的是key value对的session参数, 跟在数据库后面, 每个key value对使用";"分割, 也可以不设置数据库名, 如url3
        String url2 = "jdbc:hive2://zkNode1:2181,zkNode2:2181,zkNode3:2181/default;serviceDiscoveryMode=zooKeeper;zooKeeperNamespace=hiveserver2_zk";
        // 注意不设置数据库名时, 端口号后'/'和端口号后面第一个';'之间要是空字符串, 因为, 解析的时候会把端口号后面'/'和'/'后第一个';'之间的部分看作数据库
        // 下面的url如果改为jdbc:hive2://zkNode1:2181,zkNode2:2181,zkNode3:2181/serviceDiscoveryMode=zooKeeper;zooKeeperNamespace=hiveserver2_zk, 则会把serviceDiscoveryMode=zooKeeper看作是数据库名
        String url3 = "jdbc:hive2://zkNode1:2181,zkNode2:2181,zkNode3:2181/;serviceDiscoveryMode=zooKeeper;zooKeeperNamespace=hiveserver2_zk";
        // hive.server2.transport.mode=http;hive.server2.thrift.http.path=hs2是hiveconf参数, 即?后设置hiveconf参数, 多个参数用";"隔开
        String url4 = "jdbc:hive2://bds-test-001:10000/default?hive.server2.transport.mode=http;hive.server2.thrift.http.path=hs2";
        // 不设置数据库, 如下方式
        String url5 = "jdbc:hive2://bds-test-001:10000/?hive.server2.transport.mode=http;hive.server2.thrift.http.path=hs2";
        // stab=salesTable;icol=customerID是变量设置, 即#后面设置变量数据, 多个变量使用";"隔开
        String url6 = "jdbc:hive2://bds-test-001:10000/db#stab=salesTable;icol=customerID";
        // 不设置数据库如下所示:
        String url7 = "jdbc:hive2://bds-test-001:10000/#stab=salesTable;icol=customerID";
        // 包含各种设置的url
        String url8 = "jdbc:hive2://bds-test-001:10000/default;user=datajingdo_m;password=123?hive.server2.transport.mode=http;hive.server2.thrift.http.path=hs2#stab=salesTable;icol=customerID";
        Properties info = new Properties();
        info.put("url", "jdbc:hive2://bds-test-001:10000/default");
        info.put("user", "datajingdo_m");
        info.put("password", "123");
        info.put("hiveconf:spark.jd.instance.name", "default");
        info.put("hiveconf:spark.jd.instance.owner.name", "datajingdo_m");
        info.put("hiveconf:spark.jd.instance.owner.type", "USER");
        Utils.JdbcConnectionParams connParams = parseURL(url8, info);

//        String dataStr = "BDS-TEST-002:10000\n" +
//                "cZxid = 0xa002ea844\n" +
//                "ctime = Fri Dec 23 11:39:25 CST 2016\n" +
//                "mZxid = 0xa002ea844\n" +
//                "mtime = Fri Dec 23 11:39:25 CST 2016\n" +
//                "pZxid = 0xa002ea844\n" +
//                "cversion = 0\n" +
//                "dataVersion = 0\n" +
//                "aclVersion = 0\n" +
//                "ephemeralOwner = 0x258d78d9ed70018\n" +
//                "dataLength = 18\n" +
//                "numChildren = 0";
//        Matcher matcher = kvPattern.matcher(dataStr);
//        System.out.println(matcher.find());
//        applyConfs(dataStr);
        HandleIdentifier identifier = new HandleIdentifier();
        System.out.println(identifier.toString());
        String guid64 = Base64.encodeBase64URLSafeString(identifier.toTHandleIdentifier().getGuid()).trim();
        System.out.println(guid64);
        System.out.println(org.apache.hadoop.hive.shims.Utils.getTokenStrForm(HiveAuthFactory.HS2_CLIENT_TOKEN));
        String[] names = "hive/_HOST@HADOOP.JD".split("[/@]");
        System.out.println(names.length);
        for (String name : names) {
            System.out.println(name);
        }
    }
}
