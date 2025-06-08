let chartInstance = null; // Przechowujemy instancję wykresu globalnie
let remoteChartInstance = null;
let userChartInstance = null;
const userId = document.getElementById("userId").value;



function updateChart(presentCount, onLeaveCount, notLoggedCount, presentNames, onLeaveNames, notLoggedNames, totalEmployees) {
    const canvas = document.getElementById("availabilityChart");
    const ctx = canvas.getContext("2d");

    document.getElementById("legendPresentCount").textContent = presentCount;
    document.getElementById("legendOnLeaveCount").textContent = onLeaveCount;
    document.getElementById("legendNotLoggedCount").textContent = notLoggedCount;
    document.getElementById("employeeCount").textContent = `${presentCount}/${totalEmployees}`;

    if (chartInstance !== null) {
        chartInstance.destroy();
        chartInstance = null;
    }

    const chartColors = [
        "rgba(54, 162, 235, 0.6)",   // Obecni
        "rgba(75, 192, 192, 0.8)",   // Nieobecni
        "rgba(255, 99, 132, 0.8)"    // Niezalogowani
    ];

    chartInstance = new Chart(ctx, {
        type: "doughnut",
        data: {
            labels: ["Obecni", "Nieobecni", "Niezalogowani"],
            datasets: [{
                data: [presentCount, onLeaveCount, notLoggedCount],
                backgroundColor: chartColors,
                borderWidth: 2,
                hoverOffset: 8,
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            cutout: "65%",
            plugins: {
                legend: { display: false },
                tooltip: { enabled: false }
            },
//            onClick: (chartEvent, elements) => {
                onHover: (chartEvent, elements) => {
                const tooltipEl = document.getElementById('chartjs-tooltip');
                const canvas = document.getElementById('availabilityChart');

                if (elements.length === 0 || !chartEvent.native) return;

                const segmentIndex = elements[0].index;

                let category = "";
                let names = [];
                let color = chartColors[segmentIndex];

                switch (segmentIndex) {
                    case 0:
                        category = "Obecni";
                        names = presentNames;
                        break;
                    case 1:
                        category = "Nieobecni";
                        names = onLeaveNames;
                        break;
                    case 2:
                        category = "Niezalogowani";
                        names = notLoggedNames;
                        break;
                }

                tooltipEl.innerHTML = `
                    <div>
                        <span class="tooltip-color-box" style="background:${color}"></span>
                        <strong>${category}: ${names.length}</strong>
                    </div>
                    ${names.map(name => `<div>${name}</div>`).join("")}
                `;

                const container = document.querySelector(".chart-content") || canvas.parentElement;
                const containerRect = container.getBoundingClientRect();

                const mouseX = chartEvent.native.clientX - containerRect.left + 40;
                const mouseY = chartEvent.native.clientY - containerRect.top + 230;

                tooltipEl.style.left = `${mouseX}px`;
                tooltipEl.style.top = `${mouseY}px`;
                tooltipEl.style.display = 'block';
            }
        }
    });

    document.addEventListener('click', function (e) {
        const tooltipEl = document.getElementById('chartjs-tooltip');
        const canvas = document.getElementById("availabilityChart");

        if (!tooltipEl.contains(e.target) && !canvas.contains(e.target)) {
            tooltipEl.style.display = 'none';
        }
    });
}

function updateRemoteChart(officeCount, homeofficeCount, officeNames, homeofficeNames) {
    const canvas = document.getElementById("remoteWorkChart");
    const ctx = canvas.getContext("2d");

    document.getElementById("legendOfficeCount").textContent = officeCount;
    document.getElementById("legendHomeofficeCount").textContent = homeofficeCount;

    if (remoteChartInstance) {
        remoteChartInstance.destroy();
    }

    const chartColors = [
        "rgba(255, 205, 86, 0.8)",   // Biuro
        "rgba(153, 102, 255, 0.8)"   // Zdalnie
    ];

    remoteChartInstance = new Chart(ctx, {
        type: "doughnut",
        data: {
            labels: ["Biuro", "Zdalnie"],
            datasets: [{
                data: [officeCount, homeofficeCount],
                backgroundColor: chartColors,
                borderWidth: 2,
                hoverOffset: 8,
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            cutout: "65%",
            plugins: {
                legend: { display: false },
                tooltip: { enabled: false } // ❌ wyłączamy standardowy
            },
//            onClick: (chartEvent, elements) => {
                onHover: (chartEvent, elements) => {
                const tooltipEl = document.getElementById('chartjs-tooltip-remote');
                const tooltipContent = document.getElementById('tooltip-content-remote');


                if (elements.length === 0 || !chartEvent.native) return;

                const index = elements[0].index;

                const label = index === 0 ? "Biuro" : "Zdalnie";
                const names = index === 0 ? officeNames : homeofficeNames;
                const color = chartColors[index];

                tooltipContent.innerHTML = `
                    <div>
                        <span class="tooltip-color-box" style="background:${color}"></span>
                        <strong>${label}: ${names.length}</strong>
                    </div>
                    ${names.map(name => `<div>${name}</div>`).join("")}
                `;

                const container = document.querySelector(".chart-content") || canvas.parentElement;
                const rect = container.getBoundingClientRect();

                const mouseX = chartEvent.native.clientX - rect.left + 40;
                const mouseY = chartEvent.native.clientY - rect.top + 230;

                tooltipEl.style.left = `${mouseX}px`;
                tooltipEl.style.top = `${mouseY}px`;
                tooltipEl.style.display = 'block';
            }
        }
    });

    // Ukryj tooltip po kliknięciu poza wykres
    document.addEventListener('click', function (e) {
        const tooltipEl = document.getElementById('chartjs-tooltip-remote');
        const tooltipContent = document.getElementById('tooltip-content-remote');

        if (!tooltipEl.contains(e.target) && !canvas.contains(e.target)) {
            tooltipEl.style.display = 'none';
        }
    });
}


function loadWorkModeSummary(sectionId = "all") {
    fetch(`/api/attendance/workmode/summary?sectionId=${sectionId}`)
        .then(res => res.json())
        .then(data => {
            const total = data.total || 0;
            const office = data.office || 0;
            const homeoffice = data.homeoffice || 0;

            const active = office + homeoffice;
            document.getElementById("workModeCount").textContent = `${active}/${total}`;
            document.getElementById("legendOfficeCount").textContent = office;
            document.getElementById("legendHomeofficeCount").textContent = homeoffice;
        })
        .catch(err => console.error("❌ Błąd ładowania danych trybu pracy:", err));
}


document.addEventListener("DOMContentLoaded", function () {
    let originalData = [];
    let backlogChart;

    const canvas = document.getElementById('backlogChart');
    const startInput = document.getElementById('startDate');
    const endInput = document.getElementById('endDate');
    const filterButton = document.getElementById('filterButton');

    // 🛡️ Sprawdzenie czy elementy istnieją
    if (!canvas || !startInput || !endInput || !filterButton) {
        console.warn("Nie znaleziono jednego lub więcej elementów DOM wymaganych do utworzenia wykresu.");
        return;
    }

    fetch("/backlog/hours")
        .then(response => response.json())
        .then(backlogHoursData => {
            originalData = Object.keys(backlogHoursData).map(date => ({
                date: new Date(date),
                value: backlogHoursData[date]
            }));

            originalData.sort((a, b) => a.date - b.date);
            updateChart(getLast7DaysData());
        })
        .catch(error => console.error("Błąd podczas pobierania backlogu:", error));

    function getLast7DaysData() {
        const now = new Date();
        const sevenDaysAgo = new Date();
        sevenDaysAgo.setDate(now.getDate() - 7);
        return originalData.filter(item => item.date >= sevenDaysAgo && item.date <= now);
    }

    function updateChart(filteredData) {
        const dates = filteredData.map(item => item.date.toISOString().split("T")[0]);
        const hours = filteredData.map(item => item.value);

        if (backlogChart) {
            backlogChart.destroy();
        }

        const ctx = canvas.getContext('2d');
        if (!ctx) {
            console.error("Nie udało się uzyskać kontekstu 2D dla wykresu.");
            return;
        }

        backlogChart = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: dates,
                datasets: [{
                    label: 'Backlog w godzinach',
                    data: hours,
                    backgroundColor: 'rgba(54, 162, 235, 0.5)',
                    borderColor: 'rgba(54, 162, 235, 1)',
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: true,
                aspectRatio: 1.5,
                scales: {
                    y: {
                        beginAtZero: true,
                        title: {
                            display: true,
                            text: 'Godziny'
                        }
                    },
                    x: {
                        title: {
                            display: true,
                            text: 'Data'
                        }
                    }
                },
                plugins: {
                    datalabels: {
                        anchor: 'end',
                        align: 'top',
                        formatter: (value) => value % 1 === 0 ? value : value.toFixed(2),
                        font: {
                            weight: 'bold'
                        }
                    }
                }
            },
            plugins: [ChartDataLabels]
        });
    }

    filterButton.addEventListener('click', function () {
        const startDate = new Date(startInput.value);
        const endDate = new Date(endInput.value);

        if (!isNaN(startDate) && !isNaN(endDate)) {
            const filteredData = originalData.filter(item => item.date >= startDate && item.date <= endDate);
            updateChart(filteredData);
        }
    });

    const now = new Date();
    const sevenDaysAgo = new Date();
    sevenDaysAgo.setDate(now.getDate() - 7);

    startInput.value = sevenDaysAgo.toISOString().split("T")[0];
    endInput.value = now.toISOString().split("T")[0];
});


document.addEventListener("DOMContentLoaded", function () {
    if (!window.location.pathname.includes("/efficiency")) return;

    const sectionSelect = document.getElementById("sectionFilter");
    const barCtx = document.getElementById("weeklyChart").getContext("2d");
    let weeklyChartInstance;

    function loadWeeklyData(sectionId = "all") {
        fetch(`/api/efficiency/weekly?sectionId=${sectionId}`)
            .then(response => response.json())
            .then(data => {
                const labels = data.map(d => d.date);
                const efficiency = data.map(d => d.efficiency);
                const nonOperational = data.map(d => d.nonOperational);

                renderWeeklyChart(labels, efficiency, nonOperational);
            })
            .catch(err => console.error("❌ Błąd ładowania danych wykresu tygodniowego:", err));
    }

    const efficiencyCtx = document.getElementById("weeklyChart").getContext("2d");
    const nonOperationalCtx = document.getElementById("nonOperationalChart").getContext("2d");

    let efficiencyChartInstance;
    let nonOperationalChartInstance;

    function renderWeeklyChart(labels, efficiencyData, nonOperationalData) {
        // Zniszcz poprzednie wykresy jeśli istnieją
        if (efficiencyChartInstance) efficiencyChartInstance.destroy();
        if (nonOperationalChartInstance) nonOperationalChartInstance.destroy();

        efficiencyChartInstance = new Chart(efficiencyCtx, {
            type: "bar",
            data: {
                labels: labels,
                datasets: [{
                    label: "Efektywność (%)",
                    data: efficiencyData,
                    backgroundColor: "rgba(75, 192, 192, 0.8)"
                }]
            },
            options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        datalabels: {
                            display: function(context) {
                                    return context.dataset.data[context.dataIndex] !== 0;
                            },
                            anchor: 'center',
                            align: 'center',
                            font: {
                                weight: 'bold'
                            },
                            formatter: Math.round
                        }
                    },
                    scales: {
                        y: {
                            beginAtZero: true,
                            title: {
                                display: true,
                                text: 'Efektywność (%)'
                            }
                        }
                    }
                },
                plugins: [ChartDataLabels]
            });

        nonOperationalChartInstance = new Chart(nonOperationalCtx, {
            type: "bar",
            data: {
                labels: labels,
                datasets: [{
                    label: "Czas nieoperacyjny (h)",
                    data: nonOperationalData,
                    backgroundColor: "rgba(54, 162, 235, 0.6)"
                }]
            },
            options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        datalabels: {
                            display: function(context) {
                                    return context.dataset.data[context.dataIndex] !== 0;
                            },
                            anchor: 'center',
                            align: 'center',
                            font: {
                                weight: 'bold'
                            },
                            formatter: Math.round
                        }
                    },
                    scales: {
                        y: {
                            beginAtZero: true,
                            max: 10,
                            title: {
                                display: true,
                                text: 'Łączny czas nieoperacyjny (h)'
                            }
                        }
                    }
                },
                plugins: [ChartDataLabels]
            });
    }


    function loadChartData(sectionId = "all") {
        const efficiencyUrl = `/api/efficiency/average/section?sectionId=${sectionId}`;
        const nonOpUrl = `/api/efficiency/section/non-operational?sectionId=${sectionId}`;

        Promise.all([
            fetch(efficiencyUrl).then(res => res.json()),
            fetch(nonOpUrl).then(res => res.json())
        ])
            .then(([efficiencyData, nonOpData]) => {
                const efficiency = efficiencyData.averageSectionEfficiency || 0;
                const nonOperational = nonOpData.totalNonOperationalTime || 0;
               // updateEfficiencyVsNonOperationalChart(efficiency, nonOperational);
            })
            .catch(err => console.error("❌ Błąd ładowania danych wykresu dziennego:", err));
    }

    function loadChartsForSection(sectionId = "all") {
        loadChartData(sectionId);
        loadWeeklyData(sectionId);
        loadWorkModeSummary(sectionId); // ⬅️ to wraca tutaj

    }

    sectionSelect.addEventListener("change", () => {
        const sectionId = sectionSelect.value || "all";
        loadChartsForSection(sectionId);
        loadWorkModeSummary(sectionId);
    });

    // 🔁 Załaduj na starcie
    loadChartsForSection("all");
});

