package org.example.alfs.services;

import org.example.alfs.dto.ticket.TicketCreateDTO;
import org.example.alfs.dto.ticket.TicketViewDTO;
import org.example.alfs.entities.Ticket;
import org.example.alfs.enums.TicketStatus;
import org.example.alfs.mapper.TicketMapper;
import org.example.alfs.repositories.TicketRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class TicketService {


    private final TicketRepository ticketRepository;
    private final TicketMapper ticketMapper;

    public TicketService(TicketRepository ticketRepository,  TicketMapper ticketMapper) {
        this.ticketRepository = ticketRepository;
        this.ticketMapper = ticketMapper;
    }

    //createNewTicket
    public TicketViewDTO createNewTicket(TicketCreateDTO ticketCreateDTO) {

        Ticket  ticket = new Ticket();

        ticket.setTitle(ticketCreateDTO.getTitle());
        ticket.setDescription(ticketCreateDTO.getDescription());

        Ticket save = ticketRepository.save(ticket);

        return ticketMapper.entityToViewDTO(save);
    }

    // View by token
    public TicketViewDTO getTicketByToken(String token) {

        Ticket ticket = ticketRepository.findByReporterToken(token).
                orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));
        return ticketMapper.entityToViewDTO(ticket);
    }

    //findById
    public TicketViewDTO getTicketById(Long id) {

        Ticket ticket = ticketRepository.findById(id).
                orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));
        return ticketMapper.entityToViewDTO(ticket);

    }

    //findByReporterId
    public List<TicketViewDTO> getTicketsByReporterId(Long reporterId) {
        return ticketRepository.findByReporterId(reporterId)
                .stream()
                .map(ticketMapper::entityToViewDTO)
                .toList();
    }

    //findByInvestigatorId
        public List<TicketViewDTO> getTicketsByInvestigatorId(Long investigatorId){
            return ticketRepository.findByInvestigatorId(investigatorId)
                    .stream()
                    .map(ticketMapper::entityToViewDTO)
                    .toList();
        }

    //findByStatus
        public List<TicketViewDTO> getTicketsByStatus(TicketStatus status) {
            return ticketRepository.findByStatus(status)
                    .stream()
                    .map(ticketMapper::entityToViewDTO)
                    .toList();
        }

    //findByStatusAndInvestigatorId
        public List<TicketViewDTO> getTicketsByStatusAndInvestigator(
                TicketStatus status,
                Long investigatorId) {

            return ticketRepository
                    .findByStatusAndInvestigatorId(status, investigatorId)
                    .stream()
                    .map(ticketMapper::entityToViewDTO)
                    .toList();
        }

    //findAll (pageable)
}
