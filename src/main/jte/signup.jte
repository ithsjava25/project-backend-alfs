@param org.example.alfs.dto.auth.SignupRequestDTO request
@param String success

@template.layout(title = "Sign up", content = @`

<h1 class="text-2xl font-bold text-center mb-6">Sign up</h1>

<form method="post" action="/signup-form"
      class="space-y-4 max-w-md mx-auto bg-[#1a2b49] p-6 rounded shadow">

    <div>
        <label for="username" class="block text-white mb-1">Username</label>
        <input id="username" name="username"
               class="w-full px-3 py-2 rounded bg-white text-black" />
    </div>

    <div>
        <label for="password" class="block text-white mb-1">Password</label>
        <input id="password" type="password" name="password"
               class="w-full px-3 py-2 rounded bg-white text-black" />
    </div>

    <button type="submit"
            class="w-full bg-white text-[#1a2b49] py-2 rounded font-semibold hover:bg-gray-100 transition">
        Create account
    </button>

    <p class="text-sm text-center text-gray-300 mt-4">
        Already have an account?
        <a href="/login" class="text-[#b39359] hover:underline ml-1">
            Login
        </a>
    </p>

</form>

`, success = success)