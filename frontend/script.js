// AnyScreen Frontend JavaScript
class AnyScreenApp {
    constructor() {
        this.currentStage = 1;
        this.connectedDevices = [];
        this.pendingDevice = null;
        this.selectedScreenType = null;
        this.selectedScreenId = null;
        
        this.init();
    }

    init() {
        this.bindEvents();
        this.updateProgressIndicator();
        this.showStage(1);
        this.generateMockData();
        this.initDevControls();
    }

    initDevControls() {
        // Development controls - Remove in final version
        const devToggle = document.getElementById('devStageToggle');
        const devButtons = document.getElementById('devStageButtons');
        
        if (devToggle && devButtons) {
            devToggle.addEventListener('click', () => {
                devButtons.classList.toggle('show');
            });
        }
    }

    bindEvents() {
        // Navigation buttons
        document.getElementById('nextBtn')?.addEventListener('click', () => this.nextStage());
        document.getElementById('backBtn')?.addEventListener('click', () => this.previousStage());
        
        // Copy URL functionality
        document.getElementById('copyUrlBtn')?.addEventListener('click', () => this.copyUrl());
        
        // Device approval buttons
        document.getElementById('approveDeviceBtn')?.addEventListener('click', () => this.approveDevice());
        document.getElementById('denyDeviceBtn')?.addEventListener('click', () => this.denyDevice());
        
        // Screen selection
        document.querySelectorAll('input[name="screenType"]').forEach(radio => {
            radio.addEventListener('change', (e) => this.selectScreenType(e.target.value));
        });
        
        // Connect new device button
        document.getElementById('connectNewDeviceBtn')?.addEventListener('click', () => this.connectNewDevice());
        
        // Mock simulate device connection
        document.getElementById('simulateConnectionBtn')?.addEventListener('click', () => this.simulateDeviceConnection());
    }

    generateMockData() {
        // Generate QR code placeholder
        this.generateQRCode();
        
        // Generate shareable URL
        this.generateShareableUrl();
        
        // Generate mock screens and windows
        this.generateMockScreens();
    }


    generateQRCode() {
        const qrContainer = document.getElementById('qrCode');
        if (qrContainer) {
            // Create a simple QR code placeholder
            qrContainer.innerHTML = `
                <div class="qr-placeholder">
                    <div class="qr-grid">
                        ${Array(100).fill(0).map(() => 
                            `<div class="qr-pixel ${Math.random() > 0.5 ? 'active' : ''}"></div>`
                        ).join('')}
                    </div>
                    <p>Scan to connect</p>
                </div>
            `;
        }
    }

    generateShareableUrl() {
        const urlDisplay = document.getElementById('shareableUrl');
        if (urlDisplay) {
            const mockUrl = `http://${this.getLocalIP()}:8080/connect?session=${this.generateSessionId()}`;
            urlDisplay.textContent = mockUrl;
        }
    }

    getLocalIP() {
        // Mock IP address
        return '192.168.1.100';
    }

    generateSessionId() {
        return Math.random().toString(36).substring(2, 15);
    }

    generateMockScreens() {
        const screensContainer = document.getElementById('availableScreens');
        const windowsContainer = document.getElementById('availableWindows');
        
        if (screensContainer) {
            screensContainer.innerHTML = `
                <div class="screen-option" data-screen-id="screen-1">
                    <div class="screen-preview">
                        <div class="mock-screen-content">
                            <div class="mock-window"></div>
                            <div class="mock-window"></div>
                            <div class="mock-taskbar"></div>
                        </div>
                    </div>
                    <div class="screen-info">
                        <h4>Primary Display</h4>
                        <p>1920 × 1080 • Main Monitor</p>
                    </div>
                </div>
                <div class="screen-option" data-screen-id="screen-2">
                    <div class="screen-preview">
                        <div class="mock-screen-content secondary">
                            <div class="mock-window small"></div>
                            <div class="mock-taskbar"></div>
                        </div>
                    </div>
                    <div class="screen-info">
                        <h4>Secondary Display</h4>
                        <p>1366 × 768 • External Monitor</p>
                    </div>
                </div>
            `;
        }

        if (windowsContainer) {
            windowsContainer.innerHTML = `
                <div class="window-option" data-window-id="window-1">
                    <div class="window-icon">🌐</div>
                    <div class="window-info">
                        <h4>Google Chrome</h4>
                        <p>Web Browser • 1280 × 720</p>
                    </div>
                </div>
                <div class="window-option" data-window-id="window-2">
                    <div class="window-icon">📝</div>
                    <div class="window-info">
                        <h4>Visual Studio Code</h4>
                        <p>Code Editor • 1440 × 900</p>
                    </div>
                </div>
                <div class="window-option" data-window-id="window-3">
                    <div class="window-icon">🎵</div>
                    <div class="window-info">
                        <h4>Spotify</h4>
                        <p>Music Player • 800 × 600</p>
                    </div>
                </div>
                <div class="window-option" data-window-id="window-4">
                    <div class="window-icon">📊</div>
                    <div class="window-info">
                        <h4>LibreOffice Calc</h4>
                        <p>Spreadsheet • 1200 × 800</p>
                    </div>
                </div>
            `;
        }

        // Bind selection events
        this.bindScreenSelectionEvents();
    }

