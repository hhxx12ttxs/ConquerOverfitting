package org.libvirt;

import org.libvirt.jna.DomainPointer;
import org.libvirt.jna.DomainSnapshotPointer;
import org.libvirt.jna.Libvirt;
import org.libvirt.jna.virDomainBlockInfo;
import org.libvirt.jna.virDomainBlockStats;
import org.libvirt.jna.virDomainInfo;
import org.libvirt.jna.virDomainInterfaceStats;
import org.libvirt.jna.virDomainJobInfo;
import org.libvirt.jna.virDomainMemoryStats;
import org.libvirt.jna.virSchedParameter;
import org.libvirt.jna.virVcpuInfo;

import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.ptr.IntByReference;

/**
 * A virtual machine defined within libvirt.
 */
public final class Domain {

    static final class CreateFlags {
        static final int VIR_DOMAIN_NONE = 0;
    }

    static final class MigrateFlags {
        /**
         * live migration
         */
        static final int VIR_MIGRATE_LIVE = 1;
    }

    static final class XMLFlags {
        /**
         * dump security sensitive information too
         */
        static final int VIR_DOMAIN_XML_SECURE = 1;
        /**
         * dump inactive domain information
         */
        static final int VIR_DOMAIN_XML_INACTIVE = 2;
    }

    /**
     * the native virDomainPtr.
     */
    DomainPointer VDP;

    /**
     * The Connect Object that represents the Hypervisor of this Domain
     */
    private Connect virConnect;

    /**
     * The libvirt connection from the hypervisor
     */
    protected Libvirt libvirt;

    /**
     * Constructs a Domain object from a known native DomainPointer, and a
     * Connect object.
     *
     * @param virConnect Connect
     *            The Domain's hypervisor
     * @param VDP DomainPointer
     *            The native virDomainPtr
     */
    Domain(final Connect virConnect, final DomainPointer VDP) {
        this.virConnect = virConnect;
        this.VDP = VDP;
        libvirt = virConnect.libvirt;
    }

    /**
     * Requests that the current background job be aborted at the soonest
     * opportunity. This will block until the job has either completed, or
     * aborted.
     *
     * @return int 0 in case of success and -1 in case of failure.
     * @throws LibvirtException
     * @see <a
     *      href="http://www.libvirt.org/html/libvirt-libvirt.html#virDomainAbortJob">Libvirt
     *      Documentation</a>
     */
    public int abortJob() throws LibvirtException {
        final int returnValue = libvirt.virDomainAbortJob(VDP);
        processError();
        return returnValue;
    }

    /**
     * Creates a virtual device attachment to back end.
     *
     * @param xmlDesc String
     *            XML description of one device
     * @throws LibvirtException
     * @see <a
     *      href="http://www.libvirt.org/html/libvirt-libvirt.html#virDomainAttachDevice">Libvirt
     *      Documentation</a>
     */
    public void attachDevice(final String xmlDesc) throws LibvirtException {
        libvirt.virDomainAttachDevice(VDP, xmlDesc);
        processError();
    }

    /**
     * Creates a virtual device attachment to back end.
     *
     * @param xmlDesc String
     *            XML description of one device
     * @param flags int
     *            The an OR'ed set of virDomainDeviceModifyFlags
     * @throws LibvirtException
     * @see <a
     *      href="http://www.libvirt.org/html/libvirt-libvirt.html#virDomainAttachDeviceFlags">Libvirt
     *      Documentation</a>
     */
    public void attachDeviceFlags(final String xmlDesc, final int flags) throws LibvirtException {
        libvirt.virDomainAttachDeviceFlags(VDP, xmlDesc, flags);
        processError();
    }

    /**
     * This function returns block device (disk) stats for block devices
     * attached to the domain.
     *
     * @param path String
     *            The path to the block device
     * @return DomainBlockInfo The info, or null if an error
     * @throws LibvirtException
     */
    public DomainBlockInfo blockInfo(final String path) throws LibvirtException {
        final virDomainBlockInfo info = new virDomainBlockInfo();
        final int success = libvirt.virDomainGetBlockInfo(VDP, path, info, 0);
        processError();
        return ((success == 0) ? new DomainBlockInfo(info) : null);
    }

