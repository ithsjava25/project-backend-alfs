@param org.example.alfs.dto.ticket.TicketCreateDTO ticket

@template.layout(title = "Create Ticket", content = @`
    <h1 class="text-2xl font-bold mb-6">Create Ticket</h1>

    <form class="space-y-4">
        <input type="text" placeholder="Title"
               class="w-full border p-2 rounded"/>

        <textarea placeholder="Description"
                  class="w-full border p-2 rounded h-32"></textarea>

        <button class="bg-[#1a2b49] text-white px-4 py-2 rounded">
            Create
        </button>
    </form>
`)