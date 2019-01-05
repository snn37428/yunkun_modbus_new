package shop.mq;

import com.alibaba.fastjson.JSONObject;
import com.aliyun.openservices.ons.api.*;
import modbus.Modbus;
import modbus.protocol.ModbusAnswer;
import modbus.protocol.ModbusProtocol;
import modbus.protocol.ModbusRequest;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import shop.domain.Instruct;
import shop.excl.ReadConfig;

import javax.annotation.PostConstruct;
import java.util.Properties;

public class ConsumerService {

    private static final Logger logger = Logger.getLogger(ConsumerService.class);

    /**
     * plc ip
     */
    private static int nDeviceId = Integer.valueOf(ReadConfig.getDeviceId());
    private static String ip = ReadConfig.getIp();
    private static Properties mqConfig;

    @PostConstruct
    public void read() {
        Consumer consumer = ONSFactory.createConsumer(mqConfig);
        consumer.subscribe("TY_20181201_CONTROLLER", "919", new MessageListener() {
            public Action consume(Message message, ConsumeContext context) {
                Instruct rs = JSONObject.parseObject(new String(message.getBody()), Instruct.class);
                if (rs == null) {
                    logger.error("read : rs null");
                }
                logger.info("Consumer mq" + JSONObject.parseObject(new String(message.getBody())));
                try {
//                    HttpClientGet("https://tianyuanfarm.com/cb/back", new String(message.getBody()));
//                    HttpClientGet("http://127.0.0.1:8081/cb/back", new String(message.getBody()));
                    bulidWriteAndRead(new String(message.getBody()));
                } catch (Exception e) {
                    logger.info("HttpClientGet is Exception" + e);
                }
                logger.info("accept mq is success: " + JSONObject.parseObject(new String(message.getBody())));
                return Action.CommitMessage;
            }
        });
        consumer.start();
        logger.info("----mq consumer is started");
    }

    /**
     * MQ消息控制转化
     *
     * @param dataMsg
     */
    public static void bulidWriteAndRead(String dataMsg) {
        try {
            Instruct rs = JSONObject.parseObject(dataMsg, Instruct.class);
            if (rs == null) {
                logger.error("accept mq data is null");
                return;
            }
            Integer address = rs.getModbusAddr();
            Integer status = rs.getStatus();
            boolean tag;
            if (status == 1) {
                tag = true;
            } else if (status == 0) {
                tag = false;
            } else {
                tag = false;
                logger.error("tag status invalid");
            }
            logger.info("writeDataPLC : 进入写入PLC阶段。");
            if (writeDataPLC(address.intValue(), tag)) {
                int value = readDataPLC(address.intValue());
                if (value == status) {
                    logger.info("writeDataPLC : 写入到PLC，并读取后验证写入成功!!!");
                    HttpClientGet("https://tianyuanfarm.com/cb/back", dataMsg);
                }
            } else {
                // 重试
                for (int i = 0; i < 3; i++) {
                    logger.info("writeDataPLC : 写入到PLC，并读取后验证失败，进入重试逻辑。重试第" + i + "次");
                    if (writeDataPLC(address.intValue(), tag)) {
                        int value = readDataPLC(address.intValue());
                        if (value == status) {
                            logger.info("writeDataPLC 重试后写入成功！！！第" + i + "次重试成功！！！");
                            HttpClientGet("https://tianyuanfarm.com/cb/back", dataMsg);
                            break;
                        }
                    }
                }
                logger.info("writeDataPLC 经过3次重试后仍失败！！！");
                HttpClientGet("https://tianyuanfarm.com/cb/back/error", dataMsg);
            }
        } catch (Exception e) {
            logger.error("bulidWriteAndRead is Exception , e" + e);
        }
    }

