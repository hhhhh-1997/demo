package org.dromara.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.dromara.demo.domain.Performance;

@Mapper
public interface PerformanceMapper extends BaseMapper<Performance> {
}
