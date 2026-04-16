@param org.example.alfs.dto.ticket.TicketCreateDTO ticket
@param String success

@template.layout(title = "Preview", content = @`

<h1 class="text-2xl font-bold mb-6">Preview</h1>

    <div class="bg-gray-100 p-6 rounded mb-6">
        <h2 class="text-xl font-bold">${ticket.getTitle()}</h2>
        <p>${ticket.getDescription()}</p>
    </div>


    <div class="space-y-4">


        <form method="post" action="/tickets/create">
            <input type="hidden" name="title" value="${ticket.getTitle()}" />
            <input type="hidden" name="description" value="${ticket.getDescription()}" />

            <button class="bg-[#1a2b49] text-white px-4 py-2 rounded">
                Submit
            </button>
        </form>

    </div>



`, success = success)