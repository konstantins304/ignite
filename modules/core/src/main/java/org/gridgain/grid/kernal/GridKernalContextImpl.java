/* @java.file.header */

/*  _________        _____ __________________        _____
 *  __  ____/___________(_)______  /__  ____/______ ____(_)_______
 *  _  / __  __  ___/__  / _  __  / _  / __  _  __ `/__  / __  __ \
 *  / /_/ /  _  /    _  /  / /_/ /  / /_/ /  / /_/ / _  /  _  / / /
 *  \____/   /_/     /_/   \_,__/   \____/   \__,_/  /_/   /_/ /_/
 */

package org.gridgain.grid.kernal;

import org.apache.ignite.*;
import org.apache.ignite.configuration.*;
import org.apache.ignite.plugin.*;
import org.apache.ignite.product.*;
import org.gridgain.grid.*;
import org.gridgain.grid.kernal.managers.checkpoint.*;
import org.gridgain.grid.kernal.managers.collision.*;
import org.gridgain.grid.kernal.managers.communication.*;
import org.gridgain.grid.kernal.managers.deployment.*;
import org.gridgain.grid.kernal.managers.discovery.*;
import org.gridgain.grid.kernal.managers.eventstorage.*;
import org.gridgain.grid.kernal.managers.failover.*;
import org.gridgain.grid.kernal.managers.indexing.*;
import org.gridgain.grid.kernal.managers.loadbalancer.*;
import org.gridgain.grid.kernal.managers.securesession.*;
import org.gridgain.grid.kernal.managers.security.*;
import org.gridgain.grid.kernal.managers.swapspace.*;
import org.gridgain.grid.kernal.processors.affinity.*;
import org.gridgain.grid.kernal.processors.cache.*;
import org.gridgain.grid.kernal.processors.cache.dr.*;
import org.gridgain.grid.kernal.processors.cache.dr.os.*;
import org.gridgain.grid.kernal.processors.clock.*;
import org.gridgain.grid.kernal.processors.closure.*;
import org.gridgain.grid.kernal.processors.continuous.*;
import org.gridgain.grid.kernal.processors.dataload.*;
import org.gridgain.grid.kernal.processors.email.*;
import org.gridgain.grid.kernal.processors.ggfs.*;
import org.gridgain.grid.kernal.processors.hadoop.*;
import org.gridgain.grid.kernal.processors.interop.*;
import org.gridgain.grid.kernal.processors.job.*;
import org.gridgain.grid.kernal.processors.jobmetrics.*;
import org.gridgain.grid.kernal.processors.license.*;
import org.gridgain.grid.kernal.processors.offheap.*;
import org.gridgain.grid.kernal.processors.plugin.*;
import org.gridgain.grid.kernal.processors.port.*;
import org.gridgain.grid.kernal.processors.portable.*;
import org.gridgain.grid.kernal.processors.query.*;
import org.gridgain.grid.kernal.processors.resource.*;
import org.gridgain.grid.kernal.processors.rest.*;
import org.gridgain.grid.kernal.processors.schedule.*;
import org.gridgain.grid.kernal.processors.segmentation.*;
import org.gridgain.grid.kernal.processors.service.*;
import org.gridgain.grid.kernal.processors.session.*;
import org.gridgain.grid.kernal.processors.spring.*;
import org.gridgain.grid.kernal.processors.streamer.*;
import org.gridgain.grid.kernal.processors.task.*;
import org.gridgain.grid.kernal.processors.timeout.*;
import org.gridgain.grid.util.direct.*;
import org.gridgain.grid.util.lang.*;
import org.gridgain.grid.util.tostring.*;
import org.gridgain.grid.util.typedef.*;
import org.gridgain.grid.util.typedef.internal.*;
import org.jetbrains.annotations.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import static org.apache.ignite.IgniteSystemProperties.*;
import static org.gridgain.grid.kernal.GridComponentType.*;
import static org.gridgain.grid.kernal.GridKernalState.*;

/**
 * Implementation of kernal context.
 */
@GridToStringExclude
public class GridKernalContextImpl extends GridMetadataAwareAdapter implements GridKernalContext, Externalizable {
    /** */
    private static final long serialVersionUID = 0L;

    /** */
    private static final ThreadLocal<String> stash = new ThreadLocal<>();

    /*
     * Managers.
     * ========
     */

