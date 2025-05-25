/**
 * AutoParts Hub Main JavaScript
 * Handles frontend functionality and API interactions
 */

// Global state management
const state = {
    user: null,
    cart: [],
    notifications: [],
    token: localStorage.getItem('authToken')
};

// API URL constants
const API_BASE_URL = '/api';
const API_ENDPOINTS = {
    auth: {
        login: `${API_BASE_URL}/auth/login`,
        register: `${API_BASE_URL}/auth/register`
    },
    categories: `${API_BASE_URL}/categories`,
    products: `${API_BASE_URL}/listings`,
    cart: `${API_BASE_URL}/cart`,
    orders: `${API_BASE_URL}/orders`,
    payments: `${API_BASE_URL}/payments`,
    notifications: `${API_BASE_URL}/notifications`,
    users: `${API_BASE_URL}/users`,
    sellers: `${API_BASE_URL}/sellers`,
    reviews: `${API_BASE_URL}/reviews`
};

// Initialize the application
document.addEventListener('DOMContentLoaded', () => {
    initializeApp();
    setupEventListeners();
});

// Initialize the application
function initializeApp() {
    // Check if user is logged in
    checkAuthStatus();
    
    // Load cart from local storage
    loadCart();
    
    // Update cart count in UI
    updateCartCount();
    
    // Load and render featured products
    loadFeaturedProducts();
    
    // Load and render categories
    loadCategories();
    
    // Load and render top sellers
    loadTopSellers();
    
    // Set up vehicle selector
    setupVehicleSelector();
}

// Check authentication status
async function checkAuthStatus() {
    if (state.token) {
        try {
            // Fetch current user details
            const response = await fetch(`${API_ENDPOINTS.users}/me`, {
                headers: {
                    'Authorization': `Bearer ${state.token}`
                }
            });
            
            if (response.ok) {
                const userData = await response.json();
                state.user = userData;
                updateAuthUI(true);
                // Fetch notifications for logged in users
                fetchNotifications();
            } else {
                // Token is invalid or expired
                logout();
            }
        } catch (error) {
            console.error('Error checking auth status:', error);
            logout();
        }
    } else {
        updateAuthUI(false);
    }
}

// Update UI based on authentication status
function updateAuthUI(isLoggedIn) {
    const authButtons = document.getElementById('authButtons');
    const userMenu = document.getElementById('userMenu');
    
    if (isLoggedIn && state.user) {
        // Hide login/register buttons and show user menu
        authButtons.classList.add('d-none');
        userMenu.classList.remove('d-none');
        
        // Update username in the dropdown
        const usernameElement = document.getElementById('username');
        if (usernameElement) {
            usernameElement.textContent = state.user.firstName;
        }
    } else {
        // Show login/register buttons and hide user menu
        authButtons.classList.remove('d-none');
        userMenu.classList.add('d-none');
    }
}

// Fetch user notifications
async function fetchNotifications() {
    if (!state.token || !state.user) return;
    
    try {
        const response = await fetch(API_ENDPOINTS.notifications, {
            headers: {
                'Authorization': `Bearer ${state.token}`
            }
        });
        
        if (response.ok) {
            const notificationsData = await response.json();
            state.notifications = notificationsData;
            
            // Update notification badge
            const notificationBadge = document.getElementById('notificationBadge');
            if (notificationBadge) {
                const unreadCount = notificationsData.filter(n => !n.read).length;
                notificationBadge.textContent = unreadCount;
                notificationBadge.style.display = unreadCount > 0 ? 'inline' : 'none';
            }
        }
    } catch (error) {
        console.error('Error fetching notifications:', error);
    }
}

// Load cart from local storage
function loadCart() {
    const savedCart = localStorage.getItem('cart');
    if (savedCart) {
        try {
            state.cart = JSON.parse(savedCart);
        } catch (error) {
            console.error('Error parsing cart from localStorage:', error);
            state.cart = [];
        }
    }
}

