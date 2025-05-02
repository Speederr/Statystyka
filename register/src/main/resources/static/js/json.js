
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

/////////////////////////////////////////////////////////////////////////////////////////////////////////////
document.addEventListener('DOMContentLoaded', async () => {
    try {
        const response = await fetch('/api/user/info');
        if (!response.ok) throw new Error('Błąd odpowiedzi z serwera');

        const user = await response.json();

        if (user?.firstName && user?.lastName) {
            document.querySelector('.user .bold').textContent = `${user.firstName} ${user.lastName}`;

            const roleNames = {
                1: 'Admin',
                2: 'Manager',
                3: 'Koordynator',
                4: 'Użytkownik'
            };

            document.querySelector('.role').textContent = roleNames[user.roleId] || 'Nieznana rola';
        }

    } catch (error) {
        console.error('Błąd podczas pobierania danych użytkownika:', error);
    }
});
/////////////////////////////////////////////////////////////////////////////////////////////
document.addEventListener("DOMContentLoaded", function () {
    if (!window.location.pathname.includes("/averageTime")) {
        return;
    }

    const editButton = document.getElementById("editButton");
    let isEditing = false;

    const minuteInputs = document.querySelectorAll(".time-input.minutes");
    const secondInputs = document.querySelectorAll(".time-input.seconds");

    function addIcon(input, iconClass) {
        const icon = document.createElement("i");
        icon.classList.add("bx", iconClass);
        input.parentElement.appendChild(icon);
    }

    function replaceIcon(input, newIconClass) {
        const icon = input.parentElement.querySelector("i");
        if (icon) {
            icon.classList.replace(icon.classList[1], newIconClass);
        }
    }

    function toggleEditMode(enable) {
        minuteInputs.forEach(input => {
            input.readOnly = !enable;
            input.classList.toggle("disabled-cursor", !enable);
            input.classList.toggle("enabled-cursor", enable);
            replaceIcon(input, enable ? "bx-edit" : "bx-check");
        });

        secondInputs.forEach(input => {
            input.readOnly = !enable;
            input.classList.toggle("disabled-cursor", !enable);
            input.classList.toggle("enabled-cursor", enable);
            replaceIcon(input, enable ? "bx-edit" : "bx-check");
        });
    }

    function syncMinutesAndSeconds(input, type) {
        const processId = input.id.split("_")[1];
        const minuteInput = document.getElementById(`minutes_${processId}`);
        const secondInput = document.getElementById(`seconds_${processId}`);

        if (type === "minutes") {
            secondInput.value = (parseFloat(minuteInput.value) * 60).toFixed(0);
        } else if (type === "seconds") {
            minuteInput.value = (parseFloat(secondInput.value) / 60).toFixed(2);
        }
    }

    minuteInputs.forEach(input => {
        input.addEventListener("input", () => syncMinutesAndSeconds(input, "minutes"));
        addIcon(input, "bx-check");
        input.classList.add("disabled-cursor");
    });

    secondInputs.forEach(input => {
        input.addEventListener("input", () => syncMinutesAndSeconds(input, "seconds"));
        addIcon(input, "bx-check");
        input.classList.add("disabled-cursor");
    });

    editButton.addEventListener("click", function () {
        if (!isEditing) {
            toggleEditMode(true);
            editButton.textContent = "Zapisz";
        } else {
            const updatePromises = Array.from(minuteInputs).map(input => {
                const processId = input.id.split("_")[1];
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
                        replaceIcon(input, "bx-check");
                    }
                    return response;
                });
            });

            Promise.all(updatePromises)
                .then(responses => {
                    if (responses.every(response => response.ok)) {
                        displayMessage("success", "Zapisano pomyślnie!");
                        setTimeout(() => location.reload(), 3000);
                    } else {
                        displayMessage("error", "Wystąpił błąd podczas zapisu.");
                    }
                })
                .catch(() => displayMessage("error", "Błąd podczas komunikacji z serwerem."));

            toggleEditMode(false);
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
        displayMessage("error", "Brak wprowadzonych danych!");
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
            displayMessage("success", "Zapisano wolumen!");
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
        exportButton.addEventListener("click", function () {
            const selectedTeamId = document.getElementById("timeTeam").value;
            const rows = document.querySelectorAll("#processTable tr");
            let filteredData = [];

            rows.forEach(row => {
                const processName = row.cells[0].textContent;
                const averageTimeMinutes = row.cells[1].querySelector("input").value;
                const averageTimeSeconds = row.cells[2].querySelector("input")
                    ? row.cells[2].querySelector("input").value
                    : (parseFloat(averageTimeMinutes) * 60).toFixed(0); // Jeśli nie ma inputa, obliczamy

                const processTeamId = row.getAttribute("data-team");

                // ✅ Filtrujemy dane na podstawie wybranego teamId
                if (!selectedTeamId || processTeamId === selectedTeamId) {
                    filteredData.push({
                        processName,
                        averageTimeMinutes,
                        averageTimeSeconds
                    });
                }
            });

            if (filteredData.length === 0) {
                alert("Brak procesów do eksportu dla wybranego zespołu.");
                return;
            }
                console.log("Dane do eksportu:", JSON.stringify(filteredData, null, 2));

            fetch("/export/processes", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(filteredData)

            })
            .then(response => response.blob())
            .then(blob => {
                const url = window.URL.createObjectURL(blob);
                const a = document.createElement("a");
                a.href = url;
                a.download = "filtered_processes.xlsx";
                document.body.appendChild(a);
                a.click();
                document.body.removeChild(a);
            })
            .catch(error => console.error("Błąd eksportu:", error));
        });
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
//        console.log("✅ Połączono z WebSocket!");

        // 🎯 Subskrybuj temat powiadomień
        stompClient.subscribe("/topic/notifications", function (message) {
//            console.log("📩 Nowa wiadomość!");

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
//                    console.warn("⚠️ Element #notification-counter nie został znaleziony w DOM.");
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
                    return;
//                    console.warn("fetchNotificationCount is not defined.");
                }
            })
            .catch(error => console.error("Błąd:", error));
        });
    } else {
        return;
//        console.warn("Element #markAsReadBtn nie istnieje.");
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

function displayMyMessage(type, message) {
    const popup = document.querySelector('.popup');

    const div = document.createElement('div');
    div.className = type === 'success' ? 'message' : 'error';
    div.innerHTML = `
        <span>
            <strong>${type === 'success' ? 'Sukces!' : 'Uwaga!'}</strong>
            <span>${message}</span>
        </span>
        <i class='bx bx-x-circle' onclick="this.parentElement.style.display='none'"></i>
    `;

    popup.appendChild(div);

    setTimeout(() => {
        div.style.display = 'none';
    }, 3000);
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
        return;
//        console.warn("Element #search-userMessages nie istnieje.");
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
//                    console.warn("⚠️ Brak elementu #message-list w DOM. Przerywam renderowanie wiadomości.");
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
//        console.warn("Błąd: Element #teamForm nie istnieje!");
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
document.addEventListener("DOMContentLoaded", function () {
    const startDateInput = document.getElementById("startDate");
    const endDateInput = document.getElementById("endDate");

    if (!startDateInput || !endDateInput) {
//        console.warn("Nie znaleziono pól daty!");
        return;
    }

    function validateDates() {
        if (!startDateInput.value || !endDateInput.value) {
//            console.warn("Jedno z pól daty jest puste!");
            return;
        }

        const startDate = new Date(startDateInput.value);
        const endDate = new Date(endDateInput.value);

        console.log(`Sprawdzanie dat: startDate = ${startDate.toISOString()}, endDate = ${endDate.toISOString()}`);

        if (endDate < startDate) {
            console.log("🔹 endDate jest wcześniejsze niż startDate → ustawiam oba na endDate");
            startDateInput.value = endDateInput.value;
        } else if (startDate > endDate) {
            console.log("🔹 startDate jest późniejsze niż endDate → ustawiam oba na startDate");
            endDateInput.value = startDateInput.value;
        }
    }

    startDateInput.addEventListener("change", validateDates);
    endDateInput.addEventListener("change", validateDates);
});

document.addEventListener("DOMContentLoaded", function () {
    // Pobieramy selektory dla filtrowania zespołów
    const teamSelect = document.getElementById("timeTeam");
    const processRows = document.querySelectorAll("#processTable tr");
    const userRows = document.querySelectorAll("#user-table tbody tr");

    function filterTableRows(selectElement, rows) {
        const selectedTeamId = selectElement.value;

        rows.forEach(row => {
            const rowTeamId = row.getAttribute("data-team"); // Pobiera team z atrybutu

            if (!selectedTeamId || rowTeamId === selectedTeamId) {
                row.style.display = "";
            } else {
                row.style.display = "none";
            }
        });
    }

    // Filtrowanie procesów
    if (teamSelect) {
        teamSelect.addEventListener("change", function () {
            filterTableRows(teamSelect, processRows);
        });
    }

});
////////////////////////////////////////// ZAPISYWANIE BACKLOGU ///////////////////////////////////////////////////////
document.addEventListener("DOMContentLoaded", function () {
    const backlogForm = document.getElementById("backlog-form");
    if (!backlogForm) return; // Jeśli nie ma formularza, zakończ funkcję

    backlogForm.addEventListener("submit", function (event) {
        event.preventDefault(); // Zatrzymaj domyślną akcję formularza

        const formData = new FormData(backlogForm); // Pobierz dane z formularza

        fetch("/backlog/save", {
            method: "POST",
            body: new URLSearchParams(formData), // Konwersja na format application/x-www-form-urlencoded
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            }
        })
        .then(response => {
            if (!response.ok) {
                throw new Error("Błąd podczas zapisywania backlogu!");
            }
            return response.json(); // Parsowanie JSON
        })
        .then(data => {
            displayMessage("success", "Pomyślnie zapisano backlog!");
            setTimeout(() => location.reload(), 3000);
        })
        .catch(error => displayMessage("error", "Wystąpił błąd podczas zapisywania backlogu!"));
    });
});

////////////////////////////////////// ZMIANA I ZAPISANIE OBECNOŚCI ///////////////////////////////////////////////////
document.addEventListener("change", function (event) {
    if (event.target.classList.contains("attendance-status")) {
        let userId = event.target.getAttribute("data-user-id");
        let newStatus = event.target.value;

        let requestData = new URLSearchParams();
        requestData.append("userId", userId);
        requestData.append("status", newStatus);

        fetch("/api/attendance/update", { // 🔹 Dostosuj URL do swojego API
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            body: requestData
        })
        .then(response => response.text())
        .then(data => {
            console.log("✅ Status zaktualizowany:", data);
            displayMessage("success", "Status zapisany!");
        })
        .catch(error => console.error("❌ Błąd aktualizacji statusu:", error));
    }
});
////////////////////////////////// ZAPISYWANIE WOLUMENU PO KLIKNIĘCIU NA PLUS + ///////////////////////////////////////
document.addEventListener("DOMContentLoaded", function () {
    // Pobranie userId z ukrytego pola input, jeśli istnieje
    const userIdElement = document.getElementById('userId');
    const userId = userIdElement ? userIdElement.value.trim() : null;

    if (!userId) {
//        console.warn("⚠️ Brak ID użytkownika – funkcjonalność zapisu może być ograniczona.");
        return; // Jeśli userId nie istnieje, przerywamy dalsze operacje, ale nie blokujemy całego skryptu
    }

    // Znajdź wszystkie przyciski "+"
    document.querySelectorAll(".add-efficiency").forEach(button => {
        button.addEventListener("click", function () {
            let input = this.closest(".form-floating").querySelector("input");
            let processId = input.getAttribute("data-process-id");
            let quantity = input.value;

            if (!quantity || quantity <= 0) {
                displayMessage("error", "Brak wprowadzonych danych!");
                return;
            }

            let requestData = new URLSearchParams();
            requestData.append("userId", userId);  // Używamy dynamicznego userId
            requestData.append("processId", processId);
            requestData.append("quantity", quantity);

            fetch("/api/saved-data/save-single", {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded"
                },
                body: requestData
            })
            .then(response => response.text())
            .then(data => {
                console.log(data);
                displayMessage("success", "Zapisano wolumen!");

                // 🔄 Odświeżenie strony po 3 sekundach
                setTimeout(() => {
                    location.reload();
                }, 3000);
            })
            .catch(error => console.error("❌ Błąd zapisu:", error));
        });
    });
});


