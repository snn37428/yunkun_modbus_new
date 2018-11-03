package shop.task;

import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import shop.dao.TaskYunMapper;
import shop.domain.AlarmDo;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Alarm {

    private static final Logger logger = Logger.getLogger(Alarm.class);

    @Autowired
    private TaskYunMapper taskYunMapper;

    private static List<String> listPhone = new ArrayList<String>();
    private String man = "1";
    static final String product = "Dysmsapi";
    static final String domain = "dysmsapi.aliyuncs.com";
    static final String accessKeyId = "LTAIpy8aiA0Ngl1t";
    static final String accessKeySecret = "EiW1cYfR8CrFnsSVLrFBSlQ1jeVl60";

    @PostConstruct
    public void init() {
        logger.error("--------------------------");
        try {
            Map<String, String>mapMsg = new HashMap<String, String>();
            String template = "SMS_150170682";
            sendSms("13810653015", mapMsg, template);
        } catch (ClientException e) {
            logger.error("程序启动短信通知失败");
        }
    }

    public void sendAlarmInfo() {
        try {
            send();
            sendQuantiyAlarmInfo("PLC采集数据");
            logger.info("----sendAlarmInfo is success ! ");
        } catch (Exception e) {
            logger.error("----调用发送信息接口异常" + e);
        }
    }


    /**
     * 定时更新配置
     */
    public void taskAlarmConfig() {
        try {
            AlarmDo alarmInfo = taskYunMapper.selectMan();
            if (alarmInfo == null) {
                logger.warn("----taskAlarmConfig: alarmInfo is null");
            }
            if (StringUtils.isBlank(alarmInfo.getListMobies()) || StringUtils.isBlank(alarmInfo.getManSwitch())) {
                logger.warn("----taskAlarmConfig: phones is blank or manSwitch is blank");
            }
            // 更新主开关
            setMan(alarmInfo.getManSwitch());
            String phones = alarmInfo.getListMobies();
            String[] p = phones.toString().split("\\;");
            if (p.length == 0) {
                logger.warn("----taskAlarmConfig: phones length = 0");
            }
            List<String> pl = new ArrayList<String>();
            for (int i = 0; i< p.length; i++) {
                pl.add(p[i]);
            }
            setListPhone(pl);
            logger.warn("----taskAlarmConfig: 更新云库配置到本地完毕！");
        } catch (Exception e) {
            logger.warn("----taskAlarmConfig is Exception" + e);
        }
    }


    /**
     * 短信循环电话号码报警
     */
    private void send() {
        if ("0".equals(man)) {
            logger.error("send  switch=0,  return");
            return;
        }
        Map<String, String>mapMsg = new HashMap<String, String>();
        mapMsg.put("eMsg","PLC采集数据");
        mapMsg.put("num", "5");
        String template = "SMS_150180447";
        try {
            for (String p : listPhone) {
                if (StringUtils.isNotBlank(p)) {
                    SendSmsResponse response = sendSms(p, mapMsg, template);
                    logger.info("短信接口返回的数据----------------");
                    logger.info("Code:" + response.getCode());
                    logger.info("Message:" + response.getMessage());
                    logger.info("RequestId:  is  Exception" + response.getRequestId());
                    logger.info("BizId:  is  Exception" + response.getBizId());
                } else {
                    logger.error("send  phone  is  null");
                }
            }
        } catch (Exception e) {
            logger.error("send is Exception"  + e);
        }
    }


    /**
     * 发短信
     * @param phone
     * @return
     * @throws ClientException
     */
    private SendSmsResponse sendSms(String phone,  Map<String, String>mapMsg, String sms_template) throws ClientException {
        System.setProperty("sun.net.client.defaultConnectTimeout", "10000");
        System.setProperty("sun.net.client.defaultReadTimeout", "10000");
        IClientProfile profile = DefaultProfile.getProfile("cn-hangzhou", accessKeyId, accessKeySecret);
        DefaultProfile.addEndpoint("cn-hangzhou", "cn-hangzhou", product, domain);
        IAcsClient acsClient = new DefaultAcsClient(profile);
        SendSmsRequest request = new SendSmsRequest();
        request.setPhoneNumbers(phone);
        request.setSignName("甜圆云通知");
        request.setTemplateCode(sms_template);
        request.setTemplateParam(JSONObject.toJSONString(mapMsg));
        request.setOutId("yourOutId");
        SendSmsResponse sendSmsResponse = null;
        try {
            sendSmsResponse = acsClient.getAcsResponse(request);
        } catch (ClientException e) {
            logger.error("sendSms  is  Exception" + e);
        }
        return sendSmsResponse;
    }

    /**
     * 发送钉钉消息
     *
     * @param msg
     */
    public void sendQuantiyAlarmInfo(String msg) {

        try {
            HttpClient httpclient = HttpClients.createDefault();
            HttpPost httppost = new HttpPost(
                    "https://oapi.dingtalk.com/robot/send?access_token=cfd308e57ae0dac4df80d85cb13d2d8d9324a718db31e53369fcd75349ba2534");
            httppost.addHeader("Content-Type", "application/json; charset=utf-8");
            //String textMsg = "{ \"msgtype\": \"text\", \"text\": {\"content\": \"测试消息类型？\"}}";
            // 内容
            Map<String, Object> contentMap = new HashMap<String, Object>();
            contentMap.put("content", msg);
            // at
            Map<String, Object> atMap = new HashMap<String, Object>();
            atMap.put("atMobiles", listPhone);
            // 主体
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("msgtype", "text");
            map.put("text", JSONObject.toJSONString(contentMap));
            map.put("at", atMap);
            StringEntity strEnt = new StringEntity(JSONObject.toJSONString(map), "utf-8");
            httppost.setEntity(strEnt);
            HttpResponse response = httpclient.execute(httppost);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String result = EntityUtils.toString(response.getEntity(), "utf-8");
                //System.out.println(result);
            }
        } catch (IOException e) {
            logger.error("sendQuantiyAlarmInfo  is  Exception" + e);
        }
    }
    public String getMan() {
        return man;
    }

    public void setMan(String man) {
        this.man = man;
    }

    public static List<String> getListPhone() {
        return listPhone;
    }

    public static void setListPhone(List<String> listPhone) {
        Alarm.listPhone = listPhone;
    }
}
