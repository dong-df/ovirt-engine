package org.ovirt.engine.core.vdsbroker.builder.vminfo;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.vdsbroker.vdsbroker.IoTuneUtils.MB_TO_BYTES;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.common.businessentities.AdditionalFeature;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.GraphicsInfo;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.SupportedAdditionalClusterFeature;
import org.ovirt.engine.core.common.businessentities.UsbControllerModel;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmInit;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.businessentities.network.NetworkFilter;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.businessentities.network.VmNicFilterParameter;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.common.businessentities.qos.StorageQos;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.businessentities.storage.LunDisk;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.CinderStorageDao;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.ClusterFeatureDao;
import org.ovirt.engine.core.dao.DiskVmElementDao;
import org.ovirt.engine.core.dao.HostDeviceDao;
import org.ovirt.engine.core.dao.StorageDomainStaticDao;
import org.ovirt.engine.core.dao.StorageServerConnectionDao;
import org.ovirt.engine.core.dao.VdsDynamicDao;
import org.ovirt.engine.core.dao.VdsNumaNodeDao;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.dao.VdsStatisticsDao;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.dao.VmNumaNodeDao;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.dao.network.NetworkFilterDao;
import org.ovirt.engine.core.dao.network.NetworkQoSDao;
import org.ovirt.engine.core.dao.network.VmNicFilterParameterDao;
import org.ovirt.engine.core.dao.network.VnicProfileDao;
import org.ovirt.engine.core.dao.qos.StorageQosDao;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockConfigExtension;
import org.ovirt.engine.core.vdsbroker.monitoring.VmDevicesMonitoring;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsProperties;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VmSerialNumberBuilder;