////////////////////////////////////////////////////// WCZYTAJ WYKRES I FILTRUJ ///////////////////////////////////////
document.addEventListener("DOMContentLoaded", function () {
    if (!window.location.pathname.includes("/efficiency")) return;

    const sectionSelect = document.getElementById("sectionFilter");

function loadAvailability(sectionId = "all") {
    fetch(`/api/sections/availability/${sectionId}`)
        .then(response => response.json())
        .then(data => {
            const presentCount = data.presentCount || 0;
            const onLeaveCount = data.onLeaveCount || 0;
            const notLoggedCount = data.notLoggedCount || 0;
            const presentNames = data.presentEmployees || [];
            const onLeaveNames = data.onLeaveEmployees || [];
            const notLoggedNames = data.notLoggedEmployees || [];
            const totalEmployees = data.totalEmployees || (presentCount + onLeaveCount + notLoggedCount);

            const officeCount = data.officeCount || 0;
            const homeofficeCount = data.homeofficeCount || 0;
            const officeNames = data.officeEmployees || [];
            const homeofficeNames = data.homeofficeEmployees || [];

            updateRemoteChart(officeCount, homeofficeCount, officeNames, homeofficeNames);

            updateChart(
                presentCount,
                onLeaveCount,
                notLoggedCount,
                presentNames,
                onLeaveNames,
                notLoggedNames,
                totalEmployees
            );
        })
        .catch(error => console.error("❌ Błąd ładowania dostępności:", error));
}


    function populateSectionSelect() {
        fetch('/api/sections')
            .then(response => response.json())
            .then(data => {
                sectionSelect.innerHTML = `<option value="all">Wszyscy użytkownicy</option>`;
                data.forEach(section => {
                    let option = document.createElement("option");
                    option.value = section.id;
                    option.textContent = section.sectionName;
                    sectionSelect.appendChild(option);
                });

                loadAvailability("all");
            })
            .catch(error => console.warn(`⚠️ Błąd ładowania sekcji`));
    }

    sectionSelect.addEventListener("change", () => {
        const sectionId = sectionSelect.value;
        loadAvailability(sectionId);
    });

    populateSectionSelect();
});