    /**
     * Returns block device (disk) stats for block devices attached to this
     * domain. The path parameter is the name of the block device. Get this by
     * calling virDomainGetXMLDesc and finding the <target dev='...'> attribute
     * within //domain/devices/disk. (For example, "xvda"). Domains may have
     * more than one block device. To get stats for each you should make
     * multiple calls to this function. Individual fields within the
     * DomainBlockStats object may be returned as -1, which indicates that the
     * hypervisor does not support that particular statistic.
     *
     * @param path String
     *            Path to the block device
     * @return DomainBlockStats The statistics in a DomainBlockStats object
     * @throws LibvirtException
     */
    public DomainBlockStats blockStats(final String path) throws LibvirtException {
        final virDomainBlockStats stats = new virDomainBlockStats();
        final int success = libvirt.virDomainBlockStats(VDP, path, stats, stats.size());
        processError();
        return ((success == 0) ? new DomainBlockStats(stats) : null);
    }

    /**
     * Dumps the core of this domain on a given file for analysis. Note that for
     * remote Xen Daemon the file path will be interpreted in the remote host.
     *
     * @param to String
     *            Path for the core file
     * @param flags int
     *            Extra flags, currently unused
     * @throws LibvirtException
     */
    public void coreDump(final String to, final int flags) throws LibvirtException {
        libvirt.virDomainCoreDump(VDP, to, flags);
        processError();
    }

    /**
     * It returns the length (in bytes) required to store the complete CPU map
     * between a single virtual & all physical CPUs of a domain.
     *
     * @param maxCpus int
     * @return int
     */
    public int cpuMapLength(final int maxCpus) {
        return (((maxCpus) + 7) / 8);
    }

    /**
     * Launches this defined domain. If the call succeed the domain moves from
     * the defined to the running domains pools.
     *
     * @throws LibvirtException
     */
    public void create() throws LibvirtException {
        libvirt.virDomainCreate(VDP);
        processError();
    }

    /**
     * Destroys this domain object. The running instance is shutdown if not down
     * already and all resources used by it are given back to the hypervisor.
     * The data structure is freed and should not be used thereafter if the call
     * does not return an error. This function may requires privileged access.
     *
     * @throws LibvirtException
     */
    public void destroy() throws LibvirtException {
        libvirt.virDomainDestroy(VDP);
        processError();
    }

    /**
     * Destroys a virtual device attachment to back end.
     *
     * @param xmlDesc String
     *            XML description of one device
     * @throws LibvirtException
     * @see <a
     *      href="http://www.libvirt.org/html/libvirt-libvirt.html#virDomainDetachDevice">Libvirt
     *      Documentation</a>
     */
    public void detachDevice(final String xmlDesc) throws LibvirtException {
        libvirt.virDomainDetachDevice(VDP, xmlDesc);
        processError();
    }

    /**
     * Destroys a virtual device attachment to back end.
     *
     * @param xmlDesc String
     *            XML description of one device
     * @param flags int
     *            Extra flags, currently unused
     * @throws LibvirtException
     * @see <a
     *      href="http://www.libvirt.org/html/libvirt-libvirt.html#virDomainDetachDeviceFlags">Libvirt
     *      Documentation</a>
     */
    public void detachDeviceFlags(final String xmlDesc, final int flags) throws LibvirtException {
        libvirt.virDomainDetachDeviceFlags(VDP, xmlDesc, flags);
        processError();
    }

    @Override
    public void finalize() throws LibvirtException {
        free();
    }

    /**
     * Frees this domain object. The running instance is kept alive. The data
     * structure is freed and should not be used thereafter.
     *
     * @return int Number of references left (>= 0) for success, -1 for failure.
     * @throws LibvirtException
     */
    public int free() throws LibvirtException {
        int success = 0;
        if (VDP != null) {
            success = libvirt.virDomainFree(VDP);
            processError();
            VDP = null;
        }
        return success;
    }

    /**
     * Provides a boolean value indicating whether the network is configured to
     * be automatically started when the host machine boots.
     *
     * @return boolean The result
     * @throws LibvirtException
     */
    public boolean getAutostart() throws LibvirtException {
        final IntByReference autoStart = new IntByReference();
        libvirt.virDomainGetAutostart(VDP, autoStart);
        processError();
        return ((autoStart.getValue() != 0) ? true : false);
    }

    /**
     * Provides the connection object associated with a domain.
     *
     * @return Connect The Connect object
     */
    public Connect getConnect() {
        return virConnect;
    }

