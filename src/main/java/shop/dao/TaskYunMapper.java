package shop.dao;

import shop.domain.AlarmDo;
import shop.domain.CellDO;
import shop.domain.ConfigDO;

import java.util.List;

public interface TaskYunMapper {

    int insert(List<CellDO> cells);

    List<ConfigDO> readConfig();

    AlarmDo selectMan();
}
