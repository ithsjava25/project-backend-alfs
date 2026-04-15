@param org.example.alfs.dto.ticket.TicketCreateDTO ticket

@template.layout(title = "Preview", content = @`

<h1 class="text-2xl font-bold mb-6">Preview</h1>

    <div class="bg-gray-100 p-6 rounded mb-6">
        <h2 class="text-xl font-bold">${ticket.getTitle()}</h2>
        <p>${ticket.getDescription()}</p>
    </div>





`)