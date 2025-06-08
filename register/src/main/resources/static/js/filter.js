// === filter.js ===

// Pobiera zaznaczone checkboxy wg selektora (np. ".employee-checkbox:checked")
function getSelectedCheckboxValues(selector) {
  return Array.from(document.querySelectorAll(selector)).map(cb => cb.value);
}

// Specjalizacje
function getSelectedUserIds() {
  return getSelectedCheckboxValues(".employee-checkbox:checked");
}
function getSelectedSectionIds() {
  return getSelectedCheckboxValues(".section-checkbox:checked");
}
function getSelectedProcessNames() {
  return getSelectedCheckboxValues(".process-checkbox:checked");
}


function renderCheckboxes(container, dataList, checkboxClass, labelTextFn) {
  container.innerHTML = "";
  dataList.forEach(item => {
    const id = typeof item === "object" ? item.id : item;
    const label = labelTextFn(item);

    const wrapper = document.createElement("label");
    wrapper.innerHTML = `
      <input type="checkbox" class="${checkboxClass}" value="${id}"> ${label}
    `;
    container.appendChild(wrapper);
  });
}

// Sekcje, Pracownicy, Procesy
function renderSectionCheckboxes(container, sections) {
  renderCheckboxes(container, sections, "section-checkbox", s => s.sectionName);
}

function renderEmployeeCheckboxes(container, users) {
  renderCheckboxes(container, users, "employee-checkbox", u => `${u.firstName} ${u.lastName}`);
}

function renderProcessCheckboxes(container, processList) {
  renderCheckboxes(container, processList, "process-checkbox", p => p.processName);
}

function setupDropdownToggle({ trigger, dropdown, arrow }) {
  if (!trigger || !dropdown || !arrow) return;

  // 1) Toggle dropdown + strzałka
  trigger.addEventListener("click", function (e) {
    const isOpen = dropdown.classList.contains("show");
    dropdown.classList.toggle("show", !isOpen);
    arrow.classList.toggle("rotate", !isOpen);
    e.stopPropagation();
  });

  // 2) Kliknięcie gdziekolwiek poza dropdownem zamyka go
  document.addEventListener("click", function (e) {
    if (!dropdown.contains(e.target) && !trigger.contains(e.target)) {
      dropdown.classList.remove("show");
      arrow.classList.remove("rotate");
    }
  });

  // 3) Zatrzymaj propagację dla checkboxów i labeli wewnątrz dropdownu
  dropdown.addEventListener("click", function(e) {
    // jeśli kliknięto na checkbox *lub* na labelkę zawierającą checkbox
    const label = e.target.closest("label");
    if (
      e.target.matches('input[type="checkbox"]') ||
      (label && label.querySelector('input[type="checkbox"]'))
    ) {
      e.stopPropagation();
    }
  });
}


function loadUsersBySectionIds(sectionIds, checkboxContainer, selectedLabel, renderUserTableFn, options = {}) {
  const preserveCheckboxState = options.preserveCheckboxState ?? true;

  if (!sectionIds.length) {
    fetch(`/api/user/all-users`)
      .then(res => res.json())
      .then(users => {
        renderUserTableFn?.(users);
        renderEmployeeCheckboxes(checkboxContainer, users);

        if (!preserveCheckboxState) {
          // Wyzeruj zaznaczenie
          selectedLabel.textContent = "Pracownik";
        } else {
          selectedLabel.textContent = users.length
            ? `${getSelectedUserIds().length} wybranych`
            : "Pracownik";
        }
      });
    return;
  }

  Promise.all(sectionIds.map(id =>
    fetch(`/api/user/by-section/${id}`).then(res => res.json())
  )).then(sectionUsersArrays => {
    const flatUsers = sectionUsersArrays.flat();
    const uniqueUsersMap = new Map(flatUsers.map(user => [user.id, user]));
    const uniqueUsers = Array.from(uniqueUsersMap.values());

    renderUserTableFn?.(uniqueUsers);
    renderEmployeeCheckboxes(checkboxContainer, uniqueUsers);

    if (!preserveCheckboxState) {
      selectedLabel.textContent = "Pracownik";
    } else {
      selectedLabel.textContent = getSelectedUserIds().length > 0
        ? `${getSelectedUserIds().length} wybranych`
        : "Pracownik";
    }
  }).catch(err => {
    console.error("Błąd podczas ładowania użytkowników po sekcji:", err);
  });
}

