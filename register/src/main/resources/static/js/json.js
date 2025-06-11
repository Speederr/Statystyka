
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

        document.querySelectorAll(".time-checkbox").forEach(checkbox => {
            checkbox.disabled = !enable;
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
                const checkbox = document.getElementById(`checkbox_${processId}`);
                const isNonOperational = checkbox?.checked || false;

                return fetch(`/api/processes/update`, {
                    method: "PUT",
                    headers: {
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify({
                        id: processId,
                        averageTime: newTime,
                        nonOperational: isNonOperational
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

document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("form-process-container");

    if (form) {
        form.addEventListener("submit", function (e) {
            e.preventDefault();

            const formData = new FormData(form);

            fetch("/api/processes/saveNewProcess", {
                method: "POST",
                body: formData
            })
            .then(response => {
                if (!response.ok) throw new Error("Błąd zapisu");
                return response.text();
            })
            .then(message => {
                // ✅ Wyświetl komunikat
                displayMessage("success", "Pomyślnie dodano proces!");

                // ✅ Wyczyść formularz
                form.reset();

                // ✅ Zamknij modal
                document.getElementById("closeProcessModalBtn").click();

                // ✅ Odśwież stronę po krótkim czasie
                setTimeout(() => location.reload(), 2200);
            })
            .catch(error => {
                displayMessage("error", "Błąd podczas komunikacji z serwerem.");
            });
        });
    }
});



//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

async function calculateAndSave() {
  const userId = document.getElementById('userId').value.trim();
  if (!userId) {
    alert("Brak ID użytkownika.");
    return;
  }

  // Helper – odczyt zaznaczonego radiobuttona
  function getSelectedVolumeType() {
    const sel = document.querySelector('input[name="volumeType"]:checked');
    return sel ? sel.value : 'BASIC';
  }
  const selectedVolumeType = getSelectedVolumeType();

  // Pobranie czasu nadgodzin, jeśli dotyczy
   let overtimeMinutes = 0;
    if (selectedVolumeType !== 'BASIC') {
      const otInput = document.getElementById('overtimeMinutes');
      overtimeMinutes = otInput && +otInput.value > 0 ? +otInput.value : 0;

      if (!overtimeMinutes || overtimeMinutes <= 0) {
        displayMessage("error", "Uzupełnij czas nadgodzin przed dodaniem wolumenu!");
        return;
      }
    }

  // Zbierz dane z formularza
  const inputs = document.querySelectorAll('input[data-process-id]');
  const processVolumes = {};   // do obliczenia efektywności
  const dataList = [];         // do zapisu wolumenów
  const today = new Date().toISOString().split("T")[0];

  inputs.forEach(input => {
    const processId = Number(input.getAttribute('data-process-id'));
    const quantity  = input.value.trim();

    if (quantity !== "" && /^\d+$/.test(quantity) && +quantity > 0) {
      // do licznika efektywności
      processVolumes[processId] = +quantity;
      // do bulk save
      dataList.push({
        user_id:       +userId,
        process_id:    processId,
        quantity:      +quantity,
        todaysDate:    today,
        volumeType:    selectedVolumeType,
        overtimeMinutes // do zapisu, backend musi to odebrać
      });
    }
  });

  if (Object.keys(processVolumes).length === 0) {
    displayMessage("error", "Brak wprowadzonych danych!");
    return;
  }

  try {
      // 🔹 Krok 1: Oblicz efektywność – wysyłamy obiekt EfficiencyRequest
      const effRequest = {
        processVolumes: processVolumes,
        volumeType: selectedVolumeType,
        overtimeMinutes: overtimeMinutes
      };

      // 🔹 Krok 2: Zapisz wolumeny
      const saveResponse = await fetch("/api/saved-data/save", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(dataList)
      });
      if (saveResponse.ok) {
        displayMessage("success", "Zapisano wolumen!");
        clearInputFields();
        loadOvertimeSummary(userId);
        setTimeout(() => location.reload(), 3000);
      } else {
        throw new Error("Błąd podczas zapisywania wolumenów.");
      }

    const efficiencyResponse = await fetch(
      `/api/efficiency/calculate/${userId}`,
      {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(effRequest)
      }
    );
    if (!efficiencyResponse.ok) {
      throw new Error("Błąd podczas obliczania efektywności.");
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

                const checkbox = row.cells[3].querySelector("input[type='checkbox']");
                const nonOperational = checkbox?.checked || false;

                const processTeamId = row.getAttribute("data-team");

                // ✅ Filtrujemy dane na podstawie wybranego teamId
                if (!selectedTeamId || processTeamId === selectedTeamId) {
                    filteredData.push({
                        processName,
                        averageTimeMinutes,
                        averageTimeSeconds,
                        nonOperational
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
            document.getElementById('user-team').textContent = data.teamName || 'Brak danych';
            document.getElementById('user-section').textContent = data.sectionName || 'Brak danych';
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

document.getElementById("deleteButton")?.addEventListener("click", function (event) {
    event.preventDefault();

    const form = document.getElementById("users-form");
    const formData = new FormData(form);

    fetch("/api/user/deleteUsers", {
        method: "POST",
        body: formData
    })
    .then(res => res.text().then(msg => ({ status: res.status, msg })))
    .then(({ status, msg }) => {
        const type = status === 200 ? "success" : "error";
        displayMyMessage(type, msg);

        if (status === 200) {
            setTimeout(() => location.reload(), 2500); // opcjonalne odświeżenie
        }
    })
    .catch(err => {
        displayMyMessage("error", "❌ Błąd połączenia z serwerem.");
    });
});




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
    }, 5000);
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
////////////////////////////////////////////////////////// filtr na endpoincie notifications //////////////////////////////////////////////////////////
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
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////// filtr do endpointu adminPanel //////////////////////////////////////////////////////////
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
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


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
  const userIdElement = document.getElementById('userId');
  const userId = userIdElement ? userIdElement.value.trim() : null;
  if (!userId) return;

  // Helper – odczyt zaznaczonego radiobuttona
  function getSelectedVolumeType() {
    const sel = document.querySelector('input[name="volumeType"]:checked');
    return sel ? sel.value : 'BASIC';
  }

  document.querySelectorAll(".add-efficiency").forEach(button => {
    button.addEventListener("click", async function () {
      const input      = this.closest(".form-floating").querySelector("input");
      const processId  = Number(input.getAttribute("data-process-id"));
      const quantity   = Number(input.value);

      if (!quantity || quantity <= 0) {
        displayMessage("error", "Brak wprowadzonych danych!");
        return;
      }

      const volumeType       = getSelectedVolumeType();
      // Pobranie czasu nadgodzin, jeśli to nie BASIC
     let overtimeMinutes = 0;
      if (volumeType !== 'BASIC') {
        const otInput = document.getElementById('overtimeMinutes');
        overtimeMinutes = otInput ? Number(otInput.value) : 0;

        if (!overtimeMinutes || overtimeMinutes <= 0) {
          displayMessage("error", "Uzupełnij czas nadgodzin przed dodaniem wolumenu!");
          return;
        }
      }

      // Zbuduj payload jako JSON
      const payload = {
        userId,
        processId,
        quantity,
        volumeType,
        overtimeMinutes
      };

      try {
        const res = await fetch("/api/saved-data/save-single", {
          method: "POST",
          headers: {
            "Content-Type": "application/json"
          },
          body: JSON.stringify(payload)
        });
        if (!res.ok) throw new Error(`Status ${res.status}`);
        const text = await res.text();
        console.log(text);
        displayMessage("success", "Zapisano wolumen!");
        loadOvertimeSummary(userId);
        setTimeout(() => location.reload(), 3000);
      } catch (err) {
        console.error("❌ Błąd zapisu:", err);
        displayMessage("error", "Nie udało się zapisać danych.");
      }
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
                sectionSelect.innerHTML = `<option value="all">Sekcja</option>`;
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


// Załóżmy, że filter.js zawiera uniwersalne funkcje eksportowane globalnie
document.addEventListener("DOMContentLoaded", function () {
  if (!window.location.pathname.includes("/details")) return;

    const {
      getSelectedUserIds,
      getSelectedSectionIds,
      renderSectionCheckboxes,
      renderEmployeeCheckboxes,
      setupDropdownToggle,
      loadUsersBySectionIds,
      resetAllFilters
    } = window.filters;


  const sectionDropdown = document.getElementById("sectionDropdown");
  const sectionLabel = document.getElementById("sectionLabel");
  const sectionArrowIcon = document.getElementById("sectionArrowIcon");

  const checkboxContainer = document.getElementById("checkboxDropdown");
  const selectedLabel = document.getElementById("selectedLabel");
  const arrowIcon = document.getElementById("arrowIcon");

  const sectionCustomSelect = document.getElementById("sectionCustomSelect");
  const customSelect = document.getElementById("customSelect");
  const tableBody = document.querySelector("#employeeTable tbody");

  // 🔹 Renderowanie tabeli użytkowników
  function renderUserTable(users) {
    tableBody.innerHTML = "";
    users.forEach(user => {
      const row = document.createElement("tr");
            row.innerHTML = `
              <td><a href="/userDetails/${user.id}" title="Szczegóły użytkownika"><i class='bx bxs-user-detail'></i></a></td>
              <td>${user.firstName}</td>
              <td>${user.lastName}</td>
              <td>${user.efficiency != null ? user.efficiency + "%" : "Brak danych"}</td>
              <td>${user.nonOperational != null ? user.nonOperational + " godz." : "Brak danych"}</td>
              <td>${user.positionName || "Brak"}</td>
              <td>
                <select class="attendance-status" data-user-id="${user.id}">
                  <option value="present" ${user.attendanceStatus === "present" ? "selected" : ""}>Obecny</option>
                  <option value="leave" ${user.attendanceStatus === "leave" ? "selected" : ""}>Nieobecny</option>
                  <option value="notloggedin" ${user.attendanceStatus === "notloggedin" ? "selected" : ""}>Niezalogowany</option>
                </select>
              </td>`;
      tableBody.appendChild(row);
    });
  }

      // 🔹 Obsługa zmiany filtrów
    function handleFilterChange() {
      const sectionIds = getSelectedSectionIds();
      const userIds = getSelectedUserIds();

      sectionLabel.textContent = sectionIds.length > 0
        ? `${sectionIds.length} wybranych`
        : "Sekcja";

      selectedLabel.textContent = userIds.length > 0
        ? `${userIds.length} wybranych`
        : "Pracownik";

      if (userIds.length > 0) {
        // 🟢 Wyświetl tylko zaznaczonych pracowników
        fetch(`/api/user/filter-by-ids?ids=${userIds.join(",")}`)
          .then(res => res.json())
          .then(renderUserTable);
      } else if (sectionIds.length > 0) {
        // 🟡 Żadne checkboxy nie zaznaczone, ale są sekcje -> odśwież listę pracowników z tych sekcji, ale NIE zaznaczaj żadnych checkboxów!
        fetch(`/api/user/table/by-sections?ids=${sectionIds.join(",")}`)
          .then(res => res.json())
          .then(renderUserTable);
      } else {
        // 🔴 Nic nie zaznaczone – pokazujemy pustą tabelę
        renderUserTable([]);
      }
    }


// 🔹 Przycisk "Wyczyść"
document.querySelector(".btnContainer .mainBtn").addEventListener("click", () => {
  window.filters.resetAllFilters(
    sectionDropdown,
    checkboxContainer,
    sectionLabel,
    selectedLabel,
    renderUserTable
  );
});



  // === Obsługa filtrowania sekcji i pracowników ===

  sectionDropdown.addEventListener("change", async () => {
    const sectionIds = getSelectedSectionIds();
    const sectionLabel = document.getElementById("sectionLabel");
    const selectedLabel = document.getElementById("selectedLabel");
    const checkboxContainer = document.getElementById("checkboxDropdown");

    // 🔹 Aktualizacja etykiety sekcji
    sectionLabel.textContent = sectionIds.length > 0
      ? `${sectionIds.length} wybranych`
      : "Sekcja";

    // 🔹 Pobranie użytkowników z sekcji (do checkboxów)
    const sectionUsersArrays = await Promise.all(
      sectionIds.map(id => fetch(`/api/user/by-section/${id}`).then(res => res.json()))
    );
    const flatUsers = sectionUsersArrays.flat();
    const uniqueUsers = Array.from(new Map(flatUsers.map(u => [u.id, u])).values());
    const allowedUserIds = new Set(uniqueUsers.map(u => String(u.id)));

    // 🔹 Zostaw tylko zaznaczonych użytkowników z wybranych sekcji
    const selectedUserIds = getSelectedUserIds().filter(id => allowedUserIds.has(id));

    // 🔹 Odśwież listę checkboxów z zachowaniem zaznaczeń
    renderEmployeeCheckboxes(checkboxContainer, uniqueUsers);
    document.querySelectorAll(".employee-checkbox").forEach(cb => {
      cb.checked = selectedUserIds.includes(cb.value);
    });

    // 🔹 Aktualizacja etykiety pracowników
    selectedLabel.textContent = selectedUserIds.length > 0
      ? `${selectedUserIds.length} wybranych`
      : "Pracownik";

    // 🔹 Renderuj odpowiednich użytkowników w tabeli
    if (selectedUserIds.length > 0) {
      fetch(`/api/user/filter-by-ids?ids=${selectedUserIds.join(",")}`)
        .then(res => res.json())
        .then(renderUserTable);
    } else if (sectionIds.length > 0) {
      Promise.all(
        sectionIds.map(id => fetch(`/api/user/table/by-section/${id}`).then(res => res.json()))
      ).then(dataArrays => {
        const fullUsers = dataArrays.flat();
        const uniqueFullUsers = Array.from(new Map(fullUsers.map(u => [u.id, u])).values());
        renderUserTable(uniqueFullUsers);
      });
    } else {
      fetch(`/api/user/all-users`)
        .then(res => res.json())
        .then(renderUserTable);
    }
  });


  checkboxContainer.addEventListener("change", handleFilterChange);

  setupDropdownToggle({ trigger: sectionCustomSelect, dropdown: sectionDropdown, arrow: sectionArrowIcon });
  setupDropdownToggle({ trigger: customSelect, dropdown: checkboxContainer, arrow: arrowIcon });

  // 🔹 Inicjalizacja
fetch('/api/sections')
  .then(res => res.json())
  .then(sections => {
    renderSectionCheckboxes(sectionDropdown, sections);

    // Reset etykiety sekcji
    sectionLabel.textContent = "Sekcja";

    // Faza inicjalizacji
    window.filters.__initPhaseDone = false;

      loadUsersBySectionIds([], checkboxContainer, selectedLabel, renderUserTable, { preserveCheckboxState: false });

    window.filters.__initPhaseDone = true;
  });

});

document.addEventListener("DOMContentLoaded", function () {
  if (!window.location.pathname.includes("/chartDetails")) return;

  const {
    getSelectedUserIds,
    getSelectedSectionIds,
    getSelectedProcessNames,
    renderSectionCheckboxes,
    renderEmployeeCheckboxes,
    renderProcessCheckboxes,
    loadUsersBySectionIdsToChart,
    setupDropdownToggle,
    resetAllFiltersOnChart
  } = window.filters;

  // === Obsługa filtrowania sekcji, pracowników i procesów ===
    const sectionDropdownChart = document.getElementById("sectionDropdownChart");
    const sectionLabelChart = document.getElementById("sectionLabel");
    const sectionArrowIconChart = document.getElementById("sectionArrowIcon");

    const employeeDropdownChart = document.getElementById("employeeDropdownChart");
    const selectedLabelChart = document.getElementById("selectedLabel");
    const employeeArrowIconChart = document.getElementById("arrowIcon");

    const processDropdownChart = document.getElementById("processDropdownChart");
    const processLabelChart = document.getElementById("processLabel");
    const processArrowIconChart = document.getElementById("processArrow");

    setupDropdownToggle({ trigger: document.getElementById("sectionCustomSelect"), dropdown: sectionDropdownChart, arrow: sectionArrowIconChart });
    setupDropdownToggle({ trigger: document.getElementById("customSelect"), dropdown: employeeDropdownChart, arrow: employeeArrowIconChart });
    setupDropdownToggle({ trigger: document.getElementById("processSelect"), dropdown: processDropdownChart, arrow: processArrowIconChart });

    fetch('/api/sections')
      .then(res => res.json())
      .then(sections => {
        renderSectionCheckboxes(sectionDropdownChart, sections);
        sectionLabelChart.textContent = "Sekcja";
        loadUsersBySectionIdsToChart([], employeeDropdownChart, selectedLabelChart);
      });

    sectionDropdownChart.addEventListener("change", async () => {
      const sectionIds = getSelectedSectionIds();

      sectionLabelChart.textContent = sectionIds.length > 0
        ? `${sectionIds.length} wybranych`
        : "Sekcja";

      const sectionUsersArrays = await Promise.all(
        sectionIds.map(id => fetch(`/api/user/by-section/${id}`).then(res => res.json()))
      );
      const flatUsers = sectionUsersArrays.flat();
      const uniqueUsers = Array.from(new Map(flatUsers.map(u => [u.id, u])).values());
      const allowedUserIds = new Set(uniqueUsers.map(u => String(u.id)));

      const selectedUserIds = getSelectedUserIds().filter(id => allowedUserIds.has(id));

      renderEmployeeCheckboxes(employeeDropdownChart, uniqueUsers);
      document.querySelectorAll(".employee-checkbox").forEach(cb => {
        cb.checked = selectedUserIds.includes(cb.value);
      });

      selectedLabelChart.textContent = selectedUserIds.length > 0
        ? `${selectedUserIds.length} wybranych`
        : "Pracownik";

      const reportDate = document.getElementById("reportDate")?.value;
      const processIds = getSelectedProcessNames().map(Number); // 🔄 zamień na ID

      renderChart({
        date: reportDate,
        sectionIds,
        userIds: selectedUserIds,
        processIds
      });
    });

    employeeDropdownChart.addEventListener("change", () => {
      const reportDate = document.getElementById("reportDate")?.value;
      const sectionIds = getSelectedSectionIds();
      const userIds = getSelectedUserIds();
      const processIds = getSelectedProcessNames().map(Number);

      selectedLabelChart.textContent = userIds.length > 0
        ? `${userIds.length} wybranych`
        : "Pracownik";

      renderChart({
        date: reportDate,
        sectionIds: sectionIds.length > 0 ? sectionIds : undefined,
        userIds,
        processIds
      });
    });

    fetch('/api/processes/by-logged-user')
      .then(res => res.json())
      .then(processes => {
        renderProcessCheckboxes(processDropdownChart, processes);
        processLabelChart.textContent = "Proces";
      })
      .catch(err => {
        console.error("❌ Błąd pobierania procesów:", err);
        processLabelChart.textContent = "Błąd";
      });


    processDropdownChart.addEventListener("change", () => {
      const reportDate = document.getElementById("reportDate")?.value;
      const sectionIds = getSelectedSectionIds();
      const userIds = getSelectedUserIds();
      const processIds = getSelectedProcessNames().map(Number);

      processLabelChart.textContent = processIds.length > 0
        ? `${processIds.length} wybranych`
        : "Proces";

      renderChart({
        date: reportDate,
        sectionIds: sectionIds.length > 0 ? sectionIds : undefined,
        userIds,
        processIds
      });
    });

    // 🔹 Przycisk "Wyczyść"
     document.getElementById("clearFiltersBtn").addEventListener("click", () => {
      window.filters.resetAllFiltersOnChart(
        sectionDropdownChart,
        employeeDropdownChart,
        processDropdownChart,
        sectionLabelChart,
        selectedLabelChart,
        processLabelChart,
        () => renderChart({ date: new Date().toISOString().split("T")[0] })
      );
    });


});



/////////////////////////////// EXPORT RAPORTU WYKONANIA //////////////////////////////////////////////
const exportBtn = document.getElementById("exportXlsxBtn");

if (exportBtn) {
  exportBtn.addEventListener("click", function () {
    const form = document.getElementById("reportForm");
    if (!form) return;

    const startDate = form.startDate?.value;
    const endDate = form.endDate?.value;

    const selectedProcesses = Array.from(document.querySelectorAll('#processDropdown input:checked')).map(cb => cb.value);
    const selectedUsers = Array.from(document.querySelectorAll('#userDropdown input:checked')).map(cb => cb.value);
    const selectedOvertime = Array.from(document.querySelectorAll('#overtimeDropdown input:checked')).map(cb => cb.value);

    const params = new URLSearchParams();
    if (startDate) params.append("startDate", startDate);
    if (endDate) params.append("endDate", endDate);
    if (selectedProcesses.length > 0) params.append("processes", selectedProcesses.join(","));
    if (selectedUsers.length > 0) params.append("users", selectedUsers.join(","));
    if (selectedOvertime.length > 0) params.append("overtime", selectedOvertime.join(","));

    // 🔽 Pobranie pliku z serwera
    window.location.href = `/api/saved-data/get-report/export?${params.toString()}`;
  });
}


document.getElementById("exportBacklogBtn")?.addEventListener("click", (e) => {
    e.preventDefault();

    const startDate = document.getElementById("startDate")?.value;
    const endDate = document.getElementById("endDate")?.value;

    const params = new URLSearchParams();
    if (startDate) params.append("startDate", startDate);
    if (endDate) params.append("endDate", endDate);

    window.location.href = `/backlog/export?${params.toString()}`;
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


//////////////////////////////////////// PAGINACJA, FILTROWANIE I WYŚWIETLANIE STRON ////////////////////////////////////
// 🔁 Zmienne globalne
const pageSize = 100;
let currentPage = 1;
let allData = [];
let filteredData = [];

const tableBody            = document.querySelector("#report-table tbody");
const recordCountInfo      = document.getElementById("recordCountInfo");
const currentPageIndicator = document.getElementById("currentPageIndicator");
const prevBtn              = document.getElementById("prevPage");
const nextBtn              = document.getElementById("nextPage");
const clearBtn             = document.getElementById("clearBtn");

// 🧾 Renderowanie strony tabeli
function renderTablePage() {
  const start = (currentPage - 1) * pageSize;
  const end   = start + pageSize;
  const pageSlice = filteredData.slice(start, end);

    if (!tableBody) {
    return;
    }

  tableBody.innerHTML = "";
  pageSlice.forEach(row => {
    const tr = document.createElement("tr");
    tr.innerHTML = `
      <td>${row.processName}</td>
      <td>${row.quantity}</td>
      <td>${row.todaysDate}</td>
      <td>${row.username}</td>
      <td>${row.volumeType}</td>
      <td>${row.overtimeMinutes}</td>
    `;
    tableBody.appendChild(tr);
  });

  // ⬅️ Te linie MUSZĄ być tutaj
  const from = filteredData.length === 0 ? 0 : start + 1;
  const to   = Math.min(end, filteredData.length);
  recordCountInfo.textContent      = `Wyświetlono ${from}–${to} z ${filteredData.length} rekordów`;
  currentPageIndicator.textContent = `Strona ${currentPage} z ${Math.ceil(filteredData.length / pageSize)}`;
  prevBtn.disabled = currentPage === 1;
  nextBtn.disabled = end >= filteredData.length;
}

// 🔄 Pobieranie danych z API
function fetchData(startDate = null, endDate = null) {
  const query = new URLSearchParams();
  if (startDate) query.append("startDate", startDate);
  if (endDate)   query.append("endDate", endDate);

  fetch(`/api/saved-data/get-report?${query.toString()}`)
    .then(res => res.json())
    .then(data => {
      allData      = data;
      filteredData = [...allData];
      currentPage  = 1;
      renderTablePage();
      loadUniqueFiltersFromTable(); // odśwież dropdowny filtrów
    })
    .catch(err => console.error("❌ Błąd pobierania danych:", err));
}

// 🔍 Zastosowanie filtrów na allData → filteredData
function applyFilters() {
  const selProc     = Array.from(document.querySelectorAll('#processDropdown input:checked')).map(cb => cb.value);
  const selUsers    = Array.from(document.querySelectorAll('#userDropdown   input:checked')).map(cb => cb.value);
  const selOvertime = Array.from(document.querySelectorAll('#overtimeDropdown input:checked')).map(cb => cb.value);

  filteredData = allData.filter(item =>
    (selProc.length     === 0 || selProc.includes(item.processName)) &&
    (selUsers.length    === 0 || selUsers.includes(item.username))    &&
    (selOvertime.length === 0 || selOvertime.includes(item.volumeType))
  );

  currentPage = 1;
  renderTablePage();
}

// 🔽 Inicjalizacja dropdownów z checkboxami
function setupDropdown(selectId, labelId, arrowId, dropdownId) {
  const select   = document.getElementById(selectId);
  const label    = document.getElementById(labelId);
  const arrow    = document.getElementById(arrowId);
  const dropdown = document.getElementById(dropdownId);
  if (!select || !label || !arrow || !dropdown) return;

  label.dataset.default = label.textContent;

  select.addEventListener("click", e => {
    dropdown.classList.toggle("show");
    arrow.classList.toggle("rotate");
    e.stopPropagation();
  });

  document.addEventListener("click", e => {
    if (!select.contains(e.target)) {
      dropdown.classList.remove("show");
      arrow.classList.remove("rotate");
    }
  });

  dropdown.addEventListener("change", () => {
    const cnt = dropdown.querySelectorAll("input:checked").length;
    label.textContent = cnt > 0 ? `${cnt} wybranych` : label.dataset.default;
    applyFilters();
  });

  // blokada zamykania dropdownu gdy klik na checkbox/label
  dropdown.addEventListener("click", e => {
    if (
      e.target.matches('input[type="checkbox"]') ||
      e.target.closest('label')?.querySelector('input[type="checkbox"]')
    ) {
      e.stopPropagation();
    }
  });
}

// 🔄 Budowa listy opcji filtrów (procesy, użytkownicy) i statycznego overtime
function loadUniqueFiltersFromTable() {
  const rows      = document.querySelectorAll("#report-table tbody tr");
  const processSet = new Set();
  const userSet    = new Set();

  rows.forEach(row => {
    const cells = row.querySelectorAll("td");
    if (cells.length === 6) {
      processSet.add(cells[0].textContent.trim());
      userSet.add(cells[3].textContent.trim());
    }
  });

  renderFilterCheckboxes("#processDropdown", Array.from(processSet));
  renderFilterCheckboxes("#userDropdown", Array.from(userSet));

  // statycznie rodzaje czasu pracy
  const overtimeOptions = [
    "Podstawowy czas pracy",
    "Nadgodziny płatne",
    "Nadgodziny do odbioru",
    "Odebrane częściowo"
  ];
  renderFilterCheckboxes("#overtimeDropdown", overtimeOptions);

  // przywrócenie etykiet i defaultów
  document.getElementById("processLabel").textContent  = "Proces";
  document.getElementById("processLabel").dataset.default  = "Proces";
  document.getElementById("userLabel").textContent     = "Pracownik";
  document.getElementById("userLabel").dataset.default     = "Pracownik";
  document.getElementById("overtimeLabel").textContent = "Rodzaj czasu pracy";
  document.getElementById("overtimeLabel").dataset.default = "Rodzaj czasu pracy";
}

// 📋 Pomocnicza: wyrenderuj checkboxy w danym kontenerze
function renderFilterCheckboxes(containerSelector, values) {
  const container = document.querySelector(containerSelector);
  if (!container) return;
  container.innerHTML = "";
  values.forEach(value => {
    const label = document.createElement("label");
    label.innerHTML = `<input type="checkbox" value="${value}"> ${value}`;
    container.appendChild(label);
  });
}

// 🚀 Po załadowaniu DOM
document.addEventListener("DOMContentLoaded", () => {
  if (!document.getElementById("report-table")) return;

  // paginacja
  prevBtn.addEventListener("click", () => {
    if (currentPage > 1) {
      currentPage--;
      renderTablePage();
    }
  });
  nextBtn.addEventListener("click", () => {
    if (currentPage * pageSize < filteredData.length) {
      currentPage++;
      renderTablePage();
    }
  });

  // czyszczenie filtrów
  clearBtn?.addEventListener("click", () => {
    document.querySelectorAll(
      "#processDropdown input, #userDropdown input, #overtimeDropdown input"
    ).forEach(cb => cb.checked = true);

    document.getElementById("processLabel").textContent  = "Proces";
    document.getElementById("userLabel").textContent     = "Pracownik";
    document.getElementById("overtimeLabel").textContent = "Rodzaj czasu pracy";

    // reset dat w formularzu jeżeli masz:
    const endDateInput   = document.getElementById("endDate");
    const startDateInput = document.getElementById("startDate");
    const today = new Date();
    const sevenDaysAgo = new Date(today);
    sevenDaysAgo.setDate(today.getDate() - 6);
    const toISO = d => d.toISOString().split("T")[0];
    if (startDateInput) startDateInput.value = toISO(sevenDaysAgo);
    if (endDateInput)   endDateInput.value   = toISO(today);

    fetchData(toISO(sevenDaysAgo), toISO(today));
  });

  // dropdowny filtrów
  setupDropdown("processSelect",  "processLabel",  "processArrow",  "processDropdown");
  setupDropdown("userSelect",     "userLabel",     "userArrow",     "userDropdown");
  setupDropdown("overtimeSelect", "overtimeLabel", "overtimeArrow", "overtimeDropdown");

  // formularz dat
  document.getElementById("reportForm")?.addEventListener("submit", e => {
    e.preventDefault();
    const startDate = document.getElementById("startDate").value;
    const endDate   = document.getElementById("endDate").value;
    fetchData(startDate, endDate);
  });

  // pierwsze pobranie danych
  fetchData();
});

function getSelectedVolumeType() {
  const sel = document.querySelector('input[name="volumeType"]:checked');
  return sel ? sel.value : 'BASIC';
}
/////////////////////////////// index header //////////////////////////////////////////////////////
document.addEventListener("DOMContentLoaded", () => {
  const radios = document.querySelectorAll('input[name="volumeType"]');
  const overtimeSection = document.getElementById("overtimeInputSection");
  const overtimeInput   = document.getElementById("overtimeMinutes");
  const confirmBtn      = document.getElementById("confirmOvertime");
  const datePicker      = document.getElementById("overtimeDate");
  // przechowujemy wybrany typ (domyślnie BASIC)
  let selectedVolumeType = "BASIC";
  let customOvertime     = 0;

  if (!radios.length || !overtimeSection) return;

  // na starcie chowamy sekcję nadgodzin
  overtimeSection.classList.add("hidden");

  // mapa komunikatów dla każdego typu
  const messages = {
    BASIC:            "Dodano wolumen!",
    OVERTIME_PAID:    "Dodano nadgodziny płatne",
    OVERTIME_OFF:     "Dodano nadgodziny do odbioru",
    DEDUCT_PARTIAL:   "Odebrano nadgodziny",
    DEDUCT_FULL_DAY: "Odebrany dzień w całości: "
  };

  // 1) Obsługa zmiany radio
  radios.forEach(radio => {
    radio.addEventListener("change", function() {
      selectedVolumeType = this.value;  // zapamiętujemy typ

      if (!overtimeSection || !overtimeInput) return;

      if (this.value === "BASIC") {
        // BASIC: chowamy input + odblokowujemy
        overtimeSection.classList.add("hidden");
        overtimeSection.classList.remove("show");
        overtimeInput.disabled = false;
        enableProcessInputs();
        customOvertime = 0;

      } else {
        // inne typy: pokazujemy input
        overtimeSection.classList.remove("hidden");
        overtimeSection.classList.add("show");

        // jeśli pełny dzień – zablokuj i ustaw na 480
        if (this.value === "DEDUCT_FULL_DAY") {
          overtimeInput.value = 480;
          overtimeInput.disabled = true;
        } else {
          // nadgodziny lub częściowe odejmowanie – odblokuj input
          overtimeInput.disabled = false;
          overtimeInput.value = ""; // wyczyść poprzednią wartość
        }

        disableProcessInputs();
      }
    });
  });

  // 2) Potwierdzenie
  if (confirmBtn && overtimeInput) {
      confirmBtn.addEventListener("click", async () => {
        const userId = Number(document.getElementById("userId")?.value);
        const date   = datePicker?.value;

      if (selectedVolumeType === "DEDUCT_FULL_DAY") {

            const minutes = 480;

        // czekamy na wynik modalnego zapytania
        const ok = await askConfirmation(
          `Czy na pewno chcesz odebrać ${minutes} min w dniu ${date}?`
        );
        if (!ok) return;

        try {
          const res = await fetch("/api/saved-data/deduct-full-day", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ userId, date })
          });
          if (!res.ok) throw new Error(res.statusText);
          const fullMsg = `${messages.DEDUCT_FULL_DAY}${date}`;
          displayMessage("success", fullMsg);
          enableProcessInputs();
          await loadOvertimeSummary(userId, date);
          setTimeout(() => location.reload(), 3000);
        } catch (err) {
          console.error("Fetch error:", err);
          displayMessage("error", "Wystąpił błąd przy odejmowaniu pełnego dnia.");
        }
        return;
      }

      // dla pozostałych typów – normalny flow z minutami
      const val = Number(overtimeInput.value);
      if (!val || val <= 0) {
        displayMessage("error", "Wpisz poprawny czas nadgodzin w minutach.");
        return;
      }
      customOvertime = val;
      enableProcessInputs();
      await loadOvertimeSummary(userId, date);
      const baseMsg = messages[selectedVolumeType] || "Zapisano";
      const fullMsg = `${baseMsg}: ${customOvertime} min`;
      displayMessage("success", fullMsg);
    });
  }

  function disableProcessInputs() {
    document.querySelectorAll("input[data-process-id]").forEach(i => i.disabled = true);
    document.querySelectorAll(".add-efficiency").forEach(icon => {
      icon.classList.add("disabled");
    });
    const saveAllBtn = document.getElementById("saveAllBtn");
    if (saveAllBtn) {
      saveAllBtn.classList.add("disabled");
      saveAllBtn.style.pointerEvents = "none";
      saveAllBtn.style.opacity = "0.5";
      saveAllBtn.style.cursor = "default";
    }
  }

  function enableProcessInputs() {
    document.querySelectorAll("input[data-process-id]").forEach(i => i.disabled = false);
    document.querySelectorAll(".add-efficiency").forEach(icon => {
      icon.classList.remove("disabled");
      icon.style.pointerEvents = "";
      icon.style.opacity = "";
    });
    const saveAllBtn = document.getElementById("saveAllBtn");
    if (saveAllBtn) {
      saveAllBtn.classList.remove("disabled");
      saveAllBtn.style.pointerEvents = "";
      saveAllBtn.style.opacity = "";
      saveAllBtn.style.cursor = "";
    }
  }
});


document.addEventListener("DOMContentLoaded", () => {
  const overtimeInput = document.getElementById("overtimeMinutes");
  if (!overtimeInput) return;  // jeśli nie ma pola, wychodzimy

  // Blokuj kropkę i przecinek przy naciskaniu klawiszy
  overtimeInput.addEventListener("keydown", (e) => {
    if (e.key === "." || e.key === ",") {
      e.preventDefault();
    }
  });

  // Przy wklejaniu/usuwaniu – usuwaj kropki i przecinki
  overtimeInput.addEventListener("input", () => {
    const cleaned = overtimeInput.value.replace(/[\.,]/g, "");
    if (cleaned !== overtimeInput.value) {
      overtimeInput.value = cleaned;
    }
  });
});

document.addEventListener("DOMContentLoaded", () => {
  const container = document.querySelector(".vts-container");
  const selector  = document.querySelector(".volume-type-selector");
  const minutesInput  = document.getElementById("overtimeMinutes");

  // Jeżeli brak głównego kontenera, nic nie robimy
  if (!container || !selector || !minutesInput) return;

  // Nagłówek dropdownu
  const header = container.querySelector(".vts-header");
  if (header) {
    header.addEventListener("click", () => {
      container.classList.toggle("expanded");
    });
  }

  // Radio – pokazanie/ukrycie daty przy DEDUCT_PARTIAL
  const radios = selector.querySelectorAll('input[name="volumeType"]');
  radios.forEach(radio => {
    radio.addEventListener("change", () => {
      if (radio.value === "DEDUCT_FULL_DAY") {
        selector.classList.add("pickup");
      } else {
        selector.classList.remove("pickup");
      }
    });
  });
});

document.addEventListener("DOMContentLoaded", () => {
  const wrapper = document.querySelector(".process-list-wrapper");
  if (!wrapper) return;

  document
    .querySelectorAll('input[name="volumeType"]')
    .forEach(radio => {
      radio.addEventListener("change", () => {
        if (radio.value === "BASIC") {
          wrapper.classList.remove("collapsed");
        } else {
          wrapper.classList.add("collapsed");
        }
      });
    });
});

document.addEventListener("DOMContentLoaded", () => {
  const radios = document.querySelectorAll('input[name="volumeType"]');
  const overtimeContainer = document.getElementById("overtimeContainer");

  radios.forEach(radio => {
    radio.addEventListener("change", () => {
      if (radio.value === "BASIC") {
        overtimeContainer.classList.remove("show");
        overtimeContainer.classList.add("hidden");
      } else {
        overtimeContainer.classList.remove("hidden");
        overtimeContainer.classList.add("show");
      }
    });
  });
});

///////////////////////////////////////// wczytywanie nadgodzin do kontenerów ///////////////////////////////////////
async function loadOvertimeSummary(userId, date) {
    if (!window.location.pathname.includes("/index")) {
        return;
    }

  try {
    // jeśli date jest przekazane, dodajemy ?date=…, inaczej nie dodajemy
    const url = date
      ? `/api/saved-data/overtime/${userId}?date=${encodeURIComponent(date)}`
      : `/api/saved-data/overtime/${userId}`;

    const res = await fetch(url);
    if (!res.ok) throw new Error(res.status);

    const { paidMinutes, offMinutes } = await res.json();
    document.getElementById("overtimePaid").textContent = paidMinutes + " min";
    document.getElementById("overtimeOff").textContent  = offMinutes  + " min";

  } catch (e) {
    console.error("Nie udało się wczytać nadgodzin:", e);
  }
}

document.addEventListener("DOMContentLoaded", () => {
  const userId = document.getElementById("userId")?.value;
  if (userId) {
    loadOvertimeSummary(userId);
  }
});

///////////////////////////////////////// odbieranie nadgodzin ///////////////////////////////////////
document.addEventListener("DOMContentLoaded", () => {
  const dateInput = document.getElementById("overtimeDate");
  if (!dateInput) return;

  const toISODate = d => d.toISOString().split("T")[0];

  const today = new Date();
  const past  = new Date();
  const future = new Date();

  past.setDate(today.getDate() - 7);
  future.setDate(today.getDate() + 7);

  const pastStr   = toISODate(past);
  const todayStr  = toISODate(today);
  const futureStr = toISODate(future);

  dateInput.min   = pastStr;
  dateInput.max   = futureStr;
  dateInput.value = todayStr;
});

const modal    = document.getElementById("confirmModal");
const confirmText = document.getElementById("confirmText");
const yesBtn   = document.getElementById("confirmYes");
const noBtn    = document.getElementById("confirmNo");

function askConfirmation(message) {
  return new Promise(resolve => {
    confirmText.textContent = message;
    modal.classList.remove("hidden");

    yesBtn.onclick = () => {
      modal.classList.add("hidden");
      resolve(true);
    };
    noBtn.onclick = () => {
      modal.classList.add("hidden");
      resolve(false);
    };
  });
}
///////////////////////////////////////// tabela z nadgodzinami //////////////////////////////////////////////

//(async function() {
//
//  // jeśli nie ma tabeli, wychodzimy
//  const table = document.querySelector("#overtime-table");
//  if (!table) return;
//
//  // 1) Pobierz surowe dane, w których jest już userId
//  const raw = await fetch("/api/overtime/get-all-overtime")
//                    .then(r => r.json());
//
//  // 2) Pivot po userId
//  const byUser = raw.reduce((acc, {userId, firstName, lastName, volumeType, totalOvertime}) => {
//    if (!acc[userId]) {
//      acc[userId] = {
//        userId,
//        firstName,
//        lastName,
//        paid: 0,
//        off: 0,
//        deducted: 0
//      };
//    }
//    switch (volumeType) {
//      case "Nadgodziny płatne":    acc[userId].paid     = totalOvertime; break;
//      case "Nadgodziny do odbioru": acc[userId].off      = totalOvertime; break;
//      case "Odebrane częściowo":    acc[userId].deducted = totalOvertime; break;
//    }
//    return acc;
//  }, {});
//  // 3) Zamień mapę na tablicę – będzie tyle wierszy, ilu masz unikalnych userId
//  const tableData = Object.values(byUser);
//
//  // 4) Renderuj główną tabelę
//  const tbody = document.querySelector("#overtime-table tbody");
//  tbody.innerHTML = "";
//  tableData.forEach(row => {
//    const tr = document.createElement("tr");
//    tr.innerHTML = `
//      <td>
//        <input type="checkbox" class="select-overtime-row"
//               data-user-id="${row.userId}" />
//      </td>
//        <td>
//          <a href="/userOvertimeDetails/overtime/${row.userId}" title="Szczegóły">
//            <i class="bx bxs-user-detail"></i>
//          </a>
//        </td>
//      <td>${row.firstName}</td>
//      <td>${row.lastName}</td>
//      <td>${row.paid}</td>
//      <td>${row.off}</td>
//      <td>${row.deducted}</td>
//    `;
//    tbody.appendChild(tr);
//  });
//})();

const userTableBody = document.querySelector("#overtime-user-table tbody");

async function loadOvertimeDetails(userId) {

  if (!userTableBody) return;

  try {
    // 1) Fetch danych z endpointu
    const res = await fetch(`/api/overtime/${userId}/details`);
    if (!res.ok) {
      throw new Error(`Błąd podczas wczytywania danych: ${res.status}`);
    }
    const details = await res.json();

    // 2) Znajdź <tbody> tabeli i wyczyść go
    userTableBody.innerHTML = "";

    // 3) Iteruj po każdy obiekt i twórz wiersz
    details.forEach(d => {
      const tr = document.createElement("tr");
      tr.innerHTML = `
        <td>${d.processName}</td>
        <td>${d.quantity}</td>
        <td>${d.date}</td>
        <td>${d.username}</td>
        <td>${d.volumeType}</td>
        <td>${d.overtimeMinutes}</td>
      `;
      userTableBody.appendChild(tr);
    });

  } catch (err) {
    console.error("Nie udało się wczytać szczegółów nadgodzin:", err);
  }
}
document.addEventListener("DOMContentLoaded", () => {
  // jeżeli jesteśmy na stronie szczegółów (tabela istnieje)
  const table = document.querySelector("#overtime-user-table");
  if (!table) return;

  // wyciągnij userId z URL: ostatni fragment po slashu
  const parts = window.location.pathname.split("/");
  const userId = parts[parts.length - 1];
  if (!userId) return;

  // załaduj szczegóły
  loadOvertimeDetails(userId);
});

///////////////////////////////////// filtry nadgodzin dla usera ////////////////////////////////////////////////
// 🧾 RENDEROWANIE AKTUALNEJ STRONY TABELI
function renderOvertimeTablePage() {
  const start = (currentPage - 1) * pageSize;
  const end   = start + pageSize;
  const pageSlice = filteredData.slice(start, end);

  userTableBody.innerHTML = "";
  pageSlice.forEach(item => {
    const tr = document.createElement("tr");
    tr.innerHTML = `
      <td>${item.processName}</td>
      <td>${item.quantity}</td>
      <td>${item.date}</td>
      <td>${item.username}</td>
      <td>${item.volumeType}</td>
      <td>${item.overtimeMinutes}</td>
    `;
    userTableBody.appendChild(tr);
  });

  // INFO O ZAKRESIE WYNIKÓW I NAWIGACJA
  const from = filteredData.length === 0 ? 0 : start + 1;
  const to   = Math.min(end, filteredData.length);
  recordCountInfo.textContent      = `Wyświetlono ${from}–${to} z ${filteredData.length} rekordów`;
  currentPageIndicator.textContent = `Strona ${currentPage} z ${Math.ceil(filteredData.length / pageSize)}`;
  prevBtn.disabled = currentPage === 1;
  nextBtn.disabled = end >= filteredData.length;
}

// 🔄 POBIERANIE DANYCH Z API I RESET FILTRÓW
function fetchOvertimeData(startDateOvertime = null, endDateOvertime = null, userId = null) {
  const query = new URLSearchParams();
  if (startDateOvertime) query.append("startDateOvertime", startDateOvertime);
  if (endDateOvertime)   query.append("endDateOvertime",   endDateOvertime);
  if (userId)            query.append("userId", userId);

  fetch(`/api/overtime/get-data?${query.toString()}`)
    .then(res => res.json())
    .then(data => {
      allData      = data;
      filteredData = [...allData];
      currentPage  = 1;
      renderOvertimeTablePage();
      loadUniqueOvertimeFiltersFromTable();
    })
    .catch(err => console.error("❌ Błąd pobierania danych:", err));
}

// 🔍 ZASTOSOWANIE FILTRÓW
function applyOvertimeFilters() {
  const selProc     = Array.from(document.querySelectorAll('#overtimeProcessDropdown input:checked')).map(cb => cb.value);
  const selOvertime = Array.from(document.querySelectorAll('#userOvertimeDropdown input:checked')).map(cb => cb.value);

  filteredData = allData.filter(item =>
    (selProc.length     === 0 || selProc.includes(item.processName)) &&
    (selOvertime.length === 0 || selOvertime.includes(item.volumeType))
  );

  currentPage = 1;
  renderOvertimeTablePage();
}

// 🔽 BUDOWA LISTY OPCJI FILTRÓW
function loadUniqueOvertimeFiltersFromTable() {
  const rows       = document.querySelectorAll("#overtime-user-table tbody tr");
  const processSet = new Set();
  const overtimeSet= new Set();
//  const tableBody = getElementById("overtime-user-table");

  if (!userTableBody) return;

  rows.forEach(row => {
    const cells = row.querySelectorAll("td");
    if (cells.length === 6) {
      processSet.add(cells[0].textContent.trim());
      overtimeSet.add(cells[4].textContent.trim());
    }
  });

  renderOvertimeFilterCheckboxes("#overtimeProcessDropdown", Array.from(processSet));
  renderOvertimeFilterCheckboxes("#userOvertimeDropdown", [
    "Nadgodziny płatne",
    "Nadgodziny do odbioru",
    "Odebrane częściowo"
  ]);

  // reset etykiet
  document.getElementById("overtimeProcessLabel").textContent   = "Proces";
  document.getElementById("overtimeProcessLabel").dataset.default = "Proces";
  document.getElementById("overtimeLabel").textContent          = "Rodzaj czasu pracy";
  document.getElementById("overtimeLabel").dataset.default      = "Rodzaj czasu pracy";
}

// 📋 RENDEROWANIE CHECKBOXÓW DLA FILTRÓW
function renderOvertimeFilterCheckboxes(containerSelector, values) {
  const container = document.querySelector(containerSelector);
  if (!container) return;

  container.innerHTML = "";
  values.forEach(val => {
    const label = document.createElement("label");
    label.innerHTML = `<input type="checkbox" value="${val}"> ${val}`;
    container.appendChild(label);
  });
}

// 🔄 KONFIGURACJA DROPDOWNÓW
function setupOvertimeDropdown(selectId, labelId, arrowId, dropdownId) {
  const select   = document.getElementById(selectId);
  const label    = document.getElementById(labelId);
  const arrow    = document.getElementById(arrowId);
  const dropdown = document.getElementById(dropdownId);
  if (!select || !label || !arrow || !dropdown) return;

  label.dataset.default = label.textContent;

  select.addEventListener("click", e => {
    dropdown.classList.toggle("show");
    arrow.classList.toggle("rotate");
    e.stopPropagation();
  });

  document.addEventListener("click", e => {
    if (!select.contains(e.target)) {
      dropdown.classList.remove("show");
      arrow.classList.remove("rotate");
    }
  });

  dropdown.addEventListener("change", () => {
    const cnt = dropdown.querySelectorAll("input:checked").length;
    label.textContent = cnt > 0 ? `${cnt} wybranych` : label.dataset.default;
    applyOvertimeFilters();
  });

  dropdown.addEventListener("click", e => {
    if (
      e.target.matches('input[type="checkbox"]') ||
      e.target.closest('label')?.querySelector('input[type="checkbox"]')
    ) {
      e.stopPropagation();
    }
  });
}

//// 🚀 INICJALIZACJA PO ZAŁADOWANIU DOM
document.addEventListener("DOMContentLoaded", () => {

  if (!userTableBody) return;

  const currentUserId = document.getElementById("userId")?.value;

  // 2) ustaw domyślne daty w inputs
  const startInput = document.getElementById("startDateOvertime");
  const endInput   = document.getElementById("endDateOvertime");
  const today = new Date();
  const ago   = new Date(today);
  ago.setDate(today.getDate() - 6);
  const toISO = d => d.toISOString().split("T")[0];
  if (startInput) startInput.value = toISO(ago);
  if (endInput)   endInput.value   = toISO(today);

  // 3) paginacja
  prevBtn.addEventListener("click", () => {
    if (currentPage>1) {
      currentPage--;
      renderOvertimeTablePage();
    }
  });
  nextBtn.addEventListener("click", () => {
    if (currentPage*pageSize<filteredData.length) {
      currentPage++;
      renderOvertimeTablePage();
    }
  });

  // 4) clearBtn reset
  clearBtn.addEventListener("click", () => {
    document.querySelectorAll(
      "#overtimeProcessDropdown input, #userOvertimeDropdown input"
    ).forEach(cb => cb.checked=true);
    startInput.value = toISO(ago);
    endInput  .value = toISO(today);
    fetchOvertimeData(toISO(ago), toISO(today), currentUserId);
  });

  // 5) dropdowny
  setupOvertimeDropdown(
    "overtimeProcessSelect",
    "overtimeProcessLabel",
    "overtimeProcessArrow",
    "overtimeProcessDropdown"
  );
  setupOvertimeDropdown(
    "overtimeUserSelect",
    "overtimeLabel",
    "userOvertimeArrow",
    "userOvertimeDropdown"
  );

  // 6) data‐form submit
  document.getElementById("overtimeForm")?.addEventListener("submit", e => {
    e.preventDefault();
    fetchOvertimeData(startInput.value, endInput.value, currentUserId);
  });

  // 7) pierwsze załadowanie
  fetchOvertimeData(toISO(ago), toISO(today), currentUserId);
});



document.addEventListener("DOMContentLoaded", () => {
  if (!window.location.pathname.includes("/overtimeReport")) return;

  const sectionToggleBtn = document.getElementById("overtimeSectionCustomSelect");
  const sectionDropdown = document.getElementById("overtimeSectionDropdown");
  const sectionLabel = document.getElementById("overtimeSectionLabel");
  const sectionArrow = document.getElementById("overtimeSectionArrowIcon");

  const employeeToggleBtn = document.getElementById("overtimeCustomSelect");
  const employeeDropdown = document.getElementById("overtimeCheckboxDropdown");
  const employeeLabel = document.getElementById("overtimeSelectedLabel");
  const employeeArrow = document.getElementById("overtimeArrowIcon");

  const {
    renderSectionCheckboxes,
    renderEmployeeCheckboxes,
    setupDropdownToggle
  } = window.filters;

  let filteredData = [];
  let rawData = [];
  let allData = [];
  let selectedEmployeeIds = []; // 🔥 Tu przechowujemy zaznaczonych pracowników

  // render sekcji
  fetch("/api/sections")
    .then(res => res.json())
    .then(sections => {
      renderSectionCheckboxes(sectionDropdown, sections);
      sectionLabel.textContent = sectionLabel.dataset.default || "Sekcja";
    })
    .catch(err => console.error("Nie udało się pobrać sekcji:", err));

  // pobierz dane i renderuj tabelę
  fetch("/api/overtime/get-all-overtime")
    .then(res => res.json())
    .then(raw => {
      allData = raw.map(row => ({
        userId: row.userId,
        firstName: row.firstName,
        lastName: row.lastName,
        sectionId: row.sectionId,
        paid: row.paid,
        off: row.off,
        deducted: row.deducted
      }));

      renderOvertimeForAllUsersTable(allData);
      renderEmployeeCheckboxes(
        employeeDropdown,
        allData.map(u => ({ id: u.userId, firstName: u.firstName, lastName: u.lastName }))
      );
    });

  // toggle dropdownów
  setupDropdownToggle({
    trigger: sectionToggleBtn,
    dropdown: sectionDropdown,
    arrow: sectionArrow
  });
  setupDropdownToggle({
    trigger: employeeToggleBtn,
    dropdown: employeeDropdown,
    arrow: employeeArrow
  });

  // filtracja przy zmianie
  function onFilterChange(isSectionChange) {
    const selSections = Array.from(
      sectionDropdown.querySelectorAll("input.section-checkbox:checked")
    ).map(cb => cb.value);

    const secCnt = selSections.length;
    sectionLabel.textContent = secCnt > 0
      ? `${secCnt} wybranych`
      : sectionLabel.dataset.default || "Sekcja";

    // 🔥 Jeśli zmienił się filtr sekcji, odśwież dropdown pracowników
    if (isSectionChange) {
      const employeesToShow = selSections.length > 0
        ? allData.filter(row => selSections.includes(String(row.sectionId)))
        : allData;

      renderEmployeeCheckboxes(
        employeeDropdown,
        employeesToShow.map(u => ({
          id: u.userId,
          firstName: u.firstName,
          lastName: u.lastName
        }))
      );

      if (selSections.length === 0) {
        // 🔥 Jeśli brak sekcji – odznacz wszystkich pracowników
        selectedEmployeeIds = [];
      } else {
        // 🔥 Usuń tylko tych, którzy wypadli z dropdowna
        const currentDropdownUserIds = employeesToShow.map(u => String(u.userId));
        selectedEmployeeIds = selectedEmployeeIds.filter(id => currentDropdownUserIds.includes(id));
      }

      // 🔥 Odtwórz zaznaczenia checkboxów
      employeeDropdown.querySelectorAll("input.employee-checkbox").forEach(cb => {
        cb.checked = selectedEmployeeIds.includes(cb.value);
      });
    }


    // 🔥 Aktualizuj zaznaczenia po każdej zmianie checkboxów pracowników
    selectedEmployeeIds = Array.from(
      employeeDropdown.querySelectorAll("input.employee-checkbox:checked")
    ).map(cb => cb.value);

    const empCnt = selectedEmployeeIds.length;
    employeeLabel.textContent = empCnt > 0
      ? `${empCnt} wybranych`
      : employeeLabel.dataset.default || "Pracownik";

    // 🔥 Filtrowanie tabeli
    filteredData = allData.filter(row =>
      (selSections.length === 0 || selSections.includes(String(row.sectionId))) &&
      (selectedEmployeeIds.length === 0 || selectedEmployeeIds.includes(String(row.userId)))
    );

    renderOvertimeForAllUsersTable(filteredData);
  }

  // eventy do sekcji i pracowników
  sectionDropdown.addEventListener("change", () => {
    onFilterChange(true); // true = zmiana w sekcjach
  });
  employeeDropdown.addEventListener("change", () => {
    onFilterChange(false); // false = zmiana w pracownikach
  });

  // render tabeli
  function renderOvertimeForAllUsersTable(rows) {
    const tbody = document.querySelector("#overtime-table tbody");
    if (!tbody) return;
    tbody.innerHTML = "";
    rows.forEach(row => {
    console.log(rows);
      const tr = document.createElement("tr");
      tr.innerHTML = `
        <td><input type="checkbox" class="select-overtime-row"
                   data-user-id="${row.userId}"></td>
        <td>
          <a href="/userOvertimeDetails/overtime/${row.userId}" title="Szczegóły">
            <i class="bx bxs-user-detail"></i>
          </a>
        </td>
        <td>${row.firstName}</td>
        <td>${row.lastName}</td>
        <td>${row.paid}</td>
        <td>${row.off}</td>
        <td>${row.deducted}</td>
      `;
      tbody.appendChild(tr);
    });
  }

    document.getElementById("clearOvertimeBtn").addEventListener("click", () => {
        //reset checkboxów
        [sectionDropdown, employeeDropdown].forEach(dropdown => {
            dropdown.querySelectorAll("input[type='checkbox']").forEach(cb => cb.checked = false)
        });

        //reset etykiet
        sectionLabel.textContent = "Sekcja";
        employeeLabel.textContent = "Pracownik";
        //reset tabeli
        renderOvertimeForAllUsersTable(allData);
    });

    // export danych
document.getElementById("exportOvertimeSummaryXlsxBtn").addEventListener("click", function () {
  const rows = Array.from(document.querySelectorAll("#overtime-table tbody tr"));
  const dataToExport = rows.map(row => {
    const cells = row.querySelectorAll("td");
    return {
      firstName: cells[2].textContent.trim(),
      lastName: cells[3].textContent.trim(),
      overtimePaid: parseInt(cells[4].textContent.trim(), 10) || 0,
      overtimeOff: parseInt(cells[5].textContent.trim(), 10) || 0,
      deductPartial: parseInt(cells[6].textContent.trim(), 10) || 0
    };
  });

  // 🔥 Wyślij POST z JSON
  fetch("/api/overtime/exportAll", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(dataToExport)
  }).then(res => res.blob())
    .then(blob => {
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = "Nadgodziny podsumowanie.xlsx";
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
    });
});

});

document.addEventListener("DOMContentLoaded", () => {
   if (!window.location.pathname.includes("/userOvertimeDetails")) return;

  document.getElementById("exportXlsxBtn").addEventListener("click", function () {
    const rows = Array.from(document.querySelectorAll("#overtime-user-table tbody tr"));

    const dataToExport = rows.map(row => {
      const cells = row.querySelectorAll("td");

      return {
        processName: cells[0].textContent.trim(),
        quantity: parseInt(cells[1].textContent.trim(), 10) || 0,
        todaysDate: cells[2].textContent.trim(),       // np. "2025-06-08"
        username: cells[3].textContent.trim(),
        volumeType: cells[4].textContent.trim(),
        overtimeMinutes: parseInt(cells[5].textContent.trim(), 10) || 0
      };
    });

    // 🔥 Wyślij POST z JSON
    fetch("/api/overtime/exportOvertimeForDate", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(dataToExport)
    }).then(res => res.blob())
      .then(blob => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement("a");
        a.href = url;
        a.download = "Nadgodziny_szczegóły.xlsx";
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
      }).catch(err => {
        console.error("❌ Błąd przy eksporcie XLSX:", err);
      });
  });
});


document.addEventListener("DOMContentLoaded", function () {
    // Obsługa przycisku archiwizacji
    const payoutBtn = document.getElementById("payoutOvertimeBtn");
    if (!payoutBtn) return;

    payoutBtn.addEventListener("click", function () {
        // Zbierz zaznaczone userId z checkboxów
        const selected = Array.from(document.querySelectorAll(".select-overtime-row:checked"))
            .map(cb => Number(cb.getAttribute("data-user-id")));

        if (selected.length === 0) {
            alert("Zaznacz przynajmniej jednego użytkownika!");
            return;
        }

        // (Opcjonalnie) Pytanie o notatkę
        const note = prompt("Podaj notatkę do archiwizacji (opcjonalnie):");

        // Wywołanie endpointu /archive-paid
        fetch("/api/overtime/archive-paid" + (note ? `?note=${encodeURIComponent(note)}` : ""), {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(selected)
        })
            .then(response => {
                if (!response.ok) throw new Error("Błąd archiwizacji!");
                return response.text();
            })
            .then(msg => {
                alert(msg);
                // Odśwież tabelę (np. ponownie pobierz dane z backendu)
                location.reload();
            })
            .catch(err => {
                alert("Błąd archiwizacji: " + err.message);
            });
    });
});
