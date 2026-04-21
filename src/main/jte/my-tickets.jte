@param java.util.List<org.example.alfs.dto.ticket.TicketViewDTO> tickets
@param String success

@template.layout(title = "My tickets", content = @`

<h1 class="text-2xl font-bold mb-6">My tickets</h1>

@if(tickets.isEmpty())
    <p>No tickets yet.</p>
@else
    <div class="space-y-4">
        @for (var ticket : tickets)
            <div class="border p-4 rounded mb-4 bg-white shadow hover:shadow-md transition">

                <h2 class="font-bold text-lg">
                    ${ticket.getTitle()}
                </h2>

                <p class="text-gray-600">
                    ${ticket.getDescription()}
                </p>

                <p class="text-sm text-gray-400 mt-1">
                    Created: ${ticket.getFormattedCreatedAt()}
                </p>
                <p>Status: ${ticket.getStatus()}</p>

                <a href="/tickets/${ticket.getId()}"
                   class="text-[#1a2b49] font-semibold hover:underline mt-2 inline-block">
                    View ticket →
                </a>

            </div>
        @endfor
    </div>
@endif

`, success = success)