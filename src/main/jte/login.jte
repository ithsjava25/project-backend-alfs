@template.layout(title = "Login", content = @`
    <div class="flex flex-col items-center justify-center min-h-[70vh] gap-6">
        <h1 class="text-2xl font-bold mb-6">Login</h1>

    <div class="bg-[#1a2b49] text-white p-6 rounded shadow max-w-md w-full mx-auto">

        <form method="post" action="/login-form" class="space-y-4">

        <label for="username">Username</label>
        <input id="username" name="username" />
            <div>
                <label for="password">Password</label>

                <input id="password" type="password" name="password" />
            </div>
            <button type="submit"
                    class="w-full bg-white text-[#1a2b49] py-2 rounded font-semibold hover:bg-gray-100 transition">
                Login
            </button>
        </form>


    </div>
        <h1 class="text-2xl font-bold mb-6">Got a token?</h1>
    <div class="bg-[#1a2b49] text-white p-6 rounded shadow max-w-md w-full mx-auto">
        <form method="get" action="/tickets/token" class="space-y-4">
            <h2 class="text-xl font-bold leading-tight">Login with token</h2>
            <input type="text" name="token"
                   placeholder="Enter your token" />
            <button type="submit"
                    class="w-full bg-white text-[#1a2b49] py-2 rounded font-semibold hover:bg-gray-100 transition">
                View Report
            </button>
        </form>
    </div>
        </div>

`)