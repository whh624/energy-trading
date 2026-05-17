package com.energytrading.mapper;

import com.energytrading.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface OperationLogMapper {
    void insert(OperationLog log);
    List<OperationLog> selectAll();
    List<OperationLog> selectByTargetId(Long targetId);
}
