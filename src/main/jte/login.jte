@param String error
@param String tokenError
@param String success
@template.layout(title = "Login", content = @`
    <div class="flex flex-col items-center justify-center min-h-[70vh] gap-6">
        <h1 class="text-2xl font-bold mb-6">Login</h1>

    <div class="bg-[#1a2b49] text-white p-6 rounded shadow max-w-md w-full mx-auto">
        @if(error != null)
            <div class="bg-red-100 text-red-700 px-4 py-2 rounded text-sm text-center mb-4">
                Invalid username or password
            </div>
        @endif
        <form method="post" action="/login-form" class="space-y-4">

            <label for="username">Username</label>
            <input id="username" name="username"
                   class="w-full px-3 py-2 rounded bg-white text-black" />

            <div>
                <label for="password">Password</label>

                <input id="password" type="password" name="password"
                       class="w-full px-3 py-2 rounded bg-white text-black" />
            </div>

            <button type="submit"
                    class="w-full bg-white text-[#1a2b49] py-2 rounded font-semibold hover:bg-gray-100 transition">
                Login
            </button>

            <p class="text-sm text-center mt-4">
                Don’t have an account?
                <a href="/signup" class="text-[#b39359] hover:underline">
                    Create account here
                </a>
            </p>
        </form>


    </div>
        <h1 class="text-2xl font-bold mb-6">Got a token?</h1>
    <div class="bg-[#1a2b49] text-white p-6 rounded shadow max-w-md w-full mx-auto">
            @if(tokenError != null)
                <div class="bg-red-100 text-red-700 px-4 py-2 rounded text-sm text-center mb-4">
                    Invalid token
                </div>
            @endif
        <form method="get"
              onsubmit="event.preventDefault(); window.location='/tickets/token/' + this.token.value;"
              class="space-y-4">

            <h2 class="text-xl font-bold leading-tight">View report with token</h2>

            <input type="text" name="token"
                   placeholder="Enter your token"
                   class="w-full px-3 py-2 rounded bg-white text-black" />

            <button type="submit"
                    class="w-full bg-white text-[#1a2b49] py-2 rounded font-semibold hover:bg-gray-100 transition">
                View Report
            </button>

        </form>
    </div>
        </div>

`, success = success)