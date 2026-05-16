/**
 * Flash-Cart v2.0 — Premium Frontend Application
 * JWT Authentication, Categories, Split Types, Settlement Recording
 */

// Set your production backend URL here once you deploy the Spring Boot app (e.g., to Railway or Render)
const PROD_BACKEND_URL = 'https://your-backend-app.up.railway.app'; 

const API = window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1'
    ? 'http://localhost:8080/api'
    : `${PROD_BACKEND_URL}/api`;

const state = {
    token: localStorage.getItem('fc_token'),
    user: JSON.parse(localStorage.getItem('fc_user') || 'null'),
    currentView: 'dashboard',
    groups: [],
    splitType: 'EQUAL'
};

// ============================================================================
// INIT
// ============================================================================
document.addEventListener('DOMContentLoaded', () => {
    if (state.token && state.user) {
        showApp();
        loadDashboard();
    } else {
        showAuth();
    }
    setupListeners();
});

function showAuth() {
    document.getElementById('auth-screen').style.display = 'flex';
    document.getElementById('app-shell').style.display = 'none';
}

function showApp() {
    document.getElementById('auth-screen').style.display = 'none';
    document.getElementById('app-shell').style.display = 'flex';
    if (state.user) {
        document.getElementById('sidebar-username').textContent = state.user.username || 'User';
        document.getElementById('sidebar-email').textContent = state.user.email || '';
        document.getElementById('user-avatar').textContent = (state.user.username || 'U')[0].toUpperCase();
    }
}

// ============================================================================
// EVENT LISTENERS
// ============================================================================
function setupListeners() {
    // Auth tabs
    document.querySelectorAll('.auth-tab').forEach(tab => {
        tab.addEventListener('click', () => {
            document.querySelectorAll('.auth-tab').forEach(t => t.classList.remove('active'));
            document.querySelectorAll('.auth-form').forEach(f => f.classList.remove('active'));
            tab.classList.add('active');
            const form = tab.dataset.tab === 'login' ? 'login-form' : 'register-form';
            document.getElementById(form).classList.add('active');
        });
    });

    // Auth forms
    document.getElementById('login-form').addEventListener('submit', handleLogin);
    document.getElementById('register-form').addEventListener('submit', handleRegister);

    // Navigation
    document.querySelectorAll('.nav-item').forEach(item => {
        item.addEventListener('click', (e) => {
            e.preventDefault();
            const view = e.currentTarget.dataset.view;
            switchView(view);
            // Close mobile sidebar
            document.getElementById('sidebar').classList.remove('open');
        });
    });

    // Logout
    document.getElementById('logout-btn').addEventListener('click', logout);
    const logoutMobile = document.getElementById('logout-btn-mobile');
    if (logoutMobile) logoutMobile.addEventListener('click', logout);

    // Mobile menu
    const menuToggle = document.getElementById('menu-toggle');
    if (menuToggle) menuToggle.addEventListener('click', () => {
        document.getElementById('sidebar').classList.toggle('open');
    });

    // Create group
    document.getElementById('create-group-btn').addEventListener('click', () => openModal('group-modal'));
    document.getElementById('create-group-form').addEventListener('submit', handleCreateGroup);

    // Add expense
    document.getElementById('add-expense-form').addEventListener('submit', handleAddExpense);
    document.getElementById('expense-group').addEventListener('change', onGroupSelectChange);

    // Split type selector
    document.querySelectorAll('.split-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            document.querySelectorAll('.split-btn').forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            state.splitType = btn.dataset.split;
            updateSplitInputs();
        });
    });

    // Payment form
    document.getElementById('payment-form').addEventListener('submit', handleRecordPayment);

    // Modal close
    document.querySelectorAll('.modal-close, .modal-cancel').forEach(btn => {
        btn.addEventListener('click', closeAllModals);
    });
    document.querySelectorAll('.modal').forEach(m => {
        m.addEventListener('click', (e) => { if (e.target === m) m.style.display = 'none'; });
    });

    // Populate categories
    populateCategories();
}