@ExtendWith({MockitoExtension.class, MockConfigExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
public class VmInfoBuildUtilsTest {

    private static final Guid VNIC_PROFILE_ID = Guid.newGuid();
    private static final Guid VM_NIC_ID = Guid.newGuid();
    private static final Guid NETWORK_FILTER_ID = Guid.newGuid();
    private static final String NETWORK_FILTER_NAME = "clean-traffic";
    private static final Guid NETWORK_FILTER_PARAMETER_0_ID = Guid.newGuid();
    private static final Guid NETWORK_FILTER_PARAMETER_1_ID = Guid.newGuid();
    private static final String NETWORK_FILTER_PARAMETER_0_NAME = "IP";
    private static final String NETWORK_FILTER_PARAMETER_0_VALUE = "10.0.0.1";
    private static final String NETWORK_FILTER_PARAMETER_1_NAME = "IP";
    private static final String NETWORK_FILTER_PARAMETER_1_VALUE = "10.0.0.2";
    private static final Guid VM_ID = Guid.newGuid();
    private static final Guid DISK_IMAGE_ID = Guid.newGuid();
    private static final Guid LUN_DISK_ID = Guid.newGuid();
    private static final Guid CLUSTER_ID = Guid.newGuid();

    @Mock
    private ClusterDao clusterDao;
    @Mock
    private NetworkDao networkDao;
    @Mock
    private NetworkFilterDao networkFilterDao;
    @Mock
    private NetworkClusterDao networkClusterDao;
    @Mock
    private NetworkQoSDao networkQosDao;
    @Mock
    private StorageQosDao storageQosDao;
    @Mock
    private VmDeviceDao vmDeviceDao;
    @Mock
    private VnicProfileDao vnicProfileDao;
    @Mock
    private VmNicFilterParameterDao vmNicFilterParameterDao;
    @Mock
    private ClusterFeatureDao clusterFeatureDao;
    @Mock
    private VmNumaNodeDao vmNumaNodeDao;
    @Mock
    private AuditLogDirector auditLogDirector;
    @Mock
    private OsRepository osRepository;
    @Mock
    private StorageDomainStaticDao storageDomainStaticDao;
    @Mock
    private StorageServerConnectionDao storageServerConnectionDao;
    @Mock
    private VdsNumaNodeDao vdsNumaNodeDao;
    @Mock
    private VdsStaticDao vdsStaticDao;
    @Mock
    private VdsDynamicDao vdsDynamicDao;
    @Mock
    private VdsStatisticsDao vdsStatisticsDao;
    @Mock
    private HostDeviceDao hostDeviceDao;
    @Mock
    private DiskVmElementDao diskVmElementDao;
    @Mock
    private VmDevicesMonitoring vmDevicesMonitoring;
    @Mock
    private VmSerialNumberBuilder vmSerialNumberBuilder;
    @Mock
    private MultiQueueUtils multiQueueUtils;
    @Mock
    private CinderStorageDao cinderStorageDao;

    @InjectMocks
    private VmInfoBuildUtils underTest;

    private StorageQos qos;

    private VmDevice vmDevice;

    private DiskImage diskImage = new DiskImage();

    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.of(
                MockConfigDescriptor.of(ConfigValues.LibgfApiSupported, Version.v4_2, false),
                MockConfigDescriptor.of(ConfigValues.LibgfApiSupported, Version.v4_3, true)
        );
    }

    @BeforeEach
    public void setUp() {
        diskImage.setDiskProfileId(Guid.newGuid());

        qos = new StorageQos();
        qos.setId(Guid.newGuid());

        vmDevice = new VmDevice();

        VnicProfile vnicProfile = new VnicProfile();
        vnicProfile.setNetworkFilterId(NETWORK_FILTER_ID);
        when(vnicProfileDao.get(VNIC_PROFILE_ID)).thenReturn(vnicProfile);

        NetworkFilter networkFilter = new NetworkFilter();
        networkFilter.setName(NETWORK_FILTER_NAME);
        when(networkFilterDao.getNetworkFilterById(NETWORK_FILTER_ID)).thenReturn(networkFilter);

        when(vmNicFilterParameterDao.getAllForVmNic(VM_NIC_ID)).thenReturn(createVmNicFilterParameters());
    }

    List<VmNicFilterParameter> createVmNicFilterParameters() {
        List<VmNicFilterParameter> vmNicFilterParameters = new ArrayList();
        vmNicFilterParameters.add(new VmNicFilterParameter(
                NETWORK_FILTER_PARAMETER_0_ID,
                VM_NIC_ID,
                NETWORK_FILTER_PARAMETER_0_NAME,
                NETWORK_FILTER_PARAMETER_0_VALUE
        ));
        vmNicFilterParameters.add(new VmNicFilterParameter(
                NETWORK_FILTER_PARAMETER_1_ID,
                VM_NIC_ID,
                NETWORK_FILTER_PARAMETER_1_NAME,
                NETWORK_FILTER_PARAMETER_1_VALUE
        ));
        return vmNicFilterParameters;
    }

    void assertIoTune(Map<String, Long> ioTune,
                      long totalBytesSec, long readBytesSec, long writeBytesSec,
                      long totalIopsSec, long readIopsSec, long writeIopsSec) {
        assertEquals(ioTune.get(VdsProperties.TotalBytesSec).longValue(), totalBytesSec);
        assertEquals(ioTune.get(VdsProperties.ReadBytesSec).longValue(), readBytesSec);
        assertEquals(ioTune.get(VdsProperties.WriteBytesSec).longValue(), writeBytesSec);

        assertEquals(ioTune.get(VdsProperties.TotalIopsSec).longValue(), totalIopsSec);
        assertEquals(ioTune.get(VdsProperties.ReadIopsSec).longValue(), readIopsSec);
        assertEquals(ioTune.get(VdsProperties.WriteIopsSec).longValue(), writeIopsSec);
    }

    @Test
    public void testHandleIoTune() {
        when(storageQosDao.getQosByDiskProfileId(diskImage.getDiskProfileId())).thenReturn(qos);
        qos.setMaxThroughput(100);
        qos.setMaxIops(10000);

        underTest.handleIoTune(vmDevice, underTest.loadStorageQos(diskImage));

        assertIoTune(getIoTune(vmDevice), 100L * MB_TO_BYTES, 0, 0, 10000, 0, 0);
    }

    @Test
    public void testNoStorageQuotaAssigned() {
        underTest.handleIoTune(vmDevice, underTest.loadStorageQos(diskImage));
        assertNull(vmDevice.getSpecParams());
    }

    @Test
    public void testNoCpuProfileAssigned() {
        diskImage.setDiskProfileId(null);
        underTest.handleIoTune(vmDevice, underTest.loadStorageQos(diskImage));
        assertNull(vmDevice.getSpecParams());
    }

    @SuppressWarnings("unchecked")
    private Map<String, Long> getIoTune(VmDevice vmDevice) {
        return (Map<String, Long>) vmDevice.getSpecParams().get(VdsProperties.Iotune);
    }

    @Test
    public void testAddNetworkFiltersToNic() {
        Map<String, Object> struct = new HashMap<>();
        VmNic vmNic = new VmNic();
        vmNic.setVnicProfileId(VNIC_PROFILE_ID);
        vmNic.setId(VM_NIC_ID);

        underTest.addNetworkFiltersToNic(struct, vmNic);
        List<Map<String, Object>> parametersList =
                (List<Map<String, Object>>) struct.get(VdsProperties.NETWORK_FILTER_PARAMETERS);

        assertNotNull(struct.get(VdsProperties.NW_FILTER));
        assertEquals(NETWORK_FILTER_NAME, struct.get(VdsProperties.NW_FILTER));
        assertNotNull(parametersList);

        assertEquals(2, parametersList.size());
        assertEquals(NETWORK_FILTER_PARAMETER_0_NAME, parametersList.get(0).get("name"));
        assertEquals(NETWORK_FILTER_PARAMETER_0_VALUE, parametersList.get(0).get("value"));
        assertEquals(NETWORK_FILTER_PARAMETER_1_NAME, parametersList.get(1).get("name"));
        assertEquals(NETWORK_FILTER_PARAMETER_1_VALUE, parametersList.get(1).get("value"));
    }

    private Map<Guid, Disk> mockUnsortedDisksMap(VmDevice lunDiskVmDevice, VmDevice diskImageVmDevice) {
        when(vmDeviceDao.get(lunDiskVmDevice.getId())).thenReturn(lunDiskVmDevice);
        when(vmDeviceDao.get(diskImageVmDevice.getId())).thenReturn(diskImageVmDevice);

        DiskVmElement nonBootDiskVmElement = new DiskVmElement(lunDiskVmDevice.getId());
        nonBootDiskVmElement.setBoot(false);
        nonBootDiskVmElement.setDiskInterface(DiskInterface.VirtIO_SCSI);

        DiskVmElement bootDiskVmElement = new DiskVmElement(diskImageVmDevice.getId());
        bootDiskVmElement.setBoot(true);
        bootDiskVmElement.setDiskInterface(DiskInterface.VirtIO_SCSI);

        LunDisk lunDisk = new LunDisk();
        lunDisk.setId(LUN_DISK_ID);
        lunDisk.setDiskVmElements(Collections.singleton(nonBootDiskVmElement));

        DiskImage diskImage = new DiskImage();
        diskImage.setId(DISK_IMAGE_ID);
        diskImage.setDiskVmElements(Collections.singleton(bootDiskVmElement));

        Map<Guid, Disk> map = new HashMap<>();
        map.put(lunDisk.getId(), lunDisk);
        map.put(diskImage.getId(), diskImage);
        return map;
    }

    @Test
    public void testGetVmDeviceUnitMapForScsiDisks() {
        VmDevice lunDiskVmDevice = new VmDevice(new VmDeviceId(LUN_DISK_ID, VM_ID),
                VmDeviceGeneralType.DISK,
                VmDeviceType.DISK.getName(),
                "",
                null,
                true,
                true,
                null,
                "",
                null,
                null,
                null);

        VmDevice diskImageVmDevice = new VmDevice(new VmDeviceId(DISK_IMAGE_ID, VM_ID),
                VmDeviceGeneralType.DISK,
                VmDeviceType.DISK.getName(),
                "",
                null,
                true,
                true,
                null,
                "",
                null,
                null,
                null);

        VM vm = new VM();
        vm.setId(VM_ID);
        vm.setDiskMap(mockUnsortedDisksMap(lunDiskVmDevice, diskImageVmDevice));
        vm.setClusterArch(ArchitectureType.x86_64);
        vm.setClusterCompatibilityVersion(Version.v4_2);

        Map<Integer, Map<VmDevice, Integer>> vmDeviceUnitMap =
                underTest.getVmDeviceUnitMapForScsiDisks(vm, DiskInterface.VirtIO_SCSI, false, false);

        // Ensures that the boot disk unit is lower
        assertEquals((Integer) 1, vmDeviceUnitMap.get(0).get(lunDiskVmDevice));
        assertEquals((Integer) 0, vmDeviceUnitMap.get(0).get(diskImageVmDevice));
    }

    @Test
    public void testMakeDiskName() {
        assertEquals("hda", underTest.makeDiskName("ide", 0));
        assertEquals("hda", underTest.makeDiskName("blabla", 0));
        assertEquals("hdb", underTest.makeDiskName("blabla", 1));
        assertEquals("hdc", underTest.makeDiskName("ide", 2));
        assertEquals("sdd", underTest.makeDiskName("sata", 3));
        assertEquals("sde", underTest.makeDiskName("scsi", 4));
        assertEquals("fdf", underTest.makeDiskName("fdc", 5));
        assertEquals("vdh", underTest.makeDiskName("virtio", 7));
    }

    private Set<SupportedAdditionalClusterFeature> getSupportedAdditionalClusterFeatures(Boolean enabled) {
        SupportedAdditionalClusterFeature clusterFeature = new SupportedAdditionalClusterFeature();
        AdditionalFeature feature =
                new AdditionalFeature(Guid.newGuid(), VmInfoBuildUtils.VDSM_LIBGF_CAP_NAME, Version.v4_2, null, null);
        clusterFeature.setFeature(feature);
        clusterFeature.setEnabled(enabled);
        return Collections.singleton(clusterFeature);
    }

    @Test
    public void testGetNetworkDiskTypeForV42ClusterEnabled() {
        VM vm = new VM();
        vm.setClusterCompatibilityVersion(Version.v4_2);
        vm.setClusterId(CLUSTER_ID);
        doReturn(getSupportedAdditionalClusterFeatures(true)).when(clusterFeatureDao).getAllByClusterId(CLUSTER_ID);
        assertEquals(VdsProperties.NETWORK, underTest.getNetworkDiskType(vm, StorageType.GLUSTERFS).get());
    }

    @Test
    public void testGetNetworkDiskTypeForV42() {
        VM vm = new VM();
        vm.setClusterCompatibilityVersion(Version.v4_2);
        vm.setClusterId(CLUSTER_ID);
        doReturn(getSupportedAdditionalClusterFeatures(false)).when(clusterFeatureDao).getAllByClusterId(CLUSTER_ID);
        assertFalse(underTest.getNetworkDiskType(vm, StorageType.GLUSTERFS).isPresent());
    }

    @Test
    public void testGetNetworkDiskTypeForV43() {
        VM vm = new VM();
        vm.setClusterCompatibilityVersion(Version.v4_3);
        vm.setClusterId(CLUSTER_ID);
        assertEquals(VdsProperties.NETWORK, underTest.getNetworkDiskType(vm, StorageType.GLUSTERFS).get());
    }

    @Test
    public void testBuildCloudInitPayload() {
        VmInit vmInit = new VmInit();
        vmInit.setCustomScript("packages: [foo]");

        Map<String, byte[]> stringMap = assertDoesNotThrow(() -> underTest.buildPayload(vmInit));
        String userData = new String(stringMap.get("openstack/latest/user_data"));
        assertTrue(userData.startsWith("#cloud-config"));
        assertTrue(userData.contains("packages: [foo]"));
    }

    @Test
    public void testBuildIgnition() {
        VmInit vmInit = new VmInit();
        vmInit.setCustomScript("{\"ignition\": {} }");

        Map<String, byte[]> stringMap = assertDoesNotThrow(() -> underTest.buildPayload(vmInit));
        assertTrue(new String(stringMap.get("openstack/latest/user_data")).startsWith("{\"ignition\""));
    }

    @Test
    public void testBuildCloudInitWitNullCustomScript() {
        VmInit vmInit = new VmInit();

        Map<String, byte[]> stringMap = assertDoesNotThrow(() -> underTest.buildPayload(vmInit));
        Assertions.assertThat(stringMap.get("openstack/latest/user_data")).isNotEmpty();
    }

    @Test
    public void testBuildCloudInitWitNullVmInit() {
        Map<String, byte[]> stringMap = assertDoesNotThrow(() -> underTest.buildPayload(null));
        Assertions.assertThat(stringMap.get("openstack/latest/user_data")).isNotEmpty();
    }

    @Test
    public void testIsTabletEnabled() {
        VM vm = new VM();
        Map<GraphicsType, GraphicsInfo> m = new HashMap<>();
        vm.setGraphicsInfos(m);

        // No VNC -- No HighPerformance -- No USB Controller
        vm.setVmType(VmType.Desktop);
        reset(osRepository);
        when(osRepository.getOsUsbControllerModel(anyInt(), any(), any())).thenReturn(UsbControllerModel.NONE);
        assertFalse(underTest.isTabletEnabled(vm));

        // No VNC -- HighPerformance -- No USB Controller
        vm.setVmType(VmType.HighPerformance);
        reset(osRepository);
        when(osRepository.getOsUsbControllerModel(anyInt(), any(), any())).thenReturn(UsbControllerModel.NONE);
        assertFalse(underTest.isTabletEnabled(vm));

        // No VNC -- No HighPerformance -- USB Controller
        vm.setVmType(VmType.Desktop);
        reset(osRepository);
        when(osRepository.getOsUsbControllerModel(anyInt(), any(), any())).thenReturn(UsbControllerModel.EHCI);
        assertFalse(underTest.isTabletEnabled(vm));

        // No VNC -- HighPerformance -- with USB Controller
        vm.setVmType(VmType.HighPerformance);
        reset(osRepository);
        when(osRepository.getOsUsbControllerModel(anyInt(), any(), any())).thenReturn(UsbControllerModel.EHCI);
        assertFalse(underTest.isTabletEnabled(vm));


        // Adding VNC
        m.put(GraphicsType.VNC, new GraphicsInfo());

        // with VNC -- No HighPerformance -- No USB Controller
        vm.setVmType(VmType.Desktop);
        reset(osRepository);
        when(osRepository.getOsUsbControllerModel(anyInt(), any(), any())).thenReturn(UsbControllerModel.NONE);
        assertFalse(underTest.isTabletEnabled(vm));

        // with VNC -- HighPerformance -- No USB Controller
        vm.setVmType(VmType.HighPerformance);
        reset(osRepository);
        when(osRepository.getOsUsbControllerModel(anyInt(), any(), any())).thenReturn(UsbControllerModel.NONE);
        assertFalse(underTest.isTabletEnabled(vm));

        // with VNC -- No HighPerformance -- USB Controller
        vm.setVmType(VmType.Desktop);
        reset(osRepository);
        when(osRepository.getOsUsbControllerModel(anyInt(), any(), any())).thenReturn(UsbControllerModel.EHCI);
        assertTrue(underTest.isTabletEnabled(vm));

        // with VNC -- HighPerformance -- with USB Controller
        vm.setVmType(VmType.HighPerformance);
        reset(osRepository);
        when(osRepository.getOsUsbControllerModel(anyInt(), any(), any())).thenReturn(UsbControllerModel.EHCI);
        assertFalse(underTest.isTabletEnabled(vm));

        // Adding SPICE
        m.put(GraphicsType.SPICE, new GraphicsInfo());

        // SPICE+VNC -- No HighPerformance -- No USB Controller
        vm.setVmType(VmType.Desktop);
        reset(osRepository);
        when(osRepository.getOsUsbControllerModel(anyInt(), any(), any())).thenReturn(UsbControllerModel.NONE);
        assertFalse(underTest.isTabletEnabled(vm));

        // SPICE+VNC -- HighPerformance -- No USB Controller
        vm.setVmType(VmType.HighPerformance);
        reset(osRepository);
        when(osRepository.getOsUsbControllerModel(anyInt(), any(), any())).thenReturn(UsbControllerModel.NONE);
        assertFalse(underTest.isTabletEnabled(vm));

        // SPICE+VNC -- No HighPerformance -- USB Controller
        vm.setVmType(VmType.Desktop);
        reset(osRepository);
        when(osRepository.getOsUsbControllerModel(anyInt(), any(), any())).thenReturn(UsbControllerModel.EHCI);
        assertTrue(underTest.isTabletEnabled(vm));

        // SPICE+VNC -- HighPerformance -- with USB Controller
        vm.setVmType(VmType.HighPerformance);
        reset(osRepository);
        when(osRepository.getOsUsbControllerModel(anyInt(), any(), any())).thenReturn(UsbControllerModel.EHCI);
        assertFalse(underTest.isTabletEnabled(vm));

        // Just SPICE
        m.clear();
        m.put(GraphicsType.SPICE, new GraphicsInfo());

        // SPICE+VNC -- No HighPerformance -- No USB Controller
        vm.setVmType(VmType.Desktop);
        reset(osRepository);
        when(osRepository.getOsUsbControllerModel(anyInt(), any(), any())).thenReturn(UsbControllerModel.NONE);
        assertFalse(underTest.isTabletEnabled(vm));

        // SPICE+VNC -- HighPerformance -- No USB Controller
        vm.setVmType(VmType.HighPerformance);
        reset(osRepository);
        when(osRepository.getOsUsbControllerModel(anyInt(), any(), any())).thenReturn(UsbControllerModel.NONE);
        assertFalse(underTest.isTabletEnabled(vm));

        // SPICE+VNC -- No HighPerformance -- USB Controller
        vm.setVmType(VmType.Desktop);
        reset(osRepository);
        when(osRepository.getOsUsbControllerModel(anyInt(), any(), any())).thenReturn(UsbControllerModel.EHCI);
        assertFalse(underTest.isTabletEnabled(vm));

        // SPICE+VNC -- HighPerformance -- with USB Controller
        vm.setVmType(VmType.HighPerformance);
        reset(osRepository);
        when(osRepository.getOsUsbControllerModel(anyInt(), any(), any())).thenReturn(UsbControllerModel.EHCI);
        assertFalse(underTest.isTabletEnabled(vm));
    }
}
