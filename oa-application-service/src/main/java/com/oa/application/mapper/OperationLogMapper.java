package com.oa.application.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oa.common.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {
}
