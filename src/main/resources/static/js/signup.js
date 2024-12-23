document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('signup-form');

    form.addEventListener('submit', (event) => {
        event.preventDefault(); // Prevent form submission from reloading the page

        // Get form values
        const user = {
            username: document.getElementById('username').value,
            password: document.getElementById('password').value,
            email: document.getElementById('email').value,
            firstname: document.getElementById('firstName').value,
            lastname: document.getElementById('lastName').value,
            address: document.getElementById('address').value
        };

        // Validate fields
        if (!validateUsername(user.username)) {
            alert('Username must be at least 3 characters long and contain no special characters.');
            return;
        }

        if (!validatePassword(user.password)) {
            alert('Password must be at least 8 characters long and include at least one letter, one number, and one special character.');
            return;
        }

        if (!validateEmail(user.email)) {
            alert('Please enter a valid email address.');
            return;
        }

        if (!validateName(user.firstname)) {
            alert('First name must be at least 2 characters long and contain only letters.');
            return;
        }

        if (!validateName(user.lastname)) {
            alert('Last name must be at least 2 characters long and contain only letters.');
            return;
        }

        if (!user.address || user.address.trim() === '') {
            alert('Address cannot be empty.');
            return;
        }

        // Perform AJAX request to submit registration form
        fetch('/registerUser', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(user)
        })
        .then(response => {
            if (!response.ok) {
                return response.json().then(err => {
                    throw new Error('Network response was not ok: ' + err.message);
                });
            }
            return response.json();
        })
        .then(data => {
            if (data.success) {
                alert('Registration successful! You can now log in.');
                window.location.href = '/login';
            } else {
                alert('Registration failed: ' + data.message);
            }
        })
        .catch(error => {
            console.error('Error:', error.message);
            alert('An error occurred: ' + error.message);
        });
    });

    // Validation functions

    function validateUsername(username) {
        const usernameRegex = /^[a-zA-Z0-9]{3,}$/; // Minimum 3 characters, alphanumeric only
        return usernameRegex.test(username);
    }

    function validatePassword(password) {
        const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[!@#$%^&*])[A-Za-z\d!@#$%^&*]{8,}$/; // Minimum 8 chars, at least 1 letter, 1 number, and 1 special character
        return passwordRegex.test(password);
    }

    function validateEmail(email) {
        const emailRegex = /^[a-z0-9._%+-]+@[a-z0-9.-]+\.[a-z]{2,}$/; // Basic email validation
        return emailRegex.test(email);
    }

    function validateName(name) {
        const nameRegex = /^[a-zA-Z]{2,}$/; // Only letters, minimum 2 characters
        return nameRegex.test(name);
    }
});
