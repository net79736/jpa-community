package com.jpacommunity.member.controller;

import com.jpacommunity.common.web.response.ResponseDto;
import com.jpacommunity.member.controller.response.MemberResponse;
import com.jpacommunity.member.dto.delete.MemberDeleteRequest;
import com.jpacommunity.member.dto.get.MemberGetRequest;
import com.jpacommunity.member.entity.Member;
import com.jpacommunity.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.jpacommunity.common.web.response.ResponseStatus.SUCCESS;


@Slf4j
@RestController
@RequestMapping("/api/admin/members")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority(T(org.myteam.server.member.domain.MemberRole).ADMIN.name())")
public class AdminController {
    private final MemberService memberService;

    @GetMapping("/email")
    public ResponseEntity<?> getByEmail(@Valid MemberGetRequest memberGetRequest, BindingResult bindingResult) {
        log.info("MemberController getByEmail 메서드 실행 : {}", memberGetRequest);
        MemberResponse response = memberService.getByEmail(memberGetRequest.getEmail());
        return new ResponseEntity<>(new ResponseDto<>(SUCCESS.getValue(), "회원 정보 조회 성공", response), HttpStatus.OK);
    }

    @GetMapping("/nickname")
    public ResponseEntity<?> getByNickname(@Valid MemberGetRequest memberGetRequest, BindingResult bindingResult) {
        log.info("MemberController getByNickname 메서드 실행 : {}", memberGetRequest);
        MemberResponse response = memberService.getByNickname(memberGetRequest.getNickname());
        return new ResponseEntity<>(new ResponseDto<>(SUCCESS.getValue(), "회원 정보 조회 성공", response), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<?> list() {
        log.info("getAllMembers : 회원 정보 목록 조회 메서드 실행");
        List<Member> allMembers = memberService.list();
        List<MemberResponse> response = allMembers.stream().map(MemberResponse::new).toList();
        return new ResponseEntity<>(new ResponseDto<>(SUCCESS.getValue(), "회원 정보 목록 조회 성공", response), HttpStatus.OK);
    }

    @Deprecated
    @DeleteMapping("/delete")
    public ResponseEntity<?> delete(@RequestBody MemberDeleteRequest memberDeleteRequest, BindingResult bindingResult) {
        log.info("MemberController delete 메서드 실행 : {}", memberDeleteRequest);
        String email = memberDeleteRequest.getEmail();
        memberService.delete(email);
        return new ResponseEntity<>(new ResponseDto<>(SUCCESS.getValue(), "회원 삭제 성공", null), HttpStatus.OK);
    }
}
