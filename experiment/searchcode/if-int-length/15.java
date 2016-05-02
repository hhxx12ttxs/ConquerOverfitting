package edu.wpi.first.wpilibj.host;

import java.io.DataInputStream;
import java.io.IOException;

public class DriverStationState extends DashboardSerializableObject {

    protected int digitalIn;
    protected int digitalOut;
    protected int batteryVoltageWhole;
    protected int batteryVoltageDecimal;
    protected int status;
    protected int errorReport;
    protected int teamId;
    protected byte[] imageVersionBytes;
    protected String imageVersionString;
    protected boolean isEStopOn;
    protected boolean isEnabled;
    protected boolean isAutonomous;
    protected boolean isPcConnectedEthernerPort2;
    protected boolean isRobotLinkActive;
    protected boolean isCompetition;
    private boolean errorReportIsPacketLossLimit;
    private boolean errorReportIsWrongTreamIdReceived;
    private boolean errorReportIscRioImageChecksumMismatch;
    private boolean errorReportIsFpgaImageChecksumMismatch;
    private boolean errorReportIsType3;
    private boolean errorReportIsType2;
    private boolean errorReportIsType1;
    private boolean errorReportIsSoftwareImageVersionMismatch;

    private int dataSequenceNumber;

    private String displayString;

    private String errorString;

    public DriverStationState() {
        super("");
        imageVersionBytes = new byte[8];
    }

