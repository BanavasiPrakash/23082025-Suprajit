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

  // Highlight buttons
  document.getElementById('new-user-btn').classList.toggle('active', tab === 'signup');
  document.getElementById('login-btn').classList.toggle('active', tab === 'login');

  // Clear messages
  document.getElementById('signup-message').textContent = '';
  document.getElementById('login-message').textContent = '';

  // Hide forgot-password when switching
  document.getElementById('forgot-password-form').style.display = 'none';
  document.getElementById('reset-message').textContent = '';
}

/* ---------- SIGNUP ---------- */
async function submitSignup(event) {
  event.preventDefault();
  const username = document.getElementById('signup-username').value.trim();
  const email = document.getElementById('signup-email')?.value.trim(); // may exist
  const password = document.getElementById('signup-password').value.trim();
  const msg = document.getElementById('signup-message');
  msg.textContent = '';
  msg.className = 'message';

  try {
    const body = email ? {username, email, password} : {username, password};
    const res = await fetch('http://localhost:8080/api/auth/signup', {
      method: 'POST',
      headers: {'Content-Type': 'application/x-www-form-urlencoded'},
      body: new URLSearchParams(body)
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

/* ---------- LOGIN ---------- */
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
      const userData = await res.json();
      // safer: do NOT save password
      sessionStorage.setItem('currentUser', JSON.stringify({username: userData.username, role: userData.role}));

      msg.textContent = 'Login successful! Redirecting...';
      msg.classList.add('success');

      // 🔁 Adjust redirect path as needed
      setTimeout(() => { window.location.href = 'status.html'; }, 1000);

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

/* ---------- FORGOT PASSWORD ---------- */
function showForgotPasswordForm() {
  document.getElementById('forgot-password-form').style.display = 'block';
  document.getElementById('login-container').classList.remove('active');
  document.getElementById('login-btn').classList.remove('active');
  document.getElementById('reset-email').value = '';
  document.getElementById('reset-message').textContent = '';
}

async function submitForgotPassword() {
  const email = document.getElementById('reset-email').value.trim();
  const msg = document.getElementById('reset-message');
  msg.textContent = '';
  msg.className = 'message';

  if (!email) {
    msg.textContent = 'Please enter your email.';
    msg.classList.add('error');
    return;
  }

  try {
    const res = await fetch('http://localhost:8080/api/auth/forgot-password', {
      method: 'POST',
      headers: {'Content-Type': 'application/x-www-form-urlencoded'},
      body: new URLSearchParams({email})
    });
    const text = await res.text();
    if (res.ok) {
      msg.textContent = text;
      msg.classList.add('success');
    } else {
      msg.textContent = text || 'Failed to send reset email.';
      msg.classList.add('error');
    }
  } catch (error) {
    msg.textContent = 'Error: ' + error.message;
    msg.classList.add('error');
  }
}

/* Remove default visible cards or button highlights on page load */
document.getElementById('new-user-btn').classList.remove('active');
document.getElementById('login-btn').classList.remove('active');

document.getElementById('signup-container').classList.remove('active');
document.getElementById('login-container').classList.remove('active');
document.getElementById('forgot-password-form').style.display = 'none';

/* Hide card containers if clicking outside */
document.body.addEventListener('click', function(event) {
  const signupContainer = document.getElementById('signup-container');
  const loginContainer = document.getElementById('login-container');
  const forgotPasswordForm = document.getElementById('forgot-password-form');
  
  const cards = [signupContainer, loginContainer, forgotPasswordForm];
  
  const newUserBtn = document.getElementById('new-user-btn');
  const loginBtn = document.getElementById('login-btn');

  const clickedInsideCard = cards.some(card => card.contains(event.target));
  const clickedOnButtons = (newUserBtn.contains(event.target) || loginBtn.contains(event.target));

  if (!clickedInsideCard && !clickedOnButtons) {
    signupContainer.classList.remove('active');
    loginContainer.classList.remove('active');
    forgotPasswordForm.style.display = 'none';
    
    newUserBtn.classList.remove('active');
    loginBtn.classList.remove('active');
    
    document.getElementById('signup-message').textContent = '';
    document.getElementById('login-message').textContent = '';
    document.getElementById('reset-message').textContent = '';
  }
});
