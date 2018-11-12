package shop.task;


import com.alibaba.fastjson.JSONObject;
import modbus.Modbus;
import modbus.protocol.ModbusAnswer;
import modbus.protocol.ModbusProtocol;
import modbus.protocol.ModbusRequest;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import shop.dao.TaskYunMapper;
import shop.domain.CUBO;
import shop.domain.CellDO;
import shop.excl.ReadConfig;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TaskScheduled {

    private static final Logger logger = Logger.getLogger(TaskScheduled.class);

    /**
     * 加载的云库配置
     */
    private static List<CUBO> cubo = new ArrayList<CUBO>();

    /**
     * plc ip
     */
    private static int nDeviceId = Integer.valueOf(ReadConfig.getDeviceId());
    private static String ip = ReadConfig.getIp();

    /**
     * 报警
     */
    @Autowired
    private Alarm alarm;

    /**
     * 云库
     */
    @Autowired
    private TaskYunMapper taskYunMapper;

    /**
     * 连接Plc资源
     */
    private static management.DevicesManagement manager = new management.DevicesManagement(true);
    private static int nServerListPos = manager.add(ip, Modbus.DEFAULT_PORT);
    private static int nServerDataType = ModbusProtocol.DATATYPE_INT32;
    private static int nConvMode = ModbusProtocol.CONVMOD_0123_3210;
    private static int nJavaDataType = ModbusProtocol.DATATYPE_JAVA_INT32;
    private static ModbusRequest req = new ModbusRequest();
    private static ModbusAnswer ans = new ModbusAnswer();

    @PostConstruct
    public void init() {
        try {
            cubo = ReadConfig.getCuboList();
            logger.info("----init: cubo success" + JSONObject.toJSONString(cubo));
            // 清除ReadConfig内存
            ReadConfig.clear();
            // 发送设置
            req.setServerDataType(nServerDataType);
            // 设置为需要检查所有反馈信息
            req.setCheckAnswer(ModbusProtocol.CHKASK_ALL);
            // 接收设置
            ans.setServerDataType(nServerDataType);
            // 必须设置的内容
            ans.setConvertMode(nServerDataType, nConvMode, nJavaDataType);
        } catch (Exception e) {
            logger.error("init is Exception", e);
        }
    }

    public void resert() {
        manager = new management.DevicesManagement(true);
        nServerListPos = manager.add(ip, Modbus.DEFAULT_PORT);
        nServerDataType = ModbusProtocol.DATATYPE_INT32;
        nConvMode = ModbusProtocol.CONVMOD_0123_3210;
        nJavaDataType = ModbusProtocol.DATATYPE_JAVA_INT32;
        req = new ModbusRequest();
        ans = new ModbusAnswer();
        init();
        logger.info("----通信失败后，报警次数大于三次，重新建立链接");
    }


    /**
     * 主任务 循环执行
     */
    public void taskRun() {
        List<CellDO> cells = new ArrayList<CellDO>();
        try {
            for (CUBO cb : cubo) {
                if (cb == null) {
                    logger.info("————taskRun: cb null");
                    continue;
                }
                read(cb, cells);
            }
        } catch (Exception e) {
            alarm.sendAlarmInfo(1);
            logger.error("----连接PLC通信失败！！！，异常信息：" + e);
            return;
        }
        if (CollectionUtils.isEmpty(cells)) {
            alarm.sendAlarmInfo(1);
            logger.error("----单次任务读取PLC数据为空，不写云库直接返回。");
            return;
        }
//        test(cells);
        try {
            int rs = taskYunMapper.insert(cells);
            if (rs > 0) {
                logger.warn("----写云库成功！！！");
                alarm.cleanSwitch();
            }
        } catch (Exception e) {
            alarm.sendAlarmInfo(2);
            logger.error("----写云库失败！！！，异常信息：" + e);
        }
    }

    /**
     * 读取1 3 4 功能码主读方法
     *
     * @param cb
     * @param cells
     * @throws Exception
     */
    public void read(CUBO cb, List<CellDO> cells) throws Exception {

        if (nServerListPos == -1) {
            logger.warn("————read: nServerListPos = -1 progress return");
            return;
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            int start = cb.getnFrom();
            int num = cb.getAddNum();
            if ("1".equals(cb.getModel())) {
                cb.setValue(readModelData1(start, num));
                cells.addAll(CUBO2Cell(cb));
            } else if ("3".equals(cb.getModel())) {
                cb.setValue(readModelData3(start, num, cb.getDataType()));
                cells.addAll(CUBO2Cell(cb));
            } else if ("4".equals(cb.getModel())) {
                cb.setValue(readModelData4(start, num, cb.getDataType()));
                cells.addAll(CUBO2Cell(cb));
            }
        } catch (Exception e) {
            logger.error("----read is Exception" + e);
            throw e;
        }
    }

    /**
     * plc 读取模型 转 入库模型
     *
     * @param cb
     */
    private List<CellDO> CUBO2Cell(CUBO cb) {
        List<CellDO> cells = new ArrayList<CellDO>();
        ;
        try {
            List<String> values = cb.getValue();
            if (CollectionUtils.isEmpty(values)) {
                return cells;
            }
            for (int i = 0; i < values.size(); i++) {
                CellDO cell = new CellDO();
                cell.setName(cb.getDotName().get(i));
                cell.setDesc(cb.getDotDesc().get(i));
                cell.setValue(values.get(i));
                cell.setGroupCode(cb.getGroupCode());
                cell.setConfigId(cb.getListConfigId().get(i));
                cell.setType(cb.getType());
                cell.setModel(cb.getModel());
                cell.setAddress(cb.getListAddress().get(i));
                cell.setCreated(new Date());
                cells.add(cell);
            }
        } catch (Exception e) {
            logger.warn("————CUBO2Cell is Exception", e);
        }
        return cells;
    }

    /**
     * 读取功能码1
     *
     * @param nFrom
     * @param nNum
     */
    private List<String> readModelData1(int nFrom, int nNum) throws Exception {

        List<String> responseData = new ArrayList<String>();

        try {
            ans.setConvertMode(ModbusProtocol.DATATYPE_INT32, ModbusProtocol.CONVMOD_0123_3210,
                    ModbusProtocol.DATATYPE_JAVA_FLOAT32);
            int nError = req.sendReadCoil(nDeviceId, nFrom, nNum);
            if (nError == ModbusProtocol.ERROR_NONE) {
                logger.debug("readModelData-1:sendReadCoil-参数设置有效");
            } else {
                logger.debug("readModelData-1:sendReadCoil-参数设置无效，" + ModbusProtocol.getErrorMessage(nError));
            }
            // 2、发送指令
            nError = manager.write(nServerListPos, req);
            if (nError == ModbusProtocol.ERROR_NONE) {
                logger.debug("readModelData-1:sendReadCoil-发送成功");
            } else {
                logger.warn("readModelData-1:sendReadCoil-发送失败，" + ModbusProtocol.getErrorMessage(nError));
            }
            // 3、接收数据
            nError = manager.read(nServerListPos, ans);
            if (nError == ModbusProtocol.ERROR_NONE) {
                logger.debug("readModelData-1:sendReadCoil-接收成功");
            } else {
                logger.warn("readModelData-1:sendReadCoil-接受失败，" + ModbusProtocol.getErrorMessage(nError));
            }
            // 4、接收数据后，通过该方法读取相应数据
            if (nError == ModbusProtocol.ERROR_NONE) {
                for (int i = nFrom; i < nFrom + nNum; i++) {
                    int nCoilStatus = ans.getBitByIndex(i);
                    if (nCoilStatus == -1) {
                        // 设置无效值
                        nCoilStatus = 9911;
                        logger.info("*****readModelData-1 is failed*****");
                    }
                    responseData.add(String.valueOf(nCoilStatus));
                }
                logger.debug("readModelData-1:sendReadCoil-读取成功");
            }
        } catch (Exception e) {
            logger.error("readModelData-1 is Exception", e);
            throw new Exception("readModelData-1 is Exception");
        }
        if (CollectionUtils.isEmpty(responseData)) {
            logger.error("readModelData1 is null");
            return responseData;
        }
        logger.debug("readModelData1-----------" + responseData);
        logger.info("model-1-,读取成功。");
        return responseData;
    }

    /**
     * 读取功能码3
     *
     * @param nFrom
     * @param nNum
     */
    private List<String> readModelData3(int nFrom, int nNum, int dataType) throws Exception {

        List<String> responseData = new ArrayList<String>();
        try {
            ans.setConvertMode(dataType, ModbusProtocol.CONVMOD_0123_3210,
                    ModbusProtocol.DATATYPE_JAVA_FLOAT32);
            // 1、设置发送指令参数
            int nError = req.sendReadHoldingRegister(nDeviceId, nFrom, nNum);
            if (nError == ModbusProtocol.ERROR_NONE) {
                logger.debug("readModelData-3:sendReadCoil-参数设置有效");
            } else {
                logger.warn("readModelData-3:sendReadCoil-参数设置无效，" + ModbusProtocol.getErrorMessage(nError));
            }
            // 2、发送指令
            nError = manager.write(nServerListPos, req);
            if (nError == ModbusProtocol.ERROR_NONE) {
                logger.debug("readModelData-3:sendReadCoil-发送成功");
            } else {
                logger.warn("readModelData-3:sendReadCoil-发送失败，" + ModbusProtocol.getErrorMessage(nError));
            }
            // 3、接收数据
            nError = manager.read(nServerListPos, ans);
            if (nError == ModbusProtocol.ERROR_NONE) {
                logger.debug("readModelData-3:sendReadCoil-接受成功");
            } else {
                logger.warn("readModelData-3:sendReadCoil-接受失败，" + ModbusProtocol.getErrorMessage(nError));
            }
            // 4、接收数据后，通过该方法读取相应数据
            if (nError == ModbusProtocol.ERROR_NONE) {
                for (int i = nFrom; i < nFrom + nNum; i++) {
                    //选择方法与Java端数据类型有关
                    //int data = ans.getIntByIndex(i);
                    float data = ans.getFloatByIndex(i - nFrom);
                    if ((float) 268435456 == data) {
                        data = 9911;
                        logger.warn("*****readModelData-3 is failed*****");
                    }
                    responseData.add(String.valueOf(data));
                }
                logger.debug("readModelData-3:sendReadCoil-读取成功");
            }
        } catch (Exception e) {
            logger.error("readModelData-3 is Exception", e);
            throw new Exception("readModelData-3 is Exception");
        }
        if (CollectionUtils.isEmpty(responseData)) {
            logger.error("readModelData-3 is null");
            return responseData;
        }
        logger.debug("readModelData3-----------" + responseData);
        logger.info("model-3-,读取成功。");
        return responseData;
    }

    /**
     * 读取功能码4
     *
     * @param nFrom
     * @param nNum
     */
    private List<String> readModelData4(int nFrom, int nNum, int dataType) throws Exception {

        List<String> responseData = new ArrayList<String>();
        try {
            ans.setConvertMode(dataType, ModbusProtocol.CONVMOD_0123_3210,
                    ModbusProtocol.DATATYPE_JAVA_INT32);

            int nError = req.sendReadInputRegister(nDeviceId, nFrom, nNum);
            if (nError == ModbusProtocol.ERROR_NONE) {
                logger.debug("readModelData-4:sendReadCoil-参数设置有效");
            } else {
                logger.warn("readModelData-4:sendReadCoil-参数设置无效，" + ModbusProtocol.getErrorMessage(nError));
            }
            // 2、发送指令
            nError = manager.write(nServerListPos, req);
            if (nError == ModbusProtocol.ERROR_NONE) {
                logger.debug("readModelData-4:sendReadCoil-发送成功");
            } else {
                logger.warn("readModelData-4:sendReadCoil-发送失败，" + ModbusProtocol.getErrorMessage(nError));
            }
            // 3、接收数据
            nError = manager.read(nServerListPos, ans);
            if (nError == ModbusProtocol.ERROR_NONE) {
                logger.debug("readModelData-4:sendReadCoil-接受有效");
            } else {
                logger.warn("readModelData-4:sendReadCoil-接受失败，" + ModbusProtocol.getErrorMessage(nError));
            }
            // 4、接收数据成功，则通过该方法读取相应数据
            if (nError == ModbusProtocol.ERROR_NONE) {
                //注：i的值为第几个数据,因此起点为0,而不是字节数的起点也与nAddressFrom不同
                int nDataFrom = 0;
                for (int i = nDataFrom; i < nNum; i++) {
                    //选择的数据类型，与setConvertMode方法中设置的Java端数据类型有关
                    if (dataType == 105) {
                        int data = ans.getIntByIndex(i);
                        if ((float) 268435456 == data) {
                            data = 9911;
                            logger.info("*****readModelData-4  data(int) is failed*****");
                        }
                        responseData.add(String.valueOf(data));
                    } else if (dataType == 107) {
                        float data = ans.getFloatByIndex(i);
                        if ((float) 268435456 == data) {
                            logger.info("*****readModelData-4 data(float) is failed*****");
                            continue;
                        }
                        responseData.add(String.valueOf(data));
                    }
                }
                logger.debug("readModelData-4:sendReadCoil-读取成功");
            }
        } catch (Exception e) {
            logger.error("readModelData-4 is Exception", e);
            throw new Exception("readModelData-4 is Exception");
        }
        if (CollectionUtils.isEmpty(responseData)) {
            logger.error("readModelData-4 is null");
            return responseData;
        }
        logger.debug("readModelData4-----------" + responseData);
        logger.info("model-4-,读取成功。");
        return responseData;
    }

    private void test(List<CellDO> cells) {
        CellDO cellDO = new CellDO();
        cellDO.setConfigId("1");
        cellDO.setValue("1");
        cellDO.setAddress(2);
        cellDO.setCreated(new Date());
        cellDO.setDesc("ceshi");
        cellDO.setType("1");
        cellDO.setGroupCode("1");
        cellDO.setName("1");
        cellDO.setModel("1");
        cells.add(cellDO);
    }
}
