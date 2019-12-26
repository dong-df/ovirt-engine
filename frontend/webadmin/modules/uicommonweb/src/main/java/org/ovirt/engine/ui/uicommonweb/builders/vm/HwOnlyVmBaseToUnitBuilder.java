package org.ovirt.engine.ui.uicommonweb.builders.vm;

import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.ui.uicommonweb.builders.BaseSyncBuilder;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;

public class HwOnlyVmBaseToUnitBuilder extends BaseSyncBuilder<VmBase, UnitVmModel> {
    @Override
    protected void build(VmBase vm, UnitVmModel model) {
        model.getMemSize().setEntity(vm.getMemSizeMb());
        model.getMaxMemorySize().setEntity(vm.getMaxMemorySizeMb()  );
        model.getIoThreadsEnabled().setEntity(vm.getNumOfIoThreads() != 0);
        model.getNumOfIoThreads().setEntity(Integer.toString(vm.getNumOfIoThreads()));
        model.getMinAllocatedMemory().setEntity(vm.getMinAllocatedMem());
        model.getUsbPolicy().setSelectedItem(vm.getUsbPolicy());
        model.getNumOfMonitors().setSelectedItem(vm.getNumOfMonitors());
        model.setSingleQxlEnabled(vm.getSingleQxlPci());
        model.setBootSequence(vm.getDefaultBootSequence());
        model.getTotalCPUCores().setEntity(Integer.toString(vm.getNumOfCpus()));
        model.getNumOfSockets().setSelectedItem(vm.getNumOfSockets());
        model.getCoresPerSocket().setSelectedItem(vm.getCpuPerSocket());
        model.getThreadsPerCore().setSelectedItem(vm.getThreadsPerCpu());
        model.getIsSmartcardEnabled().setEntity(vm.isSmartcardEnabled());
        model.setSelectedMigrationDowntime(vm.getMigrationDowntime());
        model.selectMigrationPolicy(vm.getMigrationPolicyId());
        model.getEmulatedMachine().setSelectedItem(vm.getCustomEmulatedMachine());
        model.getCustomCpu().setSelectedItem(vm.getCustomCpuName());
        model.getBiosType().setSelectedItem(vm.getBiosType());
        model.getHostCpu().setEntity(vm.isUseHostCpuFlags());
        model.getTscFrequency().setEntity(vm.getUseTscFrequency());
    }
}