    /**
     * Gets the hypervisor ID number for the domain.
     *
     * @return int The hypervisor ID
     * @throws LibvirtException
     */
    public int getID() throws LibvirtException {
        final int returnValue = libvirt.virDomainGetID(VDP);
        processError();
        return returnValue;
    }

    /**
     * Extract information about a domain. Note that if the connection used to
     * get the domain is limited only a partial set of the information can be
     * extracted.
     *
     * @return DomainInfo A DomainInfo object describing this domain
     * @throws LibvirtException
     * @see <a
     *      href="http://www.libvirt.org/html/libvirt-libvirt.html#virDomainGetInfo">Libvirt
     *      Documentation</a>
     */
    public DomainInfo getInfo() throws LibvirtException {
        final virDomainInfo vInfo = new virDomainInfo();
        final int success = libvirt.virDomainGetInfo(VDP, vInfo);
        processError();
        return ((success == 0) ? new DomainInfo(vInfo) : null);
    }

    /**
     * Extract information about progress of a background job on a domain. Will
     * return an error if the domain is not active.
     *
     * @return DomainJobInfo A DomainJobInfo object describing this domain
     * @throws LibvirtException
     * @see <a
     *      href="http://www.libvirt.org/html/libvirt-libvirt.html#virDomainGetJobInfo">Libvirt
     *      Documentation</a>
     */
    public DomainJobInfo getJobInfo() throws LibvirtException {
        final virDomainJobInfo vInfo = new virDomainJobInfo();
        final int success = libvirt.virDomainGetJobInfo(VDP, vInfo);
        processError();
        return ((success == 0) ? new DomainJobInfo(vInfo) : null);
    }

    /**
     * Retrieve the maximum amount of physical memory allocated to a domain.
     *
     * @return long The memory in kilobytes
     * @throws LibvirtException
     */
    public long getMaxMemory() throws LibvirtException {
        final NativeLong returnValue = libvirt.virDomainGetMaxMemory(VDP);
        processError();
        return returnValue.longValue();
    }

    /**
     * Provides the maximum number of virtual CPUs supported for the guest VM.
     * If the guest is inactive, this is basically the same as
     * virConnectGetMaxVcpus. If the guest is running this will reflect the
     * maximum number of virtual CPUs the guest was booted with.
     *
     * @return int The number of VCPUs
     * @throws LibvirtException
     */
    public int getMaxVcpus() throws LibvirtException {
        final int returnValue = libvirt.virDomainGetMaxVcpus(VDP);
        processError();
        return returnValue;
    }

    /**
     * Gets the public name for this domain.
     *
     * @return String The name
     * @throws LibvirtException
     */
    public String getName() throws LibvirtException {
        final String returnValue = libvirt.virDomainGetName(VDP);
        processError();
        return returnValue;
    }

    /**
     * Gets the type of domain operation system.
     *
     * @return String The type
     * @throws LibvirtException
     */
    public String getOSType() throws LibvirtException {
        final String returnValue = libvirt.virDomainGetOSType(VDP);
        processError();
        return returnValue;
    }

    /**
     * Gets the scheduler parameters.
     *
     * @return SchedParameter[] An array of SchedParameter objects
     * @throws LibvirtException
     */
    public SchedParameter[] getSchedulerParameters() throws LibvirtException {
        final IntByReference nParams = new IntByReference();
        SchedParameter[] returnValue = new SchedParameter[0];
        final String scheduler = libvirt.virDomainGetSchedulerType(VDP, nParams);
        processError();
        if (scheduler != null) {
            final virSchedParameter[] nativeParams = new virSchedParameter[nParams.getValue()];
            returnValue = new SchedParameter[nParams.getValue()];
            libvirt.virDomainGetSchedulerParameters(VDP, nativeParams, nParams);
            processError();
            for (int x = 0; x < nParams.getValue(); x++) {
                returnValue[x] = SchedParameter.create(nativeParams[x]);
            }
        }
        return returnValue;
    }

    // getSchedulerType
    // We don't expose the nparams return value, it's only needed for the
    // SchedulerParameters allocations,
    // but we handle that in getSchedulerParameters internally.
    /**
     * Gets the scheduler type.
     *
     * @return String[] The type of the scheduler
     * @throws LibvirtException
     */
    public String[] getSchedulerType() throws LibvirtException {
        final IntByReference nParams = new IntByReference();
        final String returnValue = libvirt.virDomainGetSchedulerType(VDP, nParams);
        processError();
        return new String[] { returnValue };
    }

