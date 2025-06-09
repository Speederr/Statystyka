//document.addEventListener("DOMContentLoaded", () => {
//    const loginForm = document.getElementById("login-form");
//    const errorDiv = document.getElementById("error");
//
//    if (loginForm) {
//        loginForm.addEventListener("submit", function (e) {
//            e.preventDefault();
//
//            const username = document.getElementById("username").value;
//            const password = document.getElementById("password").value;
//
//            fetch("/api/auth/login", {
//                method: "POST",
//                headers: {
//                    "Content-Type": "application/json"
//                },
//                body: JSON.stringify({ username, password })
//            })
//            .then(response => {
//                if (!response.ok) {
//                    throw new Error("Błąd logowania");
//                }
//                return response.json();
//            })
//            .then(data => {
//                console.log("✅ Zalogowano:", data);
//
//                // ✅ Przekierowanie zgodnie z odpowiedzią z backendu
//                if (data.redirect) {
//                    window.location.href = data.redirect;
//                } else {
//                    // domyślnie na /index, jeśli brak redirectu
//                    window.location.href = "/index";
//                }
//            })
//            .catch(error => {
//                console.error("⛔", error);
//                if (errorDiv) errorDiv.style.display = "block";
//            });
//        });
//    }
//});


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
    const addNewProcessModal = document.getElementById('addNewProcessModal');
    const closeProcessModalBtn = document.getElementById('closeProcessModalBtn');
    const processModal = document.getElementById('processModal');

    if (addNewProcessModal && closeProcessModalBtn && processModal) {
        addNewProcessModal.addEventListener('click', function () {
            processModal.style.display = 'block';
        });

        closeProcessModalBtn.addEventListener('click', function () {
            processModal.style.display = 'none';
        });

        window.addEventListener('click', function (event) {
            if (event.target === processModal) {
                processModal.style.display = 'none';
            }
        });
    }
});

document.addEventListener('DOMContentLoaded', function () {
    const openTeamModal = document.getElementById('newTeamModalBtn');
    const closeTeamModal = document.getElementById('closeTeamModalBtn');
    const teamModal = document.getElementById('teamModal');

    if (openTeamModal && closeTeamModal && teamModal) {
        openTeamModal.addEventListener('click', function () {
            teamModal.style.display = 'block';
        });

        closeTeamModal.addEventListener('click', function () {
            teamModal.style.display = 'none';
        });

        window.addEventListener('click', function (event) {
            if (event.target === teamModal) {
                teamModal.style.display = 'none';
            }
        });
    }
});

document.addEventListener('DOMContentLoaded', function () {
    const openSectionModal = document.getElementById('newSectionModalBtn');
    const closeSectionModal = document.getElementById('closeSectionModalBtn');
    const sectionModal = document.getElementById('sectionModal');

    if (openSectionModal && closeSectionModal && sectionModal) {
        openSectionModal.addEventListener('click', function () {
            sectionModal.style.display = 'block';
        });

        closeSectionModal.addEventListener('click', function () {
            sectionModal.style.display = 'none';
        });

        window.addEventListener('click', function (event) {
            if (event.target === sectionModal) {
                sectionModal.style.display = 'none';
            }
        });
    }
});


document.addEventListener("DOMContentLoaded", function () {
    const currentDateElement = document.getElementById("currentDate");

    if (currentDateElement) {
        const today = new Date();
        const options = { year: 'numeric', month: 'long', day: 'numeric' };
        currentDateElement.textContent = today.toLocaleDateString('pl-PL', options);
    }
});

