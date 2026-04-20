@param java.util.List<org.example.alfs.dto.ticket.TicketViewDTO> tickets
@param String success

@template.layout(title = "My Assigned Tickets", content = @`

<div class="max-w-6xl mx-auto space-y-6">

    <h1 class="text-2xl font-bold">
        My Assigned Tickets
    </h1>

    <div class="bg-white rounded shadow overflow-hidden">

        <table class="w-full text-sm">

            <thead class="bg-gray-100 text-left">
                <tr>
                    <th class="p-3">ID</th>
                    <th class="p-3">Title</th>
                    <th class="p-3">Status</th>
                    <th class="p-3">Assigned</th>
                    <th class="p-3"></th>
                </tr>
            </thead>

            <tbody>

                @if(tickets != null && !tickets.isEmpty())

                    @for(var t : tickets)
                        <tr class="border-t hover:bg-gray-50">

                            <td class="p-3">
                                #${t.getId()}
                            </td>

                            <td class="p-3">
                                ${t.getTitle()}
                            </td>

                            <td class="p-3">
                                ${t.getStatus()}
                            </td>

                            <td class="p-3">
                                @if(t.getAssignedInvestigatorName() != null)
                                    ${t.getAssignedInvestigatorName()}
                                @else
                                    -
                                @endif
                            </td>

                            <td class="p-3 text-right">
                                <a href="/tickets/${t.getId()}"
                                   class="text-blue-600 hover:underline">
                                    Open
                                </a>
                            </td>

                        </tr>
                    @endfor

                @else

                    <tr>
                        <td colspan="5" class="p-6 text-center text-gray-500">
                            No tickets have been assigned to you yet.
                        </td>
                    </tr>

                @endif
            </tbody>

        </table>

    </div>

</div>

`, success = success)