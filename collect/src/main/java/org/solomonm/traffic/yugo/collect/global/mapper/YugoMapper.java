package org.solomonm.traffic.yugo.collect.global.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.solomonm.traffic.yugo.collect.global.vo.DmbAccInciVo;

@Mapper
public interface YugoMapper {
 
    void insertInciInfoList(List<DmbAccInciVo> paramList);

}
