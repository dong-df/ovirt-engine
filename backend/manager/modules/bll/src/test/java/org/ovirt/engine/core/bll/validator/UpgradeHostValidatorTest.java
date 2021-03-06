package org.ovirt.engine.core.bll.validator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.utils.RandomUtils;

public class UpgradeHostValidatorTest {

    private VDS host;

    private UpgradeHostValidator validator;

    @BeforeEach
    public void setup() {
        host = new VDS();
        validator = new UpgradeHostValidator(host);
    }

    @Test
    public void hostExists() {
        assertThat(validator.hostExists(), isValid());
    }

    @Test
    public void hostDoesNotExist() {
        validator = new UpgradeHostValidator(null);

        assertThat(validator.hostExists(), failsWith(EngineMessage.VDS_INVALID_SERVER_ID));
    }

    @Test
    public void maintenanceStatusIsSupportedForHostUpgrade() {
        host.setStatus(VDSStatus.Maintenance);

        assertThat(validator.statusSupportedForHostUpgrade(), isValid());
    }

    @Test
    public void statusNotSupportedForHostUpgrade() {
        VDS host = new VDS();
        host.setStatus(VDSStatus.Unassigned);

        assertThat(new UpgradeHostValidator(host).statusSupportedForHostUpgrade(),
                failsWith(EngineMessage.CANNOT_UPGRADE_HOST_STATUS_ILLEGAL));
    }

    @Test
    public void statusSupportedForHostUpgradeInternal() {
        host.setStatus(VDSStatus.Maintenance);

        assertThat(validator.statusSupportedForHostUpgradeInternal(), isValid());
    }

    @Test
    public void statusNotSupportedForHostUpgradeInternal() {
        host.setStatus(VDSStatus.Unassigned);

        assertThat(validator.statusSupportedForHostUpgradeInternal(),
                failsWith(EngineMessage.CANNOT_UPGRADE_HOST_STATUS_ILLEGAL));
    }

    @Test
    public void updatesAvailable() {
        host.getDynamicData().setUpdateAvailable(true);

        assertThat(validator.updatesAvailable(), isValid());
    }

    private void mockOvirtNode() {
        host.setVdsType(VDSType.oVirtNode);
    }

    @Test
    public void updatesUnavailable() {
        assertThat(validator.updatesAvailable(), failsWith(EngineMessage.NO_AVAILABLE_UPDATES_FOR_HOST));
    }

    @Test
    public void hostWasInstalled() {
        host.setHostOs(RandomUtils.instance().nextString(20));

        assertThat(validator.hostWasInstalled(), isValid());
    }

    @Test
    public void hostWasNotInstalled() {
        assertThat(validator.hostWasInstalled(), failsWith(EngineMessage.CANNOT_UPGRADE_HOST_WITHOUT_OS));
    }
}
