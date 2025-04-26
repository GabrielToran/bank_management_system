/**
 * Bank Management System JavaScript
 * Main script file for handling UI interactions and AJAX calls
 */

// DOM Ready event
document.addEventListener('DOMContentLoaded', function() {
    initializeApp();
    attachEventListeners();
    initializeDataTables();
});

/**
 * Initialize the application
 */
function initializeApp() {
    // Check if user is logged in
    checkAuthStatus();

    // Initialize tooltips and popovers if Bootstrap is used
    if (typeof bootstrap !== 'undefined') {
        const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
        tooltipTriggerList.map(function (tooltipTriggerEl) {
            return new bootstrap.Tooltip(tooltipTriggerEl);
        });

        const popoverTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="popover"]'));
        popoverTriggerList.map(function (popoverTriggerEl) {
            return new bootstrap.Popover(popoverTriggerEl);
        });
    }

    // Display current date in dashboard if element exists
    const dateDisplayEl = document.getElementById('currentDate');
    if (dateDisplayEl) {
        dateDisplayEl.textContent = new Date().toLocaleDateString();
    }
}

/**
 * Check if user is authenticated
 */
function checkAuthStatus() {
    const authToken = localStorage.getItem('authToken');
    if (!authToken && !window.location.pathname.includes('login.jsp') && !window.location.pathname.includes('index.jsp')) {
        window.location.href = 'login.jsp';
    }
}

/**
 * Attach event listeners to interactive elements
 */
function attachEventListeners() {
    // Login form submission
    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', handleLogin);
    }

    // Account creation form
    const createAccountForm = document.getElementById('createAccountForm');
    if (createAccountForm) {
        createAccountForm.addEventListener('submit', handleAccountCreation);
    }

    // Transaction form
    const transactionForm = document.getElementById('transactionForm');
    if (transactionForm) {
        transactionForm.addEventListener('submit', handleTransaction);
    }

    // Logout button
    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', handleLogout);
    }

    // Account selector in transfer form
    const accountOptions = document.querySelectorAll('.account-option');
    accountOptions.forEach(option => {
        option.addEventListener('click', function() {
            selectAccount(this);
        });
    });

    // Toggle switches for settings
    const toggleSwitches = document.querySelectorAll('.toggle-switch input');
    toggleSwitches.forEach(toggle => {
        toggle.addEventListener('change', function() {
            updateSetting(this.dataset.setting, this.checked);
        });
    });

    // Transaction filters
    const filterForm = document.getElementById('transactionFilter');
    if (filterForm) {
        filterForm.addEventListener('submit', filterTransactions);
    }

    // Modal close buttons
    const closeModalBtns = document.querySelectorAll('.close-modal, .modal-backdrop');
    closeModalBtns.forEach(btn => {
        btn.addEventListener('click', closeModal);
    });
}

/**
 * Initialize DataTables if the library is available
 */
function initializeDataTables() {
    if (typeof $.fn.DataTable !== 'undefined') {
        $('#transactionsTable').DataTable({
            responsive: true,
            order: [[0, 'desc']], // Order by date (first column) descending
            language: {
                search: "Search transactions:",
                lengthMenu: "Show _MENU_ transactions per page",
                info: "Showing _START_ to _END_ of _TOTAL_ transactions",
                emptyTable: "No transactions available"
            }
        });
    }
}

/**
 * Handle login form submission
 * @param {Event} e - Form submit event
 */
function handleLogin(e) {
    e.preventDefault();
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;

    // Basic form validation
    if (!username || !password) {
        showAlert('Please enter both username and password.', 'danger');
        return;
    }

    // Make AJAX call to login servlet
    fetch('LoginServlet', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: `username=${encodeURIComponent(username)}&password=${encodeURIComponent(password)}`
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                localStorage.setItem('authToken', data.token);
                window.location.href = 'dashboard.jsp';
            } else {
                showAlert(data.message || 'Login failed. Please check your credentials.', 'danger');
            }
        })
        .catch(error => {
            console.error('Login error:', error);
            showAlert('An error occurred during login. Please try again.', 'danger');
        });
}

