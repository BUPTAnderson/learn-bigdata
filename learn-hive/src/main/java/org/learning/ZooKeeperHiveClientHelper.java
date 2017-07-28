package org.learning;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.hive.jdbc.Utils;
import org.apache.hive.jdbc.ZooKeeperHiveClientException;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by anderson on 17-7-27.
 */
public class ZooKeeperHiveClientHelper
{
    static final Logger LOG = LoggerFactory.getLogger(ZooKeeperHiveClientHelper.class.getName());
    // Pattern for key1=value1;key2=value2
    private static final Pattern kvPattern = Pattern.compile("([^=;]*)=([^;]*)[;]?");
    /**
     * A no-op watcher class
     */
    static class DummyWatcher implements Watcher
    {
        @Override
        public void process(org.apache.zookeeper.WatchedEvent event) {
        }
    }

    static void configureConnParams(Utils.JdbcConnectionParams connParams)
            throws ZooKeeperHiveClientException
    {
        String zooKeeperEnsemble = connParams.getZooKeeperEnsemble();
        String zooKeeperNamespace =
                connParams.getSessionVars().get("zooKeeperNamespace");
        if ((zooKeeperNamespace == null) || (zooKeeperNamespace.isEmpty())) {
            zooKeeperNamespace = "hiveserver2";
        }
        List<String> serverHosts;
        Random randomizer = new Random();
        String serverNode;
        CuratorFramework zooKeeperClient =
                CuratorFrameworkFactory.builder().connectString(zooKeeperEnsemble)
                        .retryPolicy(new ExponentialBackoffRetry(1000, 3)).build();
        try {
            zooKeeperClient.start();
            serverHosts = zooKeeperClient.getChildren().forPath("/" + zooKeeperNamespace);
            // Remove the znodes we've already tried from this list
            serverHosts.removeAll(connParams.getRejectedHostZnodePaths());
            if (serverHosts.isEmpty()) {
                throw new ZooKeeperHiveClientException(
                        "Tried all existing HiveServer2 uris from ZooKeeper.");
            }
            // Now pick a server node randomly
            serverNode = serverHosts.get(randomizer.nextInt(serverHosts.size()));
            connParams.setCurrentHostZnodePath(serverNode);
            // Read data from the znode for this server node
            // This data could be either config string (new releases) or server end
            // point (old releases)
            String dataStr =
                    new String(
                            zooKeeperClient.getData().forPath("/" + zooKeeperNamespace + "/" + serverNode),
                            Charset.forName("UTF-8"));
            Matcher matcher = kvPattern.matcher(dataStr);
            // If dataStr is not null and dataStr is not a KV pattern,
            // it must be the server uri added by an older version HS2
            if ((dataStr != null) && (!matcher.find())) {
                String[] split = dataStr.split(":");
                if (split.length != 2) {
                    throw new ZooKeeperHiveClientException("Unable to read HiveServer2 uri from ZooKeeper: "
                            + dataStr);
                }
                connParams.setHost(split[0]);
                connParams.setPort(Integer.parseInt(split[1]));
            } else {
                applyConfs(dataStr, connParams);
            }
        } catch (Exception e) {
            throw new ZooKeeperHiveClientException("Unable to read HiveServer2 configs from ZooKeeper", e);
        } finally {
            // Close the client connection with ZooKeeper
            if (zooKeeperClient != null) {
                zooKeeperClient.close();
            }
        }
    }

    /**
     * Apply configs published by the server. Configs specified from client's JDBC URI override
     * configs published by the server.
     *
     * @param serverConfStr
     * @param connParams
     * @throws Exception
     */
    private static void applyConfs(String serverConfStr, Utils.JdbcConnectionParams connParams)
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
                    connParams.setHost(matcher.group(2));
                }
                // Set transportMode
                if ((matcher.group(1).equals("hive.server2.transport.mode"))
                        && !(connParams.getSessionVars().containsKey(Utils.JdbcConnectionParams.TRANSPORT_MODE))) {
                    connParams.getSessionVars().put(Utils.JdbcConnectionParams.TRANSPORT_MODE, matcher.group(2));
                }
                // Set port
                if (matcher.group(1).equals("hive.server2.thrift.port")) {
                    connParams.setPort(Integer.parseInt(matcher.group(2)));
                }
                if ((matcher.group(1).equals("hive.server2.thrift.http.port"))
                        && !(connParams.getPort() > 0)) {
                    connParams.setPort(Integer.parseInt(matcher.group(2)));
                }
                // Set sasl qop
                if ((matcher.group(1).equals("hive.server2.thrift.sasl.qop"))
                        && !(connParams.getSessionVars().containsKey(Utils.JdbcConnectionParams.AUTH_QOP))) {
                    connParams.getSessionVars().put(Utils.JdbcConnectionParams.AUTH_QOP, matcher.group(2));
                }
                // Set http path
                if ((matcher.group(1).equals("hive.server2.thrift.http.path"))
                        && !(connParams.getSessionVars().containsKey(Utils.JdbcConnectionParams.HTTP_PATH))) {
                    connParams.getSessionVars().put(Utils.JdbcConnectionParams.HTTP_PATH, matcher.group(2));
                }
                // Set SSL
                if ((matcher.group(1) != null) && (matcher.group(1).equals("hive.server2.use.SSL"))
                        && !(connParams.getSessionVars().containsKey(Utils.JdbcConnectionParams.USE_SSL))) {
                    connParams.getSessionVars().put(Utils.JdbcConnectionParams.USE_SSL, matcher.group(2));
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
                    if (matcher.group(2).equalsIgnoreCase("NOSASL")
                            && !(connParams.getSessionVars().containsKey(Utils.JdbcConnectionParams.AUTH_TYPE) && connParams
                            .getSessionVars().get(Utils.JdbcConnectionParams.AUTH_TYPE)
                            .equalsIgnoreCase(Utils.JdbcConnectionParams.AUTH_SIMPLE))) {
                        connParams.getSessionVars().put(Utils.JdbcConnectionParams.AUTH_TYPE,
                                Utils.JdbcConnectionParams.AUTH_SIMPLE);
                    }
                }
                // KERBEROS
                // If delegation token is passed from the client side, do not set the principal
                if (matcher.group(1).equalsIgnoreCase("hive.server2.authentication.kerberos.principal")
                        && !(connParams.getSessionVars().containsKey(Utils.JdbcConnectionParams.AUTH_TYPE) && connParams
                        .getSessionVars().get(Utils.JdbcConnectionParams.AUTH_TYPE)
                        .equalsIgnoreCase(Utils.JdbcConnectionParams.AUTH_TOKEN))
                        && !(connParams.getSessionVars().containsKey(Utils.JdbcConnectionParams.AUTH_PRINCIPAL))) {
                    connParams.getSessionVars().put(Utils.JdbcConnectionParams.AUTH_PRINCIPAL, matcher.group(2));
                }
            }
        }
    }
}