//wykres wykonania pracownika na /userDetails wraz z resetowaniem filtrów
document.addEventListener("DOMContentLoaded", function () {
    if (!window.location.pathname.startsWith("/userDetails/")) return;

    let userChartInstance;
    let defaultY, defaultY1;
    let currentMaxY, currentMaxY1;
    const increaseYBtn = document.getElementById("increaseY");
    const decreaseYBtn = document.getElementById("decreaseY");
    const resetYBtn = document.getElementById("reset");

    const startDateInput = document.getElementById("startDate");
    const endDateInput = document.getElementById("endDate");
    const filterButton = document.getElementById("filterButton");
    const userId = document.getElementById("userId").value;

    const {
    setupDropdownToggle,
    } = window.filters;

    const processDropdownChart = document.getElementById("processDropdownChartUser");
    const processLabelChart = document.getElementById("processUserLabel");
    const processArrowIconChart = document.getElementById("processUserArrow");
    setupDropdownToggle({ trigger: document.getElementById("processUserSelect"), dropdown: processDropdownChart, arrow: processArrowIconChart });


    let fullData = []; // wszystkie dane z API


    const today = new Date();
    const fourteenDaysAgo = new Date();
    fourteenDaysAgo.setDate(today.getDate() - 14);

    startDateInput.value = fourteenDaysAgo.toISOString().split("T")[0];
    endDateInput.value = today.toISOString().split("T")[0];


    let processMap = {};
    fetch(`/api/saved-data/summary/${userId}`)
        .then(res => res.json())
        .then(data => {
            fullData = data;
            fetch('/api/processes/by-logged-user')
                .then(res => res.json())
                .then(processes => {
                    renderProcessCheckboxes(processDropdownChart, processes);
                    processLabelChart.textContent = "Proces";
                    // Utwórz mapę ID → Nazwa
                    processMap = Object.fromEntries(processes.map(p => [String(p.id), p.processName]));

                    // Teraz checkboxy istnieją w DOM → można bezpiecznie zebrać ID
                    const processIds = getSelectedProcessNames().map(Number);
                    const initial = filterData({ processIds });
                    // domyślna Y to max quantity + trochę zapasu
                    defaultY = Math.max(1200, Math.max(...initial.map(d => d.quantity)));
                    defaultY1 = Math.max(400, Math.max(...initial.map(d => d.efficiency)));
                    currentMaxY = defaultY;
                    currentMaxY1 = defaultY1;
                    renderChart(initial);
                })
            .catch(err => {
                console.error("❌ Błąd pobierania procesów:", err);
                processLabelChart.textContent = "Błąd";
            });
        });


    function filterData({ processIds = [] } = {}) {
            const start = new Date(startDateInput.value);
            const end = new Date(endDateInput.value);
            const selectedNames = processIds.map(id => processMap[String(id)]);
            const filtered = fullData.filter(item => {
            const itemDate = new Date(item.date);
            const inDateRange = itemDate >= start && itemDate <= end;
            const processMatch = selectedNames.length === 0 || selectedNames.includes(item.processName);
        return inDateRange && processMatch;
        });
    return filtered;
    }
    window.filterData = filterData;

    function getSelectedProcessNames() {
        return Array.from(document.querySelectorAll('#processDropdownChartUser input[type="checkbox"]:checked'))
            .map(input => input.value);
    }

    function renderProcessCheckboxes(container, processes) {
        container.innerHTML = '';
        processes.forEach(proc => {
            const label = document.createElement('label');
            label.innerHTML = `
            <input type="checkbox" value="${proc.id}" unchecked />
            ${proc.processName}
            `;
            container.appendChild(label);
        });
    }

    function renderChart(filteredData) {
        const labels = [...new Set(filteredData.map(item => item.date))];
        const processes = [...new Set(filteredData.map(item => item.processName))];

        const barDatasets = processes.map(proc => ({
        type: 'bar',
        label: proc,
        data: labels.map(date => {
            const match = filteredData.find(d => d.date === date && d.processName === proc);
        return match ? match.quantity : 0;
        }),
    backgroundColor: `hsla(${Math.floor(Math.random() * 360)}, 70%, 70%, 0.5)`,
    yAxisID: 'y',
    order: 1,
    datalabels: {
        anchor: 'start',
        align: 'top',
        color: '#444',
        font: { weight: 'bold', size: 11 },
        formatter: v => v > 50 ? v : ''
    }
    }));

    const lineData = labels.map(date => {
        const match = filteredData.find(d => d.date === date);
    return match ? match.efficiency : null;
    });

    const lineDataset = {
        type: 'line',
        label: 'Efektywność (%)',
        data: lineData,
        borderColor: '#ff6384',
        borderWidth: 2,
        tension: 0.3,
        fill: false,
        yAxisID: 'y1',
        order: 10,
        z: 10,
        clip: false,
        pointRadius: 5,
        pointHoverRadius: 7,
        pointStyle: 'circle',
        pointBorderWidth: 2,
        pointBackgroundColor: '#ff6384',
        pointBorderColor: '#fff',
        datalabels: {
        anchor: 'end',
        align: 'top',
        color: '#000',
        font: { weight: 'bold', size: 11 },
        formatter: v => v != null ? v.toFixed(0) + "%" : ''
    }
    };

    if (userChartInstance) {
        userChartInstance.destroy();
    }

    userChartInstance = new Chart(document.getElementById("barChart"), {
    data: {
        labels,
        datasets: [...barDatasets.sort((a, b) => (a.order || 0) - (b.order || 0)), lineDataset]
    },
    options: {
        responsive: true,
    plugins: {
        legend: { display: false },
        datalabels: {
        display: true,
        color: '#444',
        align: 'start',
        anchor: 'top',
        formatter: v => (typeof v === "number" && v > 0 ? v : '')
    }
    },
    scales: {
        x: { stacked: true },
        y: {
            stacked: true,
            beginAtZero: true,
            max: currentMaxY,     // 🟢 używaj aktualnych wartości skali
            position: 'left',
            title: { display: true, text: 'Ilość' }
        },
        y1: {
            beginAtZero: true,
            max: currentMaxY1,    // 🟢 używaj aktualnych wartości skali
            position: 'right',
            title: { display: true, text: 'Efektywność (%)' },
            grid: { drawOnChartArea: false }
        }
    }
    },
    plugins: [ChartDataLabels]
    });
    }

    processDropdownChart.addEventListener("change", () => {
        const processIds = getSelectedProcessNames().map(Number);
        processLabelChart.textContent = processIds.length > 0 ? `${processIds.length} wybranych` : "Proces";
        const filtered = filterData({ processIds });
        renderChart(filtered);
    });

    function updateYAxis() {
    if (!userChartInstance) return console.warn("Brak wykresu do aktualizacji osi");
    userChartInstance.options.scales.y.max  = currentMaxY;
    userChartInstance.options.scales.y1.max = currentMaxY1;
    userChartInstance.update();
    }

    increaseYBtn.addEventListener("click", () => {
    currentMaxY += 200;
    currentMaxY1 +=  50;
    updateYAxis();
    });

    decreaseYBtn.addEventListener("click", () => {
    currentMaxY  = Math.max(300, currentMaxY  - 200);
    currentMaxY1 = Math.max(150, currentMaxY1 -  50);
    updateYAxis();
    });

    resetYBtn.addEventListener("click", () => {
    currentMaxY  = defaultY;
    currentMaxY1 = defaultY1;
    updateYAxis();
    });

    // --- 7) Przyciski filtrowania zakresu dat
    filterButton.addEventListener("click", () => {

    const processIds = getSelectedProcessNames().map(Number);
    const newData = filterData({ processIds });

    defaultY  = Math.max(1200, Math.max(...newData.map(d => d.quantity)));
    defaultY1 = Math.max(600, Math.max(...newData.map(d => d.efficiency)));
    currentMaxY = defaultY;
    currentMaxY1 = defaultY1;
    renderChart(newData);
    });

    // Funkcja resetująca wszystkie filtry i renderująca czysty wykres
    function resetAllFiltersOnUser() {
        // 1. Resetowanie zakresu dat: od dziś do -14 dni
        const today = new Date();
        const fourteenDaysAgo = new Date();
        fourteenDaysAgo.setDate(today.getDate() - 14);

        document.getElementById("startDate").value = fourteenDaysAgo.toISOString().slice(0,10);
        document.getElementById("endDate").value   = today.toISOString().slice(0,10);

        // 2. Odznaczenie wszystkich checkboxów w dropdownie
        const processDropdown = document.getElementById("processDropdownChartUser");
        processDropdown.querySelectorAll("input[type='checkbox']").forEach(cb => {
            cb.checked = false;
            // jeśli masz jakąś klasę “selected” na li czy div, to ją też usuwamy:
            const container = cb.closest("li, .dropdown-item");
            if (container) container.classList.remove("selected");
        });

        // 3. Przywrócenie domyślnej etykiety
        document.getElementById("processUserLabel").textContent = "Proces";

        // 4. Przywrócenie domyślnego skalowania osi Y
        currentMaxY  = defaultY;
        currentMaxY1 = defaultY1;

        const params = {
          processIds: [],
          startDate: document.getElementById("startDate").value,
          endDate: document.getElementById("endDate").value
        };
        const initialData = filterData(params);

        // 6. Prze-renderowanie wykresu
        renderChart(initialData);
    }

    // Podpięcie listenera – wywoła się po kliknięciu na "Wyczyść"
        const btn = document.getElementById("clearAllDataOnUserChart");
        if (btn) {
          btn.addEventListener("click", e => {
            e.preventDefault();
            resetAllFiltersOnUser();
          });
        }
});