/**
 * Handle account creation form submission
 * @param {Event} e - Form submit event
 */
function handleAccountCreation(e) {
    e.preventDefault();
    const accountType = document.getElementById('accountType').value;
    const initialDeposit = document.getElementById('initialDeposit').value;

    // Validate input
    if (!accountType) {
        showAlert('Please select an account type.', 'warning');
        return;
    }

    if (!initialDeposit || isNaN(initialDeposit) || parseFloat(initialDeposit) <= 0) {
        showAlert('Please enter a valid initial deposit amount.', 'warning');
        return;
    }

    // Create form data for submission
    const formData = new FormData(e.target);

    // Make AJAX call to account servlet
    fetch('AccountServlet', {
        method: 'POST',
        headers: {
            'Authorization': `Bearer ${localStorage.getItem('authToken')}`,
        },
        body: formData
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                showAlert('Account created successfully!', 'success');
                // Redirect to accounts page after a brief delay
                setTimeout(() => {
                    window.location.href = 'account.jsp';
                }, 1500);
            } else {
                showAlert(data.message || 'Failed to create account.', 'danger');
            }
        })
        .catch(error => {
            console.error('Account creation error:', error);
            showAlert('An error occurred while creating the account.', 'danger');
        });
}

/**
 * Handle transaction form submission
 * @param {Event} e - Form submit event
 */
function handleTransaction(e) {
    e.preventDefault();
    const fromAccount = document.getElementById('fromAccount').value;
    const toAccount = document.getElementById('toAccount').value;
    const amount = document.getElementById('amount').value;
    const description = document.getElementById('description').value;

    // Validate inputs
    if (!fromAccount || !toAccount) {
        showAlert('Please select both source and destination accounts.', 'warning');
        return;
    }

    if (!amount || isNaN(amount) || parseFloat(amount) <= 0) {
        showAlert('Please enter a valid amount.', 'warning');
        return;
    }

    // Create form data for submission
    const formData = new FormData(e.target);

    // Make AJAX call to transaction servlet
    fetch('TransactionServlet', {
        method: 'POST',
        headers: {
            'Authorization': `Bearer ${localStorage.getItem('authToken')}`,
        },
        body: formData
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                showAlert('Transaction completed successfully!', 'success');
                // Refresh transaction list or redirect
                setTimeout(() => {
                    window.location.href = 'transaction.jsp';
                }, 1500);
            } else {
                showAlert(data.message || 'Transaction failed.', 'danger');
            }
        })
        .catch(error => {
            console.error('Transaction error:', error);
            showAlert('An error occurred during the transaction.', 'danger');
        });
}

/**
 * Handle logout
 */
function handleLogout() {
    localStorage.removeItem('authToken');
    sessionStorage.clear();
    window.location.href = 'login.jsp';
}

/**
 * Select account in transfer form
 * @param {HTMLElement} element - The clicked account option element
 */
function selectAccount(element) {
    // Remove selected class from all options
    const options = document.querySelectorAll('.account-option');
    options.forEach(option => option.classList.remove('selected'));

    // Add selected class to clicked option
    element.classList.add('selected');

    // Update hidden input with selected account ID
    const accountId = element.dataset.accountId;
    document.getElementById(element.dataset.target).value = accountId;
}

/**
 * Update user setting via AJAX
 * @param {string} setting - The setting name to update
 * @param {boolean} value - The new setting value
 */
