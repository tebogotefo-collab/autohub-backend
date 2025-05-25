/**
 * AutoParts Hub Authentication JavaScript
 * Handles login, registration, and authentication
 */

document.addEventListener('DOMContentLoaded', () => {
    // Set up event listeners for login form
    setupLoginForm();
    
    // Set up event listeners for register form
    setupRegisterForm();
    
    // Set up password toggle functionality
    setupPasswordToggle();
});

// Set up login form
function setupLoginForm() {
    const loginForm = document.getElementById('loginForm');
    
    if (loginForm) {
        loginForm.addEventListener('submit', async (event) => {
            event.preventDefault();
            
            // Show loading state
            const submitButton = loginForm.querySelector('button[type="submit"]');
            const originalText = submitButton.innerHTML;
            submitButton.disabled = true;
            submitButton.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Logging in...';
            
            // Get form data
            const email = document.getElementById('email').value;
            const password = document.getElementById('password').value;
            const rememberMe = document.getElementById('rememberMe').checked;
            
            try {
                // Call login API
                const response = await fetch(`${API_BASE_URL}/auth/login`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({
                        email,
                        password
                    })
                });
                
                if (response.ok) {
                    const data = await response.json();
                    
                    // Store token in localStorage
                    localStorage.setItem('authToken', data.token);
                    
                    // If remember me is not checked, set expiration
                    if (!rememberMe) {
                        // Set token to expire in 24 hours
                        const expiryTime = new Date().getTime() + (24 * 60 * 60 * 1000);
                        localStorage.setItem('tokenExpiry', expiryTime);
                    }
                    
                    // Show success message
                    showToast('Login successful. Redirecting...', 'success');
                    
                    // Redirect to home page or previous page
                    const redirectUrl = new URLSearchParams(window.location.search).get('redirect') || '/';
                    setTimeout(() => {
                        window.location.href = redirectUrl;
                    }, 1000);
                } else {
                    // Handle error response
                    const errorData = await response.json();
                    throw new Error(errorData.message || 'Invalid email or password');
                }
            } catch (error) {
                showToast(error.message, 'error');
                
                // Reset form state
                submitButton.disabled = false;
                submitButton.innerHTML = originalText;
            }
        });
    }
}

// Set up registration form
function setupRegisterForm() {
    const registerForm = document.getElementById('registerForm');
    
    if (registerForm) {
        registerForm.addEventListener('submit', async (event) => {
            event.preventDefault();
            
            // Show loading state
            const submitButton = registerForm.querySelector('button[type="submit"]');
            const originalText = submitButton.innerHTML;
            submitButton.disabled = true;
            submitButton.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Creating account...';
            
            // Get form data
            const firstName = document.getElementById('firstName').value;
            const lastName = document.getElementById('lastName').value;
            const email = document.getElementById('email').value;
            const password = document.getElementById('password').value;
            const confirmPassword = document.getElementById('confirmPassword').value;
            const isSeller = document.getElementById('isSeller')?.checked || false;
            
            // Validate form data
            if (password !== confirmPassword) {
                showToast('Passwords do not match', 'error');
                submitButton.disabled = false;
                submitButton.innerHTML = originalText;
                return;
            }
            
            try {
                // Call register API
                const response = await fetch(`${API_BASE_URL}/auth/register`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({
                        firstName,
                        lastName,
                        email,
                        password,
                        isSeller
                    })
                });
                
                if (response.ok) {
                    const data = await response.json();
                    
                    // Show success message
                    showToast('Account created successfully! Please log in.', 'success');
                    
                    // Redirect to login page
                    setTimeout(() => {
                        window.location.href = '/login';
                    }, 1500);
                } else {
                    // Handle error response
                    const errorData = await response.json();
                    throw new Error(errorData.message || 'Registration failed. Please try again.');
                }
            } catch (error) {
                showToast(error.message, 'error');
                
                // Reset form state
                submitButton.disabled = false;
                submitButton.innerHTML = originalText;
            }
        });
    }
}

// Set up password toggle
function setupPasswordToggle() {
    const toggleButtons = document.querySelectorAll('.toggle-password');
    
    toggleButtons.forEach(button => {
        button.addEventListener('click', () => {
            const passwordInput = button.previousElementSibling;
            const icon = button.querySelector('i');
            
            // Toggle password visibility
            if (passwordInput.type === 'password') {
                passwordInput.type = 'text';
                icon.classList.remove('fa-eye');
                icon.classList.add('fa-eye-slash');
            } else {
                passwordInput.type = 'password';
                icon.classList.remove('fa-eye-slash');
                icon.classList.add('fa-eye');
            }
        });
    });
}

// Show toast notification
function showToast(message, type = 'success') {
    // Check if function exists in main.js, if not, implement it here
    if (typeof window.showToast === 'function') {
        window.showToast(message, type);
        return;
    }
    
    // Create toast container if it doesn't exist
    let toastContainer = document.querySelector('.toast-container');
    
    if (!toastContainer) {
        toastContainer = document.createElement('div');
        toastContainer.className = 'toast-container position-fixed top-0 end-0 p-3';
        document.body.appendChild(toastContainer);
    }
    
    // Create toast element
    const toastId = `toast-${Date.now()}`;
    const toast = document.createElement('div');
    toast.className = `toast ${type === 'error' ? 'bg-danger text-white' : 'bg-success text-white'}`;
    toast.id = toastId;
    toast.setAttribute('role', 'alert');
    toast.setAttribute('aria-live', 'assertive');
    toast.setAttribute('aria-atomic', 'true');
    
    toast.innerHTML = `
        <div class="toast-header">
            <strong class="me-auto">${type === 'error' ? 'Error' : 'Success'}</strong>
            <button type="button" class="btn-close" data-bs-dismiss="toast" aria-label="Close"></button>
        </div>
        <div class="toast-body">
            ${message}
        </div>
    `;
    
    // Add toast to container
    toastContainer.appendChild(toast);
    
    // Initialize and show toast
    const toastInstance = new bootstrap.Toast(toast);
    toastInstance.show();
    
    // Remove toast after it's hidden
    toast.addEventListener('hidden.bs.toast', () => {
        toast.remove();
    });
}