    /** */
    @GridToStringExclude
    private GridDeploymentManager depMgr;

    /** */
    @GridToStringExclude
    private GridIoManager ioMgr;

    /** */
    @GridToStringExclude
    private GridDiscoveryManager discoMgr;

    /** */
    @GridToStringExclude
    private GridCheckpointManager cpMgr;

    /** */
    @GridToStringExclude
    private GridEventStorageManager evtMgr;

    /** */
    @GridToStringExclude
    private GridFailoverManager failoverMgr;

    /** */
    @GridToStringExclude
    private GridCollisionManager colMgr;

    /** */
    @GridToStringExclude
    private GridLoadBalancerManager loadMgr;

    /** */
    @GridToStringExclude
    private GridSecurityManager authMgr;

    /** */
    @GridToStringExclude
    private GridSecureSessionManager sesMgr;

    /** */
    @GridToStringExclude
    private GridSwapSpaceManager swapspaceMgr;

    /** */
    @GridToStringExclude
    private GridIndexingManager indexingMgr;

    /*
     * Processors.
     * ==========
     */

    /** */
    @GridToStringInclude
    private GridQueryProcessor qryProc;

    /** */
    @GridToStringInclude
    private GridTaskProcessor taskProc;

    /** */
    @GridToStringInclude
    private GridJobProcessor jobProc;

    /** */
    @GridToStringInclude
    private GridTimeoutProcessor timeProc;

    /** */
    @GridToStringInclude
    private GridClockSyncProcessor clockSyncProc;

    /** */
    @GridToStringInclude
    private GridResourceProcessor rsrcProc;

    /** */
    @GridToStringInclude
    private GridJobMetricsProcessor metricsProc;

    /** */
    @GridToStringInclude
    private GridClosureProcessor closProc;

    /** */
    @GridToStringInclude
    private GridServiceProcessor svcProc;

    /** */
    @GridToStringInclude
    private GridCacheProcessor cacheProc;

    /** */
    @GridToStringInclude
    private GridTaskSessionProcessor sesProc;

    /** */
    @GridToStringInclude
    private GridPortProcessor portProc;

    /** */
    @GridToStringInclude
    private GridOffHeapProcessor offheapProc;

    /** */
    @GridToStringInclude
    private GridEmailProcessorAdapter emailProc;

    /** */
    @GridToStringInclude
    private GridScheduleProcessorAdapter scheduleProc;

    /** */
    @GridToStringInclude
    private GridRestProcessor restProc;

    /** */
    @GridToStringInclude
    private GridDataLoaderProcessor dataLdrProc;

    /** */
    @GridToStringInclude
    private GridGgfsProcessorAdapter ggfsProc;

    /** */
    @GridToStringInclude
    private GridGgfsHelper ggfsHelper;

    /** */
    @GridToStringInclude
    private GridSegmentationProcessor segProc;

    /** */
    @GridToStringInclude
    private GridAffinityProcessor affProc;

    /** */
    @GridToStringInclude
    private GridLicenseProcessor licProc;

    /** */
    @GridToStringInclude
    private GridStreamProcessor streamProc;

    /** */
    @GridToStringExclude
    private GridContinuousProcessor contProc;

    /** */
    @GridToStringExclude
    private GridHadoopProcessorAdapter hadoopProc;

    /** */
    @GridToStringExclude
    private IgnitePluginProcessor pluginProc;

    /** */
    @GridToStringExclude
    private GridPortableProcessor portableProc;

    /** */
    @GridToStringExclude
    private GridInteropProcessor interopProc;

    /** */
    @GridToStringExclude
    private GridSpringProcessor spring;

    /** */
    @GridToStringExclude
    private List<GridComponent> comps = new LinkedList<>();

    /** */
    private GridEx grid;

    /** */
    private ExecutorService utilityCachePool;

    /** */
    private IgniteProduct product;

    /** */
    private IgniteConfiguration cfg;

    /** */
    private GridKernalGateway gw;

    /** Network segmented flag. */
    private volatile boolean segFlag;

    /** Time source. */
    private GridClockSource clockSrc = new GridJvmClockSource();

    /** Performance suggestions. */
    private final GridPerformanceSuggestions perf = new GridPerformanceSuggestions();

    /** Enterprise release flag. */
    private boolean ent;

    /** */
    private GridTcpMessageFactory msgFactory;

