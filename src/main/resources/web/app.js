function setStatus(msg, isError) {
    const el = document.getElementById('status');
    el.textContent = msg;
    el.className = isError ? 'error' : 'success';
}

async function parse() {
    setStatus('Парсинг... подождите', false);
    const res = await fetch('/api/parse', { method: 'POST' });
    const data = await res.json();
    setStatus(data.message, data.status !== 'ok');
}

async function loadEvents() {
    const res = await fetch('/api/events');
    const events = await res.json();

    if (!events.length) {
        setStatus('Событий нет. Сначала запустите парсинг.', true);
        return;
    }

    // Группируем по категории
    const grouped = {};
    events.forEach(e => {
        if (!grouped[e.category]) grouped[e.category] = [];
        grouped[e.category].push(e);
    });

    let html = '';
    for (const [category, items] of Object.entries(grouped)) {
        html += `<div class="category"><h2>${category} (${items.length})</h2>`;
        items.forEach(e => {
            html += `
                <div class="event">
                    <div class="event-date">${e.date || '—'} | ${e.channel}</div>
                    <div class="event-text">${e.text.substring(0, 150)}...</div>
                    ${e.students ? `<div class="event-students">👤 ${e.students}</div>` : ''}
                    <a href="${e.link}" target="_blank">🔗 Открыть пост</a>
                </div>`;
        });
        html += '</div>';
    }

    document.getElementById('events').innerHTML = html;
    setStatus(`Загружено событий: ${events.length}`, false);
}

async function generateReport() {
    const period = document.getElementById('period').value || 'текущий';
    const res = await fetch(`/api/report?period=${period}`, { method: 'POST' });
    const data = await res.json();
    setStatus(data.message, data.status !== 'ok')
    }