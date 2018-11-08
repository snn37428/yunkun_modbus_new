package shop.domain;

import java.util.Date;

public class ConfigDO {

    private Integer id;

    /**
     * configId
     */
    private String configId;

    /**
     * 点名
     */
    private String name;

    /**
     * 中文描述
     */
    private String desc;

    /**
     * 类型
     */
    private String type;

    /**
     * 功能码
     */
    private String model;

    /**
     * modbus地址
     */
    private Integer address;

    /**
     * 分组编码
     */
    private String groupCode;

    /**
     * 写库时间
     */
    private Date created;

    /**
     * 功能码 3 4 读取的时候 设置转化码
     */
    private int dataType;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getConfigId() {
        return configId;
    }

    public void setConfigId(String configId) {
        this.configId = configId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getModAddr() {
        return address;
    }

    public void setModAddr(Integer modAddr) {
        this.address = modAddr;
    }

    public String getGroupCode() {
        return groupCode;
    }

    public void setGroupCode(String groupCode) {
        this.groupCode = groupCode;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Integer getAddress() {
        return address;
    }

    public void setAddress(Integer address) {
        this.address = address;
    }

    public int getDataType() {
        return dataType;
    }

    public void setDataType(int dataType) {
        this.dataType = dataType;
    }
}