// ============================================================================
// AUTH
// ============================================================================
async function handleLogin(e) {
    e.preventDefault();
    const email = document.getElementById('login-email').value;
    const password = document.getElementById('login-password').value;
    try {
        const res = await fetch(`${API}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password })
        });
        const data = await res.json();
        if (!res.ok) throw new Error(data.error || 'Login failed');
        saveAuth(data.token, data.user);
        showApp();
        loadDashboard();
        toast('Welcome back, ' + data.user.username + '!', 'success');
    } catch (err) {
        toast(err.message, 'error');
    }
}

async function handleRegister(e) {
    e.preventDefault();
    const username = document.getElementById('reg-username').value;
    const email = document.getElementById('reg-email').value;
    const password = document.getElementById('reg-password').value;
    try {
        const res = await fetch(`${API}/auth/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, email, password })
        });
        const data = await res.json();
        if (!res.ok) throw new Error(data.error || 'Registration failed');
        saveAuth(data.token, data.user);
        showApp();
        loadDashboard();
        toast('Account created! Welcome, ' + data.user.username + '!', 'success');
    } catch (err) {
        toast(err.message, 'error');
    }
}

function saveAuth(token, user) {
    state.token = token;
    state.user = user;
    localStorage.setItem('fc_token', token);
    localStorage.setItem('fc_user', JSON.stringify(user));
}

function logout() {
    state.token = null;
    state.user = null;
    localStorage.removeItem('fc_token');
    localStorage.removeItem('fc_user');
    showAuth();
}

function authHeaders() {
    return { 'Content-Type': 'application/json', 'Authorization': `Bearer ${state.token}` };
}

async function apiFetch(url, options = {}) {
    options.headers = { ...authHeaders(), ...options.headers };
    const res = await fetch(url, options);
    if (res.status === 401 || res.status === 403) { logout(); throw new Error('Session expired'); }
    return res;
}

// ============================================================================
// NAVIGATION
// ============================================================================
function switchView(viewName) {
    state.currentView = viewName;
    document.querySelectorAll('.nav-item').forEach(n => n.classList.remove('active'));
    const navItem = document.querySelector(`.nav-item[data-view="${viewName}"]`);
    if (navItem) navItem.classList.add('active');
    document.querySelectorAll('.view').forEach(v => v.classList.remove('active'));
    const view = document.getElementById(`${viewName}-view`);
    if (view) view.classList.add('active');

    switch (viewName) {
        case 'dashboard': loadDashboard(); break;
        case 'groups': loadGroups(); break;
        case 'add-expense': loadExpenseForm(); break;
        case 'settle': loadSettlements(); break;
        case 'activity': loadActivity(); break;
    }
}

// ============================================================================
// DASHBOARD
// ============================================================================
async function loadDashboard() {
    try {
        const res = await apiFetch(`${API}/dashboard`);
        if (!res.ok) throw new Error('Failed to load dashboard');
        const d = await res.json();
        const cur = d.currency === 'INR' ? '₹' : '$';
        document.getElementById('total-owes').textContent = `${cur}${fmt(d.totalOwes)}`;
        document.getElementById('total-owed').textContent = `${cur}${fmt(d.totalOwed)}`;
        const net = d.netBalance || 0;
        const netEl = document.getElementById('net-balance');
        netEl.textContent = `${net >= 0 ? '+' : ''}${cur}${fmt(Math.abs(net))}`;
        netEl.className = `metric-value ${net >= 0 ? 'positive' : 'negative'}`;
        document.getElementById('active-groups').textContent = d.groupCount || 0;

        // Category breakdown
        const catDiv = document.getElementById('category-breakdown');
        if (d.categoryBreakdown && d.categoryBreakdown.length > 0) {
            const maxAmt = Math.max(...d.categoryBreakdown.map(c => parseFloat(c.amount)));
            catDiv.innerHTML = d.categoryBreakdown.map(c => {
                const pct = maxAmt > 0 ? (parseFloat(c.amount) / maxAmt * 100) : 0;
                return `<div class="category-item">
                    <div class="cat-icon">${esc(c.icon)}</div>
                    <div style="flex:1"><div class="cat-label">${esc(c.label)}</div>
                    <div class="category-bar"><div class="category-bar-fill" style="width:${pct}%"></div></div></div>
                    <div class="cat-amount">${cur}${fmt(c.amount)}</div>
                </div>`;
            }).join('');
        } else {
            catDiv.innerHTML = '<p class="empty-text">No expenses yet</p>';
        }

        // Recent expenses
        const recDiv = document.getElementById('recent-expenses');
        if (d.recentExpenses && d.recentExpenses.length > 0) {
            recDiv.innerHTML = d.recentExpenses.map(e => `
                <div class="activity-item">
                    <div class="activity-icon">${esc(e.categoryIcon || '📦')}</div>
                    <div class="activity-body">
                        <div class="activity-desc">${esc(e.description)}</div>
                        <div class="activity-meta">${esc(e.paidByName)} • ${esc(e.groupName)} • ${fmtDate(e.date)}</div>
                    </div>
                    <div class="activity-amount">${cur}${fmt(e.totalAmount)}</div>
                </div>
            `).join('');
        } else {
            recDiv.innerHTML = '<p class="empty-text">No expenses yet. Create a group to start!</p>';
        }
    } catch (err) {
        console.error(err);
        toast('Failed to load dashboard', 'error');
    }
}