document.addEventListener("DOMContentLoaded", function () {
  if (!window.location.pathname.includes("/details")) return;

  // --- ZMIENNE
  const sectionSelect = document.getElementById("sectionDetailsFilter");
  const checkboxContainer = document.getElementById("checkboxDropdown");
  const tableBody = document.querySelector("#employeeTable tbody");
  const selectedLabel = document.getElementById("selectedLabel");

  // --- FUNKCJE
  function getSelectedUserIds() {
    return Array.from(document.querySelectorAll(".employee-checkbox:checked")).map(cb => cb.value);
  }

  function renderUserTable(users) {
    tableBody.innerHTML = "";
    users.forEach(user => {
      const row = document.createElement("tr");
      row.innerHTML = `
        <td>
            <a href="/userDetails/${user.id}" title="Szczegóły użytkownika">
              <i class='bx bxs-user-detail'></i>
            </a>
        </td>
        <td>${user.firstName}</td>
        <td>${user.lastName}</td>
        <td>${user.efficiency !== null ? user.efficiency + "%" : "Brak danych"}</td>
        <td>${user.nonOperational !== null ? user.nonOperational + " godz." : "Brak danych"}</td>
        <td>${user.positionName ?? "Brak"}</td>
        <td>
          <select class="attendance-status" data-user-id="${user.id}">
            <option value="present" ${user.attendanceStatus === "present" ? "selected" : ""}>Obecny</option>
            <option value="leave" ${user.attendanceStatus === "leave" ? "selected" : ""}>Urlop</option>
            <option value="notloggedin" ${user.attendanceStatus === "notloggedin" ? "selected" : ""}>Niezalogowany</option>
          </select>
        </td>`;
      tableBody.appendChild(row);
    });
  }

  function handleFilterChange() {
    const sectionId = sectionSelect.value;
    const selectedUserIds = getSelectedUserIds();
    selectedLabel.textContent = selectedUserIds.length > 0 ? `${selectedUserIds.length} wybranych` : "Wszyscy pracownicy";

    if (selectedUserIds.length > 0) {
      fetch(`/api/user/filter-by-ids?ids=${selectedUserIds.join(",")}`)
        .then(res => res.json())
        .then(users => renderUserTable(users));
    } else if (sectionId !== "all") {
      fetch(`/api/user/by-section/${sectionId}`)
        .then(res => res.json())
        .then(users => renderUserTable(users));
    } else {
      fetch(`/api/user/all-users`)
        .then(res => res.json())
        .then(users => renderUserTable(users));
    }
  }

  function renderEmployeeCheckboxes(users) {
    checkboxContainer.innerHTML = "";
    users.forEach(user => {
      const wrapper = document.createElement("label");
      wrapper.innerHTML = `
        <input type="checkbox" class="employee-checkbox" value="${user.id}">
        ${user.firstName} ${user.lastName}
      `;
      checkboxContainer.appendChild(wrapper);
    });
  }

    function loadEmployeesForSection(sectionId) {
      if (sectionId === "all" || !sectionId) {
        checkboxContainer.innerHTML = "";
        handleFilterChange();
        return;
      }

      fetch(`/api/user/by-section/${sectionId}`)
        .then(res => res.json())
        .then(users => {
          renderEmployeeCheckboxes(users); // ładuje checkboxy (ID, imię, nazwisko)

          // teraz pobieramy pełne dane do tabeli
          const ids = users.map(u => u.id);

          return Promise.all(
            ids.map(id =>
              fetch(`/api/user/${id}`).then(res => res.json())
            )
          );
        })
        .then(fullUsers => {
          renderUserTable(fullUsers); // wyświetl pełne dane
        })
        .catch(err => console.error("❌ Błąd ładowania pracowników z pełnymi danymi:", err));
    }


  function populateSectionSelect() {
    fetch('/api/sections')
      .then(res => res.json())
      .then(data => {
        sectionSelect.innerHTML = `<option value="all">Wszyscy użytkownicy</option>`;
        data.forEach(section => {
          const option = document.createElement("option");
          option.value = section.id;
          option.textContent = section.sectionName;
          sectionSelect.appendChild(option);
        });
        loadEmployeesForSection("all");
      });
  }

  // --- EVENTY
  document.getElementById("customSelect").addEventListener("click", function (e) {
    checkboxContainer.style.display = checkboxContainer.style.display === "block" ? "none" : "block";
    e.stopPropagation();
  });

  document.addEventListener("click", function () {
    checkboxContainer.style.display = "none";
  });

  checkboxContainer.addEventListener("change", handleFilterChange);
  sectionSelect.addEventListener("change", () => loadEmployeesForSection(sectionSelect.value));

  document.querySelector(".btnContainer .mainBtn").addEventListener("click", function () {
    sectionSelect.value = "all";
    document.querySelectorAll(".employee-checkbox").forEach(cb => cb.checked = false);
    selectedLabel.textContent = "Kliknij, aby wybrać";
    handleFilterChange();
  });

  // START
  populateSectionSelect();
});
/////////////////////////////// EXPORT RAPORTU WYKONANIA //////////////////////////////////////////////
const exportBtn = document.getElementById("exportXlsxBtn");

