let chartInstance = null; // Przechowujemy instancję wykresu globalnie
let remoteChartInstance = null;
let userChartInstance = null;
const userId = document.getElementById("userId").value;

// Funkcja aktualizacji wykresu
function updateChart(presentCount, onLeaveCount, notLoggedCount, presentNames, onLeaveNames, notLoggedNames, totalEmployees) {
    const canvas = document.getElementById("availabilityChart");
    const ctx = canvas.getContext("2d");

    // ✅ Aktualizacja liczników w legendzie i podsumowaniu
    document.getElementById("legendPresentCount").textContent = presentCount;
    document.getElementById("legendOnLeaveCount").textContent = onLeaveCount;
    document.getElementById("legendNotLoggedCount").textContent = notLoggedCount;
    document.getElementById("employeeCount").textContent = `${presentCount}/${totalEmployees}`;

    // 🛑 Usuwanie starego wykresu
    if (chartInstance !== null) {
        chartInstance.destroy();
        chartInstance = null;
        console.log("🗑️ Stary wykres został usunięty");
    }

    // ✅ Tworzenie nowego wykresu
    chartInstance = new Chart(ctx, {
        type: "doughnut",
        data: {
            labels: ["Obecni", "Nieobecni", "Niezalogowani"],
            datasets: [{
                data: [presentCount, onLeaveCount, notLoggedCount],
                backgroundColor: [
                    "rgba(54, 162, 235, 0.6)",   // Obecni
                    "rgba(75, 192, 192, 0.8)",   // Nieobecni
                    "rgba(255, 99, 132, 0.8)"    // Niezalogowani
                ],
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
                tooltip: {
                    enabled: true,
                    callbacks: {
                        label: function (tooltipItem) {
                            const index = tooltipItem.dataIndex;
                            let category = "";
                            let names = [];

                            switch (index) {
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

                            return [`${category}: ${names.length}`, ...names];
                        }
                    }
                }
            }
        }
    });

//    console.log(`✅ Wykres zaktualizowany: Obecni: ${presentCount}, Nieobecni: ${onLeaveCount}, Niezalogowani: ${notLoggedCount}, Razem: ${totalEmployees}`);
}


function updateRemoteChart(officeCount, homeofficeCount, officeNames, homeofficeNames) {
    const ctx = document.getElementById("remoteWorkChart").getContext("2d");

    // Aktualizacja liczników
    document.getElementById("legendOfficeCount").textContent = officeCount;
    document.getElementById("legendHomeofficeCount").textContent = homeofficeCount;

    if (remoteChartInstance) {
        remoteChartInstance.destroy();
    }

    remoteChartInstance = new Chart(ctx, {
        type: "doughnut",
        data: {
            labels: ["Biuro", "Zdalnie"],
            datasets: [{
                data: [officeCount, homeofficeCount],
                backgroundColor: [
                    "rgba(255, 205, 86, 0.8)",    // Biuro
                    "rgba(153, 102, 255, 0.8)"     // Zdalnie
                ],
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
                tooltip: {
                    enabled: true,
                    callbacks: {
                        label: function (tooltipItem) {
                            const index = tooltipItem.dataIndex;
                            const names = index === 0 ? officeNames : homeofficeNames;
                            const label = index === 0 ? "Biuro" : "Zdalnie";
                            return [`${label}: ${names.length}`, ...names];
                        }
                    }
                }
            }
        }
    });
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

    function renderWeeklyChart(labels, efficiencyData, nonOperationalData) {
        if (weeklyChartInstance) {
            weeklyChartInstance.destroy();
        }

        weeklyChartInstance = new Chart(barCtx, {
            type: "bar",
            data: {
                labels: labels,
                datasets: [
                    {
                        label: "Efektywność (%)",
                        data: efficiencyData,
                        backgroundColor: "rgba(75, 192, 192, 0.8)",
                        yAxisID: 'y'
                    },
                    {
                        label: "Czas nieoperacyjny (h)",
                        data: nonOperationalData,
                        backgroundColor: "rgba(54, 162, 235, 0.6)",
                        yAxisID: 'y1'
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                interaction: {
                    mode: 'index',
                    intersect: false
                },
//                plugins: {
//                    title: {
//                        display: true,
//                        text: 'Efektywność i czas nieoperacyjny (ostatnie 7 dni)'
//                    }
//                },
                scales: {
                    y: {
                        type: 'linear',
                        position: 'left',
                        title: {
                            display: true,
                            text: 'Efektywność (%)'
                        }
                    },
                    y1: {
                        type: 'linear',
                        position: 'right',
                        grid: {
                            drawOnChartArea: false
                        },
                        min: 0,
                        max: 40, // lub większy, jeśli dane rosną
                        title: {
                            display: true,
                            text: 'Czas nieoperacyjny (h)'
                        }
                    }
                }
            }
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
                const nonOperational = nonOpData.averageNonOperationalTime || 0;
               // updateEfficiencyVsNonOperationalChart(efficiency, nonOperational);
            })
            .catch(err => console.error("❌ Błąd ładowania danych wykresu dziennego:", err));
    }

    function loadChartsForSection(sectionId = "all") {
        loadChartData(sectionId);
        loadWeeklyData(sectionId);
    }

    sectionSelect.addEventListener("change", () => {
        const sectionId = sectionSelect.value || "all";
        loadChartsForSection(sectionId);
    });

    // 🔁 Załaduj na starcie
    loadChartsForSection("all");
});



let currentMaxY = 600;
let currentMaxY1 = 250;
const defaultY = 600;
const defaultY1 = 250;

document.addEventListener("DOMContentLoaded", function () {
    if (!window.location.pathname.startsWith("/userDetails/")) return;

    const startDateInput = document.getElementById("startDate");
    const endDateInput = document.getElementById("endDate");
    const filterButton = document.getElementById("filterButton");
    const userId = document.getElementById("userId").value;

    let fullData = []; // wszystkie dane z API
    let userChartInstance;

    const today = new Date();
    const fourteenDaysAgo = new Date();
    fourteenDaysAgo.setDate(today.getDate() - 14);

    startDateInput.value = fourteenDaysAgo.toISOString().split("T")[0];
    endDateInput.value = today.toISOString().split("T")[0];

    fetch(`/api/saved-data/summary/${userId}`)
      .then(res => res.json())
      .then(data => {
        fullData = data;
        renderChart(filterData());
      });

    function filterData() {
        const start = new Date(startDateInput.value);
        const end = new Date(endDateInput.value);

        return fullData.filter(item => {
            const itemDate = new Date(item.date);
            return itemDate >= start && itemDate <= end;
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
                formatter: v => v > 0 ? v : ''
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

    filterButton.addEventListener("click", () => {
        renderChart(filterData());
    });

    // Obsługa przycisków zmiany skali
    document.getElementById("increaseY").addEventListener("click", function () {
        currentMaxY += 200;
        currentMaxY1 += 50;
        updateYAxis();
    });

    document.getElementById("decreaseY").addEventListener("click", function () {
        currentMaxY = Math.max(300, currentMaxY - 200);
        currentMaxY1 = Math.max(150, currentMaxY1 - 50);
        updateYAxis();
    });

    document.getElementById("reset").addEventListener("click", function () {
        currentMaxY = defaultY;
        currentMaxY1 = defaultY1;
        updateYAxis();
    });

    function updateYAxis() {
        if (userChartInstance) {
            userChartInstance.options.scales.y.max = currentMaxY;
            userChartInstance.options.scales.y1.max = currentMaxY1;
            userChartInstance.update();
        }
    }
});


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

