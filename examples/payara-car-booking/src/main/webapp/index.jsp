<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Car Booking Chat</title>
    <script>
        async function sendQuestion() {
            const question = document.getElementById("questionInput").value;
            const responseContainer = document.getElementById("response");

            if (!question.trim()) {
                responseContainer.innerText = "Please enter a question.";
                return;
            }

            responseContainer.innerText = "Thinking...";

            try {
                const response = await fetch(`http://127.0.0.1:8080/payara-car-booking/api/car-booking/chat?question=` + encodeURIComponent(question), {
                    method: 'GET',
                    headers: {
                        'Accept': 'text/plain'
                    }
                });

                if (!response.ok) {
                    throw new Error("Failed to get response from server");
                }

                const text = await response.text();
                responseContainer.innerText = text;
            } catch (err) {
                responseContainer.innerText = "Error: " + err.message;
            }
        }
    </script>
</head>
<body>
<h2>Car Booking Assistant</h2>
<input type="text" id="questionInput" placeholder="Ask something..." style="width: 400px;" />
<button onclick="sendQuestion()">Send</button>

<h3>Response:</h3>
<div id="response" style="white-space: pre-wrap; border: 1px solid #ccc; padding: 10px; width: 500px; min-height: 100px;"></div>
</body>
</html>
