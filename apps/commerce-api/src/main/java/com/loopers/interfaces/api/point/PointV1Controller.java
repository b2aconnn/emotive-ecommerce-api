package com.loopers.interfaces.api.point;

import com.loopers.application.point.PointFacade;
import com.loopers.application.point.PointInfo;
import com.loopers.interfaces.api.ApiResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/points")
public class PointV1Controller implements PointV1ApiSpec {
    private final PointFacade pointFacade;

    @PostMapping("")
    @Override
    public ApiResponse<PointV1Dto.ChargeResponse> charge(
//            @CurrentUser String userId,
            @RequestBody PointV1Dto.ChargeRequest chargeRequest) {
        String userId = "user1234";
        PointInfo info = pointFacade.charge(userId, chargeRequest.amount());
        PointV1Dto.ChargeResponse response = PointV1Dto.ChargeResponse.from(info);
        return ApiResponse.success(response);
    }

    @GetMapping("/{userId}")
    @Override
    public ApiResponse<PointV1Dto.InfoResponse> get(
            @PathVariable(value = "userId") String userId
    ) {
        PointInfo info = pointFacade.get(userId);
        if (info == null) {
            throw new EntityNotFoundException("사용자가 존재하지 않습니다.");
        }

        PointV1Dto.InfoResponse response = PointV1Dto.InfoResponse.from(info);
        return ApiResponse.success(response);
    }
}
