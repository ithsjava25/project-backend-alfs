@param String error
@param Integer status
@param String success


@template.layout(title = "Error", content = @`
    <div class="max-w-xl mx-auto mt-16 text-center">

        <h1 class="text-3xl font-serif text-[#1a2b49] mb-6">
            Something went wrong
        </h1>

        <p class="text-lg mb-4">
            Status: ${status}
        </p>

        <p class="text-gray-700 mb-6">
            ${error}
        </p>

        <a href="/startPage" class="btn-primary px-6 py-3 rounded">
            Back to start page
        </a>

    </div>
` , success = null)