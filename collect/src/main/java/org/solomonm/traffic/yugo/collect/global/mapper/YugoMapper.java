package org.solomonm.traffic.yugo.collect.global.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.solomonm.traffic.yugo.collect.scheduler.police.vo.PoliceYugoVo;

@Mapper
public interface YugoMapper {
 
    void insertInciInfoList(List<PoliceYugoVo> paramList);

}
