@param org.example.alfs.dto.ticket.TicketViewDTO ticket

@template.layout(title = "View Ticket", content = @`
    <h1 class="text-2xl font-bold mb-6">Ticket</h1>

    <div class="card p-6 space-y-4">
        <div>
            <strong>ID:</strong> ${ticket.getId()}
        </div>

        <div>
            <strong>Title:</strong> ${ticket.getTitle()}
        </div>

        <div>
            <strong>Description:</strong> ${ticket.getDescription()}
        </div>

        <div>
            <strong>Status:</strong> ${ticket.getStatus()}
        </div>
    </div>
`)