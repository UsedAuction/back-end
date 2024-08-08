package com.ddang.usedauction.auction.controller;

import com.ddang.usedauction.auction.dto.AuctionCreateDto;
import com.ddang.usedauction.auction.dto.AuctionServiceDto;
import com.ddang.usedauction.auction.service.AuctionService;
import com.ddang.usedauction.config.GlobalApiResponse;
import com.ddang.usedauction.validation.IsImage;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/auctions")
@RequiredArgsConstructor
@Validated
public class AuctionController {

    private final AuctionService auctionService;

    @PostMapping
    public ResponseEntity<GlobalApiResponse<AuctionCreateDto.Response>> createAuctionController(
        @IsImage(message = "이미지 파일이 아니거나 올바르지 않은 이미지 입니다. (허용하는 확장자 : .jpg, .jpeg, .png)") @RequestPart
        MultipartFile thumbnail,
        @RequestPart(required = false) List<@IsImage(message = "이미지 파일이 아니거나 올바르지 않은 이미지 입니다. (허용하는 확장자 : .jpg, .jpeg, .png)") MultipartFile> imageList,
        @Valid @RequestPart AuctionCreateDto.Request createDto) {

        String memberId = "test"; // todo: 토큰을 통해 조회

        AuctionServiceDto auction = auctionService.createAuction(thumbnail, imageList, memberId,
            createDto);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(
                GlobalApiResponse.toGlobalResponse(HttpStatus.CREATED, auction.toCreateResponse()));
    }
}