    /**
     * Get the UUID for this domain.
     *
     * @return int[] The UUID as an unpacked int array
     * @throws LibvirtException
     * @see <a href="http://www.ietf.org/rfc/rfc4122.txt">rfc4122</a>
     */
    public int[] getUUID() throws LibvirtException {
        final byte[] bytes = new byte[Libvirt.VIR_UUID_BUFLEN];
        final int success = libvirt.virDomainGetUUID(VDP, bytes);
        processError();
        return ((success == 0) ? Connect.convertUUIDBytes(bytes) : new int[0]);
    }

    /**
     * Gets the UUID for this domain as string.
     *
     * @return String The UUID in canonical String format
     * @throws LibvirtException
     * @see <a href="http://www.ietf.org/rfc/rfc4122.txt">rfc4122</a>
     */
    public String getUUIDString() throws LibvirtException {
        final byte[] bytes = new byte[Libvirt.VIR_UUID_STRING_BUFLEN];
        final int success = libvirt.virDomainGetUUIDString(VDP, bytes);
        processError();
        return ((success == 0) ? Native.toString(bytes) : null);
    }

    /**
     * Returns the cpumaps for this domain Only the lower 8 bits of each int in
     * the array contain information.
     *
     * @return int[] A bitmap of real CPUs for all vcpus of this domain
     * @throws LibvirtException
     */
    public int[] getVcpusCpuMaps() throws LibvirtException {
        int[] returnValue = new int[0];
        final int cpuCount = getMaxVcpus();
        if (cpuCount > 0) {
            final NodeInfo nodeInfo = virConnect.nodeInfo();
            final int maplength = cpuMapLength(nodeInfo.maxCpus());
            final virVcpuInfo[] infos = new virVcpuInfo[cpuCount];
            returnValue = new int[cpuCount * maplength];
            final byte[] cpumaps = new byte[cpuCount * maplength];
            libvirt.virDomainGetVcpus(VDP, infos, cpuCount, cpumaps, maplength);
            processError();
            for (int x = 0; x < cpuCount * maplength; x++) {
                returnValue[x] = cpumaps[x];
            }
        }
        return returnValue;
    }

    /**
     * Extracts information about virtual CPUs of this domain.
     *
     * @return VcpuInfo[] An array of VcpuInfo object describing the VCPUs
     * @throws LibvirtException
     */
    public VcpuInfo[] getVcpusInfo() throws LibvirtException {
        final int cpuCount = getMaxVcpus();
        final VcpuInfo[] returnValue = new VcpuInfo[cpuCount];
        final virVcpuInfo[] infos = new virVcpuInfo[cpuCount];
        libvirt.virDomainGetVcpus(VDP, infos, cpuCount, null, 0);
        processError();
        for (int x = 0; x < cpuCount; x++) {
            returnValue[x] = new VcpuInfo(infos[x]);
        }
        return returnValue;
    }

    /**
     * Provides an XML description of the domain. The description may be reused
     * later to relaunch the domain with createLinux().
     *
     * @param flags int
     *            Not used
     * @return String The XML description String
     * @throws LibvirtException
     * @see <a href="http://libvirt.org/format.html#Normal1" >The XML
     *      Description format </a>
     */
    public String getXMLDesc(final int flags) throws LibvirtException {
        final String returnValue = libvirt.virDomainGetXMLDesc(VDP, flags);
        processError();
        return returnValue;
    }

    /**
     * Determine if the domain has a snapshot.
     *
     * @return int 1 if running, 0 if inactive, -1 on error
     * @throws LibvirtException
     * @see <a href="http://www.libvirt.org/html/libvirt-libvirt.html#virDomainHasCurrentSnapshot>Libvi
     *      r t Documentation</a>
     */
    public int hasCurrentSnapshot() throws LibvirtException {
        final int returnValue = libvirt.virDomainHasCurrentSnapshot(VDP, 0);
        processError();
        return returnValue;
    }

    /**
     * Determine if the domain has a managed save image.
     *
     * @return int 0 if no image is present, 1 if an image is present, and -1 in
     *         case of error
     * @throws LibvirtException
     * @see <a href="http://www.libvirt.org/html/libvirt-libvirt.html#virDomainHasManagedSaveImage>Libvi
     *      r t Documentation</a>
     */
    public int hasManagedSaveImage() throws LibvirtException {
        final int returnValue = libvirt.virDomainHasManagedSaveImage(VDP, 0);
        processError();
        return returnValue;
    }

