package shop.dao;

import shop.domain.AlarmDo;
import shop.domain.Cell;
import shop.domain.ConfigDO;

import java.util.List;

public interface TaskYunMapper {

    int insert(List<Cell> cells);

    List<ConfigDO> readConfig();

    AlarmDo selectMan();
}
