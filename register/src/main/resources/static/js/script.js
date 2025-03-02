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
function displayMessage(type, message) {
    Swal.fire({
        icon: type, // 'success' lub 'error'
        title: message,
        showConfirmButton: false,
        timer: 3000 // Popup znika po 3 sekundach
    });
}

document.addEventListener("DOMContentLoaded", function () {
    const teamSelect = document.getElementById("team");

    if (!teamSelect) {
//        console.warn("Element #team nie istnieje w DOM.");
        return; // ❌ Przerwij działanie skryptu, jeśli `teamSelect` nie istnieje
    }

    // Pobranie listy zespołów
    fetch("/api/teams")
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            if (!Array.isArray(data) || data.length === 0) {
                console.warn("Brak danych zespołów.");
                return;
            }

            data.forEach(team => {
                let option = document.createElement("option");
                option.value = team.id;
                option.textContent = team.teamName;
                teamSelect.appendChild(option);
            });
        })
        .catch(error => {
            console.error("Błąd pobierania zespołów:", error);
        });
});


   document.addEventListener("DOMContentLoaded", function () {
       const teamSelect = document.getElementById("team");
       const sectionSelect = document.getElementById("section");

       if (teamSelect && sectionSelect) { // ✅ Sprawdzenie, czy elementy istnieją
           teamSelect.addEventListener("change", function () {
               let teamId = this.value;
               sectionSelect.innerHTML = '<option value="">Ładowanie...</option>';

               if (teamId) {
                   fetch(`/api/sections/${teamId}`)
                       .then(response => {
                           if (!response.ok) {
                               throw new Error(`HTTP error! Status: ${response.status}`);
                           }
                           return response.json();
                       })
                       .then(data => {
                           sectionSelect.innerHTML = '<option value="">Wybierz sekcję</option>';
                           data.forEach(section => {
                               let option = document.createElement("option");
                               option.value = section.id;
                               option.textContent = section.sectionName;
                               sectionSelect.appendChild(option);
                           });
                       })
                       .catch(error => {
                           sectionSelect.innerHTML = '<option value="">Błąd ładowania</option>';
                           console.error("Błąd pobierania sekcji:", error);
                       });
               } else {
                   sectionSelect.innerHTML = '<option value="">Wybierz sekcję</option>';
               }
           });
       } else {
           console.warn("Elementy #team lub #section nie istnieją w DOM.");
       }
   });


document.addEventListener("DOMContentLoaded", function () {
    const addUserForm = document.getElementById("addUserForm");

    if (addUserForm) { // ✅ Sprawdzenie, czy formularz istnieje
        addUserForm.addEventListener("submit", function (event) {
            event.preventDefault();

            let formData = new URLSearchParams(new FormData(this)).toString();

            fetch("/api/users/addUser", {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded"
                },
                body: formData
            })
            .then(response => {
                if (response.status === 302) {
                    window.location.href = "/adminPanel";
                } else {
                    alert("Błąd podczas dodawania użytkownika.");
                }
            })
            .catch(error => console.error("Błąd:", error));
        });
    } else {
        console.warn("Element #addUserForm nie istnieje.");
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




console.log("Skrypt SCRIPT został załadowany");