    /**
     * Returns network interface stats for interfaces attached to this domain.
     * The path parameter is the name of the network interface. Domains may have
     * more than network interface. To get stats for each you should make
     * multiple calls to this function. Individual fields within the
     * DomainInterfaceStats object may be returned as -1, which indicates that
     * the hypervisor does not support that particular statistic.
     *
     * @param path String
     *            Path to the interface
     * @return DomainInterfaceStats The statistics in a DomainInterfaceStats object
     * @throws LibvirtException
     */
    public DomainInterfaceStats interfaceStats(String path) throws LibvirtException {
        final virDomainInterfaceStats stats = new virDomainInterfaceStats();
        libvirt.virDomainInterfaceStats(VDP, path, stats, stats.size());
        processError();
        return new DomainInterfaceStats(stats);
    }

    /**
     * Determine if the domain is currently running.
     *
     * @return int 1 if running, 0 if inactive, -1 on error
     * @throws LibvirtException
     * @see <a
     *      href="http://www.libvirt.org/html/libvirt-libvirt.html#virDomainIsActive">Libvirt
     *      Documentation</a>
     */
    public int isActive() throws LibvirtException {
        final int returnValue = libvirt.virDomainIsActive(VDP);
        processError();
        return returnValue;
    }

    /**
     * Determine if the domain has a persistent configuration which means it
     * will still exist after shutting down.
     *
     * @return int 1 if persistent, 0 if transient, -1 on error
     * @throws LibvirtException
     * @see <a
     *      href="http://www.libvirt.org/html/libvirt-libvirt.html#virDomainIsPersistent">Libvirt
     *      Documentation</a>
     */
    public int isPersistent() throws LibvirtException {
        final int returnValue = libvirt.virDomainIsPersistent(VDP);
        processError();
        return returnValue;
    }

    /**
     * Suspend a domain and save its memory contents to a file on disk.
     *
     * @return int 0 in case of success or -1 in case of failure
     * @throws LibvirtException
     * @see <a
     *      href="http://www.libvirt.org/html/libvirt-libvirt.html#virDomainManagedSave">Libvirt
     *      Documentation</a>
     */
    public int managedSave() throws LibvirtException {
        final int returnValue = libvirt.virDomainManagedSave(VDP, 0);
        processError();
        return returnValue;
    }

    /**
     * Remove any managed save images from the domain.
     *
     * @return int
     * @throws LibvirtException
     * @see <a
     *      href="http://www.libvirt.org/html/libvirt-libvirt.html#virDomainManagedSaveRemove">Libvirt
     *      Documentation</a>
     */
    public int managedSaveRemote() throws LibvirtException {
        final int returnValue = libvirt.virDomainManagedSaveRemove(VDP, 0);
        processError();
        return returnValue;
    }

    /**
     * This function provides memory statistics for the domain.
     *
     * @param number int
     *            The number of stats to retrieve
     * @return MemoryStatistic[] The collection of stats, or null if an error occurs.
     * @throws LibvirtException
     */
    public MemoryStatistic[] memoryStats(final int number) throws LibvirtException {
        final virDomainMemoryStats[] stats = new virDomainMemoryStats[number];
        MemoryStatistic[] returnStats = null;
        final int result = libvirt.virDomainMemoryStats(VDP, stats, number, 0);
        processError();
        if (result >= 0) {
            returnStats = new MemoryStatistic[result];
            for (int x = 0; x < result; x++) {
                returnStats[x] = new MemoryStatistic(stats[x]);
            }
        }
        return returnStats;
    }