    /** */
    private int pluginMsg = GridTcpCommunicationMessageFactory.MAX_COMMON_TYPE;

    /** */
    private Map<Byte, GridTcpCommunicationMessageProducer> pluginMsgs;

    /**
     * No-arg constructor is required by externalization.
     */
    public GridKernalContextImpl() {
        // No-op.
    }

    /**
     * Creates new kernal context.
     *
     * @param log Logger.
     * @param grid Grid instance managed by kernal.
     * @param cfg Grid configuration.
     * @param gw Kernal gateway.
     * @param utilityCachePool Utility cache pool.
     * @param ent Release enterprise flag.
     */
    @SuppressWarnings("TypeMayBeWeakened")
    protected GridKernalContextImpl(GridLoggerProxy log,
        GridEx grid,
        IgniteConfiguration cfg,
        GridKernalGateway gw,
        ExecutorService utilityCachePool,
        boolean ent) {
        assert grid != null;
        assert cfg != null;
        assert gw != null;

        this.grid = grid;
        this.cfg = cfg;
        this.gw = gw;
        this.ent = ent;
        this.utilityCachePool = utilityCachePool;

        try {
            spring = SPRING.create(false);
        }
        catch (GridException ignored) {
            if (log != null && log.isDebugEnabled())
                log.debug("Failed to load spring component, will not be able to extract userVersion from " +
                    "META-INF/gridgain.xml.");
        }
    }

    /** {@inheritDoc} */
    @Override public Iterator<GridComponent> iterator() {
        return comps.iterator();
    }

    /** {@inheritDoc} */
    @Override public List<GridComponent> components() {
        return Collections.unmodifiableList(comps);
    }

    /**
     * @param comp Manager to add.
     */
    public void add(GridComponent comp) {
        assert comp != null;

        /*
         * Managers.
         * ========
         */

        if (comp instanceof GridDeploymentManager)
            depMgr = (GridDeploymentManager)comp;
        else if (comp instanceof GridIoManager)
            ioMgr = (GridIoManager)comp;
        else if (comp instanceof GridDiscoveryManager)
            discoMgr = (GridDiscoveryManager)comp;
        else if (comp instanceof GridCheckpointManager)
            cpMgr = (GridCheckpointManager)comp;
        else if (comp instanceof GridEventStorageManager)
            evtMgr = (GridEventStorageManager)comp;
        else if (comp instanceof GridFailoverManager)
            failoverMgr = (GridFailoverManager)comp;
        else if (comp instanceof GridCollisionManager)
            colMgr = (GridCollisionManager)comp;
        else if (comp instanceof GridSecurityManager)
            authMgr = (GridSecurityManager)comp;
        else if (comp instanceof GridSecureSessionManager)
            sesMgr = (GridSecureSessionManager)comp;
        else if (comp instanceof GridLoadBalancerManager)
            loadMgr = (GridLoadBalancerManager)comp;
        else if (comp instanceof GridSwapSpaceManager)
            swapspaceMgr = (GridSwapSpaceManager)comp;
        else if (comp instanceof GridIndexingManager)
            indexingMgr = (GridIndexingManager)comp;

        /*
         * Processors.
         * ==========
         */

        else if (comp instanceof GridTaskProcessor)
            taskProc = (GridTaskProcessor)comp;
        else if (comp instanceof GridJobProcessor)
            jobProc = (GridJobProcessor)comp;
        else if (comp instanceof GridTimeoutProcessor)
            timeProc = (GridTimeoutProcessor)comp;
        else if (comp instanceof GridClockSyncProcessor)
            clockSyncProc = (GridClockSyncProcessor)comp;
        else if (comp instanceof GridResourceProcessor)
            rsrcProc = (GridResourceProcessor)comp;
        else if (comp instanceof GridJobMetricsProcessor)
            metricsProc = (GridJobMetricsProcessor)comp;
        else if (comp instanceof GridCacheProcessor)
            cacheProc = (GridCacheProcessor)comp;
        else if (comp instanceof GridTaskSessionProcessor)
            sesProc = (GridTaskSessionProcessor)comp;
        else if (comp instanceof GridPortProcessor)
            portProc = (GridPortProcessor)comp;
        else if (comp instanceof GridEmailProcessorAdapter)
            emailProc = (GridEmailProcessorAdapter)comp;
        else if (comp instanceof GridClosureProcessor)
            closProc = (GridClosureProcessor)comp;
        else if (comp instanceof GridServiceProcessor)
            svcProc = (GridServiceProcessor)comp;
        else if (comp instanceof GridScheduleProcessorAdapter)
            scheduleProc = (GridScheduleProcessorAdapter)comp;
        else if (comp instanceof GridSegmentationProcessor)
            segProc = (GridSegmentationProcessor)comp;
        else if (comp instanceof GridAffinityProcessor)
            affProc = (GridAffinityProcessor)comp;
        else if (comp instanceof GridRestProcessor)
            restProc = (GridRestProcessor)comp;
        else if (comp instanceof GridDataLoaderProcessor)
            dataLdrProc = (GridDataLoaderProcessor)comp;
        else if (comp instanceof GridGgfsProcessorAdapter)
            ggfsProc = (GridGgfsProcessorAdapter)comp;
        else if (comp instanceof GridOffHeapProcessor)
            offheapProc = (GridOffHeapProcessor)comp;
        else if (comp instanceof GridLicenseProcessor)
            licProc = (GridLicenseProcessor)comp;
        else if (comp instanceof GridStreamProcessor)
            streamProc = (GridStreamProcessor)comp;
        else if (comp instanceof GridContinuousProcessor)
            contProc = (GridContinuousProcessor)comp;
        else if (comp instanceof GridHadoopProcessorAdapter)
            hadoopProc = (GridHadoopProcessorAdapter)comp;
        else if (comp instanceof GridPortableProcessor)
            portableProc = (GridPortableProcessor)comp;
        else if (comp instanceof GridInteropProcessor)
            interopProc = (GridInteropProcessor)comp;
        else if (comp instanceof IgnitePluginProcessor)
            pluginProc = (IgnitePluginProcessor)comp;
        else if (comp instanceof GridQueryProcessor)
            qryProc = (GridQueryProcessor)comp;
        else
            assert (comp instanceof GridPluginComponent) : "Unknown manager class: " + comp.getClass();

        comps.add(comp);
    }