// Save cart to local storage
function saveCart() {
    localStorage.setItem('cart', JSON.stringify(state.cart));
    updateCartCount();
}

// Update cart count in UI
function updateCartCount() {
    const cartCountElement = document.getElementById('cartCount');
    if (cartCountElement) {
        const itemCount = state.cart.reduce((total, item) => total + item.quantity, 0);
        cartCountElement.textContent = itemCount;
    }
}

// Load featured products
async function loadFeaturedProducts() {
    try {
        const response = await fetch(`${API_ENDPOINTS.products}/featured`);
        if (response.ok) {
            const products = await response.json();
            renderFeaturedProducts(products);
        }
    } catch (error) {
        console.error('Error loading featured products:', error);
    }
}

// Render featured products
function renderFeaturedProducts(products) {
    const container = document.getElementById('featuredProductsContainer');
    if (!container) return;
    
    // Clear existing content
    container.innerHTML = '';
    
    // Create and append product cards
    products.forEach(product => {
        const productCard = createProductCard(product);
        container.appendChild(productCard);
    });
}

// Create product card element
function createProductCard(product) {
    const col = document.createElement('div');
    col.className = 'col-6 col-md-4 col-lg-3';
    
    const hasDiscount = product.discountPrice && product.discountPrice < product.price;
    
    col.innerHTML = `
        <div class="product-card card h-100 shadow-sm">
            <div class="position-relative">
                <img src="${product.imageUrl || '/images/default-product.jpg'}" class="card-img-top" alt="${product.title}">
                ${hasDiscount ? `<span class="badge bg-danger position-absolute top-0 start-0 m-2">Sale</span>` : ''}
                <button class="btn btn-sm btn-outline-secondary position-absolute top-0 end-0 m-2 favorite-btn" data-id="${product.id}">
                    <i class="far fa-heart"></i>
                </button>
            </div>
            <div class="card-body d-flex flex-column">
                <p class="small text-muted mb-1">${product.brand.name}</p>
                <h5 class="card-title">${product.title}</h5>
                <div class="ratings mb-2">
                    ${createStarRating(product.averageRating || 0)}
                    <span class="text-muted ms-1">(${product.reviewCount || 0})</span>
                </div>
                <div class="d-flex justify-content-between align-items-center mt-auto">
                    <div class="price-container">
                        ${hasDiscount ? 
                            `<span class="text-decoration-line-through text-muted small">$${product.price.toFixed(2)}</span>
                             <span class="fw-bold text-primary">$${product.discountPrice.toFixed(2)}</span>` :
                            `<span class="fw-bold text-primary">$${product.price.toFixed(2)}</span>`
                        }
                    </div>
                    <button class="btn btn-sm btn-primary add-to-cart-btn" data-id="${product.id}">
                        <i class="fas fa-cart-plus me-1"></i> Add
                    </button>
                </div>
            </div>
        </div>
    `;
    
    // Add event listeners to buttons
    const addToCartBtn = col.querySelector('.add-to-cart-btn');
    addToCartBtn.addEventListener('click', () => addToCart(product));
    
    const favoriteBtn = col.querySelector('.favorite-btn');
    favoriteBtn.addEventListener('click', () => toggleFavorite(product.id, favoriteBtn));
    
    return col;
}

// Create star rating HTML
function createStarRating(rating) {
    const fullStars = Math.floor(rating);
    const hasHalfStar = rating % 1 >= 0.5;
    const emptyStars = 5 - fullStars - (hasHalfStar ? 1 : 0);
    
    let starsHtml = '';
    
    // Add full stars
    for (let i = 0; i < fullStars; i++) {
        starsHtml += '<i class="fas fa-star text-warning"></i>';
    }
    
    // Add half star if needed
    if (hasHalfStar) {
        starsHtml += '<i class="fas fa-star-half-alt text-warning"></i>';
    }
    
    // Add empty stars
    for (let i = 0; i < emptyStars; i++) {
        starsHtml += '<i class="far fa-star text-warning"></i>';
    }
    
    return starsHtml;
}