document.addEventListener("DOMContentLoaded", function () {
    const currentPath = window.location.pathname;

    if (!currentPath.startsWith("/userDetails")) return;

    if (!userId) {
      console.error("❌ Brak userId – nie można pobrać danych użytkownika.");
      return;
    }

    fetch(`/api/user/summary/${userId}`)
      .then(res => res.json())
      .then(summary => {
        const efficiencyEl = document.getElementById("averageEfficiency");
        const hoursEl = document.getElementById("nonOperationalHours");

        if (efficiencyEl) {
          efficiencyEl.innerText = `${summary.averageEfficiency.toFixed(0)}%`;
        }

        if (hoursEl) {
          hoursEl.innerText = `${(summary.totalNonOperationalHours / 60).toFixed(1)} h`;
        }
      })
      .catch(err => console.error('Błąd pobierania danych podsumowania:', err));

});

//////////////////////////////////////////wykres wykonania - execution report/////////////////////////////////////////////////////////////
async function renderChart(params) {
  const query = new URLSearchParams();
  if (params.date) query.append("date", params.date);
  if (params.sectionIds?.length) query.append("sectionIds", params.sectionIds.join(","));
  if (params.userIds?.length) query.append("userIds", params.userIds.join(","));
  if (params.processIds?.length) query.append("processId", params.processIds.join(","));

  try {
    const response = await fetch(`/api/chart/stacked-summary?${query.toString()}`);
    const data = await response.json();

    if (chartInstance) chartInstance.destroy();
    Chart.register(ChartDataLabels);

    const ctx = document.getElementById("executionChart").getContext("2d");
    chartInstance = new Chart(ctx, {
      type: "bar",
      data: {
        labels: data.labels,
        datasets: data.datasets
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          datalabels: {
            color: 'black',
            font: {
              weight: 'bold'
            },
            display: function (context) {
              const value = context.dataset.data[context.dataIndex];
              const isBar = context.dataset.type === 'bar' || !context.dataset.type;
              const isLine = context.dataset.type === 'line';

              if (isBar) {
                return typeof value === 'number' && value > 50;
              }
              if (isLine) {
                return true;
              }
              return false;
            },
            formatter: function (value, context) {
              return context.dataset.type === 'line'
                ? `${value.toFixed(0)}%`
                : Math.round(value);
            },
            anchor: 'start',
            align: 'top'
          },
          tooltip: {
            callbacks: {
              filter: function (tooltipItem) {
                return tooltipItem.parsed.y !== 0;
              }
            }
          },
          legend: { display: false }
        },
        scales: {
          x: { stacked: true },
          y: {
            stacked: true,
            beginAtZero: true,
            max: currentMaxY,
            title: { display: true, text: "Ilość" }
          },
          y1: {
            position: 'right',
            beginAtZero: true,
            max: currentMaxY1,
            title: { display: true, text: "Efektywność (%)" },
            grid: { drawOnChartArea: false }
          }
        }
      }
    });

    // 🔄 Aktualizuj filtr procesów na podstawie widocznych danych
    if (typeof window.updateProcessFilter === "function") {
      window.updateProcessFilter(data.datasets);
    }

  } catch (err) {
    console.error("Błąd podczas wczytywania danych wykresu:", err);
  }
}
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

