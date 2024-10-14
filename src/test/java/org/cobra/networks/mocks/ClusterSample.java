package org.cobra.networks.mocks;

import org.cobra.networks.ChannelNode;

import java.util.ArrayList;
import java.util.List;

public class ClusterSample {

    public static List<ChannelNode> singletonCluster() {
        return clusterWith(1);
    }

    public static List<ChannelNode> clusterWith(final int nodeNums) {
        final List<ChannelNode> nodes = new ArrayList<>(nodeNums);
        for (int i = 0; i < nodeNums; i++) {
            nodes.add(new ChannelNode("localhost", 9002));
        }

        return nodes;
    }
}
