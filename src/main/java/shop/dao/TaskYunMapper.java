package shop.dao;

import shop.domain.AlarmDo;
import shop.domain.ConfigDO;

import java.util.List;

public interface TaskYunMapper {

    List<ConfigDO> readConfig();

    AlarmDo selectMan();
}