    bindScreenSelectionEvents() {
        document.querySelectorAll('.screen-option').forEach(option => {
            option.addEventListener('click', () => this.selectScreen(option.dataset.screenId, 'screen'));
        });

        document.querySelectorAll('.window-option').forEach(option => {
            option.addEventListener('click', () => this.selectScreen(option.dataset.windowId, 'window'));
        });
    }

    selectScreenType(type) {
        this.selectedScreenType = type;
        const screensSection = document.getElementById('screensSection');
        const windowsSection = document.getElementById('windowsSection');
        
        if (type === 'fullScreen') {
            screensSection.style.display = 'block';
            windowsSection.style.display = 'none';
        } else {
            screensSection.style.display = 'none';
            windowsSection.style.display = 'block';
        }
        
        this.selectedScreenId = null;
        this.updateSelectionButtons();
    }

    selectScreen(id, type) {
        // Remove previous selections
        document.querySelectorAll('.screen-option, .window-option').forEach(option => {
            option.classList.remove('selected');
        });
        
        // Add selection to clicked item
        const selectedElement = document.querySelector(`[data-${type}-id="${id}"]`);
        if (selectedElement) {
            selectedElement.classList.add('selected');
            this.selectedScreenId = id;
            this.updateSelectionButtons();
        }
    }

    updateSelectionButtons() {
        const nextBtn = document.getElementById('nextBtn');
        if (nextBtn && this.currentStage === 2) {
            nextBtn.disabled = !this.selectedScreenType || !this.selectedScreenId;
        }
    }

    simulateDeviceConnection() {
        // Simulate a device trying to connect
        this.pendingDevice = {
            name: 'Miguel\'s iPhone',
            type: 'Mobile Device',
            ip: '192.168.1.105',
            os: 'iOS 17.4',
            browser: 'Safari 17.0',
            timestamp: new Date().toLocaleTimeString()
        };
        
        // Go to stage 1 and show device approval
        this.currentStage = 1;
        this.updateProgressIndicator();
        this.showStage(1);
        this.showDeviceApproval();
    }

    showDeviceApproval() {
        if (!this.pendingDevice) return;
        
        // Find and populate the device requests section
        const deviceRequests = document.querySelector('.device-requests');
        if (deviceRequests) {
            deviceRequests.innerHTML = `
                <div class="device-request">
                    <div class="device-info">
                        <div class="device-icon">
                            <i class="fas fa-mobile-alt"></i>
                        </div>
                        <div class="device-details">
                            <h3>${this.pendingDevice.name}</h3>
                            <div class="device-specs">
                                <span><i class="fas fa-globe"></i> ${this.pendingDevice.ip}</span>
                                <span><i class="fab fa-apple"></i> ${this.pendingDevice.os}</span>
                                <span><i class="fab fa-safari"></i> ${this.pendingDevice.browser}</span>
                                <span><i class="fas fa-clock"></i> ${this.pendingDevice.timestamp}</span>
                            </div>
                        </div>
                    </div>
                    <div class="device-actions">
                        <button class="btn-deny" id="denyDeviceBtn">
                            <i class="fas fa-times"></i> Deny
                        </button>
                        <button class="btn-allow" id="approveDeviceBtn">
                            <i class="fas fa-check"></i> Allow
                        </button>
                    </div>
                </div>
            `;
            
            // Re-bind the event listeners for the new buttons
            document.getElementById('approveDeviceBtn')?.addEventListener('click', () => this.approveDevice());
            document.getElementById('denyDeviceBtn')?.addEventListener('click', () => this.denyDevice());
        }
        
        // Make sure we're on the select section (stage 2)
        this.currentStage = 2;
        this.updateProgressIndicator();
        this.showStage(2);
    }