    /**
     * @param helper Helper to add.
     */
    public void addHelper(Object helper) {
        assert helper != null;

        if (helper instanceof GridGgfsHelper)
            ggfsHelper = (GridGgfsHelper)helper;
        else
            assert false : "Unknown helper class: " + helper.getClass();
    }

    /** {@inheritDoc} */
    @Override public Collection<String> compatibleVersions() {
        return grid.compatibleVersions();
    }

    /** {@inheritDoc} */
    @Override public boolean isStopping() {
        GridKernalState state = gw.getState();

        return state == STOPPING || state == STOPPED;
    }

    /** {@inheritDoc} */
    @Override public UUID localNodeId() {
        return discovery() == null ? cfg.getNodeId() : discovery().localNode().id();
    }

    /** {@inheritDoc} */
    @Override public String gridName() {
        return cfg.getGridName();
    }

    /** {@inheritDoc} */
    @Override public GridKernalGateway gateway() {
        return gw;
    }

    /** {@inheritDoc} */
    @Override public GridEx grid() {
        return grid;
    }

    /** {@inheritDoc} */
    @Override public IgniteConfiguration config() {
        return cfg;
    }

    /** {@inheritDoc} */
    @Override public GridTaskProcessor task() {
        return taskProc;
    }

    /** {@inheritDoc} */
    @Override public GridJobProcessor job() {
        return jobProc;
    }

    /** {@inheritDoc} */
    @Override public GridTimeoutProcessor timeout() {
        return timeProc;
    }

    /** {@inheritDoc} */
    @Override public GridClockSyncProcessor clockSync() {
        return clockSyncProc;
    }

    /** {@inheritDoc} */
    @Override public GridResourceProcessor resource() {
        return rsrcProc;
    }

    /** {@inheritDoc} */
    @Override public GridJobMetricsProcessor jobMetric() {
        return metricsProc;
    }

    /** {@inheritDoc} */
    @Override public GridCacheProcessor cache() {
        return cacheProc;
    }

    /** {@inheritDoc} */
    @Override public GridTaskSessionProcessor session() {
        return sesProc;
    }

    /** {@inheritDoc} */
    @Override public GridClosureProcessor closure() {
        return closProc;
    }

    /** {@inheritDoc} */
    @Override public GridServiceProcessor service() {
        return svcProc;
    }

