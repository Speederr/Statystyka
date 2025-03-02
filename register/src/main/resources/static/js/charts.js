//document.addEventListener("DOMContentLoaded", function () {
//    // Pobranie liczby obecnych i nieobecnych pracowników
//    const presentCount = parseInt(document.getElementById("presentCount")?.textContent.trim()) || 0;
//    const onLeaveCount = parseInt(document.getElementById("onLeaveCount")?.textContent.trim()) || 0;
//
//    // Pobranie listy pracowników
//    const presentNames = Array.from(document.querySelectorAll("#presentList li"))
//        .map(li => li.dataset.name);
//    const onLeaveNames = Array.from(document.querySelectorAll("#onLeaveList li"))
//        .map(li => li.dataset.name);
//
//    console.log("Obecni:", presentNames);
//    console.log("Na urlopie:", onLeaveNames);
//
//    // Pobranie kontekstu dla wykresu
//    const ctx = document.getElementById("availabilityChart")?.getContext("2d");
//
//    if (!ctx) {
//        console.warn("Element canvas dla wykresu nie istnieje.");
//        return;
//    }
//
//    // Wykres kołowy
//    new Chart(ctx, {
//        type: "doughnut",
//        data: {
//            labels: ["Obecni", "Nieobecni"],
//            datasets: [{
//                data: [presentCount, onLeaveCount], // Liczby pracowników
//                backgroundColor: ["#4CAF50", "#FF5733"],
//                borderWidth: 1
//            }]
//        },
//        options: {
//            responsive: true,
//            plugins: {
//                legend: {
//                    position: "bottom"
//                },
//                tooltip: {
//                    callbacks: {
//                        label: function (tooltipItem) {
//                            let category = tooltipItem.dataIndex === 0 ? "Obecni" : "Nieobecni";
//                            let names = tooltipItem.dataIndex === 0 ? presentNames : onLeaveNames;
//                            return [`${category}: ${names.length}`, ...names];
//                        }
//                    }
//                }
//            }
//        }
//    });
//});

document.addEventListener("DOMContentLoaded", function () {
    const presentCount = parseInt(document.getElementById("presentCount")?.textContent.trim()) || 0;
    const onLeaveCount = parseInt(document.getElementById("onLeaveCount")?.textContent.trim()) || 0;

    // Pobieranie listy obecnych i na urlopie
    const presentNames = Array.from(document.querySelectorAll("#presentList li")).map(li => li.dataset.name);
    const onLeaveNames = Array.from(document.querySelectorAll("#onLeaveList li")).map(li => li.dataset.name);

    const ctx = document.getElementById("availabilityChart").getContext("2d");

    new Chart(ctx, {
        type: "doughnut",
        data: {
            labels: ["Obecni", "Nieobecni"],
            datasets: [{
                data: [presentCount, onLeaveCount],
                backgroundColor: ["#4CAF50", "#FF5733"], // Zielony dla obecnych, czerwony dla urlopu
                borderWidth: 2, // Cieńsza obwódka
                hoverOffset: 8, // Powiększenie po najechaniu
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            cutout: "65%", // 🔹 Cieńszy pierścień jak w pierwszym przykładzie
            plugins: {
                legend: {
                    display: false, // Ukrywamy wbudowaną legendę Chart.js
                },
                tooltip: {
                    enabled: true,
                    callbacks: {
                        label: function (tooltipItem) {
                            let index = tooltipItem.dataIndex;
                            let category = index === 0 ? "Obecni" : "Nieobecni";
                            let names = index === 0 ? presentNames : onLeaveNames;

                            return [`${category}: ${names.length}`, ...names];
                        }
                    }
                }
            }
        }
    });
});

document.addEventListener("DOMContentLoaded", function () {
    const ctx = document.getElementById("backlogTrendChart").getContext("2d");

    // Dane przykładowe – zamień na dynamiczne z backendu
    const backlogData = [10, 15, 13, 18, 25, 30, 27]; // Liczba spraw w backlogu każdego dnia
    const dates = ["01 Mar", "02 Mar", "03 Mar", "04 Mar", "05 Mar", "06 Mar", "07 Mar"]; // Daty

    new Chart(ctx, {
        type: "line",
        data: {
            labels: dates,
            datasets: [{
                label: "Liczba spraw w backlogu",
                data: backlogData,
                borderColor: "#FF5733",
                backgroundColor: "rgba(255, 87, 51, 0.2)",
                borderWidth: 2,
                fill: true,
                tension: 0.3
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: true,
                    position: "bottom"
                }
            },
            scales: {
                x: {
                    title: {
                        display: true,
                        text: "Data"
                    }
                },
                y: {
                    beginAtZero: true,
                    title: {
                        display: true,
                        text: "Liczba spraw"
                    }
                }
            }
        }
    });
});
