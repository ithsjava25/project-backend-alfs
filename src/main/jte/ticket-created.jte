@param String token
@param String success

@template.layout(title = "Ticket Created", content = @`

<h1 class="text-2xl font-bold mb-6">Ditt ärende är skapat!</h1>

<div class="space-y-4">

    <p>
        Spara denna kod:
        <strong>${token}</strong>
    </p>

    <p>
        <a href="/tickets/token/${token}"
           class="text-blue-600 underline">
            Gå till ditt ärende
        </a>
    </p>

</div>

`, success = success)