// ============================================================================
// GROUPS
// ============================================================================
async function loadGroups() {
    try {
        const res = await apiFetch(`${API}/groups`);
        if (!res.ok) throw new Error('Failed to load groups');
        const groups = await res.json();
        state.groups = groups;
        const grid = document.getElementById('groups-grid');
        if (groups.length === 0) {
            grid.innerHTML = '<div class="empty-state"><div class="empty-icon">👥</div><h3>No groups yet</h3><p>Create a group to start splitting expenses</p></div>';
            return;
        }
        grid.innerHTML = groups.map(g => `
            <div class="group-card">
                <div class="group-card-header">
                    <span class="group-type-icon">${esc(g.groupTypeIcon || '📁')}</span>
                    <span class="group-name">${esc(g.name)}</span>
                </div>
                ${g.description ? `<div class="group-desc">${esc(g.description)}</div>` : ''}
                <div class="group-stats">
                    <span class="group-stat"><strong>${g.memberCount}</strong> members</span>
                    <span class="group-stat"><strong>${g.expenseCount}</strong> expenses</span>
                </div>
                <div class="group-actions">
                    <button class="btn-primary btn-small" onclick="viewGroup(${g.id})">View Details</button>
                </div>
            </div>
        `).join('');
    } catch (err) {
        toast('Failed to load groups', 'error');
    }
}

async function handleCreateGroup(e) {
    e.preventDefault();
    const name = document.getElementById('group-name').value;
    const description = document.getElementById('group-description').value;
    const groupType = document.getElementById('group-type').value;
    const memberEmails = document.getElementById('group-members').value.split(',').map(s => s.trim()).filter(Boolean);
    try {
        const res = await apiFetch(`${API}/groups`, {
            method: 'POST',
            body: JSON.stringify({ name, description, groupType, memberEmails })
        });
        if (!res.ok) { const d = await res.json(); throw new Error(d.error); }
        toast('Group created!', 'success');
        closeAllModals();
        document.getElementById('create-group-form').reset();
        loadGroups();
    } catch (err) {
        toast('Failed to create group: ' + err.message, 'error');
    }
}

function viewGroup(groupId) {
    // Switch to add-expense view for now and preselect group
    switchView('add-expense');
    setTimeout(() => {
        const sel = document.getElementById('expense-group');
        sel.value = groupId;
        sel.dispatchEvent(new Event('change'));
    }, 300);
}

// ============================================================================
// ADD EXPENSE
// ============================================================================
async function loadExpenseForm() {
    try {
        const res = await apiFetch(`${API}/groups`);
        if (!res.ok) throw new Error('Failed to load groups');
        const groups = await res.json();
        state.groups = groups;
        const sel = document.getElementById('expense-group');
        sel.innerHTML = '<option value="">Select group</option>' +
            groups.map(g => `<option value="${g.id}">${esc(g.name)}</option>`).join('');
    } catch (err) {
        toast('Failed to load groups', 'error');
    }
}

function onGroupSelectChange() {
    const groupId = document.getElementById('expense-group').value;
    const payerSel = document.getElementById('expense-payer');
    const splitsDiv = document.getElementById('expense-splits-container');
    if (!groupId) {
        payerSel.innerHTML = '<option value="">Select payer</option>';
        splitsDiv.innerHTML = '';
        return;
    }
    const group = state.groups.find(g => String(g.id) === String(groupId));
    if (!group) return;
    payerSel.innerHTML = '<option value="">Select payer</option>' +
        (group.members || []).map(m => `<option value="${m.id}">${esc(m.username || m.email)}</option>`).join('');
    renderSplitMembers(group.members || []);
}