    public void dashboardSerializableReadFrom(DataInputStream in) {
        try {
            digitalIn = in.read();
            digitalOut = in.read();
            batteryVoltageWhole = in.read();
            batteryVoltageDecimal = in.read();
            status = in.read();
            errorReport = in.read();
            teamId = in.read() * 100 + in.read();
            int read = in.read(imageVersionBytes);
            if (read != imageVersionBytes.length) {
                throw new RuntimeException("Did not rid 8 bytes for version");
            }
            imageVersionString = new String(imageVersionBytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        isEStopOn = (status & 0x40) != 0;
        isEnabled = (status & 0x20) != 0;
        isAutonomous = (status & 0x10) != 0;
        isPcConnectedEthernerPort2 = (status & 0x04) != 0;
        isRobotLinkActive = (status & 0x02) != 0;
        isCompetition = (status & 0x01) != 0;
        errorReportIsPacketLossLimit = (errorReport & 0x80) != 0;
        errorReportIsWrongTreamIdReceived = (errorReport & 0x40) != 0;
        errorReportIscRioImageChecksumMismatch = (errorReport & 0x20) != 0;
        errorReportIsFpgaImageChecksumMismatch = (errorReport & 0x10) != 0;
        errorReportIsType3 = (errorReport & 0x08) != 0;
        errorReportIsType2 = (errorReport & 0x04) != 0;
        errorReportIsType1 = (errorReport & 0x02) != 0;
        errorReportIsSoftwareImageVersionMismatch = (errorReport & 0x01) != 0;
    }

    public void dashboardSerializableStringChildrenTo(StringBuilder builder, String indent) {
    }
    
    public void dashboardSerializableStringShallowTo(StringBuilder builder, String indent) {
        super.dashboardSerializableStringShallowTo(builder, indent);
        builder.append(indent);
        builder.append("digitalIn: ");
        builder.append(digitalIn);
        builder.append('\n');
        builder.append(indent);
        builder.append("digitalOut: ");
        builder.append(digitalOut);
        builder.append('\n');
        builder.append(indent);
        builder.append("batteryVoltage: ");
        builder.append(batteryVoltageWhole);
        builder.append('.');
        builder.append(batteryVoltageDecimal);
        builder.append('\n');

        String nextIndent = DashboardSerializer.nextIndentString(indent);
        builder.append(indent);
        builder.append("status: ");
        builder.append(status);
        builder.append('\n');
        builder.append(nextIndent);
        builder.append("isEStopOn: ");
        builder.append(isEStopOn);
        builder.append('\n');
        builder.append(nextIndent);
        builder.append("isEnabled: ");
        builder.append(isEnabled);
        builder.append('\n');
        builder.append(nextIndent);
        builder.append("isAutonomous: ");
        builder.append(isAutonomous);
        builder.append('\n');
        builder.append(nextIndent);
        builder.append("isPcConnectedEthernerPort2: ");
        builder.append(isPcConnectedEthernerPort2);
        builder.append('\n');
        builder.append(nextIndent);
        builder.append("isRobotLinkActive: ");
        builder.append(isRobotLinkActive);
        builder.append('\n');
        builder.append(nextIndent);
        builder.append("isCompetition: ");
        builder.append(isCompetition);
        builder.append('\n');

        builder.append(indent);
        builder.append("errorReport: ");
        builder.append(errorReport);
        builder.append('\n');
        builder.append(nextIndent);
        builder.append("isPacketLossLimit: ");
        builder.append(errorReportIsPacketLossLimit);
        builder.append('\n');
        builder.append(nextIndent);
        builder.append("isWrongTreamIdReceived: ");
        builder.append(errorReportIsWrongTreamIdReceived);
        builder.append('\n');
        builder.append(nextIndent);
        builder.append("iscRioImageChecksumMismatch: ");
        builder.append(errorReportIscRioImageChecksumMismatch);
        builder.append('\n');
        builder.append(nextIndent);
        builder.append("isFpgaImageChecksumMismatch: ");
        builder.append(errorReportIsFpgaImageChecksumMismatch);
        builder.append('\n');
        builder.append(nextIndent);
        builder.append("isType3: ");
        builder.append(errorReportIsType3);
        builder.append('\n');
        builder.append(nextIndent);
        builder.append("isType2: ");
        builder.append(errorReportIsType2);
        builder.append('\n');
        builder.append(nextIndent);
        builder.append("isType1: ");
        builder.append(errorReportIsType1);
        builder.append('\n');
        builder.append(nextIndent);
        builder.append("isSoftwareImageVersionMismatch: ");
        builder.append(errorReportIsSoftwareImageVersionMismatch);
        builder.append('\n');
        
        builder.append(indent);
        builder.append("teamId: ");
        builder.append(teamId);
        builder.append('\n');
        builder.append(indent);
        builder.append("imageVersion: ");
        builder.append(imageVersionString);
        builder.append('\n');
        builder.append("dataSequenceNumber: ");
        builder.append(dataSequenceNumber);
        builder.append('\n');
        builder.append("displayString: ");
        builder.append(displayString);
        builder.append('\n');
        builder.append("errorString: ");
        builder.append(errorString);
        builder.append('\n');
    }
    
    public void userDataReadFrom(DataInputStream in) {
        try {
            dataSequenceNumber = in.read();
            int length = in.readInt();
            byte[] bytes = new byte[length];
            in.read(bytes, 0, length);
            displayString = new String(bytes);
            length = in.readInt();
            bytes = new byte[length];
            in.read(bytes, 0, length);
            errorString = new String(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int getBatteryVoltageDecimal() {
        return batteryVoltageDecimal;
    }

    public void setBatteryVoltageDecimal(int batteryVoltageDecimal) {
        this.batteryVoltageDecimal = batteryVoltageDecimal;
    }

    public int getBatteryVoltageWhole() {
        return batteryVoltageWhole;
    }

    public void setBatteryVoltageWhole(int batteryVoltageWhole) {
        this.batteryVoltageWhole = batteryVoltageWhole;
    }

    public int getDataSequenceNumber() {
        return dataSequenceNumber;
    }

    public void setDataSequenceNumber(int dataSequenceNumber) {
        this.dataSequenceNumber = dataSequenceNumber;
    }

    public int getDigitalIn() {
        return digitalIn;
    }

    public void setDigitalIn(int digitalIn) {
        this.digitalIn = digitalIn;
    }

    public int getDigitalOut() {
        return digitalOut;
    }

    public void setDigitalOut(int digitalOut) {
        this.digitalOut = digitalOut;
    }

    public String getDisplayString() {
        return displayString;
    }

    public void setDisplayString(String displayString) {
        this.displayString = displayString;
    }

    public int getErrorReport() {
        return errorReport;
    }

    public void setErrorReport(int errorReport) {
        this.errorReport = errorReport;
    }

    public boolean isErrorReportIsFpgaImageChecksumMismatch() {
        return errorReportIsFpgaImageChecksumMismatch;
    }

    public void setErrorReportIsFpgaImageChecksumMismatch(boolean errorReportIsFpgaImageChecksumMismatch) {
        this.errorReportIsFpgaImageChecksumMismatch = errorReportIsFpgaImageChecksumMismatch;
    }

    public boolean isErrorReportIsPacketLossLimit() {
        return errorReportIsPacketLossLimit;
    }

    public void setErrorReportIsPacketLossLimit(boolean errorReportIsPacketLossLimit) {
        this.errorReportIsPacketLossLimit = errorReportIsPacketLossLimit;
    }

    public boolean isErrorReportIsSoftwareImageVersionMismatch() {
        return errorReportIsSoftwareImageVersionMismatch;
    }

    public void setErrorReportIsSoftwareImageVersionMismatch(boolean errorReportIsSoftwareImageVersionMismatch) {
        this.errorReportIsSoftwareImageVersionMismatch = errorReportIsSoftwareImageVersionMismatch;
    }

    public boolean isErrorReportIsType1() {
        return errorReportIsType1;
    }

    public void setErrorReportIsType1(boolean errorReportIsType1) {
        this.errorReportIsType1 = errorReportIsType1;
    }

    public boolean isErrorReportIsType2() {
        return errorReportIsType2;
    }

    public void setErrorReportIsType2(boolean errorReportIsType2) {
        this.errorReportIsType2 = errorReportIsType2;
    }

    public boolean isErrorReportIsType3() {
        return errorReportIsType3;
    }

    public void setErrorReportIsType3(boolean errorReportIsType3) {
        this.errorReportIsType3 = errorReportIsType3;
    }

    public boolean isErrorReportIsWrongTreamIdReceived() {
        return errorReportIsWrongTreamIdReceived;
    }

    public void setErrorReportIsWrongTreamIdReceived(boolean errorReportIsWrongTreamIdReceived) {
        this.errorReportIsWrongTreamIdReceived = errorReportIsWrongTreamIdReceived;
    }

    public boolean isErrorReportIscRioImageChecksumMismatch() {
        return errorReportIscRioImageChecksumMismatch;
    }

    public void setErrorReportIscRioImageChecksumMismatch(boolean errorReportIscRioImageChecksumMismatch) {
        this.errorReportIscRioImageChecksumMismatch = errorReportIscRioImageChecksumMismatch;
    }

    public String getErrorString() {
        return errorString;
    }

    public void setErrorString(String errorString) {
        this.errorString = errorString;
    }

    public byte[] getImageVersionBytes() {
        return imageVersionBytes;
    }

    public void setImageVersionBytes(byte[] imageVersionBytes) {
        this.imageVersionBytes = imageVersionBytes;
    }

    public String getImageVersionString() {
        return imageVersionString;
    }

    public void setImageVersionString(String imageVersionString) {
        this.imageVersionString = imageVersionString;
    }

    public boolean isIsAutonomous() {
        return isAutonomous;
    }

    public void setIsAutonomous(boolean isAutonomous) {
        this.isAutonomous = isAutonomous;
    }

    public boolean isIsCompetition() {
        return isCompetition;
    }

    public void setIsCompetition(boolean isCompetition) {
        this.isCompetition = isCompetition;
    }

    public boolean isIsEStopOn() {
        return isEStopOn;
    }

    public void setIsEStopOn(boolean isEStopOn) {
        this.isEStopOn = isEStopOn;
    }

    public boolean isIsEnabled() {
        return isEnabled;
    }

    public void setIsEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public boolean isIsPcConnectedEthernerPort2() {
        return isPcConnectedEthernerPort2;
    }

    public void setIsPcConnectedEthernerPort2(boolean isPcConnectedEthernerPort2) {
        this.isPcConnectedEthernerPort2 = isPcConnectedEthernerPort2;
    }

    public boolean isIsRobotLinkActive() {
        return isRobotLinkActive;
    }

    public void setIsRobotLinkActive(boolean isRobotLinkActive) {
        this.isRobotLinkActive = isRobotLinkActive;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    
    
}

