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
public class PointHistoryDto {

    private Long id;
    private PointType pointType;
    private int pointAmount;
    private int curPointAmount;
    private Long memberId;

    public static PointHistoryDto fromPointHistory(PointHistory pointHistory) {
        return PointHistoryDto.builder()
            .id(pointHistory.getId())
            .pointType(pointHistory.getPointType())
            .pointAmount(pointHistory.getPointAmount())
            .curPointAmount(pointHistory.getCurPointAmount())
            .memberId(pointHistory.getMember().getId())
            .build();
    }
}
