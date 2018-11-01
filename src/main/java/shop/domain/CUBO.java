package shop.domain;

import java.util.List;

public class CUBO {


    /**
     * 地址点功能码
     */
    private String model;

    /**
     * 点步长
     */
    private int addNum;

    /**
     * 起始点
     */
    private int nFrom;

    /**
     * 类型
     */
    private String type;

    /**
     * 点集合
     */
    private List<Integer> dotList;

    /**
     * 点描述
     */
    private List<String> dotName;

    /**
     * 点描述 中文
     */
    private List<String> dotDesc;

    /**
     * plc configId
     * @return
     */
    private List<String> listConfigId;

    /**
     * 读取的PLC数值
     * @return
     */
    private List<String> value;

    /**
     * 分组编码
     * @return
     */
    private String groupCode;

    /**
     * 功能码 3 4 读取的时候 设置转化码
     */
    private int dataType;

    public int getDataType() {
        return dataType;
    }

    public void setDataType(int dataType) {
        this.dataType = dataType;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getAddNum() {
        return addNum;
    }

    public void setAddNum(int addNum) {
        this.addNum = addNum;
    }

    public int getnFrom() {
        return nFrom;
    }

    public void setnFrom(int nFrom) {
        this.nFrom = nFrom;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Integer> getDotList() {
        return dotList;
    }

    public void setDotList(List<Integer> dotList) {
        this.dotList = dotList;
    }

    public List<String> getDotName() {
        return dotName;
    }

    public void setDotName(List<String> dotName) {
        this.dotName = dotName;
    }

    public List<String> getDotDesc() {
        return dotDesc;
    }

    public void setDotDesc(List<String> dotDesc) {
        this.dotDesc = dotDesc;
    }

    public List<String> getValue() {
        return value;
    }

    public void setValue(List<String> value) {
        this.value = value;
    }

    public String getGroupCode() {
        return groupCode;
    }

    public void setGroupCode(String groupCode) {
        this.groupCode = groupCode;
    }

    public List<String> getListConfigId() {
        return listConfigId;
    }

    public void setListConfigId(List<String> listConfigId) {
        this.listConfigId = listConfigId;
    }
}