    /**
     * Migrate this domain object from its current host to the destination host
     * given by dconn (a connection to the destination host). Flags may be one
     * of more of the following: Domain.VIR_MIGRATE_LIVE Attempt a live
     * migration. If a hypervisor supports renaming domains during migration,
     * then you may set the dname parameter to the new name (otherwise it keeps
     * the same name). If this is not supported by the hypervisor, dname must be
     * NULL or else you will get an error. Since typically the two hypervisors
     * connect directly to each other in order to perform the migration, you may
     * need to specify a path from the source to the destination. This is the
     * purpose of the uri parameter.If uri is NULL, then libvirt will try to
     * find the best method. Uri may specify the hostname or IP address of the
     * destination host as seen from the source, or uri may be a URI giving
     * transport, hostname, user, port, etc. in the usual form. Uri should only
     * be specified if you want to migrate over a specific interface on the
     * remote host. For Qemu/KVM, the uri should be of the form
     * "tcp://hostname[:port]". This does not require TCP auth to be setup
     * between the connections, since migrate uses a straight TCP connection
     * (unless using the PEER2PEER flag, in which case URI should be a full
     * fledged libvirt URI). Refer also to driver documentation for the
     * particular URIs supported. If set to 0, libvirt will choose a suitable
     * default. Some hypervisors do not support this feature and will return an
     * error if bandwidth is not 0. To see which features are supported by the
     * current hypervisor, see Connect.getCapabilities,
     * /capabilities/host/migration_features. There are many limitations on
     * migration imposed by the underlying technology - for example it may not
     * be possible to migrate between different processors even with the same
     * architecture, or between different types of hypervisor.
     *
     * @param dconn Connect
     *            Destination host (a Connect object)
     * @param flags long
     *            Flags
     * @param dname String
     *            (Optional) Rename domain to this at destination
     * @param uri String
     *            (Optional) Dest hostname/URI as seen from the source host
     * @param bandwidth long
     *            Optional) Specify migration bandwidth limit in Mbps
     * @return Domain The new domain object if the migration was successful, or NULL in
     *         case of error. Note that the new domain object exists in the
     *         scope of the destination connection (dconn).
     * @throws LibvirtException
     */
    public Domain migrate(final Connect dconn, final long flags, final String dname, final String uri, final long bandwidth) throws LibvirtException {
        final DomainPointer newPtr = libvirt.virDomainMigrate(VDP, dconn.VCP, new NativeLong(flags), dname, uri, new NativeLong(bandwidth));
        processError();
        return new Domain(dconn, newPtr);
    }

    /**
     * Sets maximum tolerable time for which the domain is allowed to be paused
     * at the end of live migration.
     *
     * @param downtime long
     *            The time to be down
     * @return int 0 in case of success, -1 otherwise.
     * @throws LibvirtException
     * @see <a
     *      href="http://www.libvirt.org/html/libvirt-libvirt.html#virDomainMigrateSetMaxDowntime">LIbvirt
     *      Documentation</a>
     */
    public int migrateSetMaxDowntime(final long downtime) throws LibvirtException {
        final int returnValue = libvirt.virDomainMigrateSetMaxDowntime(VDP, downtime, 0);
        processError();
        return returnValue;
    }

    /**
     * Migrate the domain object from its current host to the destination host
     * given by uri.
     *
     * @param uri String
     *            The destination URI
     * @param flags long
     *            Controls the migrate
     * @param dname String
     *            The name at the destnation
     * @param bandwidth long
     *            Specify the migration bandwidth
     * @return int 0 if successful, -1 if not
     * @throws LibvirtException
     * @see http://www.libvirt.org/html/libvirt-libvirt.html#virDomainMigrateToURI
     */
    public int migrateToURI(final String uri, final long flags, final String dname, final long bandwidth) throws LibvirtException {
        final int returnValue = libvirt.virDomainMigrateToURI(VDP, uri, new NativeLong(flags), dname, new NativeLong(bandwidth));
        processError();
        return returnValue;
    }

    /**
     * Dynamically changes the real CPUs which can be allocated to a virtual
     * CPU. This function requires privileged access to the hypervisor.
     *
     * @param vcpu int
     *            Virtual cpu number
     * @param cpumap int[]
     *            Bit map of real CPUs represented by the the lower 8 bits of
     *            each int in the array. Each bit set to 1 means that
     *            corresponding CPU is usable. Bytes are stored in little-endian
     *            order: CPU0-7, 8-15... In each byte, lowest CPU number is
     *            least significant bit.
     * @throws LibvirtException
     */
    public void pinVcpu(final int vcpu, final int[] cpumap) throws LibvirtException {
        final byte[] packedMap = new byte[cpumap.length];
        for (int x = 0; x < cpumap.length; x++) {
            packedMap[x] = (byte) cpumap[x];
        }
        libvirt.virDomainPinVcpu(VDP, vcpu, packedMap, cpumap.length);
        processError();
    }

    /**
     * Error handling logic to throw errors. Must be called after every libvirt call.
     *
     * @throws LibvirtException
     */
    protected void processError() throws LibvirtException {
        virConnect.processError();
    }