function renderSplitMembers(members) {
    const div = document.getElementById('expense-splits-container');
    const showInput = state.splitType !== 'EQUAL';
    const placeholder = state.splitType === 'PERCENTAGE' ? '%' : state.splitType === 'RATIO' ? 'Ratio' : '₹';
    div.innerHTML = members.map(m => `
        <div class="split-row">
            <input type="checkbox" id="split-${m.id}" value="${m.id}" name="expense-splits" checked>
            <label for="split-${m.id}">${esc(m.username || m.email)}</label>
            ${showInput ? `<input type="number" class="split-value" data-user-id="${m.id}" placeholder="${placeholder}" step="any" min="0">` : ''}
        </div>
    `).join('');
}

function updateSplitInputs() {
    const groupId = document.getElementById('expense-group').value;
    if (!groupId) return;
    const group = state.groups.find(g => String(g.id) === String(groupId));
    if (group) renderSplitMembers(group.members || []);
}

async function handleAddExpense(e) {
    e.preventDefault();
    const groupId = document.getElementById('expense-group').value;
    const description = document.getElementById('expense-description').value;
    const amount = parseFloat(document.getElementById('expense-amount').value);
    const paidById = document.getElementById('expense-payer').value;
    const category = document.getElementById('expense-category').value;
    const notes = document.getElementById('expense-notes').value;
    const splitUserIds = Array.from(document.querySelectorAll('[name="expense-splits"]:checked')).map(cb => parseInt(cb.value));

    if (!groupId || !paidById || splitUserIds.length === 0) {
        toast('Please fill all required fields', 'error'); return;
    }

    let splitDetails = null;
    if (state.splitType !== 'EQUAL') {
        splitDetails = [];
        document.querySelectorAll('.split-value').forEach(input => {
            const uid = parseInt(input.dataset.userId);
            if (splitUserIds.includes(uid)) {
                splitDetails.push({ userId: uid, value: parseFloat(input.value) || 0 });
            }
        });
    }

    try {
        const res = await apiFetch(`${API}/expenses`, {
            method: 'POST',
            body: JSON.stringify({
                groupId: parseInt(groupId), description, totalAmount: amount,
                paidById: parseInt(paidById), splitUserIds, splitType: state.splitType,
                category, notes, splitDetails
            })
        });
        if (!res.ok) { const d = await res.json(); throw new Error(d.error); }
        toast('Expense added!', 'success');
        document.getElementById('add-expense-form').reset();
        document.getElementById('expense-splits-container').innerHTML = '';
        switchView('dashboard');
    } catch (err) {
        toast('Failed: ' + err.message, 'error');
    }
}

// ============================================================================
// SETTLEMENTS
// ============================================================================
async function loadSettlements() {
    try {
        const res = await apiFetch(`${API}/settlements`);
        if (!res.ok) throw new Error('Failed to load');
        const settlements = await res.json();
        const div = document.getElementById('settlement-details');
        if (!settlements || settlements.length === 0) {
            div.innerHTML = '<div class="empty-state"><div class="empty-icon">✅</div><h3>All settled!</h3><p>No outstanding debts</p></div>';
            return;
        }
        div.innerHTML = settlements.map(s => `
            <div class="settlement-card">
                <div class="settlement-info">
                    <div class="settlement-names">${esc(s.payerName)}<span class="arrow">→</span>${esc(s.receiverName)}</div>
                    <div class="settlement-group">${esc(s.groupName)}</div>
                </div>
                <div class="settlement-amount">₹${fmt(s.amount)}</div>
                <button class="btn-settle" onclick="openPaymentModal(${s.payerId},${s.receiverId},${s.groupId},'${esc(s.payerName)}','${esc(s.receiverName)}',${s.amount})">Settle</button>
            </div>
        `).join('');
    } catch (err) {
        toast('Failed to load settlements', 'error');
    }
}

function openPaymentModal(payerId, receiverId, groupId, payerName, receiverName, amount) {
    document.getElementById('pay-payer-id').value = payerId;
    document.getElementById('pay-receiver-id').value = receiverId;
    document.getElementById('pay-group-id').value = groupId;
    document.getElementById('pay-amount').value = parseFloat(amount).toFixed(2);
    document.getElementById('payment-summary').innerHTML = `<strong>${esc(payerName)}</strong> pays <strong>${esc(receiverName)}</strong>`;
    openModal('payment-modal');
}

