
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

    // Dodaj event listener do lewej listy
    leftList.addEventListener('click', function (event) {
        const item = event.target;

        // Sprawdź, czy kliknięty element to <li>
        if (item.tagName === 'LI') {
            try {
                // Zmieniamy 'unchecked' na 'checked' lub odwrotnie
                if (item.classList.contains('checked')) {
                    item.classList.replace('checked', 'unchecked'); // Zastępujemy 'checked' na 'unchecked'
                } else {
                    item.classList.replace('unchecked', 'checked'); // Zastępujemy 'unchecked' na 'checked'
                }
                updateButtonsState();
            } catch (error) {
                console.error("Wystąpił błąd w selectedItems:", error);
            }
        }
    });

    // Dodaj event listener do prawej listy
    rightList.addEventListener('click', function (event) {
        const item = event.target;

        // Sprawdź, czy kliknięty element to <li>
        if (item.tagName === 'LI') {
            try {
                // Zmieniamy 'unchecked' na 'checked' lub odwrotnie
                if (item.classList.contains('checked')) {
                    item.classList.replace('checked', 'unchecked'); // Zastępujemy 'checked' na 'unchecked'
                } else {
                    item.classList.replace('unchecked', 'checked'); // Zastępujemy 'unchecked' na 'checked'
                }
                updateButtonsState();
            } catch (error) {
                console.error("Wystąpił błąd w selectedItems:", error);
            }
        }
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