    /**
     * Reboot this domain, the domain object is still usable there after but the
     * domain OS is being stopped for a restart. Note that the guest OS may
     * ignore the request.
     *
     * @param flags int
     *            Extra flags for the reboot operation, not used yet
     * @throws LibvirtException
     */
    public void reboot(final int flags) throws LibvirtException {
        libvirt.virDomainReboot(VDP, flags);
        processError();
    }

    /**
     * Resume this suspended domain, the process is restarted from the state
     * where it was frozen by calling virSuspendDomain(). This function may
     * requires privileged access.
     *
     * @throws LibvirtException
     */
    public void resume() throws LibvirtException {
        libvirt.virDomainResume(VDP);
        processError();
    }

    /**
     * Revert the domain to a given snapshot.
     *
     * @param snapshot DomainSnapshot
     *            The snapshot to revert to
     * @return int 0 if the creation is successful, -1 on error.
     * @throws LibvirtException
     * @see <a href=
     *      "http://www.libvirt.org/html/libvirt-libvirt.html#virDomainRevertToSnapshot"
     *      >Libvirt Documentation</>
     */
    public int revertToSnapshot(final DomainSnapshot snapshot) throws LibvirtException {
        final int returnCode = libvirt.virDomainRevertToSnapshot(snapshot.VDSP, 0);
        processError();
        return returnCode;
    }

    /**
     * Suspends this domain and saves its memory contents to a file on disk.
     * After the call, if successful, the domain is not listed as running
     * anymore (this may be a problem). Use Connect.virDomainRestore() to
     * restore a domain after saving.
     *
     * @param to String
     *            Path for the output file
     * @throws LibvirtException
     */
    public void save(final String to) throws LibvirtException {
        libvirt.virDomainSave(VDP, to);
        processError();
    }

    /**
     * Configures the network to be automatically started when the host machine
     * boots.
     *
     * @param autostart boolean
     * @throws LibvirtException
     */
    public void setAutostart(final boolean autostart) throws LibvirtException {
        final int autoValue = ((autostart) ? 1 : 0);
        libvirt.virDomainSetAutostart(VDP, autoValue);
        processError();
    }

    /**
     * * Dynamically change the maximum amount of physical memory allocated to a
     * domain. This function requires privileged access to the hypervisor.
     *
     * @param memory long
     *            The amount memory in kilobytes
     * @throws LibvirtException
     */
    public void setMaxMemory(final long memory) throws LibvirtException {
        libvirt.virDomainSetMaxMemory(VDP, new NativeLong(memory));
        processError();
    }

    /**
     * Dynamically changes the target amount of physical memory allocated to
     * this domain. This function may requires privileged access to the
     * hypervisor.
     *
     * @param memory long
     *            In kilobytes
     * @throws LibvirtException
     */
    public void setMemory(final long memory) throws LibvirtException {
        libvirt.virDomainSetMemory(VDP, new NativeLong(memory));
        processError();
    }

    /**
     * Changes the scheduler parameters.
     *
     * @param params SchedParameter[]
     *            An array of SchedParameter objects to be changed
     * @throws LibvirtException
     */
    public void setSchedulerParameters(final SchedParameter[] params) throws LibvirtException {
        final virSchedParameter[] input = new virSchedParameter[params.length];
        for (int x = 0; x < params.length; x++) {
            input[x] = SchedParameter.toNative(params[x]);
        }
        libvirt.virDomainSetSchedulerParameters(VDP, input, params.length);
        processError();
    }

    /**
     * Dynamically changes the number of virtual CPUs used by this domain. Note
     * that this call may fail if the underlying virtualization hypervisor does
     * not support it or if growing the number is arbitrary limited. This
     * function requires privileged access to the hypervisor.
     *
     * @param nvcpus int
     *            The new number of virtual CPUs for this domain
     * @throws LibvirtException
     */
    public void setVcpus(final int nvcpus) throws LibvirtException {
        libvirt.virDomainSetVcpus(VDP, nvcpus);
        processError();
    }

    /**
     * Shuts down this domain, the domain object is still usable there after but
     * the domain OS is being stopped. Note that the guest OS may ignore the
     * request. TODO: should we add an option for reboot, knowing it may not be
     * double in the general case ?.
     *
     * @throws LibvirtException
     */
    public void shutdown() throws LibvirtException {
        libvirt.virDomainShutdown(VDP);
        processError();
    }

