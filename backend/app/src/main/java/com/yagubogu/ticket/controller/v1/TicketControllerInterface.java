package com.yagubogu.ticket.controller.v1;

import com.yagubogu.auth.dto.MemberClaims;
import com.yagubogu.ticket.dto.v1.TicketResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Ticket", description = "직관 티켓 관련 API")
@RequestMapping("/tickets")
public interface TicketControllerInterface {

    @Operation(summary = "직관 티켓 조회", description = "checkInId에 해당하는 직관 티켓 화면 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "직관 티켓 조회 성공"),
            @ApiResponse(responseCode = "404", description = "해당 직관 내역을 찾을 수 없음")
    })
    @GetMapping("/{checkInId}")
    ResponseEntity<TicketResponse> findTicket(
            @Parameter(hidden = true) MemberClaims memberClaims,
            @PathVariable long checkInId
    );
}