function updateSetting(setting, value) {
    fetch('CustomerServlet', {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${localStorage.getItem('authToken')}`,
        },
        body: JSON.stringify({
            setting: setting,
            value: value
        })
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                showAlert(`Setting "${setting}" updated successfully.`, 'success');
            } else {
                showAlert(data.message || 'Failed to update setting.', 'danger');
            }
        })
        .catch(error => {
            console.error('Setting update error:', error);
            showAlert('An error occurred while updating the setting.', 'danger');
        });
}

/**
 * Filter transactions based on form inputs
 * @param {Event} e - Form submit event
 */
function filterTransactions(e) {
    e.preventDefault();

    const startDate = document.getElementById('startDate').value;
    const endDate = document.getElementById('endDate').value;
    const minAmount = document.getElementById('minAmount').value;
    const maxAmount = document.getElementById('maxAmount').value;
    const transactionType = document.getElementById('transactionType').value;

    // Build query parameters
    const params = new URLSearchParams();
    if (startDate) params.append('startDate', startDate);
    if (endDate) params.append('endDate', endDate);
    if (minAmount) params.append('minAmount', minAmount);
    if (maxAmount) params.append('maxAmount', maxAmount);
    if (transactionType) params.append('type', transactionType);

    // Fetch filtered transactions
    fetch(`TransactionServlet?${params.toString()}`, {
        headers: {
            'Authorization': `Bearer ${localStorage.getItem('authToken')}`,
        }
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                updateTransactionTable(data.transactions);
            } else {
                showAlert(data.message || 'Failed to filter transactions.', 'warning');
            }
        })
        .catch(error => {
            console.error('Transaction filter error:', error);
            showAlert('An error occurred while filtering transactions.', 'danger');
        });
}

/**
 * Update transaction table with new data
 * @param {Array} transactions - Array of transaction objects
 */
function updateTransactionTable(transactions) {
    const tableBody = document.querySelector('#transactionsTable tbody');
    if (!tableBody) return;

    // Clear existing rows
    tableBody.innerHTML = '';

    if (transactions.length === 0) {
        // Display no data message
        const noDataRow = document.createElement('tr');
        noDataRow.innerHTML = `<td colspan="6" class="text-center">No transactions found matching your filters.</td>`;
        tableBody.appendChild(noDataRow);
        return;
    }

    // Populate with new data
    transactions.forEach(transaction => {
        const row = document.createElement('tr');

        // Format date
        const date = new Date(transaction.date);
        const formattedDate = date.toLocaleDateString() + ' ' + date.toLocaleTimeString();

        // Determine transaction class based on type
        let amountClass = '';
        if (transaction.type === 'DEBIT') {
            amountClass = 'text-danger';
        } else if (transaction.type === 'CREDIT') {
            amountClass = 'text-success';
        }

        row.innerHTML = `
            <td>${formattedDate}</td>
            <td>${transaction.description}</td>
            <td>${transaction.fromAccount || '-'}</td>
            <td>${transaction.toAccount || '-'}</td>
            <td class="${amountClass}">${formatCurrency(transaction.amount)}</td>
            <td>${transaction.status}</td>
        `;

        tableBody.appendChild(row);
    });

    // Reinitialize DataTable if it exists
    if (typeof $.fn.DataTable !== 'undefined') {
        $('#transactionsTable').DataTable().destroy();
        $('#transactionsTable').DataTable({
            responsive: true,
            order: [[0, 'desc']]
        });
    }
}

/**
 * Format currency amount
 * @param {number} amount - Amount to format
 * @return {string} Formatted currency string
 */
function formatCurrency(amount) {
    return new Intl.NumberFormat('en-US', {
        style: 'currency',
        currency: 'USD'
    }).format(amount);
}

/**
 * Display alert message
 * @param {string} message - Alert message
 * @param {string} type - Alert type (success, info, warning, danger)
 */
function showAlert(message, type = 'info') {
    // Check if alerts container exists, create if not
    let alertsContainer = document.getElementById('alertsContainer');
    if (!alertsContainer) {
        alertsContainer = document.createElement('div');
        alertsContainer.id = 'alertsContainer';
        alertsContainer.className = 'alerts-container';
        document.body.prepend(alertsContainer);
    }

    // Create alert element
    const alertEl = document.createElement('div');
    alertEl.className = `alert alert-${type} alert-dismissible fade show`;
    alertEl.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    `;

    // Add to container
    alertsContainer.appendChild(alertEl);

    // Auto-dismiss after 5 seconds
    setTimeout(() => {
        alertEl.classList.remove('show');
        setTimeout(() => alertEl.remove(), 150);
    }, 5000);
}

/**
 * Open a modal
 * @param {string} modalId - ID of the modal to open
 */
function openModal(modalId) {
    const modal = document.getElementById(modalId);
    if (!modal) return;

    modal.style.display = 'flex';
    document.body.classList.add('modal-open');
}

/**
 * Close the current modal
 */
function closeModal() {
    const openModals = document.querySelectorAll('.modal-backdrop');
    openModals.forEach(modal => {
        modal.style.display = 'none';
    });
    document.body.classList.remove('modal-open');
}

/**
 * Load account details
 * @param {string} accountId - ID of the account to load
 */
function loadAccountDetails(accountId) {
    fetch(`AccountServlet?id=${accountId}`, {
        headers: {
            'Authorization': `Bearer ${localStorage.getItem('authToken')}`,
        }
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                // Update account details UI
                updateAccountDetailsUI(data.account);

                // Load last 5 transactions for this account
                loadAccountTransactions(accountId, 5);
            } else {
                showAlert(data.message || 'Failed to load account details.', 'warning');
            }
        })
        .catch(error => {
            console.error('Account details error:', error);
            showAlert('An error occurred while loading account details.', 'danger');
        });
}

/**
 * Update account details UI
 * @param {Object} account - Account data object
 */
function updateAccountDetailsUI(account) {
    // Update account header
    document.getElementById('accountName').textContent = account.name;
    document.getElementById('accountNumber').textContent = account.number;
    document.getElementById('accountBalance').textContent = formatCurrency(account.balance);

    // Update account status indicator
    const statusIndicator = document.getElementById('accountStatus');
    statusIndicator.className = `status-indicator ${account.status.toLowerCase()}`;
    statusIndicator.textContent = account.status;

    // Update account details
    document.getElementById('accountType').textContent = account.type;
    document.getElementById('openDate').textContent = new Date(account.openDate).toLocaleDateString();
    document.getElementById('interestRate').textContent = account.interestRate ? `${account.interestRate}%` : 'N/A';
    document.getElementById('minimumBalance').textContent = formatCurrency(account.minimumBalance || 0);
}

/**
 * Load account transactions
 * @param {string} accountId - ID of the account
 * @param {number} limit - Maximum number of transactions to load
 */
function loadAccountTransactions(accountId, limit = 5) {
    fetch(`TransactionServlet?accountId=${accountId}&limit=${limit}`, {
        headers: {
            'Authorization': `Bearer ${localStorage.getItem('authToken')}`,
        }
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                updateRecentTransactionsUI(data.transactions);
            } else {
                showAlert(data.message || 'Failed to load recent transactions.', 'warning');
            }
        })
        .catch(error => {
            console.error('Recent transactions error:', error);
            showAlert('An error occurred while loading recent transactions.', 'danger');
        });
}