function resetAllFilters(sectionContainer, userContainer, sectionLabelEl, userLabelEl, renderTableFn) {
  // Odznacz wszystkie checkboxy
  document.querySelectorAll(".employee-checkbox, .section-checkbox").forEach(cb => cb.checked = false);

  // Resetuj etykiety
  sectionLabelEl.textContent = "Sekcja";
  userLabelEl.textContent = "Pracownik";

  // Wczytaj pełną tabelę bez zaznaczania checkboxów
  loadUsersBySectionIds([], userContainer, userLabelEl, renderTableFn, { preserveCheckboxState: false });
}

function resetAllFiltersOnChart(sectionDropdown, employeeDropdown, processDropdown, sectionLabel, employeeLabel, processLabel, renderChartCallback) {
  // Odznacz checkboxy
  [sectionDropdown, employeeDropdown, processDropdown].forEach(dropdown => {
    dropdown.querySelectorAll("input[type='checkbox']").forEach(cb => cb.checked = false);
  });

  // Reset etykiet
  sectionLabel.textContent = "Sekcja";
  employeeLabel.textContent = "Pracownik";
  processLabel.textContent = "Proces";

  // Reset daty do dziś
  const today = new Date().toISOString().split("T")[0];
  document.getElementById("reportDate").value = today;

  // Wywołaj ponownie wykres bez filtrów
  if (typeof renderChartCallback === "function") {
    renderChartCallback();
  }
}



function loadUsersBySectionIdsToChart(
  sectionIds,
  checkboxContainer,
  selectedLabel,
  processDropdown = null,
  processLabel = null,
  processesFromChart = [],
  options = {}
) {
  const preserveCheckboxState = options.preserveCheckboxState ?? true;

  const updateProcessDropdown = () => {
    if (!processDropdown || !processLabel || !Array.isArray(processesFromChart)) return;
    renderProcessCheckboxes(processDropdown, processesFromChart);
    const selectedProcesses = getSelectedProcessNames();
    processLabel.textContent = selectedProcesses.length > 0
      ? `${selectedProcesses.length} wybranych`
      : "Proces";
  };

  if (!sectionIds.length) {
    fetch(`/api/user/all-users`)
      .then(res => res.json())
      .then(users => {
        renderEmployeeCheckboxes(checkboxContainer, users);
        selectedLabel.textContent = preserveCheckboxState && getSelectedUserIds().length > 0
          ? `${getSelectedUserIds().length} wybranych`
          : "Pracownik";

        updateProcessDropdown();
      });
    return;
  }

  Promise.all(sectionIds.map(id =>
    fetch(`/api/user/by-section/${id}`).then(res => res.json())
  ))
    .then(sectionUsersArrays => {
      const flatUsers = sectionUsersArrays.flat();
      const uniqueUsersMap = new Map(flatUsers.map(user => [user.id, user]));
      const uniqueUsers = Array.from(uniqueUsersMap.values());

      renderEmployeeCheckboxes(checkboxContainer, uniqueUsers);
      selectedLabel.textContent = preserveCheckboxState && getSelectedUserIds().length > 0
        ? `${getSelectedUserIds().length} wybranych`
        : "Pracownik";

      updateProcessDropdown();
    })
    .catch(err => {
      console.error("Błąd podczas ładowania użytkowników po sekcji:", err);
    });
}

// Exporty do globalnego użytku
window.filters = {
  getSelectedUserIds,
  getSelectedSectionIds,
  getSelectedProcessNames,
  renderSectionCheckboxes,
  renderEmployeeCheckboxes,
  renderProcessCheckboxes,
  setupDropdownToggle,
  loadUsersBySectionIds,
  resetAllFilters,
  loadUsersBySectionIdsToChart,
  resetAllFiltersOnChart
};

