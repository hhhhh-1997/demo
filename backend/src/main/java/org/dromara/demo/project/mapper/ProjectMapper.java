package org.dromara.demo.project.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.demo.project.domain.Project;
import org.dromara.demo.project.vo.NameValue;

import java.math.BigDecimal;
import java.util.List;

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

    /**
     * 按二级分类统计储备库项目数量（待下达 / 已下达）。
     *
     * @param status1 状态一
     * @param status2 状态二
     * @return 分类分布
     */
    List<NameValue> countGroupByType(@Param("status1") String status1, @Param("status2") String status2);

    /**
     * 按所属单位统计储备库项目数量（待下达 / 已下达）。
     *
     * @param status1 状态一
     * @param status2 状态二
     * @return 单位分布
     */
    List<NameValue> countGroupByDept(@Param("status1") String status1, @Param("status2") String status2);

    /**
     * 汇总储备库项目投资金额（待下达 / 已下达）。
     *
     * @param status1 状态一
     * @param status2 状态二
     * @return 投资金额合计（元）
     */
    BigDecimal sumInvestmentAmount(@Param("status1") String status1, @Param("status2") String status2);
}
