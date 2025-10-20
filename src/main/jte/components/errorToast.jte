@param String toastMsg

!{String id = String.valueOf(Math.round(Math.random() * 10000));}

<div id="toast-${id}" class="toast">

    <p>Error</p>
    <p>${toastMsg}</p>

    <script>
        setTimeout(() => {
            const toast = document.getElementById("toast-${id}")
            toast.style.animation = "toast-outro 200ms"
            setTimeout(() => {
                toast.remove()
            }, 200)
        }, 5000)
    </script>
</div>