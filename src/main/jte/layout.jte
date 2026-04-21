@import org.springframework.security.core.context.SecurityContextHolder
@param String title
@param gg.jte.Content content
@param String success

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>${title}</title>

    <!-- Tailwind -->
    <script src="https://cdn.tailwindcss.com"></script>

    <!-- Custom CSS -->
    <link rel="stylesheet" href="/css/style.css">
</head>

<body class="min-h-screen flex flex-col">

<!-- HEADER -->
<header class="bg-[#1a2b49] text-white py-6 px-4 header-border">
    <div class="max-w-5xl mx-auto flex justify-between items-center">
        <a href="/" class="hover:opacity-80 transition">
            <h1 class="text-xl font-bold uppercase">
                ALFS
                whistleblower system
            </h1>
        </a>



        <nav class="space-x-6 text-sm">

            <a href="/" class="hover:text-[#b39359]">Home</a>

            @if(
                SecurityContextHolder.getContext().getAuthentication() == null
                || !SecurityContextHolder.getContext().getAuthentication().isAuthenticated()
                || "anonymousUser".equals(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                || SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_REPORTER"))
            )
                <a href="/tickets/create" class="hover:text-[#b39359]">Report</a>
            @endif

            @if(
                SecurityContextHolder.getContext().getAuthentication() != null
                && SecurityContextHolder.getContext().getAuthentication().isAuthenticated()
                && !"anonymousUser".equals(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
            )

                @if(SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_REPORTER")))
                    <a href="/tickets/my" class="hover:text-[#b39359]">My tickets</a>
                @endif

                @if(SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_INVESTIGATOR")))
                    <a href="/tickets/assigned" class="hover:text-[#b39359]">My cases</a>
                @endif

                @if(SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")))
                    <a href="/admin/tickets" class="hover:text-[#b39359]">All tickets</a>
                @endif

                <form method="post" action="/auth/logout" style="display:inline;">
                    <button type="submit" class="hover:text-[#b39359]">Sign out</button>
                </form>

            @else

                <a href="/login" class="hover:text-[#b39359]">Sign in</a>

            @endif

        </nav>
    </div>
</header>

<!-- CONTENT -->
<main class="flex-grow max-w-5xl mx-auto w-full p-4 md:p-10">

    ${content}

</main>

<!-- FOOTER -->
<footer class="bg-white border-t py-6 px-4">
    <div class="max-w-5xl mx-auto text-sm text-gray-600">
        © ALFS
    </div>
</footer>


 @if(success != null)
    <div id="toast" class="fixed top-5 right-5 z-50">

        <div class="bg-white border border-gray-300 shadow-lg rounded-md inline-block overflow-hidden">

            <!-- CONTENT -->
            <div class="flex items-center gap-2 px-3 py-2 whitespace-nowrap">

                <!-- ICON -->
                <div class="text-green-500 text-base">✔</div>

                <!-- TEXT -->
                <div class="text-gray-800 text-sm leading-tight">
                    ${success}
                </div>

                <!-- CLOSE -->
                <button onclick="closeToast()" class="text-gray-400 text-sm hover:text-black">
                    ✕
                </button>
            </div>

            <!-- PROGRESS BAR -->
            <div id="toast-bar" class="h-[2px] bg-green-500 animate-toast"></div>
        </div>
    </div>
@endif



<script>
    const toast = document.getElementById("toast");

    if (toast) {
        setTimeout(() => {
            toast.style.transition = "opacity 0.5s ease";
            toast.style.opacity = "0";

            setTimeout(() => toast.remove(), 500);
        }, 3000);
    }
</script>
</body>
</html>