if (exportBtn) {
    exportBtn.addEventListener("click", function () {
        const form = document.getElementById("reportForm");
        if (!form) return; // zabezpieczenie na wypadek braku formularza

        const startDate = form.startDate?.value;
        const endDate = form.endDate?.value;

        const params = new URLSearchParams();
        if (startDate) params.append("startDate", startDate);
        if (endDate) params.append("endDate", endDate);

        window.location.href = `/api/saved-data/get-report/export?${params.toString()}`;
    });
}


//////////////////////////////////////// PAGINACJA FILTROWANIE I WYŚWIETLANIE STRON ////////////////////////////////////
document.addEventListener("DOMContentLoaded", function () {
    if (!document.getElementById("report-table")) return;

    const pageSize = 100;
    let currentPage = 1;
    let allData = [];

    const tableBody = document.querySelector("#report-table tbody");
    const recordCountInfo = document.getElementById("recordCountInfo");
    const currentPageIndicator = document.getElementById("currentPageIndicator");

    const prevBtn = document.getElementById("prevPage");
    const nextBtn = document.getElementById("nextPage");

    const form = document.getElementById("reportForm");

    // 🔄 Pobieranie danych z API
    function fetchData(startDate = null, endDate = null) {
        const query = new URLSearchParams();
        if (startDate) query.append("startDate", startDate);
        if (endDate) query.append("endDate", endDate);

        fetch(`/api/saved-data/get-report?${query.toString()}`)
            .then(res => res.json())
            .then(data => {
                allData = data;
                currentPage = 1;
                renderTablePage();
            })
            .catch(err => console.error("❌ Błąd pobierania danych:", err));
    }

    // 🧾 Renderowanie strony tabeli
    function renderTablePage() {
        const start = (currentPage - 1) * pageSize;
        const end = start + pageSize;
        const pagedData = allData.slice(start, end);

        tableBody.innerHTML = "";
        pagedData.forEach(row => {
            const tr = document.createElement("tr");
            tr.innerHTML = `
                <td>${row.process}</td>
                <td>${row.quantity}</td>
                <td>${row.date}</td>
                <td>${row.employee}</td>
            `;
            tableBody.appendChild(tr);
        });

        recordCountInfo.textContent = `Wyświetlono ${Math.min(end, allData.length)} z ${allData.length} rekordów`;
        currentPageIndicator.textContent = `Strona ${currentPage} z ${Math.ceil(allData.length / pageSize)}`;

        prevBtn.disabled = currentPage === 1;
        nextBtn.disabled = end >= allData.length;
    }

    // 🧭 Nawigacja
    prevBtn.addEventListener("click", () => {
        if (currentPage > 1) {
            currentPage--;
            renderTablePage();
        }
    });

    nextBtn.addEventListener("click", () => {
        if ((currentPage * pageSize) < allData.length) {
            currentPage++;
            renderTablePage();
        }
    });

    // 📅 Obsługa formularza filtrowania
    form.addEventListener("submit", function (e) {
        e.preventDefault();
        const startDate = document.getElementById("startDate").value;
        const endDate = document.getElementById("endDate").value;
        fetchData(startDate, endDate);
    });

    // 🔄 Inicjalne pobranie danych
    fetchData();
});


