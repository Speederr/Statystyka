document.addEventListener('DOMContentLoaded', function() {
    fetch('/api/user/info')
        .then(response => response.json())
        .then(user => {
            if (user && user.firstName && user.lastName) {
                document.querySelector('.user .bold').textContent = `${user.firstName} ${user.lastName}`; // Set name
                document.querySelector('.user-img').src = user.avatar_url || '/images/2.jpg'; // Set avatar

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




document.addEventListener("DOMContentLoaded", function() {
    // Funkcja pobierająca procesy z backendu
    function fetchProcesses() {
        fetch('/api/processes')
            .then(response => response.json())
            .then(data => {
                const leftList = document.getElementById('left-list');
                leftList.innerHTML = ''; // Czyszczenie listy przed dodaniem elementów
                data.forEach(process => {
                    const li = document.createElement('li');
                    li.classList.add('unchecked');
                    li.textContent = process;
                    leftList.appendChild(li);
                });
            })
            .catch(error => console.error('Błąd podczas pobierania procesów:', error));
    }

    // Wywołaj funkcję fetchProcesses podczas ładowania strony
    fetchProcesses();
});


document.getElementById('save').addEventListener('click', async () => {
    try {
        const rightItems = rightList.querySelectorAll('li');
        const savedItems = [];

        // Zbieramy wszystkie elementy z prawej listy
        rightItems.forEach(item => {
            savedItems.push(item.innerText);
        });

        // Wysyłamy dane do serwera
        const response = await fetch('/api/items/save', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(savedItems)
        });

        if (!response.ok) {
            throw new Error('Błąd podczas zapisywania elementów');
        }

        console.log('Zapisane elementy:', savedItems);
    } catch (error) {
        console.error("Wystąpił błąd przy zapisywaniu elementów:", error);
    }
});

