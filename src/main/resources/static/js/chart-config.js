/* ============================================================
   HandOver.AI — chart-config.js  (Chart.js helpers)
   ============================================================ */

/**
 * Creates a readiness doughnut chart.
 * @param {string} canvasId – id of the <canvas> element
 * @param {number} score – readiness percentage 0-100
 * @returns {Chart}
 */
function createReadinessChart(canvasId, score) {
  const ctx = document.getElementById(canvasId);
  if (!ctx) return null;
  const missing = 100 - score;

  return new Chart(ctx, {
    type: 'doughnut',
    data: {
      labels: ['Completed', 'Missing'],
      datasets: [{
        data: [score, missing],
        backgroundColor: ['#009688', 'rgba(255,193,7,0.35)'],
        borderColor: ['#00796B', 'rgba(255,193,7,0.6)'],
        borderWidth: 2,
        hoverOffset: 6
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: true,
      cutout: '72%',
      plugins: {
        legend: {
          position: 'bottom',
          labels: {
            color: getComputedStyle(document.documentElement).getPropertyValue('--ho-text').trim() || '#E8EAF0',
            padding: 16,
            font: { size: 13, weight: '600' }
          }
        },
        tooltip: {
          callbacks: {
            label: ctx => `${ctx.label}: ${ctx.parsed}%`
          }
        }
      }
    }
  });
}

/**
 * Creates a task status bar chart.
 * @param {string} canvasId
 * @param {{ completed: number, inProgress: number, blocked: number, overdue: number }} counts
 * @returns {Chart}
 */
function createTaskStatusChart(canvasId, counts) {
  const ctx = document.getElementById(canvasId);
  if (!ctx) return null;

  return new Chart(ctx, {
    type: 'bar',
    data: {
      labels: ['Completed', 'In Progress', 'Blocked', 'Overdue'],
      datasets: [{
        label: 'Tasks',
        data: [counts.completed || 0, counts.inProgress || 0, counts.blocked || 0, counts.overdue || 0],
        backgroundColor: ['#4CAF50', '#2196F3', '#F44336', '#FF9800'],
        borderRadius: 6,
        barThickness: 36
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: true,
      plugins: {
        legend: { display: false }
      },
      scales: {
        x: {
          ticks: {
            color: getComputedStyle(document.documentElement).getPropertyValue('--ho-text-muted').trim() || '#8892A4',
            font: { weight: '600' }
          },
          grid: { display: false }
        },
        y: {
          beginAtZero: true,
          ticks: {
            stepSize: 1,
            color: getComputedStyle(document.documentElement).getPropertyValue('--ho-text-muted').trim() || '#8892A4'
          },
          grid: { color: 'rgba(255,255,255,0.05)' }
        }
      }
    }
  });
}
