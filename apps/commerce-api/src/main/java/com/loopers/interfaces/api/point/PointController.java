package com.loopers.interfaces.api.point;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.loopers.application.point.PointService;
import com.loopers.application.point.dto.PointResult;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.point.dto.PointChargeRequest;
import com.loopers.interfaces.api.point.dto.PointChargeResponse;
import com.loopers.interfaces.api.point.dto.PointInfoResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/points")
public class PointController implements PointApiSpec {
    private final PointService pointService;

    @Override
    @PostMapping("/charge")
    public ApiResponse<PointChargeResponse> charge(
            @RequestHeader("X-USER-ID") Long userId, @RequestBody PointChargeRequest chargeRequest) {
        PointResult result = pointService.charge(userId, chargeRequest.amount());

        PointChargeResponse response = PointChargeResponse.from(result);
        return ApiResponse.success(response);
    }

    @Override
    @GetMapping("")
    public ApiResponse<PointInfoResponse> get(@RequestHeader("X-USER-ID") Long userId) {
        PointResult result = pointService.get(userId);

        PointInfoResponse response = PointInfoResponse.from(result);
        return ApiResponse.success(response);
    }
}