/**
 * Update recent transactions UI
 * @param {Array} transactions - Array of transaction objects
 */
function updateRecentTransactionsUI(transactions) {
    const transactionsList = document.getElementById('recentTransactions');
    if (!transactionsList) return;

    // Clear existing items
    transactionsList.innerHTML = '';

    if (transactions.length === 0) {
        // Display no transactions message
        const noTransactionsEl = document.createElement('li');
        noTransactionsEl.className = 'no-transactions';
        noTransactionsEl.innerHTML = `
            <div class="no-data-icon">📊</div>
            <p class="no-data-message">No recent transactions found</p>
        `;
        transactionsList.appendChild(noTransactionsEl);
        return;
    }

    // Add transactions
    transactions.forEach(transaction => {
        const item = document.createElement('li');
        item.className = 'transaction-item';

        // Determine transaction icon and class based on type
        let transactionIcon = '↔️';
        let amountClass = '';

        if (transaction.type === 'DEBIT') {
            transactionIcon = '↑';
            amountClass = 'text-danger';
        } else if (transaction.type === 'CREDIT') {
            transactionIcon = '↓';
            amountClass = 'text-success';
        }

        item.innerHTML = `
            <div class="transaction-icon">${transactionIcon}</div>
            <div class="transaction-details">
                <div class="transaction-title">${transaction.description}</div>
                <div class="transaction-date">${new Date(transaction.date).toLocaleDateString()}</div>
            </div>
            <div class="transaction-amount ${amountClass}">${formatCurrency(transaction.amount)}</div>
        `;

        transactionsList.appendChild(item);
    });
}