document.addEventListener('DOMContentLoaded', function () {
    const allRadios = document.querySelectorAll('input[type="radio"][data-process-id]');
    const userId = document.getElementById('userId')?.value;

    allRadios.forEach(radio => {
        radio.addEventListener('change', function () {
            const processId = this.dataset.processId;
            const level = this.value;

            fetch('/matrix/saveSingle', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    userId: userId,
                    processId: processId,
                    level: level
                })
            })
            .then(response => {
                if (response.ok) {
                    displayMyMessage('success', 'Zapisano zmiany!');
                } else {
                    displayMyMessage('error', 'Błąd podczas zapisu.');
                }
            })
            .catch(error => {
                console.error(error);
                displayMyMessage('error', 'Błąd sieci.');
            });
        });
    });
});



document.addEventListener('DOMContentLoaded', function () {
    const userLevelsInput = document.getElementById('userLevelsData');

    if (userLevelsInput) {
        const userLevelsData = userLevelsInput.value;

        if (userLevelsData) {
            const userLevels = JSON.parse(userLevelsData);

            userLevels.forEach(levelEntry => {
                const processId = levelEntry.processId;
                const level = levelEntry.level;

                const selector = `input[name="level_${processId}"][value="${level}"]`;
                const radioButton = document.querySelector(selector);

                if (radioButton) {
                    radioButton.checked = true;
                    const td = radioButton.parentElement;
                    td.classList.add(`level-${level}`);
                }
            });
        }
    }
});

document.addEventListener('DOMContentLoaded', function () {
    const radios = document.querySelectorAll('input[type="radio"][data-process-id]');

    radios.forEach(radio => {
        radio.addEventListener('change', function () {
            const processId = this.dataset.processId;
            const level = this.value;

            // Usuń stare klasy z wszystkich TD w tym wierszu
            const row = this.closest('tr');
            for (let i = 1; i <= 5; i++) {
                const cell = row.children[i];
                cell.classList.remove('level-0', 'level-1', 'level-2', 'level-3', 'level-4');
            }

            // Dodaj nową klasę do aktywnej komórki
            const activeCell = this.parentElement;
            activeCell.classList.add(`level-${level}`);
        });
    });
});

