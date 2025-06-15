package com.anyscreen.constants;

public class HTMLConstants {
    private HTMLConstants (){}

    public static final String TEST_PAGE = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>AnyScreen - Screen Stream Test</title>
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        margin: 20px;
                        background-color: #f5f5f5;
                    }
                    .container {
                        max-width: 1200px;
                        margin: 0 auto;
                        background: white;
                        padding: 20px;
                        border-radius: 8px;
                        box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                    }
                    .controls {
                        margin-bottom: 20px;
                        padding: 15px;
                        background: #f8f9fa;
                        border-radius: 5px;
                    }
                    .stream-container {
                        text-align: center;
                        border: 2px solid #ddd;
                        border-radius: 5px;
                        overflow: hidden;
                        background: #000;
                    }
                    .stream-img {
                        max-width: 100%;
                        height: auto;
                        display: block;
                    }
                    button {
                        padding: 10px 20px;
                        margin: 5px;
                        background: #007bff;
                        color: white;
                        border: none;
                        border-radius: 5px;
                        cursor: pointer;
                    }
                    button:hover {
                        background: #0056b3;
                    }
                    button:disabled {
                        background: #6c757d;
                        cursor: not-allowed;
                    }
                    .status {
                        margin-top: 15px;
                        padding: 10px;
                        background: #e9ecef;
                        border-radius: 5px;
                        font-family: monospace;
                        font-size: 12px;
                    }
                    .error {
                        color: red;
                        background: #ffe6e6;
                    }
                    .success {
                        color: green;
                        background: #e6ffe6;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <h1>AnyScreen - Screen Stream Test</h1>
                    
                    <div class="controls">
                        <h3>Stream Controls</h3>
                        <button onclick="loadScreens()">Load Available Screens</button>
                        <button onclick="startStream(0)" id="stream0">Stream Screen 0</button>
                        <button onclick="startStream(1)" id="stream1">Stream Screen 1</button>
                        <button onclick="stopStream()">Stop Stream</button>
                        <button onclick="getStatus()">Get Status</button>
                    </div>
                    
                    <div class="stream-container">
                        <img id="streamImage" class="stream-img" style="display: none;" />
                        <div id="noStream" style="padding: 50px; color: #666;">
                            No stream active. Click "Stream Screen X" to start.
                        </div>
                    </div>
                    
                    <div id="status" class="status">
                        Ready - Click "Load Available Screens" to start
                    </div>
                </div>
                
                <script>
                    let currentStream = null;
                    
                    function updateStatus(message, isError = false) {
                        const statusDiv = document.getElementById('status');
                        statusDiv.textContent = new Date().toLocaleTimeString() + ' - ' + message;
                        statusDiv.className = 'status ' + (isError ? 'error' : 'success');
                    }
                    
                    async function loadScreens() {
                        try {
                            const response = await fetch('/screens');
                            const screens = await response.json();
                            updateStatus(`Found ${screens.length} screens: ` + 
                                screens.map(s => `Screen ${s.index} (${s.width}x${s.height}${s.isPrimary ? ', Primary' : ''})`).join(', '));
                        } catch (error) {
                            updateStatus('Error loading screens: ' + error.message, true);
                        }
                    }
                    
                    function startStream(screenIndex) {
                        stopStream(); // Stop any existing stream
                        
                        const img = document.getElementById('streamImage');
                        const noStream = document.getElementById('noStream');
                        
                        img.src = `/stream/${screenIndex}?t=${Date.now()}`;
                        img.style.display = 'block';
                        noStream.style.display = 'none';
                        
                        currentStream = screenIndex;
                        updateStatus(`Streaming screen ${screenIndex}...`);
                        
                        img.onerror = function() {
                            updateStatus(`Failed to load stream for screen ${screenIndex}`, true);
                            stopStream();
                        };
                        
                        img.onload = function() {
                            updateStatus(`Stream active for screen ${screenIndex}`);
                        };
                    }
                    
                    function stopStream() {
                        const img = document.getElementById('streamImage');
                        const noStream = document.getElementById('noStream');
                        
                        img.src = '';
                        img.style.display = 'none';
                        noStream.style.display = 'block';
                        
                        currentStream = null;
                        updateStatus('Stream stopped');
                    }
                    
                    async function getStatus() {
                        try {
                            const response = await fetch('/status');
                            const status = await response.json();
                            updateStatus(`Server: ${status.server}, Streaming: ${status.streaming}, Clients: ${status.clients}`);
                        } catch (error) {
                            updateStatus('Error getting status: ' + error.message, true);
                        }
                    }
                    
                    // Auto-load screens on page load
                    window.onload = function() {
                        loadScreens();
                    };
                </script>
            </body>
            </html>
            """;
}