//document.addEventListener("DOMContentLoaded", function () {
//  if (!window.location.pathname.includes("/index")) return;
//
//    const teamSelect = document.getElementById("team");
//    const sectionSelect = document.getElementById("section");
//    const positionSelect = document.getElementById("position");
//    const setupForm = document.getElementById("setupForm");
//    const modal = document.getElementById("firstLoginModal");
//
//    const teamSectionMap = {};
//
//    // 🔽 Pobranie danych zespołów, sekcji i stanowisk
//    fetch("/api/user/setup-data")
//        .then(res => res.json())
//        .then(data => {
//            // Zespoły
//            data.teams.forEach(team => {
//                const option = new Option(team.teamName, team.id);
//                teamSelect.appendChild(option);
//                teamSectionMap[team.id] = team.sections; // przypisz sekcje do zespołu
//            });
//
//            // Stanowiska
//            data.positions.forEach(pos => {
//                const option = new Option(pos.positionName, pos.id);
//                positionSelect.appendChild(option);
//            });
//        })
//        .catch(err => console.error("❌ Błąd ładowania danych setupu:", err));
//
//    // 🔁 Dynamiczna zmiana sekcji przy zmianie zespołu
//    teamSelect.addEventListener("change", function () {
//        const selectedTeamId = this.value;
//        sectionSelect.innerHTML = "<option value=''>Wybierz sekcję</option>";
//
//        if (teamSectionMap[selectedTeamId]) {
//            teamSectionMap[selectedTeamId].forEach(section => {
//                const option = new Option(section.sectionName, section.id);
//                sectionSelect.appendChild(option);
//            });
//        }
//    });
document.addEventListener("DOMContentLoaded", function () {
    const path = window.location.pathname;

    if (!["/index", "/adminPanel"].some(p => path.includes(p))) return;

    const teamSelect = document.getElementById("team");
    const sectionSelect = document.getElementById("section");
    const positionSelect = document.getElementById("position");
    const setupForm = document.getElementById("setupForm");
    const modal = document.getElementById("firstLoginModal");

    if (!teamSelect || !sectionSelect || !positionSelect) return;

    const teamSectionMap = {};

    fetch("/api/user/setup-data")
        .then(res => res.json())
        .then(data => {
            // Zespoły
            data.teams.forEach(team => {
                const option = new Option(team.teamName, team.id);
                teamSelect.appendChild(option);
                teamSectionMap[team.id] = team.sections; // przypisanie sekcji
            });

            // Stanowiska
            data.positions.forEach(pos => {
                const option = new Option(pos.positionName, pos.id);
                positionSelect.appendChild(option);
            });
        })
        .catch(err => console.error("❌ Błąd ładowania danych setupu:", err));

    teamSelect.addEventListener("change", function () {
        const selectedTeamId = this.value;
        sectionSelect.innerHTML = "<option value=''>Wybierz sekcję</option>";

        if (teamSectionMap[selectedTeamId]) {
            teamSectionMap[selectedTeamId].forEach(section => {
                const option = new Option(section.sectionName, section.id);
                sectionSelect.appendChild(option);
            });
        }
    });


    // 🔁 Sprawdzenie, czy pokazać modal (pierwsze logowanie)
    fetch("/api/user/whoami")
        .then(res => res.json())
        .then(user => {
            // Sprawdź czy hasło zostało zmienione i czy pola są puste
            if (user.passwordChanged && !user.isCreateByAdmin &&
                (!user.team || !user.section || !user.position)) {
                modal?.classList.remove("hidden");
            }
        })
        .catch(err => console.error("❌ Błąd pobierania danych użytkownika:", err));

      // 🔁 Obsługa formularza zapisu danych – tylko jeśli istnieje
      if (setupForm) {
        setupForm.addEventListener("submit", function (e) {
          e.preventDefault();

          const formData = new FormData(setupForm);

          fetch("/api/user/complete-setup", {
            method: "POST",
            body: formData
          })
            .then(res => {
              if (res.ok) {
                displayMessage('success', 'Dane zostały zaktualizowane!');
                modal?.classList.add("hidden");
                setTimeout(() => location.reload(), 3000);
              } else {
                displayMessage('error', 'Błąd zapisu danych.');
              }
            })
            .catch(err => {
              console.error("❌ Błąd wysyłki danych:", err);
              displayMessage('error', 'Wystąpił błąd połączenia.');
            });
        });
      }
});


function handleBack() {
    const path = window.location.pathname;

    if (path.startsWith("/matrix")) {
        if (path === "/matrix") {
            // Jesteś na /matrix => wróć na /index
            window.location.href = "/processes";
        } else {
            // Jesteś na /matrix/{userId} => wróć na /userDetails/{userId}
            const userId = path.split("/")[2]; // wyciągnij userId z URL
            window.location.href = `/userDetails/${userId}`;
        }
    } else {
        // Bezpiecznik: jakby coś było nie tak, wracamy na index
        window.location.href = "/index";
    }
}



//console.log("Skrypt SCRIPT został załadowany");