    approveDevice() {
        if (this.pendingDevice) {
            this.connectedDevices.push({
                ...this.pendingDevice,
                id: Date.now(),
                status: 'connected'
            });
            
            this.pendingDevice = null;
            this.hideDeviceApproval();
            this.nextStage(); // This will go to stage 3 (confirm-section - screen selection)
        }
    }

    denyDevice() {
        this.pendingDevice = null;
        this.hideDeviceApproval();
        // Go back to stage 1
        this.currentStage = 1;
        this.updateProgressIndicator();
        this.showStage(1);
    }

    hideDeviceApproval() {
        // Reset the device requests section to show the original content
        const deviceRequests = document.querySelector('.device-requests');
        if (deviceRequests) {
            deviceRequests.innerHTML = `
                <div class="device-request">
                    <div class="device-info">
                        <div class="device-icon">
                            <i class="fas fa-mobile-alt"></i>
                        </div>
                        <div class="device-details">
                            <h3>iPhone 14 Pro</h3>
                            <div class="device-specs">
                                <span><i class="fas fa-globe"></i> 192.168.1.105</span>
                                <span><i class="fab fa-apple"></i> iOS 16.4</span>
                                <span><i class="fab fa-safari"></i> Safari 16.4</span>
                            </div>
                        </div>
                    </div>
                    <div class="device-actions">
                        <button class="btn-deny" onclick="denyDevice(this)">
                            <i class="fas fa-times"></i> Deny
                        </button>
                        <button class="btn-allow" onclick="allowDevice(this)">
                            <i class="fas fa-check"></i> Allow
                        </button>
                    </div>
                </div>
                <div class="device-request">
                    <div class="device-info">
                        <div class="device-icon">
                            <i class="fas fa-laptop"></i>
                        </div>
                        <div class="device-details">
                            <h3>MacBook Pro</h3>
                            <div class="device-specs">
                                <span><i class="fas fa-globe"></i> 192.168.1.108</span>
                                <span><i class="fab fa-apple"></i> macOS Ventura</span>
                                <span><i class="fab fa-chrome"></i> Chrome 118</span>
                            </div>
                        </div>
                    </div>
                    <div class="device-actions">
                        <button class="btn-deny" onclick="denyDevice(this)">
                            <i class="fas fa-times"></i> Deny
                        </button>
                        <button class="btn-allow" onclick="allowDevice(this)">
                            <i class="fas fa-check"></i> Allow
                        </button>
                    </div>
                </div>
            `;
        }
    }

    copyUrl() {
        const urlElement = document.getElementById('shareableUrl');
        if (urlElement) {
            navigator.clipboard.writeText(urlElement.textContent).then(() => {
                const copyBtn = document.getElementById('copyUrlBtn');
                const originalText = copyBtn.textContent;
                copyBtn.textContent = 'Copied!';
                copyBtn.classList.add('copied');
                
                setTimeout(() => {
                    copyBtn.textContent = originalText;
                    copyBtn.classList.remove('copied');
                }, 2000);
            });
        }
    }

    nextStage() {
        if (this.currentStage < 5) {
            this.currentStage++;
            this.updateProgressIndicator();
            this.showStage(this.currentStage);
        }
    }

    previousStage() {
        if (this.currentStage > 1) {
            this.currentStage--;
            this.updateProgressIndicator();
            this.showStage(this.currentStage);
        }
    }

    updateProgressIndicator() {
        const steps = document.querySelectorAll('.progress-step');
        steps.forEach((step, index) => {
            const stepNumber = index + 1;
            step.classList.remove('active', 'completed');
            
            if (stepNumber < this.currentStage) {
                step.classList.add('completed');
            } else if (stepNumber === this.currentStage) {
                step.classList.add('active');
            }
        });
    }

    showStage(stageNumber) {
        // Hide all stages
        document.querySelectorAll('.content-section').forEach(stage => {
            stage.classList.remove('active');
        });
        
        // Show current stage
        let currentStageId;
        if (stageNumber === 1) currentStageId = 'connect-section';
        else if (stageNumber === 2) currentStageId = 'select-section';
        else if (stageNumber === 3) currentStageId = 'confirm-section';
        else if (stageNumber === 4) currentStageId = 'final-section';
        else if (stageNumber === 5) currentStageId = 'session-section';
        
        const currentStage = document.getElementById(currentStageId);
        if (currentStage) {
            currentStage.classList.add('active');
        }
        
        // Update navigation buttons
        this.updateNavigationButtons();
        
        // Handle stage-specific logic
        if (stageNumber === 4) {
            this.updateConfirmationStage();
        }
    }