document.addEventListener("DOMContentLoaded", function () {
  setupYAxisControls(() => chartInstance, {
    increaseBtnId: "increaseYExec",
    decreaseBtnId: "decreaseYExec",
    resetBtnId: "resetYExec",
    defaultY: 1200,
    defaultY1: 400,
    stepY: 200,
    stepY1: 50,
    minY: 300,
    minY1: 150
  });
});


document.getElementById("filterButton").addEventListener("click", () => {
    const reportDateInput = document.getElementById("reportDate");
    if (!reportDateInput) {
      //  console.warn("⛔ Element #reportDate nie został znaleziony.");
        return;
    }

    const reportDate = reportDateInput.value;
    const sectionIds = getSelectedSectionIds();
    const userIds = getSelectedUserIds();
    const processIds = getSelectedProcessNames();

    renderChart({
      date: reportDate,
      sectionIds,
      userIds,
      processIds
    });
});

window.addEventListener("DOMContentLoaded", () => {
    const today = new Date().toISOString().split("T")[0];
    const reportDateInput = document.getElementById("reportDate");
    if (!reportDateInput) {
      //  console.warn("⛔ Element #reportDate nie został znaleziony.");
        return;
    }
    document.getElementById("reportDate").value = today;

    renderChart({ date: today });
});

