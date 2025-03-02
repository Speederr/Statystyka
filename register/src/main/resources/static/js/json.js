
// ✅ Funkcja do czyszczenia wszystkich pól input po zapisie
function clearInputFields() {
    document.querySelectorAll("input[name='quantity']").forEach(input => {
        input.value = "";
        input.classList.remove("error");
    });
}

// ✅ Blokowanie wpisywania niedozwolonych znaków w inputach
document.querySelectorAll("input[name='quantity']").forEach(input => {
    input.addEventListener("input", function () {
        this.value = this.value.replace(/[^0-9]/g, ""); // Usuwa wszystko poza liczbami
    });
});
// ✅ Funkcja dodająca ikonkę do pola input
function addIcon(input, iconClass) {
    // Jeśli już istnieje ikona, nie dodawaj kolejnej
    if (input.parentNode.querySelector(".bx")) return;

    const icon = document.createElement("i");
    icon.className = `bx ${iconClass} edit-icon`;
    input.parentNode.appendChild(icon);
}
// ✅ Funkcja zamieniająca ikonę na inną (np. z bx-edit na bx-check)
function replaceIcon(input, newIconClass) {
    const icon = input.parentNode.querySelector(".bx");
    if (icon) {
        icon.className = `bx ${newIconClass} edit-icon`;
    }
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////
document.addEventListener('DOMContentLoaded', function() {
    fetch('/api/user/info')
        .then(response => response.json())
        .then(user => {
            if (user && user.firstName && user.lastName) {
                document.querySelector('.user .bold').textContent = `${user.firstName} ${user.lastName}`; // Set name

                // Map role ID to role name (example mapping)
                const roleNames = {
                    1: 'Admin',
                    2: 'Manager',
                    3: 'Koordynator',
                    4: 'Użytkownik'
                    // Add other role mappings here
                };

                // Get role name based on role ID
                const roleName = roleNames[user.roleId] || 'Nieznana rola'; // Default if ID not found
                document.querySelector('.role').textContent = roleName; // Set role name
            }
        })
        .catch(error => console.error('Błąd podczas pobierania danych użytkownika:', error));
});

/////////////////////////////////////////////////////////////////////////////////////////////
document.addEventListener("DOMContentLoaded", function () {
    // Sprawdzenie, czy jesteśmy na stronie /averageTime
    if (!window.location.pathname.includes("/averageTime")) {
        return;
    }

    const editButton = document.getElementById("editButton");
    let isEditing = false;

    // Po załadowaniu strony ustaw ikony "bx-check"
    const inputs = document.querySelectorAll("tbody input");
    inputs.forEach(input => addIcon(input, "bx-check"));

    editButton.addEventListener("click", function () {
        if (!isEditing) {
            // Tryb edycji: Odblokowujemy inputy i zmieniamy ikonki na "bx-edit"
            inputs.forEach(input => {
                input.removeAttribute("readonly");
                replaceIcon(input, "bx-edit");
            });

            editButton.textContent = "Zapisz";

        } else {
            // Tryb zapisu: Wysyłamy dane do backendu
            const updatePromises = Array.from(inputs).map(input => {
                const processId = input.id.split("_")[1]; // Pobieramy ID procesu
                const newTime = input.value;

                return fetch(`/api/processes/update`, {
                    method: "PUT",
                    headers: {
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify({
                        id: processId,
                        averageTime: newTime
                    })
                }).then(response => {
                    if (response.ok) {
                        replaceIcon(input, "bx-check"); // Zmieniamy ikonę na "check" po zapisaniu
                    }
                    return response;
                });
            });

            // Obsługa zapisania danych
            Promise.all(updatePromises)
                .then(responses => {
                    if (responses.every(response => response.ok)) {
                        displayMessage("success", "Zapisano pomyślnie!");
                    } else {
                        displayMessage("error", "Wystąpił błąd podczas zapisu.");
                    }
                })
                .catch(() => displayMessage("error", "Błąd podczas komunikacji z serwerem."));

            // Tryb wyjścia z edycji
            inputs.forEach(input => input.setAttribute("readonly", "true"));
            editButton.textContent = "Edytuj";
        }
        isEditing = !isEditing;
    });
});
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


async function calculateAndSave() {
    const userId = document.getElementById('userId').value.trim();
    if (!userId) {
        alert("Brak ID użytkownika.");
        return;
    }

    // Zbierz dane z formularza
    const inputs = document.querySelectorAll('input[data-process-id]');
    const processVolumes = {};   // Dane do obliczenia efektywności
    const dataList = [];         // Dane do zapisania wolumenów
    const today = new Date().toISOString().split("T")[0]; // Dzisiejsza data

    inputs.forEach(input => {
        const processId = input.getAttribute('data-process-id');
        const quantity = input.value.trim();

        if (quantity !== "" && /^\d+$/.test(quantity) && Number(quantity) > 0) {
            // Dodaj do obiektu JSON dla efektywności
            processVolumes[processId] = parseInt(quantity, 10);

            // Dodaj do listy dla zapisu wolumenów
            dataList.push({
                user_id: Number(userId),
                process_id: Number(processId),
                quantity: Number(quantity),
                todaysDate: today
            });
        }
    });

    if (Object.keys(processVolumes).length === 0) {
        displayMessage("error", "Brak wprowadzonych danych.");
        return;
    }

    try {
        // 🔹 Krok 1: Oblicz efektywność
        const efficiencyResponse = await fetch(`/api/efficiency/calculate/${userId}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(processVolumes)
        });

        if (efficiencyResponse.ok) {
//            console.log("Efektywność została pomyślnie obliczona!");
        } else {
            throw new Error("Błąd podczas obliczania efektywności.");
        }

        // 🔹 Krok 2: Zapisz wolumeny
        const saveResponse = await fetch("/api/saved-data/save", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(dataList)
        });

        if (saveResponse.ok) {
            displayMessage("success", "Wolumen zapisany pomyślnie!");
            clearInputFields();
        } else {
            throw new Error("Błąd podczas zapisywania wolumenów.");
        }
    } catch (error) {
        console.error('Błąd:', error);
        displayMessage("error", "Wystąpił błąd podczas operacji. Spróbuj ponownie.");
    }
}
document.addEventListener("DOMContentLoaded", function () {
    const exportButton = document.getElementById('exportToXLSX');
    if (exportButton) {
        exportButton.addEventListener('click', function () {
            fetch('/export/processes')
                .then(response => {
                    if (response.ok) {
                        return response.blob();
                    }
                    throw new Error('Failed to export data');
                })
                .then(blob => {
                    const url = window.URL.createObjectURL(blob);
                    const a = document.createElement('a');
                    a.style.display = 'none';
                    a.href = url;
                    a.download = 'processes.xlsx';
                    document.body.appendChild(a);
                    a.click();
                    window.URL.revokeObjectURL(url);
                })
                .catch(error => console.error('Error:', error));
        });
    } else {
        //console.warn('Warning: Element with ID "exportToXLSX" not found.');
        return;
    }
});


document.addEventListener("DOMContentLoaded", function () {
    const saveAllBtn = document.getElementById('saveAllBtn');
    if (saveAllBtn) {
        saveAllBtn.addEventListener('click', async function () {
            calculateAndSave();
        });
    } else {
       // console.warn('Element with ID saveAllBtn not found.');
        return;
    }
});


window.addEventListener("DOMContentLoaded", () => {
    const currentPath = window.location.pathname;

    if (currentPath === "/profile") {  // Sprawdź, czy jesteś na stronie /profile
        fetchUserProfile();
    }
});

function fetchUserProfile() {
    fetch('/api/user/profile')  // Właściwy endpoint
        .then(response => {
            if (!response.ok) {
                throw new Error('Błąd podczas pobierania danych użytkownika');
            }
            return response.json();
        })
        .then(data => {
            document.getElementById('user-firstname').textContent = data.firstName || 'Brak danych';
            document.getElementById('user-lastname').textContent = data.lastName || 'Brak danych';
            document.getElementById('user-login').textContent = data.username || 'Brak danych';
            document.getElementById('user-email').textContent = data.email || 'Brak danych';
        })
        .catch(error => {
            console.error('Wystąpił błąd:', error);
            displayMessage('error', 'Nie udało się załadować danych użytkownika');
        });
}
const MAX_FILE_SIZE = 2 * 1024 * 1024; // 2 MB

// Funkcja do pobierania avatara
function fetchUserAvatar() {
    fetch('/api/user/avatar')
        .then(response => {
            if (!response.ok) {
                throw new Error(`Błąd: ${response.status}`);
            }
            return response.text();
        })
        .then(base64Image => {
            if (base64Image) {
                const userAvatar = document.getElementById('user-avatar');
                const profileImage = document.getElementById('profile-image');

                if (userAvatar) {
                    userAvatar.src = `data:image/jpeg;base64,${base64Image}`;
                }

                if (profileImage) {
                    profileImage.src = `data:image/jpeg;base64,${base64Image}`;
                }
            } else {
                console.log('Brak avatara do wyświetlenia.');
            }
        })
        .catch(error => {
            console.error('Wystąpił błąd podczas pobierania avatara:', error);
        });
}

// Funkcja do wysyłania avatara z walidacją
function uploadAvatar(file) {
    // Sprawdzanie typu pliku
    const validTypes = ['image/jpeg', 'image/png', 'image/gif'];
    if (!validTypes.includes(file.type)) {
        displayMessage('error', 'Nieprawidłowy typ pliku. Obsługiwane formaty: JPEG, PNG, GIF.');
        return;
    }

    // Sprawdzanie rozmiaru pliku
    if (file.size > MAX_FILE_SIZE) {
        displayMessage('error', `Plik jest za duży. Maksymalny rozmiar to 2 MB.`);
        return;
    }

    const formData = new FormData();
    formData.append('avatar', file);

    fetch('/api/user/avatar', {
        method: 'POST',
        body: formData
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Błąd podczas zapisywania avatara');
        }
        return response.text();
    })
    .then(data => {
        displayMessage('success', 'Avatar został zaktualizowany!');
        fetchUserAvatar();  // Pobierz nowy avatar po zapisaniu
    })
    .catch(error => {
        console.error('Wystąpił błąd:', error);
        displayMessage('error', 'Nie udało się zapisać avatara.');
    });
}

// Obsługa wyboru pliku
document.addEventListener("DOMContentLoaded", function () {
const profileImageUpload = document.getElementById('profile-image-upload');
if (profileImageUpload) {
   profileImageUpload.addEventListener('change', function (event) {
       const file = event.target.files[0];
       if (file) {
           const reader = new FileReader();
           reader.onload = function (e) {
               document.getElementById('profile-image').src = e.target.result;
           };
           reader.readAsDataURL(file);
           uploadAvatar(file);
       }
   });
} else {
   //console.warn("Element #profile-image-upload nie został znaleziony.");
    return;
}
});


// Wywołanie fetchUserAvatar przy załadowaniu strony
window.addEventListener("DOMContentLoaded", fetchUserAvatar);



////////////////////////////////////////////////////////////////// NOTIFICATIONS ///////////////////////////////////////////

document.addEventListener("DOMContentLoaded", function () {
    const socket = new SockJS('/ws'); // 🔌 Połączenie WebSocket
    const stompClient = Stomp.over(socket);

    stompClient.connect({}, function () {
        console.log("✅ Połączono z WebSocket!");

        // 🎯 Subskrybuj temat powiadomień
        stompClient.subscribe("/topic/notifications", function (message) {
            console.log("📩 Nowa wiadomość!");

            // 🔄 Odśwież licznik wiadomości
            fetchNotificationCount();
        });
    });

    window.fetchNotificationCount = function fetchNotificationCount() {
        fetch('/api/messages/unread-count')
            .then(response => response.json())
            .then(data => {
                const notificationCounter = document.getElementById('notification-counter');

                if (!notificationCounter) {
                    console.warn("⚠️ Element #notification-counter nie został znaleziony w DOM.");
                    return;
                }

                if (data.count > 0) {
                    notificationCounter.textContent = data.count;
                    notificationCounter.style.display = 'flex';
                } else {
                    notificationCounter.style.display = 'none';
                }
            })
            .catch(error => console.error('❌ Błąd podczas pobierania liczby nieprzeczytanych wiadomości:', error));
    }

    // 📌 Wywołaj przy ładowaniu strony
    fetchNotificationCount();
});

document.addEventListener("DOMContentLoaded", function () {
    const markAsReadBtn = document.getElementById("markAsReadBtn");

    if (markAsReadBtn) { // Sprawdzenie, czy element istnieje
        markAsReadBtn.addEventListener("click", function () {
            const selectedMessages = document.querySelectorAll(".message-checkbox:checked");
            const messageIds = Array.from(selectedMessages).map(checkbox => checkbox.dataset.messageId);

            if (messageIds.length === 0) {
                alert("Nie zaznaczono żadnych wiadomości!");
                return;
            }

            fetch("/api/messages/mark-as-read", {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ messageIds: messageIds })
            })
            .then(response => {
                if (!response.ok) throw new Error("Błąd oznaczania jako przeczytane.");
                return response.json();
            })
            .then(() => {
                selectedMessages.forEach(checkbox => {
                    const messageItem = checkbox.closest(".message-item");
                    if (messageItem) { // Sprawdzenie, czy `messageItem` istnieje
                        messageItem.classList.add("read"); // ✅ Dodajemy klasę `.read`
                    }
                    checkbox.checked = false; // Odznaczamy checkbox
                });

                if (typeof fetchNotificationCount === "function") {
                    fetchNotificationCount(); // 🔄 Zaktualizuj licznik nieprzeczytanych wiadomości
                } else {
                    console.warn("fetchNotificationCount is not defined.");
                }
            })
            .catch(error => console.error("Błąd:", error));
        });
    } else {
        console.warn("Element #markAsReadBtn nie istnieje.");
    }
});




document.addEventListener("DOMContentLoaded", function () {
    // Pobranie elementów z DOM
    const newMessageModal = document.getElementById("newMessageModal");
    const openModalBtn = document.getElementById("new-message");
    const closeModalBtn = document.getElementById("closeNewMessageModal");
    const newMessageForm = document.getElementById("newMessageForm");

    // Sprawdzenie, czy elementy istnieją (żeby uniknąć błędu na innych stronach)
    if (!newMessageModal || !openModalBtn || !closeModalBtn || !newMessageForm) {
        return; // Jeśli brakuje któregokolwiek z tych elementów, zakończ wykonywanie skryptu
    }

    // Otwieranie modala po kliknięciu w przycisk
    openModalBtn.addEventListener("click", function () {
        newMessageModal.style.display = "flex";
    });

    // Zamknięcie modala po kliknięciu w przycisk "X"
    closeModalBtn.addEventListener("click", function () {
        newMessageModal.style.display = "none";
    });

    // Zamknięcie modala po kliknięciu poza nim
    window.addEventListener("click", function (event) {
        if (event.target === newMessageModal) {
            newMessageModal.style.display = "none";
        }
    });

    // Obsługa formularza wysyłania wiadomości
    newMessageForm.addEventListener("submit", function (event) {
        event.preventDefault();

        const recipient = document.getElementById("recipient").value;
        const subject = document.getElementById("subject").value;
        const messageContent = document.getElementById("messageContent").value;

        fetch("/api/messages/send", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({
                recipient: recipient,
                subject: subject,
                message: messageContent
            })
        })
        .then(response => response.json())
        .then(data => {
            console.log("📩 Odpowiedź serwera:", data);
            if (data.success) {
                Swal.fire("Wysłano!", "Twoja wiadomość została wysłana.", "success");
                newMessageModal.style.display = "none";
                newMessageForm.reset();
            } else {
                Swal.fire("Błąd", data.error || "Nie udało się wysłać wiadomości.", "error");
            }
        })
        .catch(error => {
            console.error("❌ Błąd sieci:", error);
            Swal.fire("Błąd", "Wystąpił problem z serwerem.", "error");
        });
    });
});




//////////////////////////////////////////////////////////////// NOTIFICATIONS END ///////////////////////////////////////////

document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById('changePasswordForm');
    if (!form) return;

    form.addEventListener('submit', async function (event) {
        event.preventDefault(); // Zatrzymanie domyślnego wysłania formularza

        const currentPassword = document.getElementById('settings-current-password').value;
        const newPassword = document.getElementById('settings-new-password').value;
        const confirmPassword = document.getElementById('settings-confirm-password').value;

        // Walidacja hasła
        if (newPassword.length < 8) {
            displayMessage('error', 'Nowe hasło musi mieć co najmniej 8 znaków.');
            return;
        }

        if (!/[A-Z]/.test(newPassword)) {
            displayMessage('error', 'Nowe hasło musi zawierać co najmniej jedną wielką literę.');
            return;
        }

        if (!/[0-9]/.test(newPassword)) {
            displayMessage('error', 'Nowe hasło musi zawierać co najmniej jedną cyfrę.');
            return;
        }

        if (newPassword !== confirmPassword) {
            displayMessage('error', 'Nowe hasło i potwierdzenie hasła muszą być takie same.');
            return;
        }

        // Wysłanie żądania AJAX do backendu
        try {
            const response = await fetch('/api/user/changePasswordInSettings', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                body: new URLSearchParams({
                    settingsCurrentPassword: currentPassword,
                    settingsNewPassword: newPassword
                })
            });

            const data = await response.json();

            if (!response.ok) {
                displayMessage('error', data.error || 'Wystąpił błąd.');
                return;
            }

            // Sukces: pokazanie popupu i przekierowanie
            displayMessage('success', data.success);

            setTimeout(() => {
                window.location.href = "/settings"; // Przekierowanie po sukcesie
            }, 2000);

        } catch (error) {
            displayMessage('error', 'Wystąpił błąd sieci. Spróbuj ponownie.');
        }
    });
});

function displayMessage(type, message) {
    Swal.fire({
        icon: type, // 'success' lub 'error'
        title: message,
        showConfirmButton: false,
        timer: 3000 // Popup znika po 3 sekundach
    });
}

    // 📌 Funkcja do formatowania daty w stylu "2 dni temu"
    function formatTimeAgo(date) {
        const now = new Date();
        const diffTime = Math.abs(now - date);
        const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24));

        if (diffDays === 0) return "Dzisiaj";
        if (diffDays === 1) return "Wczoraj";
        return `${diffDays} dni temu`;
    }
document.addEventListener("DOMContentLoaded", function () {
   const deleteSelectedBtn = document.getElementById("deleteMessageBtn");

   if (deleteSelectedBtn) {
       deleteSelectedBtn.addEventListener("click", function () {
           // 📌 Pobranie zaznaczonych checkboxów
           const selectedCheckboxes = document.querySelectorAll(".message-checkbox:checked");

           // 📌 Pobranie identyfikatorów wiadomości z dataset
           const messageIds = Array.from(selectedCheckboxes)
               .map(checkbox => checkbox.dataset.messageId)
               .filter(id => id !== undefined && id !== null && id !== "") // Usunięcie pustych wartości
               .map(id => Number(id)) // Konwersja na liczbę

           if (messageIds.length === 0) {
               alert("Nie wybrano żadnych wiadomości do usunięcia.");
               return;
           }

           console.log("🛠️ ID wiadomości do usunięcia:", messageIds);

           fetch("/api/messages/bulk-delete", {
               method: "DELETE",
               headers: { "Content-Type": "application/json" },
               body: JSON.stringify(messageIds)
           })
           .then(response => {
               if (!response.ok) throw new Error("Nie udało się usunąć zaznaczonych wiadomości.");

               // Usunięcie wiadomości z listy po sukcesie
               selectedCheckboxes.forEach(checkbox => checkbox.closest(".message-item").remove());
               displayMessage('success', 'Wiadomości zostały usunięte!');
               fetchNotificationCount();
           })
           .catch(error => console.error("Błąd usuwania wiadomości:", error));
       });
   }
});

document.addEventListener("DOMContentLoaded", function () {
    const searchInput = document.getElementById("search-userMessages");
    const messageList = document.getElementById("message-list");

    if (searchInput) { // ✅ Sprawdzenie, czy element istnieje
        searchInput.addEventListener("input", function () {
            const searchTerm = searchInput.value.toLowerCase();
            filterMessages(searchTerm);
        });
    } else {
        console.warn("Element #search-userMessages nie istnieje.");
    }

    function filterMessages(searchTerm) {
        const messages = document.querySelectorAll(".message-item");

        messages.forEach(message => {
            const titleElement = message.querySelector(".message-title");

            if (titleElement) { // ✅ Sprawdzenie, czy `message-title` istnieje
                const title = titleElement.textContent.toLowerCase();
                message.style.display = title.includes(searchTerm) ? "flex" : "none";
            }
        });
    }
});


document.addEventListener("DOMContentLoaded", function () {
    const searchInput = document.getElementById("search-user-messages");
    const clearIcon = document.getElementById("clear-search");
    const userRows = document.querySelectorAll("#user-table tbody tr"); // Pobranie wszystkich wierszy tabeli

    function filterUsers(searchTerm) {
        userRows.forEach(row => {
            const firstName = row.querySelector("td:nth-child(2)").textContent.toLowerCase();
            const lastName = row.querySelector("td:nth-child(3)").textContent.toLowerCase();
            const username = row.querySelector("td:nth-child(4)").textContent.toLowerCase();
            const email = row.querySelector("td:nth-child(5)").textContent.toLowerCase();

            if (firstName.includes(searchTerm) || lastName.includes(searchTerm) || username.includes(searchTerm) || email.includes(searchTerm)) {
                row.style.display = ""; // Pokaż wiersz, jeśli pasuje
            } else {
                row.style.display = "none"; // Ukryj wiersz, jeśli nie pasuje
            }
        });
    }

    if (searchInput) {
        searchInput.addEventListener("input", function () {
            clearIcon.style.display = searchInput.value ? "block" : "none";
            filterUsers(searchInput.value.toLowerCase()); // 🔹 Wywołujemy filtrowanie na bieżąco
        });
    }

    if (clearIcon) {
        clearIcon.addEventListener("click", function () {
            searchInput.value = "";
            clearIcon.style.display = "none";
            searchInput.focus();
            filterUsers(""); // 🔹 Resetujemy tabelę, pokazując wszystkie wiersze
        });
    }
});



document.addEventListener("DOMContentLoaded", function () {
    const messageList = document.getElementById("message-list");
    const receiveBtn = document.getElementById("receive-message");
    const sendBtn = document.getElementById("send-message");
    const selectAllCheckbox = document.getElementById("select-all-checkbox");
    const modal = document.getElementById("messageModal");
    const modalTitle = document.getElementById("modalTitle");
    const modalDate = document.getElementById("modalDate");
    const modalContent = document.getElementById("modalContent");
    const closeModal = document.getElementById("closeMessageBtn");
    const inboxTitle = document.querySelector(".inbox-header h2");

    function fetchMessages(type) {
        let url = type === "received" ? "/api/messages/received" : "/api/messages/sent";

        if (inboxTitle) {
            inboxTitle.textContent = type === "received" ? "Skrzynka odbiorcza" : "Elementy wysłane";
        }
        fetch(url)
            .then(response => {
                if (!response.ok) {
                    throw new Error(`HTTP error! Status: ${response.status}`);
                }
                return response.json();
            })
            .then(messages => {
                if (!Array.isArray(messages)) {
                    throw new Error("Niepoprawny format odpowiedzi API");
                }

                if (!messageList) {
                    console.warn("⚠️ Brak elementu #message-list w DOM. Przerywam renderowanie wiadomości.");
                    return;
                }

                messageList.innerHTML = ""; // Czyścimy listę

                if (messages.length === 0) {
                    messageList.innerHTML = `<p class="no-messages">Brak wiadomości.</p>`;
                    return;
                }

                messages.forEach((message, index) => {
                    const messageItem = document.createElement("div");
                    messageItem.classList.add("message-item");
                    messageItem.setAttribute("data-message-id", message.id);

                    if (message.read) {
                        messageItem.classList.add("read");
                    }

                    // 📌 Unikalny identyfikator dla checkboxa
                    const checkboxId = `message-checkbox-${index}`;

                    // 📌 Checkbox do zaznaczania wiadomości
                    const checkbox = document.createElement("input");
                    checkbox.type = "checkbox";
                    checkbox.classList.add("message-checkbox");
                    checkbox.id = checkboxId;
                    checkbox.dataset.messageId = message.id;

                    // 📌 Etykieta checkboxa
                    const checkboxLabel = document.createElement("label");
                    checkboxLabel.htmlFor = checkboxId;
                    checkboxLabel.classList.add("custom-checkbox");

                    // 📌 Kontener treści wiadomości
                    const messageContent = document.createElement("div");
                    messageContent.classList.add("message-content");

                    // 📌 Tytuł wiadomości (interaktywny)
                    const messageTitle = document.createElement("p");
                    messageTitle.classList.add("message-title");
                    messageTitle.textContent = message.subject;

                    // 📌 Kliknięcie w tytuł otworzy modal z treścią wiadomości
                    messageTitle.addEventListener("click", function (event) {
                        event.stopPropagation();
                        fetchMessageDetails(message.id);
                    });

                    // 📌 Data wiadomości
                    const messageDate = document.createElement("p");
                    messageDate.classList.add("message-date");
                    const messageDateTime = new Date(message.timestamp);
                    messageDate.textContent = `${formatTimeAgo(messageDateTime)} (${messageDateTime.toLocaleDateString()})`;

                    // 📌 Przycisk usuwania
                    const deleteButton = document.createElement("i");
                    deleteButton.classList.add("bx", "bx-trash", "delete-message");
                    deleteButton.addEventListener("click", function (event) {
                        event.stopPropagation(); // ⛔️ Zatrzymuje propagację, aby nie otworzyć modala
                        deleteMessage(message.id, messageItem);
                    });

                    // 📌 Dodanie elementów do drzewa DOM
                    messageContent.appendChild(messageTitle);
                    messageContent.appendChild(messageDate);

                    messageItem.appendChild(checkbox);
                    messageItem.appendChild(checkboxLabel);
                    messageItem.appendChild(messageContent);
                    messageItem.appendChild(deleteButton);

                    messageList.appendChild(messageItem);
                });
            })
            .catch(error => {
                console.error("Błąd pobierania wiadomości:", error);
            });
    }

    function fetchMessageDetails(messageId) {
        fetch(`/api/messages/${messageId}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error("Nie udało się pobrać treści wiadomości.");
                }
                return response.json();
            })
            .then(message => {
                if (!message) {
                    throw new Error("Nie znaleziono wiadomości.");
                }

                modalTitle.textContent = message.subject || "Bez tematu";
                modalDate.textContent = new Date(message.timestamp).toLocaleString();
                modalContent.textContent = message.content && message.content.trim() !== "" ? message.content : "Brak treści";
                modal.style.display = "flex";

                // 🔹 Oznacz wiadomość jako przeczytaną
                const messageItem = document.querySelector(`[data-message-id="${messageId}"]`);
                if (messageItem) {
                    messageItem.classList.add("read");
                    const title = messageItem.querySelector(".message-title");
                    if (title) {
                        title.style.fontWeight = "normal";
                        title.style.color = "#666";
                    }
                }

                // 🔹 Aktualizacja backendu - oznaczenie jako przeczytaną
                fetch(`/api/messages/mark-as-read/${messageId}`, {
                    method: "PUT",
                    headers: { "Content-Type": "application/json" }
                }).then(response => {
                    if (!response.ok) throw new Error("Nie udało się oznaczyć wiadomości jako przeczytanej.");
                    fetchNotificationCount();
                    return response.json();
                }).catch(error => console.error("Błąd oznaczania wiadomości jako przeczytanej:", error));
            })
            .catch(error => console.error("Błąd pobierania treści wiadomości:", error));
    }

    function deleteMessage(messageId, messageElement) {
        fetch(`/api/messages/${messageId}`, { method: "DELETE" })
            .then(response => {
                if (!response.ok) throw new Error("Nie udało się usunąć wiadomości.");
                messageElement.remove();
                displayMessage('success', 'Wiadomość została usunięta!');
            })
            .catch(error => console.error("Błąd usuwania wiadomości:", error));
    }

    // 📌 Obsługa zamykania modala
    if(closeModal) {
        closeModal.addEventListener("click", function () {
            modal.style.display = "none";
        });
    }

    window.addEventListener("click", function (event) {
        if (event.target === modal) {
            modal.style.display = "none";
        }
    });

    // 📌 Obsługa checkboxa „Zaznacz wszystkie”
    if (selectAllCheckbox) {
        selectAllCheckbox.addEventListener("change", function () {
            document.querySelectorAll(".message-checkbox").forEach(checkbox => {
                checkbox.checked = selectAllCheckbox.checked;
            });
        });
    }

    // 📌 Obsługa zmiany kategorii wiadomości
    if (receiveBtn) {
        receiveBtn.addEventListener("click", function () {
            fetchMessages("received");
        });
    }

    if (sendBtn) {
        sendBtn.addEventListener("click", function () {
            fetchMessages("sent");
        });
    }

    // 📌 Załaduj domyślnie odebrane wiadomości
       const currentPath = window.location.pathname;

       if (currentPath === "/notifications") {
        fetchMessages("received");
    }
});

