@param org.example.alfs.dto.ticket.TicketCreateDTO ticket
@param String success
@param Boolean isLoggedIn
@param String username
@template.layout(title = "Create Ticket", content = @`
    <h1 class="text-2xl font-bold mb-6">Create Ticket</h1>
    @if(!isLoggedIn)
        <div class="mb-4 p-3 bg-yellow-100 text-yellow-800 rounded text-sm">
            This ticket will be created <strong>anonymously</strong>.
            <a href="/login" class="underline ml-1">Sign in</a> to attach it to your account.
        </div>
    @else
        <div class="mb-4 p-3 bg-blue-100 text-blue-800 rounded text-sm">
            This ticket will be created as <strong>${username}</strong>.
            To create anonymously, please sign out first.
        </div>
    @endif
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