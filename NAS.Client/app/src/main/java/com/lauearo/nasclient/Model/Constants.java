package com.lauearo.nasclient.Model;

public class Constants {
    public static final int ACTION_REQUEST_PICK_FILE = 0;
    public static final String ACTION_NEW = "action.new";
    public static final String ACTION_CANCEL = "action.cancel";
    public static final String ACTION_DONE = "action.done";
    public static final String ACTION_FAILED = "action.failed";
    public static final String CACHEPATH = "";

    public static final String NAS_SERVER_URL = "http://192.168.43.117:8073";

    public static final int UPLOADING_STATUS_PENDING = 0x00;
    public static final int UPLOADING_STATUS_PAUSE = 0x01;
    public static final int UPLOADING_STATUS_PLAY = 0x02;
    public static final int UPLOADING_STATUS_FAILURE = 0x03;
    public static final int UPLOADING_STATUS_FINISH = 0x10;
    public static final int UPLOADING_STATUS_CANCEL = 0x11;

}
