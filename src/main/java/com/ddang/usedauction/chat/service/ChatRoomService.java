package com.ddang.usedauction.chat.service;

import com.ddang.usedauction.Member.Member;
import com.ddang.usedauction.Member.MemberRepository;
import com.ddang.usedauction.Member.exception.MemberErrorCode;
import com.ddang.usedauction.Member.exception.MemberException;
import com.ddang.usedauction.auction.Auction;
import com.ddang.usedauction.auction.AuctionRepository;
import com.ddang.usedauction.auction.exception.AuctionErrorCode;
import com.ddang.usedauction.auction.exception.AuctionException;
import com.ddang.usedauction.chat.domain.dto.ChatRoomCreateDto;
import com.ddang.usedauction.chat.domain.entity.ChatRoom;
import com.ddang.usedauction.chat.exception.ChatErrorCode;
import com.ddang.usedauction.chat.exception.ChatException;
import com.ddang.usedauction.chat.repository.ChatRoomRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

  private final ChatRoomRepository chatRoomRepository;
  private final MemberRepository memberRepository;
  private final AuctionRepository auctionRepository;

  public ChatRoomCreateDto.Response createChatRoom(Long memberId, ChatRoomCreateDto.Request dto) {
    if (chatRoomRepository.existsByAuctionId(dto.getAuctionId())) {
      throw new ChatException(ChatErrorCode.ALREADY_EXISTS_CHATROOM);
    }
    Member buyer = memberRepository.findById(memberId)
        .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND_MEMBER));

    Auction auction = auctionRepository.findById(dto.getAuctionId())
        .orElseThrow(() -> new AuctionException(AuctionErrorCode.NOT_FOUND_AUCTION));

    ChatRoom chatRoom = ChatRoom.builder()
        .seller(auction.getMember())
        .buyer(buyer)
        .auction(auction)
        .build();

    return ChatRoomCreateDto.Response.of(chatRoomRepository.save(chatRoom));
  }

  @Transactional(readOnly = true)
  public List<ChatRoomCreateDto.Response> getChatRoomList(Long memberId) {
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND_MEMBER));

    return chatRoomRepository.findChatRoomsByMemberId(member.getId()).stream()
        .map(ChatRoomCreateDto.Response::of)
        .collect(Collectors.toList());
  }

}
