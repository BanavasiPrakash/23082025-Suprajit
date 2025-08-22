function switchTab(tab) {
  const signupContainer = document.getElementById('signup-container');
  const loginContainer = document.getElementById('login-container');

  if (tab === 'signup') {
    signupContainer.classList.add('active');
    loginContainer.classList.remove('active');
  } else {
    loginContainer.classList.add('active');
    signupContainer.classList.remove('active');
  }

  document.getElementById('new-user-btn').classList.toggle('active', tab === 'signup');
  document.getElementById('login-btn').classList.toggle('active', tab === 'login');

  document.getElementById('signup-message').textContent = '';
  document.getElementById('login-message').textContent = '';
}

async function submitSignup(event) {
  event.preventDefault();
  const username = document.getElementById('signup-username').value.trim();
  const password = document.getElementById('signup-password').value.trim();
  const msg = document.getElementById('signup-message');
  msg.textContent = '';
  msg.className = 'message';

  try {
    const res = await fetch('http://localhost:8080/api/auth/signup', {
      method: 'POST',
      headers: {'Content-Type': 'application/x-www-form-urlencoded'},
      body: new URLSearchParams({username, password})
    });
    const text = await res.text();
    if (res.ok) {
      msg.textContent = 'Signup successful! Please log in.';
      msg.classList.add('success');
      switchTab('login');
    } else {
      msg.textContent = text || 'Signup failed.';
      msg.classList.add('error');
    }
  } catch (error) {
    msg.textContent = 'Error: ' + error.message;
    msg.classList.add('error');
  }
}

async function submitLogin(event) {
  event.preventDefault();
  const username = document.getElementById('login-username').value.trim();
  const password = document.getElementById('login-password').value.trim();
  const msg = document.getElementById('login-message');
  msg.textContent = '';
  msg.className = 'message';

  try {
    const res = await fetch('http://localhost:8080/api/auth/login', {
      method: 'POST',
      headers: {'Content-Type': 'application/x-www-form-urlencoded'},
      body: new URLSearchParams({username, password})
    });
    if (res.ok) {
      const userData = await res.json(); // Expecting {username, role}
      sessionStorage.setItem('currentUser', JSON.stringify({
        username: userData.username,
        role: userData.role
      }));
      msg.textContent = 'Login successful! Redirecting...';
      msg.classList.add('success');
      setTimeout(() => { window.location.href = 'index.html'; }, 1000);
    } else {
      const text = await res.text();
      msg.textContent = text || 'Login failed.';
      msg.classList.add('error');
    }
  } catch (error) {
    msg.textContent = 'Error: ' + error.message;
    msg.classList.add('error');
  }
}