document.addEventListener("DOMContentLoaded", function () {
    const teamForm = document.getElementById("teamForm");

    if (!teamForm) {
        console.warn("Błąd: Element #teamForm nie istnieje!");
        return; // Przerywamy działanie skryptu
    }

    teamForm.addEventListener("submit", function (event) {
        event.preventDefault(); // Zapobiegamy przeładowaniu strony

        const teamName = document.getElementById("teamName")?.value;

        if (!teamName) {
            console.error("Błąd: Pole 'teamName' jest puste!");
            return;
        }

        fetch("/api/teams/saveNewTeam", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({ teamName: teamName }) // Wysyłamy dane jako JSON
        })
        .then(response => {
            if (!response.ok) {
                return response.json().then(err => {
                    console.error("Serwer zwrócił błąd:", err);
                    throw new Error("Błąd podczas dodawania zespołu: " + (err.message || "Nieznany błąd"));
                });
            }
            return response.json();
        })
        .then(data => {
            displayMessage('success', 'Zespół został dodany!');
            document.getElementById("teamName").value = ""; // Wyczyść pole po dodaniu
        })
        .catch(error => console.error("Błąd:", error));
    });
});

document.addEventListener("DOMContentLoaded", function () {
    const sectionForm = document.getElementById("sectionForm");
    const sectionTeamSelect = document.getElementById("sectionTeam");
    const sectionNameInput = document.getElementById("sectionName");

    // 🔹 Załaduj zespoły do listy rozwijalnej
    function loadTeams() {
        const selectedTeam = sectionTeamSelect.value; // ✅ Pobierz aktualnie wybraną wartość przed nadpisaniem listy

        fetch("/api/teams") // Endpoint do pobierania zespołów
            .then(response => response.json())
            .then(data => {
                sectionTeamSelect.innerHTML = '<option value="">Wybierz zespół</option>';

                data.forEach(team => {
                    let option = document.createElement("option");
                    option.value = team.id;
                    option.textContent = team.teamName;
                    sectionTeamSelect.appendChild(option);
                });

                // ✅ Jeśli wcześniej użytkownik coś wybrał, przywróć to po załadowaniu listy
                if (selectedTeam) {
                    sectionTeamSelect.value = selectedTeam;
                }
            })
            .catch(error => console.error("Błąd ładowania zespołów:", error));
    }

    if(sectionForm) {
        // 🔹 Obsługa formularza dodawania sekcji
        sectionForm.addEventListener("submit", function (event) {
            event.preventDefault();

            const sectionName = sectionNameInput.value.trim();
            const teamId = sectionTeamSelect.value;

            if (!sectionName || !teamId) {
                alert("Wypełnij wszystkie pola!");
                return;
            }

            fetch("/api/sections/saveNewSection", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ sectionName: sectionName, teamId: teamId })
            })
            .then(response => {
                if (!response.ok) {
                    throw new Error("Błąd podczas dodawania sekcji.");
                }
                return response.json();
            })
            .then(() => {
                displayMessage('success', 'Sekcja została dodana!');
                sectionForm.reset();
            })
            .catch(error => console.error("Błąd:", error));
        });
    // 🔹 Otwórz modal i załaduj zespoły
    document.getElementById("sectionModal").addEventListener("click", loadTeams);
    }
});





console.log("Skrypt JSON został załadowany.");