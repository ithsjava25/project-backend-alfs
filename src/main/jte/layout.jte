@param String title
@param gg.jte.Content content

<!DOCTYPE html>
<html lang="sv">
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
        <h1 class="text-xl font-bold uppercase">
            Vissleblåsarinstansen
        </h1>

        <nav class="space-x-6 text-sm">
            <a href="/" class="hover:text-[#b39359]">Hem</a>
            <a href="/create" class="hover:text-[#b39359]">Rapportera</a>
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
        © Vissleblåsarsystem
    </div>
</footer>

</body>
</html>