    /** {@inheritDoc} */
    @Override public GridPortProcessor ports() {
        return portProc;
    }

    /** {@inheritDoc} */
    @Override public GridEmailProcessorAdapter email() {
        return emailProc;
    }

    /** {@inheritDoc} */
    @Override public GridOffHeapProcessor offheap() {
        return offheapProc;
    }

    /** {@inheritDoc} */
    @Override public GridScheduleProcessorAdapter schedule() {
        return scheduleProc;
    }

    /** {@inheritDoc} */
    @Override public GridStreamProcessor stream() {
        return streamProc;
    }

    /** {@inheritDoc} */
    @Override public GridDeploymentManager deploy() {
        return depMgr;
    }

    /** {@inheritDoc} */
    @Override public GridIoManager io() {
        return ioMgr;
    }

    /** {@inheritDoc} */
    @Override public GridDiscoveryManager discovery() {
        return discoMgr;
    }

    /** {@inheritDoc} */
    @Override public GridCheckpointManager checkpoint() {
        return cpMgr;
    }

    /** {@inheritDoc} */
    @Override public GridEventStorageManager event() {
        return evtMgr;
    }

    /** {@inheritDoc} */
    @Override public GridFailoverManager failover() {
        return failoverMgr;
    }

    /** {@inheritDoc} */
    @Override public GridCollisionManager collision() {
        return colMgr;
    }

    /** {@inheritDoc} */
    @Override public GridSecurityManager security() {
        return authMgr;
    }

    /** {@inheritDoc} */
    @Override public GridSecureSessionManager secureSession() {
        return sesMgr;
    }

    /** {@inheritDoc} */
    @Override public GridLoadBalancerManager loadBalancing() {
        return loadMgr;
    }

    /** {@inheritDoc} */
    @Override public GridSwapSpaceManager swap() {
        return swapspaceMgr;
    }

    /** {@inheritDoc} */
    @Override public GridIndexingManager indexing() {
        return indexingMgr;
    }

    /** {@inheritDoc} */
    @Override public GridLicenseProcessor license() {
        return licProc;
    }

    /** {@inheritDoc} */
    @Override public GridAffinityProcessor affinity() {
        return affProc;
    }

    /** {@inheritDoc} */
    @Override public GridRestProcessor rest() {
        return restProc;
    }