// Add product to cart
function addToCart(product) {
    // Check if product is already in cart
    const existingItemIndex = state.cart.findIndex(item => item.id === product.id);
    
    if (existingItemIndex !== -1) {
        // Increment quantity
        state.cart[existingItemIndex].quantity += 1;
    } else {
        // Add new item to cart
        state.cart.push({
            id: product.id,
            title: product.title,
            price: product.discountPrice || product.price,
            imageUrl: product.imageUrl,
            quantity: 1
        });
    }
    
    // Save cart to localStorage
    saveCart();
    
    // Show toast notification
    showToast('Product added to cart successfully!');
}

// Toggle favorite product
function toggleFavorite(productId, buttonElement) {
    if (!state.token) {
        // Redirect to login if not authenticated
        window.location.href = '/login';
        return;
    }
    
    // Toggle active class for visual feedback
    buttonElement.classList.toggle('active');
    
    // Toggle icon
    const icon = buttonElement.querySelector('i');
    icon.classList.toggle('far');
    icon.classList.toggle('fas');
    
    // TODO: Call API to add/remove favorite
}

// Load categories
async function loadCategories() {
    try {
        const response = await fetch(API_ENDPOINTS.categories);
        if (response.ok) {
            const categories = await response.json();
            renderCategories(categories);
        }
    } catch (error) {
        console.error('Error loading categories:', error);
    }
}

// Render categories
function renderCategories(categories) {
    const container = document.getElementById('categoriesContainer');
    if (!container) return;
    
    // Clear existing content
    container.innerHTML = '';
    
    // Limit to 8 categories for the homepage
    const displayCategories = categories.slice(0, 8);
    
    // Define some icons for categories
    const icons = [
        'fa-oil-can', 'fa-car-battery', 'fa-compact-disc', 'fa-bolt',
        'fa-tachometer-alt', 'fa-cogs', 'fa-filter', 'fa-lightbulb'
    ];
    
    // Create and append category cards
    displayCategories.forEach((category, index) => {
        const col = document.createElement('div');
        col.className = 'col-6 col-md-4 col-lg-3';
        
        col.innerHTML = `
            <div class="category-card card h-100 shadow-sm">
                <div class="category-icon text-center pt-4">
                    <i class="fas ${icons[index % icons.length]} fa-3x text-primary"></i>
                </div>
                <div class="card-body text-center">
                    <h5 class="card-title">${category.name}</h5>
                    <p class="card-text small text-muted">${category.productCount || 0} products</p>
                    <a href="/category/${category.slug}" class="stretched-link"></a>
                </div>
            </div>
        `;
        
        container.appendChild(col);
    });
}

// Load top sellers
async function loadTopSellers() {
    try {
        const response = await fetch(`${API_ENDPOINTS.sellers}/top`);
        if (response.ok) {
            const sellers = await response.json();
            renderTopSellers(sellers);
        }
    } catch (error) {
        console.error('Error loading top sellers:', error);
    }
}