async function handleRecordPayment(e) {
    e.preventDefault();
    try {
        const res = await apiFetch(`${API}/settlements/pay`, {
            method: 'POST',
            body: JSON.stringify({
                payerId: parseInt(document.getElementById('pay-payer-id').value),
                receiverId: parseInt(document.getElementById('pay-receiver-id').value),
                groupId: parseInt(document.getElementById('pay-group-id').value),
                amount: parseFloat(document.getElementById('pay-amount').value),
                note: document.getElementById('pay-note').value
            })
        });
        if (!res.ok) { const d = await res.json(); throw new Error(d.error); }
        toast('Payment recorded!', 'success');
        closeAllModals();
        loadSettlements();
    } catch (err) {
        toast('Failed: ' + err.message, 'error');
    }
}

// ============================================================================
// ACTIVITY
// ============================================================================
async function loadActivity() {
    try {
        const res = await apiFetch(`${API}/expenses/recent`);
        if (!res.ok) throw new Error('Failed to load');
        const expenses = await res.json();
        const div = document.getElementById('activity-feed');
        if (!expenses || expenses.length === 0) {
            div.innerHTML = '<div class="empty-state"><div class="empty-icon">📋</div><h3>No activity</h3><p>Expenses will appear here</p></div>';
            return;
        }
        div.innerHTML = expenses.map(e => `
            <div class="activity-item">
                <div class="activity-icon">${esc(e.categoryIcon || '📦')}</div>
                <div class="activity-body">
                    <div class="activity-desc">${esc(e.description)}</div>
                    <div class="activity-meta">Paid by ${esc(e.paidByName || e.paidByEmail)} • ${esc(e.groupName)} • ${fmtDate(e.date)}</div>
                </div>
                <div class="activity-amount">₹${fmt(e.totalAmount)}</div>
            </div>
        `).join('');
    } catch (err) {
        toast('Failed to load activity', 'error');
    }
}

// ============================================================================
// CATEGORIES
// ============================================================================
function populateCategories() {
    const cats = [
        { name:'FOOD', icon:'🍕', label:'Food & Drinks' },
        { name:'GROCERIES', icon:'🛒', label:'Groceries' },
        { name:'TRANSPORT', icon:'🚗', label:'Transport' },
        { name:'SHOPPING', icon:'🛍️', label:'Shopping' },
        { name:'ENTERTAINMENT', icon:'🎬', label:'Entertainment' },
        { name:'UTILITIES', icon:'💡', label:'Utilities' },
        { name:'RENT', icon:'🏠', label:'Rent' },
        { name:'MEDICAL', icon:'💊', label:'Medical' },
        { name:'TRAVEL', icon:'✈️', label:'Travel' },
        { name:'EDUCATION', icon:'📚', label:'Education' },
        { name:'SUBSCRIPTION', icon:'📱', label:'Subscriptions' },
        { name:'GIFTS', icon:'🎁', label:'Gifts' },
        { name:'SPORTS', icon:'⚽', label:'Sports' },
        { name:'PETS', icon:'🐾', label:'Pets' },
        { name:'OTHER', icon:'📦', label:'Other' }
    ];
    const sel = document.getElementById('expense-category');
    sel.innerHTML = cats.map(c => `<option value="${c.name}">${c.icon} ${c.label}</option>`).join('');
}

// ============================================================================
// MODALS & TOASTS
// ============================================================================
function openModal(id) { document.getElementById(id).style.display = 'flex'; }
function closeAllModals() { document.querySelectorAll('.modal').forEach(m => m.style.display = 'none'); }

function toast(msg, type = 'success', ms = 4000) {
    const c = document.getElementById('toast-container');
    const t = document.createElement('div');
    t.className = `toast ${type}`;
    t.innerHTML = `<span class="toast-icon">${type === 'success' ? '✓' : '⚠'}</span><span>${esc(msg)}</span><button class="toast-close">&times;</button>`;
    c.appendChild(t);
    t.querySelector('.toast-close').addEventListener('click', () => t.remove());
    setTimeout(() => t.remove(), ms);
}

// ============================================================================
// HELPERS
// ============================================================================
function esc(text) { if (!text) return ''; const d = document.createElement('div'); d.textContent = text; return d.innerHTML; }
function fmt(n) { return parseFloat(n || 0).toFixed(2); }
function fmtDate(d) { if (!d) return ''; try { return new Date(d).toLocaleDateString('en-IN', { day:'numeric', month:'short' }); } catch { return ''; } }

// Global error handlers
window.addEventListener('error', () => toast('An unexpected error occurred', 'error'));
window.addEventListener('unhandledrejection', (e) => { console.error(e.reason); });
