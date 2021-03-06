package com.a.eye.skywalking.collector.worker.application.receiver;

import com.a.eye.skywalking.collector.actor.*;
import com.a.eye.skywalking.collector.actor.selector.RollingSelector;
import com.a.eye.skywalking.collector.actor.selector.WorkerSelector;
import com.a.eye.skywalking.collector.worker.WorkerConfig;
import com.a.eye.skywalking.collector.worker.application.persistence.NodeInstancePersistence;
import com.a.eye.skywalking.collector.worker.storage.RecordData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author pengys5
 */
public class NodeInstanceReceiver extends AbstractClusterWorker {

    private Logger logger = LogManager.getFormatterLogger(NodeInstanceReceiver.class);

    public NodeInstanceReceiver(com.a.eye.skywalking.collector.actor.Role role, ClusterWorkerContext clusterContext, LocalWorkerContext selfContext) {
        super(role, clusterContext, selfContext);
    }

    @Override
    public void preStart() throws ProviderNotFoundException {
        getClusterContext().findProvider(NodeInstancePersistence.Role.INSTANCE).create(this);
    }

    @Override
    public void work(Object message) throws Exception {
        if (message instanceof RecordData) {
            getSelfContext().lookup(NodeInstancePersistence.Role.INSTANCE).tell(message);
        } else {
            logger.error("message unhandled");
        }
    }

    public static class Factory extends AbstractClusterWorkerProvider<NodeInstanceReceiver> {
        public static Factory INSTANCE = new Factory();

        @Override
        public Role role() {
            return Role.INSTANCE;
        }

        @Override
        public NodeInstanceReceiver workerInstance(ClusterWorkerContext clusterContext) {
            return new NodeInstanceReceiver(role(), clusterContext, new LocalWorkerContext());
        }

        @Override
        public int workerNum() {
            return WorkerConfig.Worker.NodeInstanceReceiver.Num;
        }
    }

    public enum Role implements com.a.eye.skywalking.collector.actor.Role {
        INSTANCE;

        @Override
        public String roleName() {
            return NodeInstanceReceiver.class.getSimpleName();
        }

        @Override
        public WorkerSelector workerSelector() {
            return new RollingSelector();
        }
    }
}
