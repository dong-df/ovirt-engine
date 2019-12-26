package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.gwtbootstrap3.client.ui.constants.IconType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class ClusterAdditionalStatusColumn extends EntityAdditionalStatusColumn<Cluster> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();
    private static final ApplicationMessages messages = AssetProvider.getMessages();
    private static final ApplicationTemplates templates = AssetProvider.getTemplates();
    private static final String[] cpus = new String[]{"Intel Conroe Family", //$NON-NLS-1$
                                                      "Intel Penryn Family", //$NON-NLS-1$
                                                      "AMD Opteron G1", //$NON-NLS-1$
                                                      "AMD Opteron G2", //$NON-NLS-1$
                                                      "AMD Opteron G3", //$NON-NLS-1$
                                                      "Intel Nehalem IBRS Family", //$NON-NLS-1$
                                                      "Intel Westmere IBRS Family", //$NON-NLS-1$
                                                      "Intel SandyBridge IBRS Family", //$NON-NLS-1$
                                                      "Intel Haswell-noTSX IBRS Family", //$NON-NLS-1$
                                                      "Intel Haswell IBRS Family", //$NON-NLS-1$
                                                      "Intel Broadwell-noTSX IBRS Family", //$NON-NLS-1$
                                                      "Intel Broadwell IBRS Family", //$NON-NLS-1$
                                                      "Intel Skylake Client IBRS Family", //$NON-NLS-1$
                                                      "Intel Skylake Server IBRS Family", //$NON-NLS-1$
                                                      "AMD EPYC IBPB"}; //$NON-NLS-1$
    private static final String[] versions = new String[]{"4.2",  //$NON-NLS-1$
                                                          "4.1",  //$NON-NLS-1$
                                                          "4.0",  //$NON-NLS-1$
                                                          "3.6"}; //$NON-NLS-1$

    private boolean isDeprecated(Cluster object) {
        for (String version : versions) {
            if (version.equals(object.getCompatibilityVersion().toString())) {
                for (String cpu : cpus) {
                    if (cpu.equals(object.getCpuName())) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    public SafeHtml getEntityValue(Cluster object) {
        SafeHtmlBuilder images = new SafeHtmlBuilder();
        boolean addSpace = false;
        if (object.isClusterCompatibilityLevelUpgradeNeeded()
                || isDeprecated(object)
                || object.hasHostWithMissingCpuFlags()) {
            images.append(getImageSafeHtml(IconType.EXCLAMATION));
            addSpace = true;
        }
        if (!object.getHostNamesOutOfSync().isEmpty()) {
            if (addSpace) {
                images.appendHtmlConstant(constants.space());
            }
            images.append(templates.brokenLinkRed());
        }
        return templates.image(images.toSafeHtml());
    }

    @Override
    public SafeHtml getEntityTooltip(Cluster object) {
        SafeHtmlBuilder tooltip = new SafeHtmlBuilder();
        boolean addLineBreaks = false;
        if (object.isClusterCompatibilityLevelUpgradeNeeded()) {
            tooltip.append(getImageSafeHtml(IconType.EXCLAMATION));
            tooltip.appendHtmlConstant(constants.space());
            tooltip.appendHtmlConstant(constants.clusterLevelUpgradeNeeded());
            addLineBreaks = true;
        }

        if (isDeprecated(object)) {
            if (addLineBreaks) {
                tooltip.appendHtmlConstant(constants.lineBreak());
                tooltip.appendHtmlConstant(constants.lineBreak());
            }
            tooltip.append(getImageSafeHtml(IconType.EXCLAMATION));
            tooltip.appendHtmlConstant(constants.space());
            tooltip.appendEscaped(messages.cpuDeprecationWarning(object.getCpuName()));
            addLineBreaks = true;
        }
        if (!object.getHostNamesOutOfSync().isEmpty()) {
            if (addLineBreaks) {
                tooltip.appendHtmlConstant(constants.lineBreak());
                tooltip.appendHtmlConstant(constants.lineBreak());
            }
            tooltip.append(hostListText(object));
            addLineBreaks = true;
        }

        if (object.hasHostWithMissingCpuFlags()) {
            if (addLineBreaks) {
                tooltip.appendHtmlConstant(constants.lineBreak());
                tooltip.appendHtmlConstant(constants.lineBreak());
            }
            tooltip.append(getImageSafeHtml(IconType.EXCLAMATION));
            tooltip.appendHtmlConstant(constants.space());
            tooltip.appendEscaped(constants.clusterHasHostWithMissingCpuFlagsWarning());
            addLineBreaks = true;
        }
        return tooltip.toSafeHtml();
    }

    private SafeHtml hostListText(Cluster object) {
        SafeHtmlBuilder builder = new SafeHtmlBuilder();
        builder.append(templates.brokenLinkRed());
        builder.appendHtmlConstant(constants.space());
        builder.appendEscaped(constants.hostsOutOfSyncWarning());
        builder.appendHtmlConstant(constants.lineBreak());
        builder.appendEscapedLines(object.getHostNamesOutOfSync());
        return builder.toSafeHtml();
    }

    @Override
    protected Cluster getEntityObject(Cluster object) {
        return object;
    }
}
