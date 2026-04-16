@param org.example.alfs.dto.ticket.TicketCreateDTO ticket
@param String success
@template.layout(title = "Create Ticket", content = @`
    <h1 class="text-2xl font-bold mb-6">Create Ticket</h1>

    <form method="post" action="/tickets/create" class="space-y-4">
        <input type="text"
               name="title"
               value="${ticket.getTitle()}"
               placeholder="Title"
               class="w-full border p-2 rounded"/>

        <textarea
                name="description"
                placeholder="Description"
                  class="w-full border p-2 rounded h-32">${ticket.getDescription()}</textarea>

        <button
                type="submit"
                class="bg-[#1a2b49] text-white px-4 py-2 rounded">
            Create
        </button>
    </form>
`, success = success)