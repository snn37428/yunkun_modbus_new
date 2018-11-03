package shop.domain;

public class AlarmDo {
    private int id;
    /**
     * 总开关
     */
    private String manSwitch;
    /**
     * 本地独属于开关
     */
    private String readSwitch;
    /**
     * 写云库
     */
    private String writeSwitch;
    /**
     * 钉钉报警
     */
    private String dingdingSwitch;
    /**
     * 钉钉报警信息
     */
    private String dingdingAlarmMsg;
    /**
     * 报警@手机号码
     */
    private String listMobies;
    /**
     * 点
     */
    private String listCells;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getManSwitch() {
        return manSwitch;
    }

    public void setManSwitch(String manSwitch) {
        this.manSwitch = manSwitch;
    }

    public String getReadSwitch() {
        return readSwitch;
    }

    public void setReadSwitch(String readSwitch) {
        this.readSwitch = readSwitch;
    }

    public String getWriteSwitch() {
        return writeSwitch;
    }

    public void setWriteSwitch(String writeSwitch) {
        this.writeSwitch = writeSwitch;
    }

    public String getDingdingSwitch() {
        return dingdingSwitch;
    }

    public void setDingdingSwitch(String dingdingSwitch) {
        this.dingdingSwitch = dingdingSwitch;
    }

    public String getDingdingAlarmMsg() {
        return dingdingAlarmMsg;
    }

    public void setDingdingAlarmMsg(String dingdingAlarmMsg) {
        this.dingdingAlarmMsg = dingdingAlarmMsg;
    }

    public String getListMobies() {
        return listMobies;
    }

    public void setListMobies(String listMobies) {
        this.listMobies = listMobies;
    }

    public String getListCells() {
        return listCells;
    }

    public void setListCells(String listCells) {
        this.listCells = listCells;
    }
}
