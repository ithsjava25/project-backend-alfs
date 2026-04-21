@param org.example.alfs.dto.ticket.TicketViewDTO ticket
@param java.util.List<org.example.alfs.entities.User> investigators
@param java.util.List<org.example.alfs.dto.comment.CommentViewDTO> comments
@param java.util.List<org.example.alfs.entities.Attachment> attachments
@param java.util.List<org.example.alfs.entities.AuditLog> auditLogs
@param String username
@param String success
@param String role
@param String accessToken

@template.layout(title = "View Ticket", content = @`

<div class="w-full max-w-[1600px] mx-auto px-6">

    <!-- HEADER -->
    <div class="flex justify-between items-center mb-6">

        <div>
            <h1 class="text-xl font-bold">
                Ticket #${ticket.getId()} - ${ticket.getTitle()}
            </h1>
        </div>

        <div class="flex gap-6 font-semibold items-center">
            @if(ticket.getAssignedInvestigatorName() != null)
                <div>
                    Assigned: ${ticket.getAssignedInvestigatorName()}
                </div>
            @endif

            <div>
                Status: ${ticket.getStatus()}
            </div>
        </div>

    </div>

    <!-- GRID -->
    <div class="grid grid-cols-4 gap-4 items-stretch">

        <!-- LEFT PANEL -->
        <div class="col-span-1 bg-white p-4 rounded shadow flex flex-col h-[600px] overflow-hidden">

            <!-- ACTIONS -->
            @if("ADMIN".equals(role) || "INVESTIGATOR".equals(role))
                <div class="shrink-0">
                    <h2 class="font-bold mb-2">Actions</h2>

                    @if("ADMIN".equals(role))
                        <form method="post" action="/tickets/${ticket.getId()}/assign" class="mb-3">
                            <select name="investigatorId" class="border p-2 rounded w-full">
                                <option value="">Select investigator</option>
                                @for(var inv : investigators)
                                    <option value="${inv.getId()}">
                                        ${inv.getUsername()}
                                    </option>
                                @endfor
                            </select>

                            <button class="bg-[#1a2b49] text-white px-3 py-2 rounded mt-2 w-full">
                                Assign
                            </button>
                        </form>
                    @endif

                    <form method="post" action="/tickets/${ticket.getId()}/status">
                        <select name="status" class="border p-2 rounded w-full">

                            @if(ticket.getStatus().name().equals("OPEN"))
                                <option value="OPEN" selected>OPEN</option>
                            @else
                                <option value="OPEN">OPEN</option>
                            @endif

                            @if(ticket.getStatus().name().equals("IN_PROGRESS"))
                                <option value="IN_PROGRESS" selected>IN_PROGRESS</option>
                            @else
                                <option value="IN_PROGRESS">IN_PROGRESS</option>
                            @endif

                            @if(ticket.getStatus().name().equals("RESOLVED"))
                                <option value="RESOLVED" selected>RESOLVED</option>
                            @else
                                <option value="RESOLVED">RESOLVED</option>
                            @endif

                            @if(ticket.getStatus().name().equals("CLOSED"))
                                <option value="CLOSED" selected>CLOSED</option>
                            @else
                                <option value="CLOSED">CLOSED</option>
                            @endif

                        </select>

                        <button class="bg-[#1a2b49] text-white px-3 py-2 rounded mt-2 w-full">
                            Update Status
                        </button>
                    </form>
                </div>
            @endif

            <!-- ATTACHMENTS -->
            <div class="flex flex-col flex-1 min-h-0">

                <h2 class="font-bold mb-2 mt-4">Attachments</h2>

                <!-- UPLOAD -->
                <form method="post"
                      action="/api/files/upload"
                      enctype="multipart/form-data"
                      class="flex flex-col gap-2 mb-4 shrink-0">

                    <input type="hidden" name="ticketId" value="${ticket.getId()}" />
                    @if(accessToken != null)
                        <input type="hidden" name="token" value="${accessToken}" />
                    @endif

                    <label class="border rounded px-3 py-2 text-sm cursor-pointer bg-gray-50 hover:bg-gray-100">
                        <span id="file-name">Choose file...</span>
                        <input type="file" name="file" class="hidden" onchange="updateFileName(this)" />
                    </label>

                    <button onclick="return validateUpload()"
                            class="bg-[#1a2b49] text-white px-3 py-2 rounded text-sm">
                        Upload
                    </button>
                </form>

                <!-- SCROLL AREA -->
                <div class="flex-1 min-h-0 overflow-y-auto pr-1">

                    @if(attachments != null)
                        @for(var att : attachments)
                            <div class="border border-gray-200 rounded px-2 py-2 mb-2 bg-gray-50 hover:bg-gray-100 transition">

                                <div class="flex items-center gap-2 text-xs font-medium text-gray-800 truncate">
                                    <span>📎</span>
                                    <span class="truncate max-w-[140px]" title="${att.getFileName()}">
                                        ${att.getFileName()}
                                    </span>
                                </div>

                                <div class="text-[10px] text-gray-500 mt-1">
                                    Uploaded by: ${att.getUploadedBy() != null ? att.getUploadedBy().getUsername() : "Anonymous"}
                                </div>

                                <div class="mt-1">
                                    <a href="/api/files/${att.getId()}/download"
                                       class="text-blue-600 text-xs hover:underline">
                                        Download
                                    </a>
                                </div>

                            </div>
                        @endfor
                    @endif

                </div>
            </div>

        </div>

        <!-- CHAT -->
        <div class="col-span-2 bg-white p-4 rounded shadow flex flex-col h-[600px] relative">

            <!-- CHAT SCROLL AREA -->
            <div id="chatContainer" class="flex-1 overflow-y-auto mb-4 space-y-4 relative">

                <!-- TICKET DESCRIPTION (ALWAYS FIRST) -->
                <div class="flex justify-start">
                    <div class="bg-white border border-gray-300 shadow-sm p-3 rounded max-w-[70%] break-words">

                        <div class="text-xs font-bold text-yellow-800 mb-1">
                            Ticket Description
                        </div>

                        <div class="text-xs font-bold mb-1">
                            @if(username != null)
                                ${username}
                            @else
                                Anonymous
                            @endif
                        </div>

                        ${ticket.getDescription()}

                        <div class="text-[10px] text-gray-500 mt-1">
                            ${ticket.getFormattedCreatedAt()}
                        </div>

                    </div>
                </div>

                <!-- COMMENTS -->
                @if(comments != null)
                    @for(var c : comments)

                        @if(c.getAuthor().equals(username))
                            <div class="flex justify-end">
                                <div class="bg-blue-500 text-white p-3 rounded max-w-[70%] break-words">

                                    <div class="text-xs font-bold text-right mb-1">
                                        ${c.getAuthor()}
                                    </div>

                                    ${c.getMessage()}

                                    <div class="text-[10px] text-blue-200 mt-1 text-right flex justify-end gap-2 items-center">
                                        ${c.getFormattedCreatedAt()}

                                        @if(c.isInternalNote())
                                            <span class="bg-blue-100 text-blue-800 px-2 py-[1px] rounded text-[9px] font-medium">
                                                internal
                                            </span>
                                        @endif
                                    </div>

                                </div>
                            </div>
                        @else
                            <div class="flex justify-start">
                                <div class="bg-gray-200 p-3 rounded max-w-[70%] break-words">

                                    <div class="text-xs font-bold">
                                        ${c.getAuthor()}
                                    </div>

                                    ${c.getMessage()}

                                    <div class="text-[10px] text-gray-500 mt-1 flex gap-2 items-center">
                                        ${c.getFormattedCreatedAt()}

                                        @if(c.isInternalNote())
                                            <span class="bg-gray-600 text-white px-2 py-[1px] rounded text-[9px] font-medium">
                                                internal
                                            </span>
                                        @endif
                                    </div>

                                </div>
                            </div>
                        @endif

                    @endfor
                @endif
            </div>



            <!-- MESSAGE INPUT -->
            <div class="mt-2 relative">
                <form method="post" action="/tickets/${ticket.getId()}/comments"
                      class="flex flex-col gap-2">

                    @if(accessToken != null)
                        <input type="hidden" name="token" value="${accessToken}" />
                    @endif
                    <button id="scrollTopBtn"
                            type="button"
                            onclick="scrollToTop()"
                            class="hidden absolute right-3 top-[-40px]
                                   w-8 h-8 flex items-center justify-center
                                   bg-[#1a2b49]/80 text-white text-sm
                                   rounded-full shadow-md
                                   hover:bg-[#1a2b49] transition">
                        ↑
                    </button>

                    <textarea name="message"
                              rows="3"
                              oninput="toggleSendButton()"
                              class="w-full border rounded px-3 py-2 text-sm resize-none"
                              placeholder="Type message..."></textarea>

                    @if("ADMIN".equals(role) || "INVESTIGATOR".equals(role))
                        <label class="flex items-center gap-2 text-xs text-gray-600">
                            <input type="checkbox" name="internalNote" value="true" />
                            Internal message
                        </label>
                    @endif

                    <div class="flex justify-end">
                        <button id="sendBtn"
                                disabled
                                class="px-4 py-2 rounded text-sm transition bg-gray-300 text-gray-500 cursor-not-allowed">
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
const chatContainer = document.getElementById('chatContainer');
const scrollBtn = document.getElementById('scrollTopBtn');

chatContainer.addEventListener('scroll', () => {
    if (chatContainer.scrollTop > 100) {
        scrollBtn.classList.remove('hidden');
    } else {
        scrollBtn.classList.add('hidden');
    }
});

function scrollToTop() {
    chatContainer.scrollTo({
        top: 0,
        behavior: 'smooth'
    });
}



function updateFileName(input) {
    const fileName = input.files.length > 0 ? input.files[0].name : "Choose file...";
    document.getElementById("file-name").textContent = fileName;
}

function validateUpload() {
    const input = document.querySelector('input[name="file"]');
    if (!input.files || input.files.length === 0) {
        alert("Please choose a file first.");
        return false;
    }
    return true;
}

function toggleSendButton() {
    const textarea = document.querySelector('textarea[name="message"]');
    const button = document.getElementById("sendBtn");

    if (textarea.value.trim() === "") {
        button.disabled = true;

        button.classList.remove("bg-[#1a2b49]", "text-white", "cursor-pointer");
        button.classList.add("bg-gray-300", "text-gray-500", "cursor-not-allowed");
    } else {
        button.disabled = false;

        button.classList.remove("bg-gray-300", "text-gray-500", "cursor-not-allowed");
        button.classList.add("bg-[#1a2b49]", "text-white", "cursor-pointer");
    }
}

window.addEventListener("load", () => {
    const chat = document.getElementById("chatContainer");
    if (chat) {
        chat.scrollTop = chat.scrollHeight;
    }
});
</script>

`, success = success)