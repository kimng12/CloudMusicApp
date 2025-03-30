<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Cloud Music - Login</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            background: #f5f5f5;
            display: flex;
            height: 100vh;
            align-items: center;
            justify-content: center;
            margin: 0;
        }
        .container {
            background: #fff;
            padding: 2em;
            box-shadow: 0 0 20px rgba(0,0,0,0.1);
            border-radius: 8px;
            width: 100%;
            max-width: 400px;
        }
        h1 {
            text-align: center;
            margin-bottom: 1em;
            color: #333;
        }
        label {
            display: block;
            margin-top: 1em;
            font-weight: bold;
        }
        input[type="text"],
        input[type="password"] {
            width: 100%;
            padding: 0.6em;
            margin-top: 0.3em;
            border: 1px solid #ccc;
            border-radius: 4px;
        }
        .btn {
            margin-top: 1.5em;
            width: 100%;
            padding: 0.7em;
            background: #007BFF;
            color: white;
            border: none;
            border-radius: 4px;
            font-weight: bold;
            cursor: pointer;
        }
        .btn:hover {
            background: #0056b3;
        }
        .bottom-text {
            text-align: center;
            margin-top: 1em;
        }
        .error {
            color: red;
            margin-top: 1em;
            text-align: center;
        }
    </style>
</head>
<body>
<div class="container">
    <h1>Login</h1>
    <form action="login" method="post">
        <label for="email">Email:</label>
        <input type="text" id="email" name="email" required>

        <label for="password">Password:</label>
        <input type="password" id="password" name="password" required>

        <button class="btn" type="submit">Login</button>

        <div class="bottom-text">
            Don't have an account? <a href="register.html">Register</a>
        </div>

        <% if (request.getAttribute("errorMessage") != null) { %>
        <div class="error">
            <%= request.getAttribute("errorMessage") %>
        </div>
        <% } %>

        <script>
            const errorDiv = document.getElementById("errorMsg");
            if (errorDiv.textContent.trim() === "${errorMessage}" || errorDiv.textContent.trim() === "") {
                errorDiv.style.display = "none";
            } else {
                errorDiv.style.display = "block";
            }
        </script>
    </form>
</div>

</body>
</html>
