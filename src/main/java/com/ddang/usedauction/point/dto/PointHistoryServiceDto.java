package com.ddang.usedauction.point.dto;

import com.ddang.usedauction.point.domain.PointHistory;
import com.ddang.usedauction.point.type.PointType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PointHistoryServiceDto {

    private Long id;
    private PointType pointType;
    private int pointAmount;
    private int curPointAmount;
    private Long memberId;

    public static PointHistoryServiceDto fromPointHistory(PointHistory pointHistory) {
        return PointHistoryServiceDto.builder()
            .id(pointHistory.getId())
            .pointType(pointHistory.getPointType())
            .pointAmount(pointHistory.getPointAmount())
            .curPointAmount(pointHistory.getCurPointAmount())
            .memberId(pointHistory.getMember().getId())
            .build();
    }
}
