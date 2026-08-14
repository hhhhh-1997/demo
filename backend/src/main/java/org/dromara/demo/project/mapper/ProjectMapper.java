package org.dromara.demo.project.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.demo.project.domain.Project;

/**
 * 项目 Mapper。
 *
 * @author demo
 * @since 2026-08-14
 */
public interface ProjectMapper extends BaseMapper<Project> {

    /**
     * 查询指定前缀下最大项目编号。
     *
     * @param prefix 项目编号前缀
     * @return 最大项目编号，无则返回 null
     */
    @Select("SELECT project_code FROM project WHERE project_code LIKE CONCAT(#{prefix}, '%') ORDER BY project_code DESC LIMIT 1")
    String selectMaxCodeByMonth(@Param("prefix") String prefix);
}
