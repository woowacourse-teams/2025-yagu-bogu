package com.yagubogu.ticket.controller.v1;

import com.yagubogu.auth.annotation.RequireRole;
import com.yagubogu.auth.dto.MemberClaims;
import com.yagubogu.ticket.dto.v1.TicketResponse;
import com.yagubogu.ticket.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequireRole
@RestController
public class TicketController implements TicketControllerInterface {

    private final TicketService ticketService;

    @Override
    public ResponseEntity<TicketResponse> findTicket(
            final MemberClaims memberClaims,
            final long checkInId
    ) {
        TicketResponse response = ticketService.findTicket(memberClaims.id(), checkInId);

        return ResponseEntity.ok(response);
    }
}
