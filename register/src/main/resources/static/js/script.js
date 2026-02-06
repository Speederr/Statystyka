
function displayMessage(type, message) {
    Swal.fire({
        icon: type, // 'success' lub 'error'
        title: message,
        showConfirmButton: false,
        timer: 3000 // Popup znika po 3 sekundach
    });
}


document.addEventListener("DOMContentLoaded", () => {
    const infoIcon = document.getElementById("info-icon");
    const tooltipText = document.getElementById("tooltipText");

    if (!infoIcon || !tooltipText) {
       // console.warn("Ostrzeżenie: Jeden z wymaganych elementów (#info-icon lub #tooltipText) nie istnieje w DOM.");
        return; // Zatrzymuje dalsze działanie, jeśli brakuje elementów
    }

    infoIcon.addEventListener("click", () => {
        if (tooltipText.classList.contains("hidden")) {
            tooltipText.classList.remove("hidden");
            tooltipText.style.opacity = 1;
            tooltipText.style.visibility = "visible";
        } else {
            tooltipText.classList.add("hidden");
            tooltipText.style.opacity = 0;
            tooltipText.style.visibility = "hidden";
        }
    });

    // Ukrywanie tooltipa po kliknięciu poza nim
    document.addEventListener("click", (event) => {
        if (!infoIcon.contains(event.target) && !tooltipText.contains(event.target)) {
            tooltipText.classList.add("hidden");
            tooltipText.style.opacity = 0;
            tooltipText.style.visibility = "hidden";
        }
    });
});

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

// Sprawdź widoczność i wykonaj togglePasswordVisibility tylko dla widocznych pól
const passwordField = document.querySelector("#password");
if (passwordField && passwordField.offsetParent !== null) {
    togglePasswordVisibility("show-password", "password");
}

const newPasswordField = document.querySelector("#new-password");
if (newPasswordField && newPasswordField.offsetParent !== null) {
    togglePasswordVisibility("show-new-password", "new-password");
}

const confirmNewPasswordField = document.querySelector("#new-confirm-password");
if (confirmNewPasswordField && confirmNewPasswordField.offsetParent !== null) {
    togglePasswordVisibility("show-new-confirm-password", "new-confirm-password");
}

const temporaryPasswordField = document.querySelector("#temporary-password");
if (temporaryPasswordField && temporaryPasswordField.offsetParent !== null) {
    togglePasswordVisibility("show-temporary-password", "temporary-password");
}

const confirmPasswordField = document.querySelector("#confirm-password");
if (confirmPasswordField && confirmPasswordField.offsetParent !== null) {
    togglePasswordVisibility("show-confirm-password", "confirm-password");
}

const settingsCurrentPasswordField = document.querySelector("#settings-current-password");
if(settingsCurrentPasswordField && settingsCurrentPasswordField.offsetParent !== null) {
    togglePasswordVisibility("show-password", "settings-current-password");
}
const settingsNewPasswordField = document.querySelector("#settings-new-password");
if(settingsNewPasswordField && settingsNewPasswordField.offsetParent !== null) {
    togglePasswordVisibility("show-new-password", "settings-new-password");
}
const settingsConfirmPasswordField = document.querySelector("#settings-confirm-password");
if(settingsConfirmPasswordField && settingsConfirmPasswordField.offsetParent !== null) {
    togglePasswordVisibility("show-confirm-password", "settings-confirm-password");
}


window.addEventListener("load", () => {
    const loader = document.querySelector(".loader");

    // Ukryj loader po zakończeniu animacji
    loader.classList.add("loader--hidden");

    loader.addEventListener("transitionend", () => {
        if (loader.classList.contains("loader--hidden")) {
            loader.style.display = "none"; // Ukryj całkowicie po zakończeniu animacji
        }
    });

    // Funkcja do obsługi wysyłania formularza
    const showLoader = () => {
        loader.style.display = "flex";  // Pokaż loader
        loader.classList.remove("loader--hidden"); // Usuń klasę ukrywającą loader
    };

    // Obsługa każdego formularza oddzielnie
    const form = document.querySelector("#form-container");
    const registerForm = document.querySelector("#register-form");
    const loginForm = document.querySelector("#login-form");

    // Sprawdzamy, czy formularze istnieją i dodajemy event listener
    if (form) {
        form.addEventListener("submit", (event) => {
            showLoader();
        });
    }

    if (registerForm) {
        registerForm.addEventListener("submit", (event) => {
            showLoader();
        });
    }

    if (loginForm) {
        loginForm.addEventListener("submit", (event) => {
            showLoader();
        });
    }
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

        if (sidebar && btn) {
            btn.onclick = function () {
                sidebar.classList.toggle('active');

                // Poczekaj na zakończenie animacji (jeśli sidebar ją ma)
                setTimeout(() => {
                    if (typeof calendar !== 'undefined' && calendar) {
                        calendar.updateSize();
                    }
                }, 500); // dopasuj do czasu animacji w CSS (np. 300ms)
            };
        }
    } catch (error) {
        console.error("Wystąpił błąd w toggleNavBar:", error);
    }
}

toggleNavBar();



