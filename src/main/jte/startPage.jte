@param String success
@template.layout(title = "Start Page", content = @`


    <div class="max-w-5xl mx-auto space-y-10">

        <div class="text-center">
            <h2 class="text-3xl font-serif text-[#1a2b49] mb-4">
                Welcome
            </h2>

            <p class="leading-relaxed max-w-2xl mx-auto">
                Here you can anonymously and securely report misconduct that concerns the public interest.
                We guarantee that your identity is protected in accordance with applicable whistleblowing legislation.
            </p>
        </div>

        <div class="grid md:grid-cols-3 gap-6">

            <div class="p-4 border-l-4 border-[#b39359] bg-gray-50">
                <h3 class="font-bold text-sm uppercase text-gray-600 mb-2">High security</h3>
                <p class="text-sm">This system is super secure</p>
            </div>

            <div class="p-4 border-l-4 border-[#b39359] bg-gray-50">
                <h3 class="font-bold text-sm uppercase text-gray-600 mb-2">Anonymity</h3>
                <p class="text-sm">You do not need to provide personal information to submit a report.</p>
            </div>

            <div class="p-4 border-l-4 border-[#b39359] bg-gray-50">
                <h3 class="font-bold text-sm uppercase text-gray-600 mb-2">Statutory protection</h3>
                <p class="text-sm">Protection against retaliation under national directives and regulations.</p>
            </div>

        </div>
        <div class="flex flex-col items-center gap-4">


            <a href="/tickets/create"
               class="bg-[#1a2b49] text-white px-8 py-4 rounded font-bold text-lg shadow hover:bg-[#2c4471] transition">
                Leave new report
            </a>


            <a href="/login"
               class="bg-white border px-6 py-2 rounded text-[#1a2b49] hover:bg-gray-100 transition">
                Sign in
            </a>

        </div>

    </div>

`, success = success)