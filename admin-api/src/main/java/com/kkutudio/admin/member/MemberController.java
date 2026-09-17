package com.kkutudio.admin.member;

import com.kkutudio.admin.auth.AdminUserPrincipal;
import com.kkutudio.admin.integration.AppAdminClient;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/apps/{appCode}/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping
    public AppAdminClient.MemberPageResponse list(@PathVariable String appCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication, HttpServletRequest request) {
        return memberService.getMembers(appCode, keyword, page, size, principal(authentication),
                request.getRemoteAddr(), request.getHeader("User-Agent"));
    }

    @GetMapping("/{memberId}")
    public AppAdminClient.MemberDetailResponse detail(@PathVariable String appCode, @PathVariable String memberId,
            Authentication authentication, HttpServletRequest request) {
        return memberService.getMember(appCode, memberId, principal(authentication), request.getRemoteAddr(),
                request.getHeader("User-Agent"));
    }

    @PostMapping("/{memberId}/suspend")
    public void suspend(@PathVariable String appCode, @PathVariable String memberId,
            @Valid @RequestBody SuspendRequest body, Authentication authentication, HttpServletRequest request) {
        memberService.suspendMember(appCode, memberId, body.reason(), principal(authentication),
                request.getRemoteAddr(), request.getHeader("User-Agent"));
    }

    @PostMapping("/{memberId}/force-logout")
    public void forceLogout(@PathVariable String appCode, @PathVariable String memberId,
            Authentication authentication, HttpServletRequest request) {
        memberService.forceLogoutMember(appCode, memberId, principal(authentication), request.getRemoteAddr(),
                request.getHeader("User-Agent"));
    }

    private AdminUserPrincipal principal(Authentication authentication) {
        return (AdminUserPrincipal) authentication.getPrincipal();
    }

    public record SuspendRequest(@NotBlank String reason) {
    }
}
