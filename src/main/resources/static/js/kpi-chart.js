function renderChart(rawData) {

    const labels  = rawData.map(d => d.mois  || d.MOIS  || '');
    const values  = rawData.map(d => parseFloat(d.total || d.TOTAL || 0));

    const ctx = document.getElementById('caChart').getContext('2d');
    new Chart(ctx, {
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
}

window.chargerGraphique = function(rawData) {
    if (typeof renderChart !== 'function') return;
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', function() { renderChart(rawData); });
    } else {
        renderChart(rawData);
    }
};