    /**
     * Creates a new snapshot of a domain based on the snapshot xml contained in
     * xmlDesc.
     *
     * @param xmlDesc String
     *            String containing an XML description of the domain
     * @return DomainSnapshot The snapshot, or null on Error
     * @throws LibvirtException
     * @see <a
     *      href="http://www.libvirt.org/html/libvirt-libvirt.html#virDomainSnapshotCreateXML">Libvirt
     *      Documentation</a>
     */
    public DomainSnapshot snapshotCreateXML(final String xmlDesc) throws LibvirtException {
        final DomainSnapshotPointer ptr = libvirt.virDomainSnapshotCreateXML(VDP, xmlDesc, 0);
        processError();
        return ((ptr != null) ? new DomainSnapshot(virConnect, ptr) : null);
    }

    /**
     * Get the current snapshot for a domain, if any.
     *
     * @return DomainSnapshot The snapshot, or null on Error
     * @throws LibvirtException
     * @see <a
     *      href="http://www.libvirt.org/html/libvirt-libvirt.html#virDomainSnapshotCurrent">Libvirt
     *      Documentation</a>
     */
    public DomainSnapshot snapshotCurrent() throws LibvirtException {
        final DomainSnapshotPointer ptr = libvirt.virDomainSnapshotCurrent(VDP, 0);
        processError();
        return ((ptr != null) ? new DomainSnapshot(virConnect, ptr) : null);
    }

    /**
     * Collect the list of domain snapshots for the given domain.
     *
     * @return String[] The list of names, or null if an error
     * @throws LibvirtException
     * @see <a
     *      href="http://www.libvirt.org/html/libvirt-libvirt.html#virDomainSnapshotListNames">Libvirt
     *      Documentation</a>
     */
    public String[] snapshotListNames() throws LibvirtException {
        String[] returnValue = null;
        final int num = snapshotNum();
        if (num >= 0) {
            returnValue = new String[num];
            if (num > 0) {
                libvirt.virDomainSnapshotListNames(VDP, returnValue, num, 0);
                processError();
            }
        }
        return returnValue;
    }

    /**
     * Retrieve a snapshot by name.
     *
     * @param name String
     *            The name
     * @return DomainSnapshot The located snapshot, or null if an error
     * @throws LibvirtException
     * @see <a
     *      href="http://www.libvirt.org/html/libvirt-libvirt.html#virDomainSnapshotLookupByName">Libvirt
     *      Documentation</a>
     */
    public DomainSnapshot snapshotLookupByName(final String name) throws LibvirtException {
        final DomainSnapshotPointer ptr = libvirt.virDomainSnapshotLookupByName(VDP, name, 0);
        processError();
        return ((ptr != null) ? new DomainSnapshot(virConnect, ptr) : null);
    }

    /**
     * Provides the number of domain snapshots for this domain.
     *
     * @return int
     * @throws LibvirtException
     * @see <a
     *      href="http://www.libvirt.org/html/libvirt-libvirt.html#virDomainSnapshotNum">Libvirt
     *      Documentation</a>
     */
    public int snapshotNum() throws LibvirtException {
        final int returnValue = libvirt.virDomainSnapshotNum(VDP, 0);
        processError();
        return returnValue;
    }

    /**
     * Suspends this active domain, the process is frozen without further access
     * to CPU resources and I/O but the memory used by the domain at the
     * hypervisor level will stay allocated. Use Domain.resume() to reactivate
     * the domain. This function requires privileged access.
     *
     * @throws LibvirtException
     */
    public void suspend() throws LibvirtException {
        libvirt.virDomainSuspend(VDP);
        processError();
    }

    /**
     * Undefined this domain but does not stop it if it is running.
     *
     * @throws LibvirtException
     */
    public void undefine() throws LibvirtException {
        libvirt.virDomainUndefine(VDP);
        processError();
    }

    /**
     * Change a virtual device on a domain.
     *
     * @param xml String
     *            The xml to update with
     * @param flags int
     *            Controls the update
     * @return int 0 in case of success, -1 in case of failure.
     * @throws LibvirtException
     * @see <a
     *      href="http://www.libvirt.org/html/libvirt-libvirt.html#virDomainUpdateDeviceFlags">Libvirt
     *      Documentation</a>
     */
    public int updateDeviceFlags(final String xml, final int flags) throws LibvirtException {
        final int returnValue = libvirt.virDomainUpdateDeviceFlags(VDP, xml, flags);
        processError();
        return returnValue;
    }

}

