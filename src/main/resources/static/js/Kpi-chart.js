let caChart;

document.addEventListener("DOMContentLoaded", function () {

    document.getElementById("applyFilter")
        .addEventListener("click", loadKpi);

    loadKpi(); // chargement initial
});

function loadKpi() {

    const mois = document.getElementById("moisFilter").value;
    const annee = document.getElementById("anneeFilter").value;

    fetch(`/finance/kpi/data?mois=${mois}&annee=${annee}`)
        .then(res => res.json())
        .then(data => {
            document.getElementById("caValue").textContent = data.ca;
            document.getElementById("beneficeValue").textContent = data.benefice;
            document.getElementById("entreesValue").textContent = data.entrees;
            document.getElementById("sortiesValue").textContent = data.sorties;
            document.getElementById("soldeValue").textContent = data.solde;
            
            console.log("DATA KPI:", data);

            drawChart(data.evolutionCA);
        })
        .catch(err => console.error(err));
}
/* CHART */

function drawChart(data) {

    const canvas = document.getElementById("caChart");
    if (!canvas || !data) return;

    const labels = data.map(d => d.mois || '');
    const values = data.map(d => Number(d.total || 0));

    const ctx = canvas.getContext("2d");

    if (caChart) {
        caChart.destroy();
    }

    caChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                label: "CA (MGA)",
                data: values,
                backgroundColor: 'rgba(64,123,74,0.75)',
                borderColor: '#407B4A',
                borderWidth: 2,
                borderRadius: 6,
                hoverBackgroundColor: '#C79E5C'
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: { display: false },
                tooltip: {
                    callbacks: {
                        label: ctx => ctx.formattedValue + ' MGA'
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    grid: { color: 'rgba(0,0,0,.05)' },
                    ticks: { callback: v => v.toLocaleString() + ' MGA' }
                },
                x: { grid: { display: false } }
            }
        }
    });
};
