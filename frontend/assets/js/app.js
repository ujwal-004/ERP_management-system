(() => {
  const API = window.ERP_API_BASE || 'http://localhost:8080/api';
  const page = document.body.dataset.page;

  const configs = {
    users: {
      endpoint: 'users',
      columns: [['id', 'ID'], ['username', 'Username'], ['email', 'Email'], ['role', 'Role']],
      fields: ['username', 'email', 'password', 'role']
    },
    products: {
      endpoint: 'products',
      columns: [['id', 'ID'], ['name', 'Product'], ['category', 'Category'], ['price', 'Price'], ['reorder_level', 'Reorder level'], ['supplier_name', 'Supplier']],
      fields: ['name', 'category', 'description', 'price', 'reorder_level', 'supplier_id']
    },
    suppliers: {
      endpoint: 'suppliers',
      columns: [['id', 'ID'], ['name', 'Supplier'], ['contact_person', 'Contact'], ['phone', 'Phone'], ['email', 'Email'], ['address', 'Address']],
      fields: ['name', 'contact_person', 'phone', 'email', 'address']
    },
    inventory: {
      endpoint: 'inventory',
      columns: [['id', 'ID'], ['product_id', 'Product ID'], ['product_name', 'Product'], ['quantity', 'Quantity'], ['reorder_level', 'Reorder level'], ['last_updated', 'Updated']],
      fields: ['product_id', 'quantity']
    }
  };

  async function api(path, options = {}) {
    const response = await fetch(API + path, {
      headers: { 'Content-Type': 'application/json', ...(options.headers || {}) },
      ...options
    });
    const data = await response.json().catch(() => ({}));
    if (!response.ok) throw new Error(data.error || `Request failed (${response.status})`);
    return data;
  }

  function message(text, isError = false) {
    const target = document.getElementById('message');
    if (!target) return;
    target.textContent = text;
    target.className = isError ? 'message error' : 'message success';
  }

  function addCell(row, value) {
    const cell = document.createElement('td');
    cell.textContent = value === null || value === undefined ? '—' : String(value);
    row.appendChild(cell);
  }

  async function loadDashboard() {
    const resources = ['users', 'products', 'suppliers', 'inventory'];
    try {
      const counts = await Promise.all(resources.map((resource) => api(`/${resource}/count`)));
      resources.forEach((resource, index) => {
        const element = document.getElementById(`count-${resource}`);
        if (element) element.textContent = counts[index].count;
      });
    } catch (error) {
      message(error.message, true);
    }
  }

  async function loadRecords(config) {
    const body = document.getElementById('records');
    body.replaceChildren();
    try {
      const records = await api(`/${config.endpoint}`);
      records.forEach((record) => {
        const row = document.createElement('tr');
        config.columns.forEach(([key]) => addCell(row, record[key]));
        const actions = document.createElement('td');
        const edit = document.createElement('button');
        edit.type = 'button';
        edit.className = 'small-button';
        edit.textContent = 'Edit';
        edit.dataset.editId = record.id;
        const remove = document.createElement('button');
        remove.type = 'button';
        remove.className = 'small-button danger';
        remove.textContent = 'Delete';
        remove.dataset.deleteId = record.id;
        actions.append(edit, remove);
        row.appendChild(actions);
        body.appendChild(row);
      });
      if (records.length === 0) {
        const row = document.createElement('tr');
        const cell = document.createElement('td');
        cell.colSpan = config.columns.length + 1;
        cell.className = 'empty';
        cell.textContent = 'No records yet. Add the first record using the form.';
        row.appendChild(cell);
        body.appendChild(row);
      }
    } catch (error) {
      message(error.message, true);
    }
  }

  function configureModule() {
    const config = configs[page];
    if (!config) return;
    const form = document.getElementById('recordForm');
    let editingId = null;

    loadRecords(config);

    form.addEventListener('submit', async (event) => {
      event.preventDefault();
      const payload = Object.fromEntries(new FormData(form).entries());
      for (const key of ['price', 'reorder_level', 'supplier_id', 'product_id', 'quantity']) {
        if (payload[key] === '') delete payload[key];
      }
      if (payload.price !== undefined) payload.price = Number(payload.price);
      if (payload.reorder_level !== undefined) payload.reorder_level = Number(payload.reorder_level);
      if (payload.supplier_id !== undefined) payload.supplier_id = Number(payload.supplier_id);
      if (payload.product_id !== undefined) payload.product_id = Number(payload.product_id);
      if (payload.quantity !== undefined) payload.quantity = Number(payload.quantity);

      try {
        const method = editingId ? 'PUT' : 'POST';
        const path = editingId ? `/${config.endpoint}/${editingId}` : `/${config.endpoint}`;
        await api(path, { method, body: JSON.stringify(payload) });
        message(editingId ? 'Record updated.' : 'Record created.');
        form.reset();
        editingId = null;
        document.getElementById('saveButton').textContent = 'Save record';
        await loadRecords(config);
      } catch (error) {
        message(error.message, true);
      }
    });

    form.addEventListener('reset', () => {
      editingId = null;
      document.getElementById('saveButton').textContent = 'Save record';
    });

    document.getElementById('records').addEventListener('click', async (event) => {
      const editId = event.target.dataset.editId;
      const deleteId = event.target.dataset.deleteId;
      try {
        if (editId) {
          const records = await api(`/${config.endpoint}`);
          const record = records.find((item) => String(item.id) === editId);
          if (!record) return;
          config.fields.forEach((field) => {
            const input = form.elements.namedItem(field);
            if (input) input.value = field === 'password' ? '' : (record[field] ?? '');
          });
          editingId = editId;
          document.getElementById('saveButton').textContent = 'Update record';
          form.scrollIntoView({ behavior: 'smooth', block: 'center' });
        }
        if (deleteId && window.confirm('Delete this record?')) {
          await api(`/${config.endpoint}/${deleteId}`, { method: 'DELETE' });
          message('Record deleted.');
          await loadRecords(config);
        }
      } catch (error) {
        message(error.message, true);
      }
    });
  }

  function configureLogin() {
    const form = document.getElementById('loginForm');
    form.addEventListener('submit', async (event) => {
      event.preventDefault();
      try {
        const payload = Object.fromEntries(new FormData(form).entries());
        const result = await api('/users/login', { method: 'POST', body: JSON.stringify(payload) });
        localStorage.setItem('erpUser', JSON.stringify(result.user));
        window.location.href = '../../dashboard.html';
      } catch (error) {
        message(error.message, true);
      }
    });
  }

  function configureRegister() {
    const form = document.getElementById('registerForm');
    form.addEventListener('submit', async (event) => {
      event.preventDefault();
      try {
        const payload = Object.fromEntries(new FormData(form).entries());
        payload.role = 'USER';
        await api('/users', { method: 'POST', body: JSON.stringify(payload) });
        message('Account created. Return to the login page.');
        form.reset();
      } catch (error) {
        message(error.message, true);
      }
    });
  }

  if (page === 'dashboard') loadDashboard();
  if (configs[page]) configureModule();
  if (page === 'login') configureLogin();
  if (page === 'register') configureRegister();

  const logout = document.getElementById('logoutButton');
  if (logout) logout.addEventListener('click', () => {
    localStorage.removeItem('erpUser');
    window.location.href = 'modules/users/login.html';
  });
})();
