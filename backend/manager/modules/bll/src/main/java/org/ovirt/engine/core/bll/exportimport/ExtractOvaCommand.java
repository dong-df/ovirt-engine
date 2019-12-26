package org.ovirt.engine.core.bll.exportimport;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.ConcurrentChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.VmCommand;
import org.ovirt.engine.core.bll.VmHandler;
import org.ovirt.engine.core.bll.VmTemplateHandler;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.action.ConvertOvaParameters;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.utils.ansible.AnsibleCommandConfig;
import org.ovirt.engine.core.common.utils.ansible.AnsibleConstants;
import org.ovirt.engine.core.common.utils.ansible.AnsibleExecutor;
import org.ovirt.engine.core.common.utils.ansible.AnsibleReturnCode;
import org.ovirt.engine.core.common.utils.ansible.AnsibleReturnValue;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.vdsbroker.PrepareImageReturn;

@NonTransactiveCommandAttribute
public class ExtractOvaCommand<T extends ConvertOvaParameters> extends VmCommand<T> {

    public static final String IMPORT_OVA_LOG_DIRECTORY = "ova";

    @Inject
    private AnsibleExecutor ansibleExecutor;
    @Inject
    private VmHandler vmHandler;
    @Inject
    private VmTemplateHandler templateHandler;
    @Inject
    @Typed(ConcurrentChildCommandsExecutionCallback.class)
    private Instance<ConcurrentChildCommandsExecutionCallback> callbackProvider;

    public ExtractOvaCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void init() {
        super.init();
        setVmName(getParameters().getVmName());
        setVdsId(getParameters().getProxyHostId());
        setClusterId(getParameters().getClusterId());
        setStoragePoolId(getParameters().getStoragePoolId());
        setStorageDomainId(getParameters().getStorageDomainId());
        if (getParameters().getVmEntityType() == VmEntityType.TEMPLATE) {
            setVmTemplateId(getParameters().getVmId());
        }
    }

    @Override
    protected void executeVmCommand() {
        try {
            updateDisksFromDb();
            List<String> diskPaths = prepareImages();
            boolean succeeded = runAnsibleImportOvaPlaybook(diskPaths);
            teardownImages();
            if (!succeeded) {
                log.error("Failed to extract OVA file");
                setCommandStatus(CommandStatus.FAILED);
            } else {
                setSucceeded(true);
            }
        } catch(EngineException e) {
            log.error("Failed to extract OVA file", e);
            setCommandStatus(CommandStatus.FAILED);
        }
    }

    private void updateDisksFromDb() {
        if (getParameters().getVmEntityType() == VmEntityType.TEMPLATE) {
            templateHandler.updateDisksFromDb(getVmTemplate());
        } else {
            vmHandler.updateDisksFromDb(getVm());
        }
    }

    private Map<Guid, Guid> getImageMappings() {
        return getParameters().getImageMappings() != null ?
                getParameters().getImageMappings()
                : Collections.emptyMap();
    }

    private boolean runAnsibleImportOvaPlaybook(List<String> diskPaths) {
        AnsibleCommandConfig commandConfig = new AnsibleCommandConfig()
                .hosts(getVds())
                .variable("ovirt_import_ova_path", getParameters().getOvaPath())
                .variable("ovirt_import_ova_disks",
                        diskPaths.stream()
                                .map(path -> String.format("'%s'", path))
                                .collect(Collectors.joining(",", "[", "]")))
                .variable("ovirt_import_ova_image_mappings",
                        getImageMappings().entrySet()
                                .stream()
                                .map(e -> String
                                        .format("\"%s\": \"%s\"", e.getValue().toString(), e.getKey().toString()))
                                .collect(Collectors.joining(", ", "{", "}")))
                // /var/log/ovirt-engine/ova/ovirt-import-ova-ansible-{hostname}-{correlationid}-{timestamp}.log
                .logFileDirectory(IMPORT_OVA_LOG_DIRECTORY)
                .logFilePrefix("ovirt-import-ova-ansible")
                .logFileName(getVds().getHostName())
                .logFileSuffix(getCorrelationId())
                .playAction("Import OVA")
                .playbook(AnsibleConstants.IMPORT_OVA_PLAYBOOK);

        AnsibleReturnValue ansibleReturnValue  = ansibleExecutor.runCommand(commandConfig);
        boolean succeeded = ansibleReturnValue.getAnsibleReturnCode() == AnsibleReturnCode.OK;
        if (!succeeded) {
            log.error("Failed to extract OVA. Please check logs for more details: {}", ansibleReturnValue.getLogFile());
            return false;
        }

        return true;
    }

    /**
     * @return a list with the corresponding mounted paths
     */
    private List<String> prepareImages() {
        return getDiskList().stream()
                .map(this::prepareImage)
                .map(PrepareImageReturn::getImagePath)
                .collect(Collectors.toList());
    }

    private List<DiskImage> getDiskList() {
        return getParameters().getVmEntityType() == VmEntityType.TEMPLATE ?
                getVmTemplate().getDiskList()
                : getVm().getDiskList();
    }

    private PrepareImageReturn prepareImage(DiskImage image) {
        VDSReturnValue vdsRetVal = imagesHandler.prepareImage(
                image.getStoragePoolId(),
                image.getStorageIds().get(0),
                image.getId(),
                image.getImageId(),
                getParameters().getProxyHostId());
        return (PrepareImageReturn) vdsRetVal.getReturnValue();
    }

    private void teardownImages() {
        getDiskList().forEach(this::teardownImage);
    }

    private void teardownImage(DiskImage image) {
        imagesHandler.teardownImage(
                image.getStoragePoolId(),
                image.getStorageIds().get(0),
                image.getId(),
                image.getImageId(),
                getParameters().getProxyHostId());
    }

    @Override
    public CommandCallback getCallback() {
        return callbackProvider.get();
    }

}
