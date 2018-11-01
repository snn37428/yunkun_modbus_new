package shop.excl;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import shop.dao.TaskYunMapper;
import shop.domain.CUBO;
import shop.domain.ConfigDO;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ReadConfig {

    private static final Logger logger = Logger.getLogger(ReadConfig.class);

    private static List<CUBO> cuboList = new ArrayList<CUBO>();

    private static Properties config;

    private static String ip = "";
    private static String port = "";
    private static String deviceId = "";

    @Autowired
    private TaskYunMapper taskYunMapper;

    public void initConfig() {
        List<ConfigDO> list1 = new ArrayList<ConfigDO>();
        List<ConfigDO> list3 = new ArrayList<ConfigDO>();
        List<ConfigDO> list4 = new ArrayList<ConfigDO>();
        List<ConfigDO> rs = null;
        try {
            // 读取云库配置
            rs = taskYunMapper.readConfig();
            if (CollectionUtils.isEmpty(rs)) {
                logger.info("————initConfig: yunConfig rs null");
            }
            logger.info("————initConfig: yunConfig success");
        } catch (Exception e) {
            logger.error("————initConfig: yunConfig Exception");
        }

        for (ConfigDO configDO : rs) {
            if (configDO == null) {
                logger.info("————initConfig: configDO null");
                continue;
            }
            if (StringUtils.isBlank(configDO.getModel())) {
                logger.info("————initConfig: configDO.getModel null");
                continue;
            }
            if ("1".equals(configDO.getModel())) {
                list1.add(configDO);
            } else if ("3".equals(configDO.getModel())) {
                setDataType(configDO);
                list3.add(configDO);
            } else if ("4".equals(configDO.getModel())) {
                setDataType(configDO);
                list4.add(configDO);
            }
        }
        CUBO cubo1 = buildCUBO(list1, 1);
        cuboList.add(cubo1);
        CUBO cubo3 = buildCUBO(list3, 3);
        cuboList.add(cubo3);
        CUBO cubo4 = buildCUBO(list4, 4);
        cuboList.add(cubo4);
        setIp(config.get("ip").toString());
        setPort(port = config.get("port").toString());
        setDeviceId(deviceId = config.get("deviceId").toString());
        logger.info("————initConfig: ip:" + ip + " : port: " + port + " deviceId: " + deviceId + "");
    }

    /**
     * 功能码 3 4 读取的时候 设置转化码
     *
     * @param configDO
     */
    private void setDataType(ConfigDO configDO) {
        if ("Dint".equals(configDO.getType())) {
            configDO.setDataType(105);
        }
        if ("Real".equals(configDO.getType())) {
            configDO.setDataType(107);
        }
    }

    /**
     * 组装PLC读取模型
     *
     * @param configDOList
     * @return bul
     */
    private CUBO buildCUBO(List<ConfigDO> configDOList, int i) {

        if (CollectionUtils.isEmpty(configDOList)) {
            logger.info("————initConfig: load model " + i + " size:0");
            return null;
        }

        CUBO cubo = new CUBO();
        List<String> listName = new ArrayList<String>();
        List<String> listDesc = new ArrayList<String>();
        List<String> listConfigId = new ArrayList<String>();
        for (ConfigDO configDO : configDOList) {
            cubo.setModel(configDO.getModel());
            cubo.setAddNum(configDOList.size());
            cubo.setGroupCode(configDO.getGroupCode());
            cubo.setType(configDO.getType());
            cubo.setDataType(configDO.getDataType());
            cubo.setnFrom(configDOList.get(0).getModAddr());
            listName.add(configDO.getName());
            listDesc.add(configDO.getDesc());
            listConfigId.add(configDO.getConfigId());
        }
        cubo.setDotName(listName);
        cubo.setDotDesc(listDesc);
        cubo.setListConfigId(listConfigId);
        logger.info("————initConfig: load model " + i + " size:" + configDOList.size());
        return cubo;
    }

    public TaskYunMapper getTaskYunMapper() {
        return taskYunMapper;
    }

    public void setTaskYunMapper(TaskYunMapper taskYunMapper) {
        this.taskYunMapper = taskYunMapper;
    }

    public static List<CUBO> getCuboList() {
        return cuboList;
    }

    public static void setCuboList(List<CUBO> cuboList) {
        ReadConfig.cuboList = cuboList;
    }

    public static void clean() {
        ReadConfig.cuboList.clear();
    }

    public static Properties getConfig() {
        return config;
    }

    public static void setConfig(Properties config) {
        ReadConfig.config = config;
    }

    public static String getIp() {
        return ip;
    }

    public static void setIp(String ip) {
        ReadConfig.ip = ip;
    }

    public static String getPort() {
        return port;
    }

    public static void setPort(String port) {
        ReadConfig.port = port;
    }

    public static String getDeviceId() {
        return deviceId;
    }

    public static void setDeviceId(String deviceId) {
        ReadConfig.deviceId = deviceId;
    }
}
