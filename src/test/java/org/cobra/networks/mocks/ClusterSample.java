package org.cobra.networks.mocks;

import org.cobra.networks.SocketNode;

import java.util.ArrayList;
import java.util.List;

public class ClusterSample {

    public static List<SocketNode> singletonCluster() {
        return clusterWith(1);
    }

    public static List<SocketNode> clusterWith(final int nodeNums) {
        final List<SocketNode> nodes = new ArrayList<>(nodeNums);
        for (int i = 0; i < nodeNums; i++) {
            nodes.add(new SocketNode("localhost", 9002));
        }

        return nodes;
    }
}