    updateNavigationButtons() {
        const backBtn = document.getElementById('backBtn');
        const nextBtn = document.getElementById('nextBtn');
        
        if (backBtn) {
            backBtn.style.display = this.currentStage > 1 ? 'block' : 'none';
        }
        
        if (nextBtn) {
            if (this.currentStage === 1) {
                nextBtn.style.display = 'none'; // Hidden until device connects
            } else if (this.currentStage === 2) {
                nextBtn.style.display = 'none'; // Hidden in device approval stage
            } else if (this.currentStage === 3) {
                nextBtn.style.display = 'block';
                nextBtn.textContent = 'Start Sharing';
                nextBtn.disabled = !this.selectedScreenType || !this.selectedScreenId;
            } else if (this.currentStage === 4) {
                nextBtn.style.display = 'block';
                nextBtn.textContent = 'Confirm & Start';
                nextBtn.disabled = false;
            } else if (this.currentStage >= 5) {
                nextBtn.style.display = 'none'; // No next button on session stage
            }
        }
    }

    updateConfirmationStage() {
        const confirmationDetails = document.getElementById('confirmationDetails');
        if (confirmationDetails && this.connectedDevices.length > 0) {
            const device = this.connectedDevices[0];
            const screenTypeText = this.selectedScreenType === 'fullScreen' ? 'Full Screen' : 'Application Window';
            const screenName = this.getScreenName();
            
            confirmationDetails.innerHTML = `
                <div class="confirmation-summary">
                    <h3>Connection Summary</h3>
                    <div class="summary-item">
                        <span class="summary-label">Connected Device:</span>
                        <span class="summary-value">${device.name}</span>
                    </div>
                    <div class="summary-item">
                        <span class="summary-label">Device IP:</span>
                        <span class="summary-value">${device.ip}</span>
                    </div>
                    <div class="summary-item">
                        <span class="summary-label">Share Type:</span>
                        <span class="summary-value">${screenTypeText}</span>
                    </div>
                    <div class="summary-item">
                        <span class="summary-label">Selected ${screenTypeText}:</span>
                        <span class="summary-value">${screenName}</span>
                    </div>
                </div>
            `;
        }
        
        this.updateActiveDevices();
    }

    getScreenName() {
        if (this.selectedScreenType === 'fullScreen') {
            return this.selectedScreenId === 'screen-1' ? 'Primary Display (1920 × 1080)' : 'Secondary Display (1366 × 768)';
        } else {
            const windowNames = {
                'window-1': 'Google Chrome',
                'window-2': 'Visual Studio Code',
                'window-3': 'Spotify',
                'window-4': 'LibreOffice Calc'
            };
            return windowNames[this.selectedScreenId] || 'Unknown Window';
        }
    }

    updateActiveDevices() {
        const activeDevicesList = document.getElementById('activeDevicesList');
        if (activeDevicesList) {
            if (this.connectedDevices.length === 0) {
                activeDevicesList.innerHTML = '<p class="no-devices">No active connections</p>';
            } else {
                activeDevicesList.innerHTML = this.connectedDevices.map(device => `
                    <div class="connected-device">
                        <div class="device-info">
                            <i class="fas fa-${device.type === 'Mobile Device' ? 'mobile-alt' : 'laptop'}"></i>
                            <div>
                                <span class="device-name">${device.name}</span>
                                <span class="device-status">Connected • ${device.ip}</span>
                            </div>
                        </div>
                        <button class="btn-disconnect" onclick="app.disconnectDevice(${device.id})" title="Disconnect device" aria-label="Disconnect this device">
                            <i class="fas fa-times"></i>
                        </button>
                    </div>
                `).join('');
            }
        }
    }

    disconnectDevice(deviceId) {
        this.connectedDevices = this.connectedDevices.filter(device => device.id !== deviceId);
        this.updateActiveDevices();
    }

    connectNewDevice() {
        this.currentStage = 1;
        this.selectedScreenType = null;
        this.selectedScreenId = null;
        this.pendingDevice = null;
        
        this.updateProgressIndicator();
        this.showStage(1);
        this.hideDeviceApproval();
    }

    // Development methods - Remove in final version
    goToStage(stageNumber) {
        if (stageNumber >= 1 && stageNumber <= 5) {
            this.currentStage = stageNumber;
            this.updateProgressIndicator();
            this.showStage(stageNumber);
            this.setupStageForTesting(stageNumber);
        }
    }
    
