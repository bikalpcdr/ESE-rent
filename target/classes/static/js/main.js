// Wait for the DOM to be fully loaded before running any scripts
document.addEventListener('DOMContentLoaded', function() {
    // Initialize date pickers for booking forms
    initDatePickers();
    
    // Initialize room search filters
    initSearchFilters();
    
    // Initialize booking date validation
    initBookingDateValidation();
    
    // Initialize payment form validation
    initPaymentFormValidation();
    
    // Setup Flash Message Auto-Hide
    setupFlashMessages();
});

/**
 * Initialize date pickers for booking forms
 */
function initDatePickers() {
    const checkInDateInput = document.getElementById('checkInDate');
    const checkOutDateInput = document.getElementById('checkOutDate');
    
    if (checkInDateInput && checkOutDateInput) {
        // Set min date to tomorrow for check-in
        const tomorrow = new Date();
        tomorrow.setDate(tomorrow.getDate() + 1);
        
        // Format date as YYYY-MM-DD for HTML date input
        const tomorrowFormatted = tomorrow.toISOString().split('T')[0];
        checkInDateInput.min = tomorrowFormatted;
        
        // Add event listener to update check-out min date when check-in changes
        checkInDateInput.addEventListener('change', function() {
            const selectedDate = new Date(this.value);
            selectedDate.setDate(selectedDate.getDate() + 1);
            const newMinCheckout = selectedDate.toISOString().split('T')[0];
            
            checkOutDateInput.min = newMinCheckout;
            
            // If current checkout date is before new min, update it
            if (checkOutDateInput.value && new Date(checkOutDateInput.value) < selectedDate) {
                checkOutDateInput.value = newMinCheckout;
            }
        });
    }
}

/**
 * Initialize room search filters behavior
 */
function initSearchFilters() {
    const searchForm = document.getElementById('roomSearchForm');
    
    if (searchForm) {
        // Add event listener for form reset button
        const resetButton = searchForm.querySelector('button[type="reset"]');
        if (resetButton) {
            resetButton.addEventListener('click', function(e) {
                e.preventDefault();
                
                // Clear all form fields
                const formInputs = searchForm.querySelectorAll('input, select');
                formInputs.forEach(input => {
                    if (input.type === 'checkbox') {
                        input.checked = false;
                    } else {
                        input.value = '';
                    }
                });
                
                // Submit the form to reset the search
                searchForm.submit();
            });
        }
    }
}

/**
 * Initialize booking date validation
 */
function initBookingDateValidation() {
    const bookingForm = document.getElementById('bookingForm');
    
    if (bookingForm) {
        bookingForm.addEventListener('submit', function(e) {
            const checkInDate = new Date(document.getElementById('checkInDate').value);
            const checkOutDate = new Date(document.getElementById('checkOutDate').value);
            const today = new Date();
            today.setHours(0, 0, 0, 0);
            
            let isValid = true;
            let errorMessage = '';
            
            // Check if check-in date is in the past
            if (checkInDate < today) {
                isValid = false;
                errorMessage = 'Check-in date cannot be in the past.';
            }
            
            // Check if check-out date is before check-in date
            if (checkOutDate <= checkInDate) {
                isValid = false;
                errorMessage = 'Check-out date must be after check-in date.';
            }
            
            if (!isValid) {
                e.preventDefault();
                
                // Show error message
                const errorDiv = document.createElement('div');
                errorDiv.className = 'alert alert-danger mt-3';
                errorDiv.textContent = errorMessage;
                
                // Remove any existing error messages
                const existingErrors = bookingForm.querySelectorAll('.alert-danger');
                existingErrors.forEach(error => error.remove());
                
                // Add new error message
                bookingForm.prepend(errorDiv);
                
                // Scroll to error message
                errorDiv.scrollIntoView({ behavior: 'smooth' });
            }
        });
    }
}

/**
 * Initialize payment form validation
 */
function initPaymentFormValidation() {
    const paymentForm = document.getElementById('paymentForm');
    
    if (paymentForm) {
        paymentForm.addEventListener('submit', function(e) {
            const cardNumber = document.getElementById('cardNumber').value;
            const cardHolderName = document.getElementById('cardHolderName').value;
            const expiryDate = document.getElementById('expiryDate').value;
            const cvv = document.getElementById('cvv').value;
            
            let isValid = true;
            let errorMessage = '';
            
            // Basic validation for card number (should be 16 digits for most cards)
            if (!/^\d{13,19}$/.test(cardNumber.replace(/\s/g, ''))) {
                isValid = false;
                errorMessage = 'Please enter a valid card number.';
            }
            
            // Validate card holder name is not empty
            if (!cardHolderName.trim()) {
                isValid = false;
                errorMessage = 'Please enter the cardholder name.';
            }
            
            // Validate expiry date format (MM/YY)
            if (!/^(0[1-9]|1[0-2])\/([0-9]{2})$/.test(expiryDate)) {
                isValid = false;
                errorMessage = 'Please enter a valid expiry date (MM/YY).';
            } else {
                // Check if card is expired
                const [month, year] = expiryDate.split('/');
                const expiryDateTime = new Date(2000 + parseInt(year), parseInt(month) - 1, 1);
                const today = new Date();
                
                if (expiryDateTime < today) {
                    isValid = false;
                    errorMessage = 'Your card has expired.';
                }
            }
            
            // Validate CVV (3 or 4 digits)
            if (!/^\d{3,4}$/.test(cvv)) {
                isValid = false;
                errorMessage = 'Please enter a valid CVV code.';
            }
            
            if (!isValid) {
                e.preventDefault();
                
                // Show error message
                const errorDiv = document.createElement('div');
                errorDiv.className = 'alert alert-danger mt-3';
                errorDiv.textContent = errorMessage;
                
                // Remove any existing error messages
                const existingErrors = paymentForm.querySelectorAll('.alert-danger');
                existingErrors.forEach(error => error.remove());
                
                // Add new error message
                paymentForm.prepend(errorDiv);
                
                // Scroll to error message
                errorDiv.scrollIntoView({ behavior: 'smooth' });
            }
        });
        
        // Format card number with spaces for readability
        const cardNumberInput = document.getElementById('cardNumber');
        if (cardNumberInput) {
            cardNumberInput.addEventListener('input', function(e) {
                let value = e.target.value.replace(/\s+/g, '');
                if (value.length > 0) {
                    value = value.match(new RegExp('.{1,4}', 'g')).join(' ');
                }
                e.target.value = value;
            });
        }
        
        // Format expiry date
        const expiryDateInput = document.getElementById('expiryDate');
        if (expiryDateInput) {
            expiryDateInput.addEventListener('input', function(e) {
                let value = e.target.value.replace(/\D/g, '');
                if (value.length > 2) {
                    value = value.substring(0, 2) + '/' + value.substring(2, 4);
                }
                e.target.value = value;
            });
        }
    }
}

/**
 * Setup auto-hide for flash messages
 */
function setupFlashMessages() {
    const flashMessages = document.querySelectorAll('.alert:not(.alert-danger)');
    
    flashMessages.forEach(function(message) {
        // Auto hide success messages after 5 seconds
        setTimeout(function() {
            message.style.opacity = '0';
            setTimeout(function() {
                message.style.display = 'none';
            }, 500);
        }, 5000);
        
        // Add close button functionality
        const closeButton = message.querySelector('.close');
        if (closeButton) {
            closeButton.addEventListener('click', function() {
                message.style.opacity = '0';
                setTimeout(function() {
                    message.style.display = 'none';
                }, 500);
            });
        }
    });
}