// Render top sellers
function renderTopSellers(sellers) {
    const container = document.getElementById('topSellersContainer');
    if (!container) return;
    
    // Clear existing content
    container.innerHTML = '';
    
    // Limit to 3 sellers for the homepage
    const displaySellers = sellers.slice(0, 3);
    
    // Create and append seller cards
    displaySellers.forEach(seller => {
        const col = document.createElement('div');
        col.className = 'col-md-4';
        
        col.innerHTML = `
            <div class="seller-card card h-100 shadow-sm">
                <div class="card-body">
                    <div class="d-flex align-items-center mb-3">
                        <div class="seller-logo me-3">
                            <img src="${seller.logoUrl || '/images/default-seller.jpg'}" class="rounded-circle" width="60" height="60" alt="${seller.businessName}">
                        </div>
                        <div>
                            <h5 class="mb-1">${seller.businessName}</h5>
                            <div class="ratings">
                                ${createStarRating(seller.averageRating || 0)}
                                <span class="text-muted ms-1">(${seller.reviewCount || 0})</span>
                            </div>
                        </div>
                    </div>
                    <p class="text-muted small">${seller.description || ''}</p>
                    <div class="seller-stats d-flex justify-content-between">
                        <div class="stat-item">
                            <span class="stat-value">${seller.positivePercentage || 0}%</span>
                            <span class="stat-label d-block small text-muted">Positive</span>
                        </div>
                        <div class="stat-item">
                            <span class="stat-value">${seller.shippingTime || 'N/A'}</span>
                            <span class="stat-label d-block small text-muted">Shipping</span>
                        </div>
                        <div class="stat-item">
                            <span class="stat-value">${seller.productCount || 0}+</span>
                            <span class="stat-label d-block small text-muted">Products</span>
                        </div>
                    </div>
                    <a href="/seller/${seller.id}" class="btn btn-outline-primary btn-sm d-block mt-3">View Store</a>
                </div>
            </div>
        `;
        
        container.appendChild(col);
    });
}

// Set up vehicle selector
function setupVehicleSelector() {
    const makeSelect = document.getElementById('carMake');
    const modelSelect = document.getElementById('carModel');
    const yearSelect = document.getElementById('carYear');
    const vehicleSearchForm = document.getElementById('vehicleSearchForm');
    
    if (!makeSelect || !modelSelect || !yearSelect || !vehicleSearchForm) return;
    
    // Enable model select when make is selected
    makeSelect.addEventListener('change', async () => {
        const selectedMake = makeSelect.value;
        
        if (selectedMake && selectedMake !== 'Select Make...') {
            modelSelect.disabled = false;
            modelSelect.innerHTML = '<option selected>Select Model...</option>';
            
            // Get models for the selected make
            // This would typically be an API call
            const models = await getModelsByMake(selectedMake);
            
            // Add model options
            models.forEach(model => {
                const option = document.createElement('option');
                option.value = model;
                option.textContent = model;
                modelSelect.appendChild(option);
            });
        } else {
            modelSelect.disabled = true;
            yearSelect.disabled = true;
        }
    });
    
    // Enable year select when model is selected
    modelSelect.addEventListener('change', async () => {
        const selectedModel = modelSelect.value;
        
        if (selectedModel && selectedModel !== 'Select Model...') {
            yearSelect.disabled = false;
            yearSelect.innerHTML = '<option selected>Select Year...</option>';
            
            // Get years for the selected model
            // This would typically be an API call
            const years = await getYearsByModel(makeSelect.value, selectedModel);
            
            // Add year options
            years.forEach(year => {
                const option = document.createElement('option');
                option.value = year;
                option.textContent = year;
                yearSelect.appendChild(option);
            });
        } else {
            yearSelect.disabled = true;
        }
    });
    
    // Handle form submission
    vehicleSearchForm.addEventListener('submit', (event) => {
        event.preventDefault();
        
        const make = makeSelect.value;
        const model = modelSelect.value;
        const year = yearSelect.value;
        
        if (make !== 'Select Make...' && model !== 'Select Model...' && year !== 'Select Year...') {
            // Redirect to the search results page with the vehicle parameters
            window.location.href = `/search?make=${make}&model=${model}&year=${year}`;
        } else {
            showToast('Please select make, model, and year', 'error');
        }
    });
}

