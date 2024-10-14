

function togglePasswordVisibility(showPasswordIconId, passwordFieldId) {
    try {
        const showPasswordIcon = document.querySelector(`#${showPasswordIconId}`);
        const passwordField = document.querySelector(`#${passwordFieldId}`);

        // Sprawdź, czy elementy istnieją, zanim spróbujesz dodać event listener
        if (showPasswordIcon && passwordField) {
            showPasswordIcon.addEventListener("click", function() {
                const type = passwordField.getAttribute("type") === "password" ? "text" : "password";
                passwordField.setAttribute("type", type);

                // Dodaj/usuń odpowiednią klasę, upewniając się, że przełączanie działa poprawnie
                showPasswordIcon.classList.toggle("fa-eye-slash");
                showPasswordIcon.classList.toggle("fa-eye");
            });
        }
    } catch (error) {
        console.error("Wystąpił błąd w togglePasswordVisibility:", error);
    }
}

// Obsługa dla każdego pola hasła (zastosowanie dla obu stron)
togglePasswordVisibility("show-temporary-password", "temporary-password");
togglePasswordVisibility("show-new-password", "newPassword");
togglePasswordVisibility("show-confirm-password", "confirmPassword");
togglePasswordVisibility("show-password", "password");

window.addEventListener("load", () => {
    const loader = document.querySelector(".loader");

    loader.classList.add("loader--hidden");

    loader.addEventListener("transitionend", () => {
        if (loader.classList.contains("loader--hidden")) {
            loader.style.display = "none"; // Ukryj całkowicie po zakończeniu animacji
        }
    });

    // Obsługa wysyłania formularza
    const form = document.querySelector("#form-container");

    form.addEventListener("submit", () => {
        loader.style.display = "flex";  // Pokaż loader
        loader.classList.remove("loader--hidden"); // Usuń klasę ukrywającą loader
    });

});

document.addEventListener('DOMContentLoaded', function () {
    try {
        const formContainer = document.querySelector('.add-role');
        const toggleHeader = document.getElementById('toggle-header');
        const toggleIcon = document.getElementById('toggle-icon');

        if (formContainer && toggleHeader && toggleIcon) {
            // Obsługa kliknięcia na nagłówek
            toggleHeader.addEventListener('click', function () {
                // Zmiana klas na kontenerze formularza
                formContainer.classList.toggle('collapsed');
                formContainer.classList.toggle('expanded');

                // Zmiana ikony
                toggleIcon.classList.toggle('bx-chevron-up');
                toggleIcon.classList.toggle('bx-chevron-down');
            });
        }
    } catch (error) {
        console.error("Wystąpił błąd w DOMContentLoaded:", error);
    }
});

function hideError() {
    try {
        document.getElementById('err-container').style.display = 'none';
        document.getElementById('container').style.display = 'none';
    } catch (error) {
        console.error("Wystąpił błąd w hideError:", error);
    }
}

function toggleNavBar() {
    try {
        let btn = document.querySelector('#btn');
        let sidebar = document.querySelector('.sidebar');

        if (sidebar) {
            btn.onclick = function () {
                sidebar.classList.toggle('active');
            };
        }
    } catch (error) {
        console.error("Wystąpił błąd w toggleNavBar:", error);
    }
}
// Wywołaj funkcję toggleNavBar, aby przypisać zdarzenie
toggleNavBar();

console.log("Skrypt został załadowany");
