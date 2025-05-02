
// Pobieranie przycisków i list
const moveRightBtn = document.getElementById('move-right');
const moveLeftBtn = document.getElementById('move-left');
const leftList = document.getElementById('left-list');
const rightList = document.getElementById('right-list');
const selectAllBtn = document.getElementById('move-right-all');
const removeAllBtn = document.getElementById('move-left-all');

// Funkcja aktualizująca stan przycisków
function updateButtonsState() {
    try {
        const hasLeftChecked = leftList.querySelector('li.checked') !== null;
        const hasRightChecked = rightList.querySelector('li.checked') !== null;

        moveRightBtn.disabled = !hasLeftChecked;
        moveLeftBtn.disabled = !hasRightChecked;
    } catch (error) {
        console.error("Wystąpił błąd w updateButtonsState:", error);
    }
}

// Funkcja do zaznaczania pojedynczych elementów z użyciem delegacji
function attachClickEventsToItems() {
    const leftList = document.getElementById('left-list');
    const rightList = document.getElementById('right-list');

    // Funkcja do zmiany klasy i ikony
    function toggleItemState(item) {
        if (item.tagName === 'LI') {
            let icon = item.querySelector('i'); // Pobierz ikonę wewnątrz <li>

            if (!icon) {
                // Jeśli nie ma jeszcze ikony, dodaj nową
                icon = document.createElement('i');
                item.appendChild(icon);
            }

            if (item.classList.contains('checked')) {
                item.classList.replace('checked', 'unchecked'); // Zmiana na "unchecked"
                icon.className = 'bx bx-x'; // Ustaw ikonę "x"
                icon.style.color = 'red'; // Czerwony kolor dla X
            } else {
                item.classList.replace('unchecked', 'checked'); // Zmiana na "checked"
                icon.className = 'bx bx-check'; // Ustaw ikonę "check"
                icon.style.color = 'green';
            }

            updateButtonsState(); // Aktualizacja przycisków, jeśli to konieczne
        }
    }

    // Dodaj event listener do lewej listy
    leftList.addEventListener('click', function (event) {
        toggleItemState(event.target);
    });

    // Dodaj event listener do prawej listy
    rightList.addEventListener('click', function (event) {
        toggleItemState(event.target);
    });
}

// Inicjalizuj stan przycisków na początku
updateButtonsState();
attachClickEventsToItems();

// Funkcja do przenoszenia zaznaczonych elementów
function moveCheckedItems(sourceList, targetList) {
    try {
        const selectedItems = sourceList.querySelectorAll('li.checked');
        selectedItems.forEach(item => {
            targetList.appendChild(item);
        });
        updateButtonsState(); // Sprawdzamy, czy przyciski powinny być aktywne
    } catch (error) {
        console.error("Wystąpił błąd w moveCheckedItems:", error);
    }
}

// Funkcja do zaznaczania elementów
function toggleSelectAll(list, shouldCheck) {
    try {
        const items = list.querySelectorAll('li');
        items.forEach(item => {
            item.classList.toggle('checked', shouldCheck);
            item.classList.toggle('unchecked', !shouldCheck);
        });
    } catch (error) {
        console.error("Wystąpił błąd w toggleSelectAll:", error);
    }
}

// Funkcja do zaznaczania wszystkich elementów
selectAllBtn.addEventListener('click', () => {
    try {
        const leftItems = leftList.querySelectorAll('li');
        const rightItems = rightList.querySelectorAll('li');

        // Zaznacz wszystkie elementy w lewej liście, jeśli są
        if (leftItems.length > 0) {
            toggleSelectAll(leftList, true);
        }
        // W przeciwnym razie, jeśli są elementy w prawej liście, zaznacz je
        else if (rightItems.length > 0) {
            toggleSelectAll(rightList, true);
        }

        updateButtonsState(); // Zaktualizuj stan przycisków, jeśli to konieczne
    } catch (error) {
        console.error("Wystąpił błąd w selectAllBtn:", error);
    }
});

// Funkcja do usuwania zaznaczenia ze wszystkich elementów
removeAllBtn.addEventListener('click', () => {
    try {
        toggleSelectAll(leftList, false);
        toggleSelectAll(rightList, false);
        updateButtonsState(); // Zaktualizuj stan przycisków, jeśli to konieczne
    } catch (error) {
        console.error("Wystąpił błąd w removeAllBtn:", error);
    }
});