// Example function to get models for a make
// In a real application, this would be an API call
async function getModelsByMake(make) {
    // Simulate API call
    return new Promise(resolve => {
        setTimeout(() => {
            const modelsByMake = {
                'Toyota': ['Corolla', 'Camry', 'RAV4', 'Highlander', 'Tacoma'],
                'Honda': ['Civic', 'Accord', 'CR-V', 'Pilot', 'Odyssey'],
                'Ford': ['F-150', 'Escape', 'Explorer', 'Mustang', 'Focus'],
                'BMW': ['3 Series', '5 Series', 'X3', 'X5', '7 Series'],
                'Mercedes-Benz': ['C-Class', 'E-Class', 'GLC', 'GLE', 'S-Class']
            };
            
            resolve(modelsByMake[make] || []);
        }, 300);
    });
}

// Example function to get years for a model
// In a real application, this would be an API call
async function getYearsByModel(make, model) {
    // Simulate API call
    return new Promise(resolve => {
        setTimeout(() => {
            // Generate a range of years from 2010 to current year
            const currentYear = new Date().getFullYear();
            const years = [];
            
            for (let year = currentYear; year >= 2010; year--) {
                years.push(year.toString());
            }
            
            resolve(years);
        }, 300);
    });
}

// Set up event listeners
function setupEventListeners() {
    // Logout button
    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', (event) => {
            event.preventDefault();
            logout();
        });
    }
    
    // Add to cart buttons (for dynamically added products)
    document.addEventListener('click', (event) => {
        if (event.target.classList.contains('add-to-cart-btn') || 
            event.target.closest('.add-to-cart-btn')) {
            const button = event.target.classList.contains('add-to-cart-btn') ? 
                event.target : event.target.closest('.add-to-cart-btn');
            
            const productId = button.dataset.id;
            // Find product data and add to cart
            // This assumes the product data is available
        }
    });
}

// Logout function
function logout() {
    // Clear authentication data
    state.user = null;
    state.token = null;
    localStorage.removeItem('authToken');
    
    // Update UI
    updateAuthUI(false);
    
    // Redirect to home page
    window.location.href = '/';
}

// Show toast notification
function showToast(message, type = 'success') {
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

// Function to handle payment for an order
async function processPayment(orderId) {
    if (!state.token) {
        window.location.href = '/login';
        return;
    }
    
    try {
        const response = await fetch(`${API_ENDPOINTS.payments}/initiate`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${state.token}`
            },
            body: JSON.stringify({
                orderId,
                returnUrl: `${window.location.origin}/orders/${orderId}?status=success`,
                cancelUrl: `${window.location.origin}/orders/${orderId}?status=cancel`
            })
        });
        
        if (response.ok) {
            const paymentData = await response.json();
            
            // Redirect to payment gateway
            window.location.href = paymentData.paymentUrl;
        } else {
            const errorData = await response.json();
            showToast(errorData.message || 'Payment initiation failed', 'error');
        }
    } catch (error) {
        console.error('Error initiating payment:', error);
        showToast('An error occurred while initiating payment. Please try again.', 'error');
    }
}

// Function to create a new order
async function createOrder(cartItems, shippingDetails) {
    if (!state.token) {
        window.location.href = '/login';
        return;
    }
    
    try {
        const response = await fetch(API_ENDPOINTS.orders, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${state.token}`
            },
            body: JSON.stringify({
                items: cartItems.map(item => ({
                    listingId: item.id,
                    quantity: item.quantity
                })),
                shippingAddress: shippingDetails.address,
                shippingCity: shippingDetails.city,
                shippingProvince: shippingDetails.province,
                shippingPostalCode: shippingDetails.postalCode,
                contactPhone: shippingDetails.phone,
                contactEmail: shippingDetails.email
            })
        });
        
        if (response.ok) {
            const orderData = await response.json();
            
            // Clear cart after successful order
            state.cart = [];
            saveCart();
            
            // Return the created order ID
            return orderData.id;
        } else {
            const errorData = await response.json();
            showToast(errorData.message || 'Order creation failed', 'error');
            return null;
        }
    } catch (error) {
        console.error('Error creating order:', error);
        showToast('An error occurred while creating the order. Please try again.', 'error');
        return null;
    }
}