    public static boolean writeDataPLC(int nFrom, boolean tag) {
        management.DevicesManagement manager = new management.DevicesManagement(true);
        int nServerListPos = manager.add(ip, Modbus.DEFAULT_PORT);
        ModbusRequest req = new ModbusRequest();
        ModbusAnswer ans = new ModbusAnswer();
        try {
            if (nServerListPos != -1) {
//                int nDeviceId = 1;
                int nServerDataType = ModbusProtocol.DATATYPE_INT32;
                int nConvMode = ModbusProtocol.CONVMOD_0123_3210;
                int nJavaDataType = ModbusProtocol.DATATYPE_JAVA_INT32;
                int nError;
                req.setServerDataType(nServerDataType);
                req.setCheckAnswer(ModbusProtocol.CHKASK_ALL);
                ans.setServerDataType(nServerDataType);
                ans.setConvertMode(nServerDataType, nConvMode, nJavaDataType);
                //0.初始化设置发送对象和接收对象的参数
                req.setServerDataType(ModbusProtocol.DATATYPE_BOOL);
                ans.setServerDataType(ModbusProtocol.DATATYPE_BOOL);
                ans.setJavaDataType(ModbusProtocol.DATATYPE_JAVA_INT32);
                //1.设置发送指令参数
                nError = req.sendWriteCoil(nDeviceId, nFrom, tag);
                if (nError == ModbusProtocol.ERROR_NONE) {
                    logger.debug("writeDataPLC : sendWriteCoil-参数设置有效");
                } else {
                    logger.debug("writeDataPLC : sendWriteCoil-参数设置无效，nError：" + ModbusProtocol.getErrorMessage(nError));
                }
                //2.发送指令
                nError = manager.write(nServerListPos, req);
                if (nError == ModbusProtocol.ERROR_NONE) {
                    logger.debug("writeDataPLC : sendWriteCoil-发送成功");
                } else {
                    logger.debug("writeDataPLC : sendWriteCoil-发送失败，nError" + ModbusProtocol.getErrorMessage(nError));
                }
                //3.接收数据
                nError = manager.read(nServerListPos, ans);
                if (nError == ModbusProtocol.ERROR_NONE) {
//                    logger.info("writeDataPLC : sendWriteCoil-写入成功");
                    return true;
                }
            }
        } catch (Exception e) {
            logger.error("writeDataPLC is Exception", e);
        }
        return false;
    }

    /**
     * 读取PLC验证
     *
     * @param nFrom
     * @return
     */
    public static int readDataPLC(int nFrom) {
        management.DevicesManagement manager = new management.DevicesManagement(true);
        int nServerListPos = manager.add(ip, Modbus.DEFAULT_PORT);
        ModbusRequest req = new ModbusRequest();
        ModbusAnswer anss = new ModbusAnswer();
        try {
            if (nServerListPos != -1) {
//                int nDeviceId = 1;
                anss.setConvertMode(ModbusProtocol.DATATYPE_INT32, ModbusProtocol.CONVMOD_0123_3210,
                        ModbusProtocol.DATATYPE_JAVA_FLOAT32);
                int nError = req.sendReadCoil(nDeviceId, nFrom, 1);
                if (nError == ModbusProtocol.ERROR_NONE) {
                    logger.debug("readDataPLC:sendReadCoil-参数设置有效");
                } else {
                    logger.debug("readDataPLC:sendReadCoil-参数设置无效，" + ModbusProtocol.getErrorMessage(nError));
                }
                // 2、发送指令
                nError = manager.write(nServerListPos, req);
                if (nError == ModbusProtocol.ERROR_NONE) {
                    logger.debug("readDataPLC:sendReadCoil-发送成功");
                } else {
                    logger.warn("readDataPLC:sendReadCoil-发送失败，" + ModbusProtocol.getErrorMessage(nError));
                }
                // 3、接收数据
                nError = manager.read(nServerListPos, anss);
                if (nError == ModbusProtocol.ERROR_NONE) {
                    logger.debug("readDataPLC:sendReadCoil-接收成功");
                } else {
                    logger.warn("readDataPLC:sendReadCoil-接受失败，" + ModbusProtocol.getErrorMessage(nError));
                }
                // 4、接收数据后，通过该方法读取相应数据
                if (nError == ModbusProtocol.ERROR_NONE) {
                    for (int i = nFrom; i < nFrom + 1; i++) {
                        int nCoilStatus = anss.getBitByIndex(i);
                        if (nCoilStatus == -1) {
                            // 设置无效值
                            nCoilStatus = 9911;
                            logger.info("*****readModelData is failed*****");
                        }
                        return nCoilStatus;
                    }
                    logger.debug("readDataPLC:sendReadCoil-读取成功");
                }
            }
        } catch (Exception e) {
            logger.error("readDataPLC is Exception", e);
        }
        return -99;
    }

    /**
     * 发送get请求
     *
     * @param url
     * @param param
     * @throws Exception
     */
    public static void HttpClientGet(String url, String param) {
        try {
            CloseableHttpClient client = HttpClients.createDefault();
            URIBuilder builder = new URIBuilder(url);
            builder.setParameter("data", param);
            HttpGet httpGet = new HttpGet(builder.build());
            CloseableHttpResponse Response = client.execute(httpGet);
            HttpEntity entity = Response.getEntity();
            String str = EntityUtils.toString(entity, "UTF-8");
            logger.info("post msg: -------------- " + httpGet.getURI().toString());
            Response.close();
        } catch (Exception e) {
            logger.debug("HttpClientGet is Exception, e:" + e);
        }
    }

    public static void setMqConfig(Properties mqConfig) {
        ConsumerService.mqConfig = mqConfig;
    }
}