let currentMaxY = 1200, defaultY = 1200;
let currentMaxY1 = 400, defaultY1 = 400;



function setupYAxisControls(getChartInstance, config) {
 const increaseBtn = document.getElementById(config.increaseBtnId);
  const decreaseBtn = document.getElementById(config.decreaseBtnId);
  const resetBtn = document.getElementById(config.resetBtnId);

  if (!increaseBtn || !decreaseBtn || !resetBtn) {
   // console.warn("⛔ Nie znaleziono jednego z przycisków do manipulacji osią.");
    return;
  }

  let currentMaxY = config.defaultY;
  let currentMaxY1 = config.defaultY1;

  function updateYAxis() {
    const chart = getChartInstance();
    if (!chart || !chart.options) return;

    chart.options.scales.y.max = currentMaxY;
    chart.options.scales.y1.max = currentMaxY1;
    chart.update();
  }

  document.getElementById(config.increaseBtnId).addEventListener("click", () => {
    currentMaxY += config.stepY;
    currentMaxY1 += config.stepY1;
    updateYAxis();
  });

  document.getElementById(config.decreaseBtnId).addEventListener("click", () => {
    currentMaxY = Math.max(config.minY, currentMaxY - config.stepY);
    currentMaxY1 = Math.max(config.minY1, currentMaxY1 - config.stepY1);
    updateYAxis();
  });

  document.getElementById(config.resetBtnId).addEventListener("click", () => {
    currentMaxY = config.defaultY;
    currentMaxY1 = config.defaultY1;
    updateYAxis();
  });

  updateYAxis();

}