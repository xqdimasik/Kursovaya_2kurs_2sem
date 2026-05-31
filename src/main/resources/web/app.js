function setStatus(msg, isError) {
    const el = document.getElementById('status');
    el.textContent = msg;
    el.className = isError ? 'error' : 'success';
}


let channels = [];

async function initChannels() {
    try {
        const res = await fetch('/api/channels');
        channels = await res.json();
        renderChannels();
    } catch (e) {
        setStatus('Ошибка загрузки каналов: ' + e.message, true);
    }
}

function renderChannels() {
    const container = document.getElementById('channelsList');
    container.innerHTML = '';
    channels.forEach((ch, i) => {
        const div = document.createElement('div');
        div.className = 'channel-item' + (ch.selected ? ' selected' : '');
        div.innerHTML = `
            <input type="checkbox" id="ch_${i}" ${ch.selected ? 'checked' : ''}
                onchange="toggleChannel(${i})">
            <label for="ch_${i}" style="cursor:pointer">
                <div class="channel-name">@${ch.name}</div>
                <div class="channel-url">t.me/${ch.name}</div>
            </label>
            <button class="btn-remove-channel" onclick="removeChannel(${i})">✕</button>
        `;
        container.appendChild(div);
    });
}

function toggleChannel(index) {
    channels[index].selected = !channels[index].selected;
    renderChannels();
    saveChannelsSilent();
}

function addChannel() {
    const input = document.getElementById('newChannel');
    const name = input.value.trim().replace('@', '');
    if (!name) return;
    if (channels.find(c => c.name === name)) {
        setStatus('Такой канал уже есть', true);
        return;
    }
    channels.push({ name, selected: true });
    input.value = '';
    renderChannels();
    saveChannelsSilent();
}

function removeChannel(index) {
    channels.splice(index, 1);
    renderChannels();
    saveChannelsSilent();
}

async function saveChannelsSilent() {
    try {
        await fetch('/api/channels', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(channels)
        });
    } catch (e) {
        console.error('Ошибка сохранения каналов:', e);
    }
}


async function parse() {
    const selected = channels.filter(c => c.selected);
    if (selected.length === 0) {
        setStatus('Выберите хотя бы один канал', true);
        return;
    }
    setStatus(`Парсинг ${selected.length} канал(ов)... подождите`, false);
    try {
        const res = await fetch('/api/parse', { method: 'POST' });
        if (!res.ok) throw new Error('Ошибка сервера: ' + res.status);
        const data = await res.json();
        setStatus(data.message, data.status !== 'ok');
    } catch (e) {
        setStatus('Ошибка: ' + e.message, true);
    }
}


function parsePeriod(input) {
    const parts = input.trim().split('-');

    // Диапазон: 01.05.2026-29.05.2026
    if (parts.length === 2 && parts[0].includes('.') && parts[1].includes('.')) {
        const from = parseDate(parts[0].trim());
        const to = parseDate(parts[1].trim());
        if (from && to) return { from, to };
    }

    // Один день: 01.05.2026
    if (parts.length === 1 && parts[0].includes('.')) {
        const day = parseDate(parts[0].trim());
        if (day) return { from: day, to: day };
    }

    return null;
}

function parseDate(str) {
    const [d, m, y] = str.split('.');
    if (!d || !m || !y) return null;
    const date = new Date(`${y}-${m}-${d}`);
    return isNaN(date) ? null : date;
}

function inPeriod(dateStr, from, to) {
    if (!dateStr) return false;
    const date = new Date(dateStr);
    const f = new Date(from); f.setHours(0, 0, 0, 0);
    const t = new Date(to);   t.setHours(23, 59, 59, 999);
    return date >= f && date <= t;
}


async function loadEvents() {
    document.getElementById('categoriesPanel').style.display = 'none';

    const periodInput = document.getElementById('period').value.trim();
    if (!periodInput) {
        setStatus('Введите период для отображения событий', true);
        document.getElementById('events').innerHTML = '';
        return;
    }

    const period = parsePeriod(periodInput);
    if (!period) {
        setStatus('Неверный формат. Используйте: 01.05.2026 или 01.05.2026-29.05.2026', true);
        return;
    }

    try {
        const res = await fetch('/api/events');
        const events = await res.json();

        const filtered = events.filter(e => inPeriod(e.date, period.from, period.to));

        if (!filtered.length) {
            setStatus('Нет событий за указанный период', true);
            document.getElementById('events').innerHTML = '';
            return;
        }

        const grouped = {};
        filtered.forEach(e => {
            if (!grouped[e.category]) grouped[e.category] = [];
            grouped[e.category].push(e);
        });

        let html = '';
        for (const [category, items] of Object.entries(grouped)) {
            html += `<div class="category"><h2>${category} (${items.length})</h2>`;
            items.forEach(e => {
                html += `
                    <div class="event">
                        <div class="event-date">${e.date || '—'} | @${e.channel}</div>
                        <div class="event-text">${e.text.substring(0, 150)}...</div>
                        ${e.students ? `<div class="event-students">👤 ${e.students}</div>` : ''}
                        <a href="${e.link}" target="_blank">🔗 Открыть пост</a>
                    </div>`;
            });
            html += '</div>';
        }

        document.getElementById('events').innerHTML = html;
        setStatus(`Найдено событий за период: ${filtered.length}`, false);
    } catch (e) {
        setStatus('Ошибка загрузки: ' + e.message, true);
    }
}


async function generateReport() {
    const periodInput = document.getElementById('period').value.trim();
    if (!periodInput) {
        setStatus('Введите период для генерации отчёта', true);
        return;
    }
    try {
        const res = await fetch(`/api/report?period=${encodeURIComponent(periodInput)}`, { method: 'POST' });
        const data = await res.json();
        setStatus(data.message, data.status !== 'ok');
    } catch (e) {
        setStatus('Ошибка: ' + e.message, true);
    }
}


let categories = [];

async function showCategories() {
    const panel = document.getElementById('categoriesPanel');
    const isVisible = panel.style.display === 'block';
    panel.style.display = isVisible ? 'none' : 'block';
    document.getElementById('events').innerHTML = '';
    if (!isVisible) {
        try {
            const res = await fetch('/api/categories');
            categories = await res.json();
            renderCategories();
        } catch (e) {
            setStatus('Ошибка загрузки категорий: ' + e.message, true);
        }
    }
}

function renderCategories() {
    const tbody = document.getElementById('categoriesBody');
    tbody.innerHTML = '';
    categories.forEach((cat, i) => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td><input type="text" value="${cat.name}"
                onchange="categories[${i}].name=this.value"></td>
            <td><input type="text" value="${cat.pattern}"
                onchange="categories[${i}].pattern=this.value"
                class="pattern-input"></td>
            <td><button onclick="deleteCategory(${i})" class="btn btn-delete">🗑</button></td>
        `;
        tbody.appendChild(tr);
    });
}

function addCategory() {
    categories.push({ name: 'Новая категория', pattern: '' });
    renderCategories();
}

function deleteCategory(index) {
    categories.splice(index, 1);
    renderCategories();
}

async function saveCategories() {
    try {
        const res = await fetch('/api/categories', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(categories)
        });
        const data = await res.json();
        setStatus(data.message, data.status !== 'ok');
    } catch (e) {
        setStatus('Ошибка сохранения: ' + e.message, true);
    }
}

initChannels();