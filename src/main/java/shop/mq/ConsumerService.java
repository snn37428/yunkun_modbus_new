package shop.mq;

import com.alibaba.fastjson.JSONObject;
import com.aliyun.openservices.ons.api.*;
import org.apache.log4j.Logger;
import shop.domain.Instruct;

import java.util.Properties;

public class ConsumerService {

    private static final Logger logger = Logger.getLogger(ConsumerService.class);

    private static Properties mqConfig;

    public void read() {
        Consumer consumer = ONSFactory.createConsumer(mqConfig);
        consumer.subscribe("TY_20181201_CONTROLLER", "CC", new MessageListener() {
            public Action consume(Message message, ConsumeContext context) {
                Instruct rs = JSONObject.parseObject(new String(message.getBody()), Instruct.class);
                if (rs == null) {
                    logger.error("read : rs null");
                }
                // TODO
                logger.info("————monitor mq: " + JSONObject.parseObject(new String(message.getBody())));
                return Action.CommitMessage;
            }
        });
        consumer.start();
        logger.info("----init mq monitor is start");
    }

    public static void setMqConfig(Properties mqConfig) {
        ConsumerService.mqConfig = mqConfig;
    }
}