    /** {@inheritDoc} */
    @Override public GridSegmentationProcessor segmentation() {
        return segProc;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override public <K, V> GridDataLoaderProcessor<K, V> dataLoad() {
        return (GridDataLoaderProcessor<K, V>)dataLdrProc;
    }

    /** {@inheritDoc} */
    @Override public GridGgfsProcessorAdapter ggfs() {
        return ggfsProc;
    }

    /** {@inheritDoc} */
    @Override public GridGgfsHelper ggfsHelper() {
        return ggfsHelper;
    }

    /** {@inheritDoc} */
    @Override public GridContinuousProcessor continuous() {
        return contProc;
    }

    /** {@inheritDoc} */
    @Override public GridHadoopProcessorAdapter hadoop() {
        return hadoopProc;
    }

    /** {@inheritDoc} */
    @Override public ExecutorService utilityCachePool() {
        return utilityCachePool;
    }

    /** {@inheritDoc} */
    @Override public GridPortableProcessor portable() {
        return portableProc;
    }

    /** {@inheritDoc} */
    @Override public GridInteropProcessor interop() {
        return interopProc;
    }

    /** {@inheritDoc} */
    @Override public GridQueryProcessor query() {
        return qryProc;
    }

    /** {@inheritDoc} */
    @Override public IgniteLogger log() {
        return config().getGridLogger();
    }

    /** {@inheritDoc} */
    @Override public IgniteLogger log(Class<?> cls) {
        return config().getGridLogger().getLogger(cls);
    }

    /** {@inheritDoc} */
    @Override public void markSegmented() {
        segFlag = true;
    }

    /** {@inheritDoc} */
    @Override public boolean segmented() {
        return segFlag;
    }

    /** {@inheritDoc} */
    @Override public GridClockSource timeSource() {
        return clockSrc;
    }

    /**
     * @param product Product.
     */
    public void product(IgniteProduct product) {
        this.product = product;
    }

    /** {@inheritDoc} */
    @Override public IgniteProduct product() {
        return product;
    }

    /**
     * Sets time source. For test purposes only.
     *
     * @param clockSrc Time source.
     */
    public void timeSource(GridClockSource clockSrc) {
        this.clockSrc = clockSrc;
    }

    /** {@inheritDoc} */
    @Override public GridPerformanceSuggestions performance() {
        return perf;
    }

    /** {@inheritDoc} */
    @Override public boolean isEnterprise() {
        return ent;
    }

    /** {@inheritDoc} */
    @Override public void printMemoryStats() {
        X.println(">>> ");
        X.println(">>> Grid memory stats [grid=" + gridName() + ']');

        for (GridComponent comp : comps)
            comp.printMemoryStats();
    }

    /** {@inheritDoc} */
    @Override public boolean isDaemon() {
        return config().isDaemon() || "true".equalsIgnoreCase(System.getProperty(GG_DAEMON));
    }

    /** {@inheritDoc} */
    @Override public String userVersion(ClassLoader ldr) {
        return spring != null ? spring.userVersion(ldr, log()) : U.DFLT_USER_VERSION;
    }

    /** {@inheritDoc} */
    @Override public PluginProvider pluginProvider(String name) throws PluginNotFoundException {
        PluginProvider plugin = pluginProc.pluginProvider(name);

        if (plugin == null)
            throw new PluginNotFoundException(name);

        return plugin;
    }

    /** {@inheritDoc} */
    @Nullable @Override public <T> T createComponent(Class<T> cls) {
        T res = pluginProc.createComponent(cls);

        if (res != null)
            return res;

        if (cls.equals(GridCacheDrManager.class))
            return (T)new GridOsCacheDrManager();

        throw new IgniteException("Unsupported component type: " + cls);
    }

    /** {@inheritDoc} */
    @Override public GridTcpMessageFactory messageFactory() {
        assert msgFactory != null;

        return msgFactory;
    }

    /** {@inheritDoc} */
    @Override public byte registerMessageProducer(GridTcpCommunicationMessageProducer producer) {
        int nextMsg = ++pluginMsg;

        if (nextMsg > Byte.MAX_VALUE)
            throw new IgniteException();

        if (pluginMsgs == null)
            pluginMsgs = new HashMap<>();

        pluginMsgs.put((byte)nextMsg, producer);

        return (byte)nextMsg;
    }

    /**
     * Creates message factory.
     */
    void createMessageFactory() {
        final GridTcpCommunicationMessageProducer[] common = GridTcpCommunicationMessageFactory.commonProducers();

        final GridTcpCommunicationMessageProducer[] producers;

        if (pluginMsgs != null) {
            producers = Arrays.copyOf(common, pluginMsg + 1);

            for (Map.Entry<Byte, GridTcpCommunicationMessageProducer> e : pluginMsgs.entrySet()) {
                assert producers[e.getKey()] == null : e.getKey();

                producers[e.getKey()] = e.getValue();
            }

            pluginMsgs = null;
        }
        else
            producers = common;

        msgFactory = new GridTcpMessageFactory() {
            @Override public GridTcpCommunicationMessageAdapter create(byte type) {
                if (type < 0 || type >= producers.length)
                    return GridTcpCommunicationMessageFactory.create(type);

                GridTcpCommunicationMessageProducer producer = producers[type];

                if (producer != null)
                    return producer.create(type);
                else
                    throw new IllegalStateException("Common message type producer is not registered: " + type);
            }
        };
    }

    /**
     * @return Plugin manager.
     */
    @Override public IgnitePluginProcessor plugins() {
        return pluginProc;
    }

    /** {@inheritDoc} */
    @Override public void writeExternal(ObjectOutput out) throws IOException {
        U.writeString(out, grid.name());
    }

    /** {@inheritDoc} */
    @Override public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        stash.set(U.readString(in));
    }

    /**
     * Reconstructs object on unmarshalling.
     *
     * @return Reconstructed object.
     * @throws ObjectStreamException Thrown in case of unmarshalling error.
     */
    protected Object readResolve() throws ObjectStreamException {
        try {
            return GridGainEx.gridx(stash.get()).context();
        }
        catch (IllegalStateException e) {
            throw U.withCause(new InvalidObjectException(e.getMessage()), e);
        }
        finally {
            stash.remove();
        }
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return S.toString(GridKernalContextImpl.class, this);
    }
}
