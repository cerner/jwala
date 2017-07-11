package com.cerner.jwala.service.initializer;

import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.service.exception.JGroupsClusterInitializerException;
import org.jgroups.Event;
import org.jgroups.JChannel;
import org.jgroups.PhysicalAddress;
import org.jgroups.ReceiverAdapter;
import org.jgroups.stack.IpAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.ArrayList;

/**
 * JGroups cluster initializer.
 * <p/>
 * Created by Jedd Cuison on 3/15/2016.
 */
public class JGroupsClusterInitializer implements InitializingBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(JGroupsClusterInitializer.class);
    private static final String JGROUPS_COORDINATOR_HOSTNAME = "jgroups.coordinator.hostname";
    private static final String JGROUPS_COORDINATOR_IP_ADDRESS = "jgroups.coordinator.ip.address";
    private static final String JGROUPS_BIND_ADDR = "jgroups.bind_addr";
    private static final String JAVA_NET_PREFER_IPV4_STACK = "java.net.preferIPv4Stack";

    private final String jgroupsJavaNetPreferIPv4Stack = ApplicationProperties.get("jgroups.java.net.preferIPv4Stack", "true");
    private final String jgroupsCoordinatorHostname = ApplicationProperties.get(JGROUPS_COORDINATOR_HOSTNAME);
    private final String jgroupsCoordinatorIPAddress = ApplicationProperties.get(JGROUPS_COORDINATOR_IP_ADDRESS);
    private final String jgroupsCoordinatorPort = ApplicationProperties.get("jgroups.coordinator.port");
    private final String jgroupsClusterConnectTimeout = ApplicationProperties.get("jgroups.cluster.connect.timeout", "10000");
    private final String jgroupsClusterName = ApplicationProperties.get("jgroups.cluster.name", "DefaultJwalaCluster");
    private final String jgroupsConfXml = ApplicationProperties.get("jgroups.conf.xml", "tcp.xml");
    private final ReceiverAdapter receiverAdapter;

    public JGroupsClusterInitializer(final ReceiverAdapter receiverAdapter) {
        this.receiverAdapter = receiverAdapter;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            IpAddress coordinatorIP;
            if (null != this.jgroupsCoordinatorIPAddress) {
                coordinatorIP = new IpAddress(this.jgroupsCoordinatorIPAddress + ":" + jgroupsCoordinatorPort);
            } else {
                if (null == this.jgroupsCoordinatorHostname) {
                    final String errorMessage = MessageFormat.format("Expecting hostname specified as property {0} when no IP address specified for the JGroups coordinator", JGROUPS_COORDINATOR_HOSTNAME);
                    LOGGER.error(errorMessage);
                    throw new JGroupsClusterInitializerException(errorMessage, null);
                }
                coordinatorIP = getIPAddressFromHostname(jgroupsCoordinatorHostname);
            }

            System.setProperty(JAVA_NET_PREFER_IPV4_STACK, jgroupsJavaNetPreferIPv4Stack);
            System.setProperty(JGROUPS_BIND_ADDR, coordinatorIP.getIpAddress().getHostAddress());

            LOGGER.debug("Starting JGroups cluster {}", jgroupsClusterName);
            final JChannel channel = new JChannel(jgroupsConfXml);
            channel.setReceiver(receiverAdapter);
            channel.connect(jgroupsClusterName, coordinatorIP, Long.parseLong(jgroupsClusterConnectTimeout));
            LOGGER.debug("JGroups connection to cluster {} SUCCESSFUL", jgroupsClusterName);

            PhysicalAddress physicalAddr = (PhysicalAddress) channel.down(new Event(Event.GET_PHYSICAL_ADDRESS, channel.getAddress()));
            LOGGER.info("JGroups cluster physical address {} {} {}", jgroupsClusterName, channel.getName(), physicalAddr);
        } catch (Exception e) {
            LOGGER.error("FAILURE using JGroups: could not connect to cluster {}", jgroupsClusterName, e);
            throw new JGroupsClusterInitializerException("JGroups cluster initialization failed!", e);
        }
    }

    private IpAddress getIPAddressFromHostname(final String jgroupsCoordinatorHostname) throws UnknownHostException {
        final InetAddress[] allByName = InetAddress.getAllByName(jgroupsCoordinatorHostname);
        if (null == allByName || 0 == allByName.length) {
            final String errorMessage = MessageFormat.format("Expecting at least 1 IP address from hostname {0}, but were returned none", jgroupsCoordinatorHostname);
            LOGGER.error(errorMessage);
            throw new JGroupsClusterInitializerException(errorMessage, null);
        }

        LOGGER.info("Getting JGroups IP address by hostname {} returned a total of {} addresses", jgroupsCoordinatorHostname, allByName.length);
        ArrayList<InetAddress> filteredAddresses = new ArrayList<>();
        for (InetAddress address : allByName) {
            LOGGER.debug("Filtering JGroups IP address {}", address);
            if (!address.isLoopbackAddress()) {
                if (Boolean.valueOf(jgroupsJavaNetPreferIPv4Stack) && address instanceof Inet4Address) {
                    LOGGER.debug("-- adding JGroups IPv4 address to list: {}", address);
                    filteredAddresses.add(address);
                } else if (!Boolean.valueOf(jgroupsJavaNetPreferIPv4Stack) && address instanceof Inet6Address) {
                    LOGGER.debug("-- adding JGroups IPv6 address to list: {}", address);
                    filteredAddresses.add(address);
                }
            }
        }

        if (filteredAddresses.size() != 1) {
            final String errorMessage = MessageFormat.format("Expecting only 1 filtered JGroups IP address, but instead found {0}. Please use the IP address property {1} to specify the JGroups coordinator node location.", filteredAddresses.size(), JGROUPS_COORDINATOR_IP_ADDRESS);
            LOGGER.error(errorMessage);
            throw new JGroupsClusterInitializerException(errorMessage, null);
        }

        return new IpAddress(filteredAddresses.get(0), Integer.parseInt(jgroupsCoordinatorPort));
    }
}
