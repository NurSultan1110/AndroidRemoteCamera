package com.project.coderneo.feature;

public class Camera {

    private String model;
    private String port;
    private String recordingStatus;
    private String iso;
    private String shutterSpeed;
    private String focal;
    private String whiteBalance;
    private String whiteBalanceCelvin;
    private String memoryStatus;

    public Camera() {
    }

    public Camera(String model, String port, String recordingStatus) {
        this.model = model;
        this.port = port;
        this.recordingStatus = recordingStatus;
    }

    public Camera(String model, String port, String recordingStatus, String iso, String shutterSpeed, String focal, String whiteBalance, String whiteBalanceCelvin, String memoryStatus) {
        this.model = model;
        this.port = port;
        this.recordingStatus = recordingStatus;
        this.iso = iso;
        this.shutterSpeed = shutterSpeed;
        this.focal = focal;
        this.whiteBalance = whiteBalance;
        this.whiteBalanceCelvin = whiteBalanceCelvin;
        this.memoryStatus = memoryStatus;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getRecordingStatus() {
        return recordingStatus;
    }

    public void setRecordingStatus(String recordingStatus) {
        this.recordingStatus = recordingStatus;
    }

    public String getIso() {
        return iso;
    }

    public void setIso(String iso) {
        this.iso = iso;
    }

    public String getShutterSpeed() {
        return shutterSpeed;
    }

    public void setShutterSpeed(String shutterSpeed) {
        this.shutterSpeed = shutterSpeed;
    }

    public String getFocal() {
        return focal;
    }

    public void setFocal(String focal) {
        this.focal = focal;
    }

    public String getWhiteBalance() {
        return whiteBalance;
    }

    public void setWhiteBalance(String whiteBalance) {
        this.whiteBalance = whiteBalance;
    }

    public String getWhiteBalanceCelvin() {
        return whiteBalanceCelvin;
    }

    public void setWhiteBalanceCelvin(String whiteBalanceCelvin) {
        this.whiteBalanceCelvin = whiteBalanceCelvin;
    }

    public String getMemoryStatus() {
        return memoryStatus;
    }

    public void setMemoryStatus(String memoryStatus) {
        this.memoryStatus = memoryStatus;
    }

    @Override
    public String toString() {
        return "Camera{" +
                "model='" + model + '\'' +
                ", port='" + port + '\'' +
                ", recordingStatus='" + recordingStatus + '\'' +
                ", iso='" + iso + '\'' +
                ", shutterSpeed='" + shutterSpeed + '\'' +
                ", focal='" + focal + '\'' +
                ", whiteBalance='" + whiteBalance + '\'' +
                ", whiteBalanceCelvin='" + whiteBalanceCelvin + '\'' +
                ", memoryStatus='" + memoryStatus + '\'' +
                '}';
    }
}
