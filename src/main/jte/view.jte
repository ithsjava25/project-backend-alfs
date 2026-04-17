@param org.example.alfs.dto.ticket.TicketViewDTO ticket
@param java.util.List<org.example.alfs.dto.comment.CommentViewDTO> comments
@param java.util.List<org.example.alfs.entities.Attachment> attachments
@param java.util.List<org.example.alfs.entities.AuditLog> auditLogs
@param String username
@param String success

@template.layout(title = "View Ticket", content = @`

<div class="w-full max-w-[1600px] mx-auto px-6">

    <!-- HEADER -->
    <div class="flex justify-between items-center mb-4">
        <div>
            <h1 class="text-xl font-bold">
                Ticket #${ticket.getId()} - ${ticket.getTitle()}
            </h1>
        </div>

        <div class="font-semibold">
            Status: ${ticket.getStatus()}
        </div>
    </div>

    <!-- GRID -->
    <div class="grid grid-cols-4 gap-4">

        <!-- LEFT PANEL -->
        <div class="col-span-1 bg-white p-4 rounded shadow flex flex-col gap-6">

            <!-- ADMIN / INVESTIGATOR ACTIONS -->
            <div>
                <h2 class="font-bold mb-2">Actions</h2>

                <!-- Assign -->
                <form method="post" action="/tickets/${ticket.getId()}/assign" class="mb-3">
                    <select name="investigatorId" class="border p-2 rounded w-full">
                        <option>Select investigator</option>
                        <!-- TODO: loop investigators later -->
                    </select>
                    <button class="bg-[#1a2b49] text-white px-3 py-2 rounded mt-2 w-full">
                        Assign
                    </button>
                </form>

                <!-- Status -->
                <form method="post" action="/tickets/${ticket.getId()}/status">
                    <select name="status" class="border p-2 rounded w-full">
                        <option value="OPEN">OPEN</option>
                        <option value="IN_PROGRESS">IN_PROGRESS</option>
                        <option value="RESOLVED">RESOLVED</option>
                        <option value="CLOSED">CLOSED</option>
                    </select>
                    <button class="bg-[#1a2b49] text-white px-3 py-2 rounded mt-2 w-full">
                        Update Status
                    </button>
                </form>
            </div>

            <!-- ATTACHMENTS -->
            <div>
                <h2 class="font-bold mb-2">Attachments</h2>

                <!-- UPLOAD -->
                <form method="post"
                      action="/api/files/upload"
                      enctype="multipart/form-data"
                      class="flex flex-col gap-2 mb-4">

                    <input type="hidden" name="ticketId" value="${ticket.getId()}" />

                    <!-- CUSTOM FILE PICKER -->
                    <label class="border rounded px-3 py-2 text-sm cursor-pointer bg-gray-50 hover:bg-gray-100">
                        <span id="file-name">Choose file...</span>
                        <input type="file" name="file" class="hidden" onchange="updateFileName(this)" />
                    </label>

                    <button class="bg-[#1a2b49] text-white px-3 py-2 rounded text-sm">
                        Upload
                    </button>

                </form>

                @if(attachments != null)
                    @for(var att : attachments)
                        <div class="text-sm mb-2 flex justify-between items-center">
                            <span>📎 ${att.getFileName()}</span>
                            <a href="/api/files/${att.getId()}/download" class="text-blue-600">
                                Download
                            </a>
                        </div>
                    @endfor
                @endif
            </div>

        </div>

        <!-- CHAT -->
        <div class="col-span-2 bg-white p-4 rounded shadow flex flex-col h-[600px]">

            <!-- CHAT MESSAGES -->
            <div class="flex-1 overflow-y-auto mb-4 space-y-4">

                @if(comments != null)
                    @for(var c : comments)

                        @if(c.getAuthor().equals(username))
                            <!-- MY MESSAGE -->
                            <div class="flex justify-end">
                                <div class="bg-blue-500 text-white p-3 rounded max-w-[70%] break-words">

                                    <div class="text-xs font-bold text-right mb-1">
                                        ${c.getAuthor()}
                                    </div>

                                    ${c.getMessage()}

                                </div>
                            </div>
                        @else
                            <!-- OTHER -->
                            <div class="flex justify-start">
                                <div class="bg-gray-200 p-3 rounded max-w-[70%] break-words">
                                    <div class="text-xs font-bold">${c.getAuthor()}</div>
                                    ${c.getMessage()}
                                </div>
                            </div>
                        @endif

                    @endfor
                @endif

            </div>

            <!-- MESSAGE INPUT -->
            <div class="mt-2">

                <form method="post" action="/tickets/${ticket.getId()}/comments"
                      class="flex flex-col gap-2">

                    <!-- TEXTAREA -->
                    <textarea name="message"
                              rows="3"
                              class="w-full border rounded px-3 py-2 text-sm resize-none"
                              placeholder="Type message..."></textarea>

                    <!-- BUTTON -->
                    <div class="flex justify-end">
                        <button class="bg-[#1a2b49] text-white px-4 py-2 rounded text-sm">
                            Send
                        </button>
                    </div>

                </form>

            </div>

        </div>

        <!-- AUDIT LOGS -->
        <div class="col-span-1 bg-white p-4 rounded shadow h-[600px] overflow-y-auto">

            <h2 class="font-bold mb-3">Audit Logs</h2>

            @if(auditLogs != null)
                @for(var log : auditLogs)
                    <div class="text-sm mb-2">
                        ${log.getAction()}
                    </div>
                @endfor
            @endif

        </div>

    </div>

</div>
<script>
function updateFileName(input) {
    const fileName = input.files.length > 0 ? input.files[0].name : "Choose file...";
    document.getElementById("file-name").textContent = fileName;
}
</script>
`, success = success)