document.addEventListener('DOMContentLoaded', function () {
    const openModalBtn = document.getElementById('openModalBtn');
    const closeModalBtn = document.getElementById('closeModalBtn');
    const userModal = document.getElementById('userModal');

    if (openModalBtn && closeModalBtn && userModal) {
        openModalBtn.addEventListener('click', function () {
            userModal.style.display = 'block';
        });

        closeModalBtn.addEventListener('click', function () {
            userModal.style.display = 'none';
        });

        window.addEventListener('click', function (event) {
            if (event.target === userModal) {
                userModal.style.display = 'none';
            }
        });
    }
});



document.addEventListener('DOMContentLoaded', function () {
    // Otwieranie modalów
    document.querySelectorAll('[data-open-modal]').forEach(btn => {
        btn.addEventListener('click', function () {
            const modalId = btn.getAttribute('data-open-modal');
            const modal = document.getElementById(modalId);
            if (modal) modal.classList.remove('hidden');
        });
    });

    // Zamknięcie modalów
    document.querySelectorAll('[data-close-modal]').forEach(btn => {
        btn.addEventListener('click', function () {
            const modalId = btn.getAttribute('data-close-modal');
            const modal = document.getElementById(modalId);
            if (modal) modal.classList.add('hidden');
        });
    });
});


document.addEventListener("DOMContentLoaded", function () {
    const currentDateElement = document.getElementById("currentDate");

    if (currentDateElement) {
        const today = new Date();
        const options = { year: 'numeric', month: 'long', day: 'numeric' };
        currentDateElement.textContent = today.toLocaleDateString('pl-PL', options);
    }

    const dateInput = document.getElementById("selectedDate");
    if (dateInput) {
        const today = new Date();
        const pad = n => String(n).padStart(2, "0");
        const todayIso = `${today.getFullYear()}-${pad(today.getMonth() + 1)}-${pad(today.getDate())}`;
        if (!dateInput.value) {
            dateInput.value = todayIso;
        }
        dateInput.max = todayIso;
    }
});




function handleBack() {
    const path = window.location.pathname;

    // Obsługa /matrix/{userId}
    if (path.startsWith("/matrix")) {
        if (path === "/matrix") {
            window.location.href = "/processes";
        } else {
            const userId = path.split("/")[2];
            window.location.href = `/userDetails/${userId}`;
        }
        return;
    }

    // Obsługa /soft-skills/{userId}
    if (path.startsWith("/soft-skills")) {
        if (path === "/soft-skills") {
            window.location.href = "/processes";
        } else {
            const userId = path.split("/")[2];
            window.location.href = `/userDetails/${userId}`;
        }
        return;
    }

    // Obsługa /app-matrix/{userId}
    if (path.startsWith("/app-matrix")) {
        if (path === "/app-matrix") {
            window.location.href = "/processes";
        } else {
            const userId = path.split("/")[2];
            window.location.href = `/userDetails/${userId}`;
        }
        return;
    }

    // Domyślnie wracaj na index
    window.location.href = "/index";
}

function navigateExecutionBack() {
    const elevated = document.getElementById("isElevatedRole");
    if (elevated) {
        window.location.href = "/efficiency";
    } else {
        window.location.href = "/index";
    }
}

document.addEventListener("DOMContentLoaded", function () {
    const tooltipToggle = document.getElementById("tooltipToggle");
    const passwordTooltip = document.getElementById("passwordTooltip");
    const arrowIcon = document.getElementById("arrowIcon");
    const flexFirstLoginContainer = document.querySelector(`.login.active`);

    if (tooltipToggle && passwordTooltip && arrowIcon && flexFirstLoginContainer) {
        tooltipToggle.addEventListener("click", () => {
            passwordTooltip.classList.toggle("active");
            arrowIcon.classList.toggle("open");
            flexFirstLoginContainer.classList.toggle("tooltip-open");
        });
    }
});
document.addEventListener("DOMContentLoaded", function () {
    const passwordInput = document.getElementById('new-password');
    if (!passwordInput) return;

    // Wzorce do sprawdzania
    const rules = [
        {
            selector: '[data-rule="length"]',
            validate: val => val.length >= 8
        },
        {
            selector: '[data-rule="uppercase"]',
            validate: val => /[A-Z]/.test(val)
        },
        {
            selector: '[data-rule="lowercase"]',
            validate: val => /[a-z]/.test(val)
        },
        {
            selector: '[data-rule="digit"]',
            validate: val => /\d/.test(val)
        },
        {
            selector: '[data-rule="special"]',
            validate: val => /[@_$!%*?&]/.test(val)
        }
    ];

    passwordInput.addEventListener('input', function () {
        const value = passwordInput.value;

        rules.forEach(rule => {
            const span = document.querySelector(rule.selector);
            if (!span) return;
            const icon = span.querySelector('.rule-icon');
            if (!icon) return;

            if (rule.validate(value)) {
                icon.classList.remove('bx-x');
                icon.classList.add('bx-check');
                icon.style.color = '#22c55e'; // zielony
            } else {
                icon.classList.remove('bx-check');
                icon.classList.add('bx-x');
                icon.style.color = '#e73c3c'; // czerwony
            }
        });
    });
});
//console.log("Skrypt SCRIPT został załadowany");