// Uniwersalna funkcja do zmiany stanu zaznaczenia dla wszystkich elementów listy
function toggleSelectAll(list, isChecked) {
    list.querySelectorAll('li').forEach(item => {
        let icon = item.querySelector('i'); // Pobierz ikonę, jeśli już istnieje

        if (!icon) {
            // Jeśli ikona nie istnieje, utwórz nową
            icon = document.createElement('i');
            item.appendChild(icon);
        }

        if (isChecked) {
            item.classList.add('checked');
            item.classList.remove('unchecked');
            icon.className = 'bx bx-check'; // Ikona ✓
            icon.style.color = 'green'; // Zielony kolor
        } else {
            item.classList.add('unchecked');
            item.classList.remove('checked');
            icon.className = 'bx bx-x'; // Ikona ❌
            icon.style.color = 'red'; // Czerwony kolor
        }
    });
}

// Event listeners dla przycisków przenoszenia
moveRightBtn.addEventListener('click', () => {
    try {
        moveCheckedItems(leftList, rightList);
    } catch (error) {
        console.error("Wystąpił błąd w moveRightBtn:", error);
    }
});

moveLeftBtn.addEventListener('click', () => {
    try {
        moveCheckedItems(rightList, leftList);
    } catch (error) {
        console.error("Wystąpił błąd w moveLeftBtn:", error);
    }
});
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
    document.getElementById("save").addEventListener("click", function() {
        const userId = document.getElementById("userId")?.value.trim();

        if (!userId) {
            console.error("Błąd: userId jest pusty!");
            alert("Nie znaleziono userId. Upewnij się, że jesteś zalogowany.");
            return;
        }

        const rightList = document.getElementById("right-list");
        if (!rightList) {
            console.error("Błąd: Element right-list nie istnieje w DOM.");
            return;
        }


        const selectedIds = [...rightList.querySelectorAll("li")]
            .map(li => li.dataset.id)
            .filter(id => id); // Usuwa puste wartości


        fetch(`/api/processes/favorites/${userId}`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(selectedIds)
        })
        .then(response => {
            if (!response.ok) {
                throw new Error(`Błąd HTTP: ${response.status}`);
            }
            return response.json();
        })
          .then(data => displayMessage("success", "Zapisano ulubione procesy!")) // ✅ Użycie nowej funkcji
          .catch(error => displayMessage("error", "Błąd podczas zapisu. Spróbuj ponownie."));

    });

//////////////////////////////////////////////////////////////////////////////////////////////////////////////
document.addEventListener("DOMContentLoaded", function () {
    const userId = document.getElementById("userId")?.value.trim();

    if (!userId) {
        console.warn("Brak userId – użytkownik niezalogowany?");
        return;
    }

    const leftList = document.getElementById("left-list");
    const rightList = document.getElementById("right-list");

    let favoriteIds = new Set(); // 🔹 Przeniesienie zmiennej do szerszego zakresu

    // Pobierz ulubione procesy
    fetch(`/api/processes/favorites/${userId}`)
        .then(response => response.json())
        .then(favorites => {
            rightList.innerHTML = '';

            favorites.forEach(favProcess => {
                const favLi = document.createElement('li');
                favLi.classList.add('unchecked');
                favLi.dataset.id = favProcess.id;
                favLi.textContent = favProcess.processName;

                const icon = document.createElement('i');
                icon.classList.add('bx', 'bx-x');
                icon.style.color = 'red';

                favLi.appendChild(icon);
                rightList.appendChild(favLi);

                favoriteIds.add(favProcess.id); // ✅ Teraz dostępne w całej funkcji
            });

            // 🔹 Pobierz procesy tylko dla zespołu użytkownika
            return fetch(`/api/processes/team/${userId}`);
        })
        .then(response => response.json())
        .then(processes => {
            leftList.innerHTML = '';

            processes.forEach(process => {
                if (!favoriteIds.has(process.id)) { // ✅ Teraz favoriteIds jest dostępne!
                    const li = document.createElement('li');
                    li.classList.add('unchecked');
                    li.dataset.id = process.id;
                    li.textContent = process.processName;

                    const icon = document.createElement('i');
                    icon.classList.add('bx', 'bx-x');
                    icon.style.color = 'red';

                    li.appendChild(icon);
                    leftList.appendChild(li);
                }
            });
        })
        .catch(error => console.error("Błąd podczas pobierania procesów:", error));
});



function displayMessage(type, message) {
    Swal.fire({
        icon: type, // 'success' lub 'error'
        title: message,
        showConfirmButton: false,
        timer: 3000 // Popup znika po 3 sekundach
    });
}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////

//console.log("Skrypt PROCESESS został załadowany.")