    setupStageForTesting(stageNumber) {
        if (stageNumber === 2 && this.connectedDevices.length === 0) {
            this.addMockDevice();
        }
        
        if (stageNumber === 3) {
            this.setupMockSelection();
            this.updateConfirmationStage();
        }
        
        if (stageNumber === 5) {
            this.setupActiveSession();
        }
    }
    
    addMockDevice() {
        this.connectedDevices.push({
            id: Date.now(),
            name: 'Test Device',
            type: 'Mobile Device',
            ip: '192.168.1.100',
            os: 'iOS 17.4',
            browser: 'Safari 17.0',
            status: 'connected'
        });
    }
    
    setupMockSelection() {
        if (!this.selectedScreenType) {
            this.selectedScreenType = 'fullScreen';
            this.selectedScreenId = 'screen-1';
        }
    }
    
    setupActiveSession() {
        if (this.connectedDevices.length === 0) {
            this.connectedDevices.push(
                {
                    id: Date.now(),
                    name: 'Miguel\'s iPhone',
                    type: 'Mobile Device',
                    ip: '192.168.1.105',
                    os: 'iOS 17.4',
                    browser: 'Safari 17.0',
                    status: 'connected'
                },
                {
                    id: Date.now() + 1,
                    name: 'MacBook Pro',
                    type: 'Laptop',
                    ip: '192.168.1.108',
                    os: 'macOS Ventura',
                    browser: 'Chrome 118',
                    status: 'connected'
                }
            );
        }
        if (!this.selectedScreenType) {
            this.selectedScreenType = 'fullScreen';
            this.selectedScreenId = 'screen-1';
        }
        this.updateActiveDevices();
    }

    resetApp() {
        this.currentStage = 1;
        this.connectedDevices = [];
        this.pendingDevice = null;
        this.selectedScreenType = null;
        this.selectedScreenId = null;
        
        this.updateProgressIndicator();
        this.showStage(1);
        this.hideDeviceApproval();
        
        // Reset form selections
        document.querySelectorAll('input[name="screenType"]').forEach(radio => {
            radio.checked = false;
        });
        
        document.querySelectorAll('.screen-option, .window-option').forEach(option => {
            option.classList.remove('selected');
        });
    }
}

// Initialize the app when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    window.app = new AnyScreenApp();
});

// Add some additional CSS for dynamic elements
const additionalStyles = `
    .qr-placeholder {
        text-align: center;
        padding: 20px;
    }
    
    .qr-grid {
        display: grid;
        grid-template-columns: repeat(10, 1fr);
        gap: 2px;
        width: 200px;
        height: 200px;
        margin: 0 auto 10px;
        background: white;
        padding: 10px;
        border-radius: 8px;
    }
    
    .qr-pixel {
        background: white;
        border-radius: 1px;
    }
    
    .qr-pixel.active {
        background: black;
    }
    
    .screen-option.selected,
    .window-option.selected {
        border-color: var(--accent-color);
        background: var(--accent-color-alpha);
    }
    
    .mock-screen-content {
        width: 100%;
        height: 100%;
        background: linear-gradient(135deg, #1a1a1a 0%, #2d1b69 100%);
        border-radius: 4px;
        position: relative;
        overflow: hidden;
    }
    
    .mock-screen-content.secondary {
        background: linear-gradient(135deg, #1a1a1a 0%, #4a1a4a 100%);
    }
    
    .mock-window {
        position: absolute;
        background: rgba(255, 255, 255, 0.1);
        border: 1px solid rgba(255, 255, 255, 0.2);
        border-radius: 4px;
    }
    
    .mock-window:first-child {
        top: 20%;
        left: 10%;
        width: 40%;
        height: 50%;
    }
    
    .mock-window:nth-child(2) {
        top: 30%;
        right: 15%;
        width: 35%;
        height: 40%;
    }
    
    .mock-window.small {
        top: 25%;
        left: 20%;
        width: 60%;
        height: 50%;
    }
    
    .mock-taskbar {
        position: absolute;
        bottom: 0;
        left: 0;
        right: 0;
        height: 8%;
        background: rgba(0, 0, 0, 0.3);
    }
    
    .copied {
        background: var(--success-color) !important;
        transform: scale(0.95);
    }
    
    .status-indicator {
        display: inline-block;
        width: 8px;
        height: 8px;
        border-radius: 50%;
        margin-right: 5px;
    }
    
    .status-indicator.active {
        background: var(--success-color);
        box-shadow: 0 0 6px var(--success-color);
    }
`;

// Inject additional styles
const styleSheet = document.createElement('style');
styleSheet.textContent = additionalStyles;
document.head.appendChild(styleSheet);
