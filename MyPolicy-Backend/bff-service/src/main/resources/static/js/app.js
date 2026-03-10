const API_BASE = ''; // Same origin - BFF serves this

const loginSection = document.getElementById('login-section');
const dashboardSection = document.getElementById('dashboard-section');
const loginForm = document.getElementById('login-form');
const loginError = document.getElementById('login-error');
const loginBtn = document.getElementById('login-btn');
const welcomeUser = document.getElementById('welcome-user');
const logoutBtn = document.getElementById('logout-btn');
const loading = document.getElementById('loading');
const portfolioContent = document.getElementById('portfolio-content');
const portfolioError = document.getElementById('portfolio-error');
const totalPoliciesEl = document.getElementById('total-policies');
const totalPremiumEl = document.getElementById('total-premium');
const totalCoverageEl = document.getElementById('total-coverage');
const policiesList = document.getElementById('policies-list');

function showLoginError(msg) {
  loginError.textContent = msg;
  loginError.style.display = 'block';
}

function hideLoginError() {
  loginError.style.display = 'none';
}

function formatCurrency(num) {
  if (num == null || isNaN(num)) return '0';
  return new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(num);
}

function formatDate(yyyymmdd) {
  if (!yyyymmdd) return '-';
  const s = String(yyyymmdd);
  if (s.length === 8) {
    return `${s.slice(6, 8)}/${s.slice(4, 6)}/${s.slice(0, 4)}`;
  }
  return s;
}

// Login
loginForm.addEventListener('submit', async (e) => {
  e.preventDefault();
  hideLoginError();
  loginBtn.disabled = true;
  loginBtn.textContent = 'Signing in...';

  const userId = document.getElementById('userId').value.trim();
  const password = document.getElementById('password').value.trim();

  try {
    const res = await fetch(`${API_BASE}/api/bff/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        customerIdOrUserId: userId,
        password: password
      })
    });

    const data = await res.json().catch(() => ({}));

    if (!res.ok) {
      const msg = data.details ? `${data.message || 'Error'}: ${data.details}` : (data.message || data.error || 'Invalid User ID or Password');
      throw new Error(msg);
    }

    const customerId = data.customer?.customerId;
    if (!customerId) {
      throw new Error('Login succeeded but customer ID not received');
    }

    sessionStorage.setItem('token', data.token || '');
    sessionStorage.setItem('customerId', customerId);
    sessionStorage.setItem('customerName', data.customer?.firstName || userId);

    loginSection.style.display = 'none';
    dashboardSection.style.display = 'block';
    welcomeUser.textContent = `Welcome, ${data.customer?.firstName || userId}`;

    await loadPortfolio(customerId);
  } catch (err) {
    showLoginError(err.message || 'Login failed. Please check your credentials.');
  } finally {
    loginBtn.disabled = false;
    loginBtn.textContent = 'Sign In';
  }
});

// Load portfolio
async function loadPortfolio(customerId) {
  loading.style.display = 'block';
  portfolioContent.style.display = 'none';
  portfolioError.style.display = 'none';

  try {
    const res = await fetch(`${API_BASE}/api/bff/portfolio/${customerId}`);
    const data = await res.json().catch(() => ({}));

    if (!res.ok) {
      const msg = data.details ? `${data.message || 'Error'}: ${data.details}` : (data.message || 'Failed to load portfolio');
      throw new Error(msg);
    }

    totalPoliciesEl.textContent = data.totalPolicies ?? 0;
    totalPremiumEl.textContent = formatCurrency(data.totalPremium);
    totalCoverageEl.textContent = formatCurrency(data.totalCoverage);

    const policies = data.policies || [];
    if (policies.length === 0) {
      policiesList.innerHTML = `
        <div class="empty-state">
          <p><strong>No policies found</strong></p>
          <p>Your insurance policies will appear here once they are loaded into the system.</p>
        </div>
      `;
    } else {
      policiesList.innerHTML = policies.map(p => `
        <div class="policy-card">
          <div class="policy-header">
            <span class="policy-number">${p.policyNumber || p.id || '-'}</span>
            <span class="policy-type">${(p.policyType || 'policy').replace(/_/g, ' ')}</span>
          </div>
          <div class="policy-details">
            <span><strong>Insurer:</strong> ${p.insurerId || '-'}</span>
            <span><strong>Premium:</strong> ${formatCurrency(p.premiumAmount)}</span>
            <span><strong>Coverage:</strong> ${formatCurrency(p.sumAssured)}</span>
          </div>
        </div>
      `).join('');
    }

    portfolioContent.style.display = 'block';
  } catch (err) {
    portfolioError.textContent = err.message || 'Failed to load your policies.';
    portfolioError.style.display = 'block';
  } finally {
    loading.style.display = 'none';
  }
}

// Logout
logoutBtn.addEventListener('click', () => {
  sessionStorage.removeItem('token');
  sessionStorage.removeItem('customerId');
  sessionStorage.removeItem('customerName');
  dashboardSection.style.display = 'none';
  loginSection.style.display = 'flex';
  loginForm.reset();
  hideLoginError();
});

// Check if already logged in
const savedCustomerId = sessionStorage.getItem('customerId');
if (savedCustomerId) {
  loginSection.style.display = 'none';
  dashboardSection.style.display = 'block';
  welcomeUser.textContent = `Welcome, ${sessionStorage.getItem('customerName') || 'User'}`;
  loadPortfolio(savedCustomerId);
}
