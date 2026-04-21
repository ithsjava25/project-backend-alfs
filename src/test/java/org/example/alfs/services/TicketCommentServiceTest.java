package org.example.alfs.services;

import org.example.alfs.mapper.TicketCommentMapper;
import org.example.alfs.repositories.TicketCommentRepository;
import org.example.alfs.repositories.TicketRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TicketCommentService Test")
@ExtendWith(MockitoExtension.class)
class TicketCommentServiceTest {

    @Mock
    TicketRepository ticketRepository;
    @Mock
    TicketCommentRepository ticketCommentRepository;
    @Mock
    TicketCommentMapper ticketCommentMapper;

    @InjectMocks
    TicketCommentService ticketCommentService;

}