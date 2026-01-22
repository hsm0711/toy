#!/bin/bash
# create-files-part2.sh - HTML, CSS, JS 파일 생성

set -e

echo "=== Part 2: 웹 리소스 파일 생성 시작 ==="

# index.html 생성
cat > src/main/resources/templates/index.html << 'EOFHTML1'
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Web Application</title>
    <link rel="stylesheet" th:href="@{/css/style.css}">
</head>
<body>
    <nav class="navbar">
        <div class="container">
            <h1 class="logo">Web App</h1>
            <ul class="nav-menu">
                <li th:each="menu : ${menus}">
                    <a th:href="@{${menu.path}}" 
                       th:text="${menu.name}"
                       th:classappend="${currentPage == menu.path.substring(1)} ? 'active' : ''">
                    </a>
                </li>
            </ul>
        </div>
    </nav>

    <main class="container">
        <section class="hero">
            <h2>환영합니다!</h2>
            <p>PDF 병합 도구를 사용하여 여러 PDF 파일을 하나로 합칠 수 있습니다.</p>
            <a th:href="@{/pdf-merge}" class="btn btn-primary">PDF 병합하기</a>
        </section>

        <section class="features">
            <div class="feature-card">
                <h3>🔄 PDF 병합</h3>
                <p>여러 PDF 파일을 하나로 병합</p>
            </div>
            <div class="feature-card">
                <h3>📋 순서 설정</h3>
                <p>원하는 순서로 PDF 정렬</p>
            </div>
            <div class="feature-card">
                <h3>⚡ 빠른 처리</h3>
                <p>빠르고 안전한 PDF 처리</p>
            </div>
        </section>
    </main>

    <footer>
        <div class="container">
            <p>&copy; 2026 Web Application. All rights reserved.</p>
        </div>
    </footer>
</body>
</html>
EOFHTML1

echo "✓ index.html 생성"

# pdf-merge.html 생성
cat > src/main/resources/templates/pdf-merge.html << 'EOFHTML2'
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>PDF 병합 - Web Application</title>
    <link rel="stylesheet" th:href="@{/css/style.css}">
</head>
<body>
    <nav class="navbar">
        <div class="container">
            <h1 class="logo">Web App</h1>
            <ul class="nav-menu">
                <li th:each="menu : ${menus}">
                    <a th:href="@{${menu.path}}" 
                       th:text="${menu.name}"
                       th:classappend="${currentPage == 'pdf-merge'} ? 'active' : ''">
                    </a>
                </li>
            </ul>
        </div>
    </nav>

    <main class="container">
        <div class="pdf-merge-container">
            <h2>PDF 파일 병합</h2>
            <p class="description">여러 개의 PDF 파일을 선택하고 순서를 조정한 후 병합하세요.</p>

            <div class="upload-area" id="uploadArea">
                <input type="file" id="fileInput" accept=".pdf" multiple style="display: none;">
                <div class="upload-prompt">
                    <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                        <polyline points="17 8 12 3 7 8"></polyline>
                        <line x1="12" y1="3" x2="12" y2="15"></line>
                    </svg>
                    <p>PDF 파일을 여기에 드래그하거나 클릭하여 선택하세요</p>
                </div>
            </div>

            <div id="fileList" class="file-list" style="display: none;">
                <h3>선택된 파일 <span id="fileCount">0</span>개</h3>
                <ul id="files"></ul>
            </div>

            <div class="action-buttons" id="actionButtons" style="display: none;">
                <button id="mergeBtn" class="btn btn-primary">PDF 병합하기</button>
                <button id="clearBtn" class="btn btn-secondary">초기화</button>
            </div>

            <div id="result" class="result" style="display: none;"></div>
            <div id="loading" class="loading" style="display: none;">
                <div class="spinner"></div>
                <p>PDF를 병합하는 중입니다...</p>
            </div>
        </div>
    </main>

    <footer>
        <div class="container">
            <p>&copy; 2026 Web Application. All rights reserved.</p>
        </div>
    </footer>

    <script th:src="@{/js/pdf-merge.js}"></script>
</body>
</html>
EOFHTML2

echo "✓ pdf-merge.html 생성"

# style.css 생성 (파일이 크므로 분할)
cat > src/main/resources/static/css/style.css << 'EOFCSS'
* {
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}

body {
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
    line-height: 1.6;
    color: #333;
    background-color: #f5f5f5;
}

.container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 20px;
}

.navbar {
    background-color: #2c3e50;
    color: white;
    padding: 1rem 0;
    box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.navbar .container {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.logo {
    font-size: 1.5rem;
    font-weight: bold;
}

.nav-menu {
    list-style: none;
    display: flex;
    gap: 2rem;
}

.nav-menu a {
    color: white;
    text-decoration: none;
    padding: 0.5rem 1rem;
    border-radius: 4px;
    transition: background-color 0.3s;
}

.nav-menu a:hover,
.nav-menu a.active {
    background-color: #34495e;
}

main {
    padding: 2rem 0;
    min-height: calc(100vh - 200px);
}

.hero {
    text-align: center;
    padding: 3rem 0;
    background: white;
    border-radius: 8px;
    margin-bottom: 2rem;
    box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.hero h2 {
    font-size: 2.5rem;
    margin-bottom: 1rem;
    color: #2c3e50;
}

.hero p {
    font-size: 1.2rem;
    color: #7f8c8d;
    margin-bottom: 2rem;
}

.btn {
    display: inline-block;
    padding: 0.75rem 1.5rem;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    font-size: 1rem;
    text-decoration: none;
    transition: all 0.3s;
}

.btn-primary {
    background-color: #3498db;
    color: white;
}

.btn-primary:hover {
    background-color: #2980b9;
}

.btn-secondary {
    background-color: #95a5a6;
    color: white;
}

.btn-secondary:hover {
    background-color: #7f8c8d;
}

.features {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
    gap: 2rem;
    margin-top: 2rem;
}

.feature-card {
    background: white;
    padding: 2rem;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.1);
    text-align: center;
}

.feature-card h3 {
    font-size: 1.5rem;
    margin-bottom: 1rem;
    color: #2c3e50;
}

.pdf-merge-container {
    background: white;
    padding: 2rem;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.pdf-merge-container h2 {
    color: #2c3e50;
    margin-bottom: 0.5rem;
}

.description {
    color: #7f8c8d;
    margin-bottom: 2rem;
}

.upload-area {
    border: 2px dashed #3498db;
    border-radius: 8px;
    padding: 3rem;
    text-align: center;
    cursor: pointer;
    transition: all 0.3s;
    margin-bottom: 2rem;
}

.upload-area:hover {
    background-color: #ecf0f1;
    border-color: #2980b9;
}

.upload-area.dragover {
    background-color: #d5e8f7;
    border-color: #2980b9;
}

.upload-prompt svg {
    color: #3498db;
    margin-bottom: 1rem;
}

.upload-prompt p {
    color: #7f8c8d;
}

.file-list {
    margin-bottom: 2rem;
}

.file-list h3 {
    margin-bottom: 1rem;
    color: #2c3e50;
}

#files {
    list-style: none;
}

#files li {
    background: #ecf0f1;
    padding: 1rem;
    margin-bottom: 0.5rem;
    border-radius: 4px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    cursor: move;
}

#files li:hover {
    background: #d5dbdb;
}

.file-info {
    display: flex;
    align-items: center;
    gap: 1rem;
}

.file-actions {
    display: flex;
    gap: 0.5rem;
}

.file-actions button {
    background: none;
    border: none;
    cursor: pointer;
    padding: 0.25rem;
    color: #7f8c8d;
}

.file-actions button:hover {
    color: #2c3e50;
}

.action-buttons {
    display: flex;
    gap: 1rem;
    margin-bottom: 2rem;
}

.result {
    padding: 1rem;
    border-radius: 4px;
    margin-top: 1rem;
}

.result.success {
    background-color: #d5f4e6;
    border: 1px solid #27ae60;
    color: #27ae60;
}

.result.error {
    background-color: #fadbd8;
    border: 1px solid #e74c3c;
    color: #e74c3c;
}

.loading {
    text-align: center;
    padding: 2rem;
}

.spinner {
    border: 4px solid #f3f3f3;
    border-top: 4px solid #3498db;
    border-radius: 50%;
    width: 40px;
    height: 40px;
    animation: spin 1s linear infinite;
    margin: 0 auto 1rem;
}

@keyframes spin {
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
}

footer {
    background-color: #2c3e50;
    color: white;
    text-align: center;
    padding: 2rem 0;
    margin-top: 3rem;
}

@media (max-width: 768px) {
    .navbar .container {
        flex-direction: column;
        gap: 1rem;
    }

    .nav-menu {
        flex-direction: column;
        gap: 0.5rem;
        text-align: center;
    }

    .features {
        grid-template-columns: 1fr;
    }

    .action-buttons {
        flex-direction: column;
    }
}
EOFCSS

echo "✓ style.css 생성"

# pdf-merge.js 생성
cat > src/main/resources/static/js/pdf-merge.js << 'EOFJS'
let selectedFiles = [];

const uploadArea = document.getElementById('uploadArea');
const fileInput = document.getElementById('fileInput');
const fileList = document.getElementById('fileList');
const filesContainer = document.getElementById('files');
const fileCount = document.getElementById('fileCount');
const actionButtons = document.getElementById('actionButtons');
const mergeBtn = document.getElementById('mergeBtn');
const clearBtn = document.getElementById('clearBtn');
const result = document.getElementById('result');
const loading = document.getElementById('loading');

uploadArea.addEventListener('click', () => {
    fileInput.click();
});

fileInput.addEventListener('change', (e) => {
    handleFiles(e.target.files);
});

uploadArea.addEventListener('dragover', (e) => {
    e.preventDefault();
    uploadArea.classList.add('dragover');
});

uploadArea.addEventListener('dragleave', () => {
    uploadArea.classList.remove('dragover');
});

uploadArea.addEventListener('drop', (e) => {
    e.preventDefault();
    uploadArea.classList.remove('dragover');
    handleFiles(e.dataTransfer.files);
});

function handleFiles(files) {
    const pdfFiles = Array.from(files).filter(file => 
        file.type === 'application/pdf' || file.name.toLowerCase().endsWith('.pdf')
    );

    if (pdfFiles.length === 0) {
        showResult('PDF 파일만 선택할 수 있습니다.', 'error');
        return;
    }

    selectedFiles = [...selectedFiles, ...pdfFiles];
    updateFileList();
    hideResult();
}

function updateFileList() {
    if (selectedFiles.length === 0) {
        fileList.style.display = 'none';
        actionButtons.style.display = 'none';
        return;
    }

    fileList.style.display = 'block';
    actionButtons.style.display = 'flex';
    fileCount.textContent = selectedFiles.length;
    
    filesContainer.innerHTML = '';
    selectedFiles.forEach((file, index) => {
        const li = document.createElement('li');
        li.draggable = true;
        li.dataset.index = index;
        
        li.innerHTML = `
            <div class="file-info">
                <span>${index + 1}.</span>
                <span>${file.name}</span>
                <span>(${formatFileSize(file.size)})</span>
            </div>
            <div class="file-actions">
                <button onclick="moveUp(${index})" ${index === 0 ? 'disabled' : ''}>↑</button>
                <button onclick="moveDown(${index})" ${index === selectedFiles.length - 1 ? 'disabled' : ''}>↓</button>
                <button onclick="removeFile(${index})">✕</button>
            </div>
        `;

        li.addEventListener('dragstart', handleDragStart);
        li.addEventListener('dragover', handleDragOver);
        li.addEventListener('drop', handleDrop);
        li.addEventListener('dragend', handleDragEnd);

        filesContainer.appendChild(li);
    });
}

function formatFileSize(bytes) {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i];
}

function moveUp(index) {
    if (index > 0) {
        [selectedFiles[index], selectedFiles[index - 1]] = [selectedFiles[index - 1], selectedFiles[index]];
        updateFileList();
    }
}

function moveDown(index) {
    if (index < selectedFiles.length - 1) {
        [selectedFiles[index], selectedFiles[index + 1]] = [selectedFiles[index + 1], selectedFiles[index]];
        updateFileList();
    }
}

function removeFile(index) {
    selectedFiles.splice(index, 1);
    updateFileList();
}

let draggedIndex = null;

function handleDragStart(e) {
    draggedIndex = parseInt(e.target.dataset.index);
    e.target.style.opacity = '0.4';
}

function handleDragOver(e) {
    e.preventDefault();
    return false;
}

function handleDrop(e) {
    e.stopPropagation();
    const dropIndex = parseInt(e.target.closest('li').dataset.index);
    
    if (draggedIndex !== dropIndex) {
        const draggedFile = selectedFiles[draggedIndex];
        selectedFiles.splice(draggedIndex, 1);
        selectedFiles.splice(dropIndex, 0, draggedFile);
        updateFileList();
    }
    return false;
}

function handleDragEnd(e) {
    e.target.style.opacity = '1';
    draggedIndex = null;
}

mergeBtn.addEventListener('click', async () => {
    if (selectedFiles.length < 2) {
        showResult('최소 2개 이상의 PDF 파일이 필요합니다.', 'error');
        return;
    }

    const formData = new FormData();
    selectedFiles.forEach(file => {
        formData.append('files', file);
    });

    const order = selectedFiles.map((_, index) => index);
    order.forEach(index => {
        formData.append('order', index);
    });

    loading.style.display = 'block';
    hideResult();
    mergeBtn.disabled = true;

    try {
        const response = await fetch('/pdf-merge/merge', {
            method: 'POST',
            body: formData
        });

        const data = await response.json();

        if (data.success) {
            showResult(
                `${data.message} <a href="${data.downloadUrl}" class="btn btn-primary" style="margin-left: 1rem;">다운로드</a>`,
                'success'
            );
        } else {
            showResult(data.message || 'PDF 병합에 실패했습니다.', 'error');
        }
    } catch (error) {
        console.error('Error:', error);
        showResult('서버 오류가 발생했습니다.', 'error');
    } finally {
        loading.style.display = 'none';
        mergeBtn.disabled = false;
    }
});

clearBtn.addEventListener('click', () => {
    selectedFiles = [];
    fileInput.value = '';
    updateFileList();
    hideResult();
});

function showResult(message, type) {
    result.innerHTML = message;
    result.className = `result ${type}`;
    result.style.display = 'block';
}

function hideResult() {
    result.style.display = 'none';
}
EOFJS

echo "✓ pdf-merge.js 생성"

echo ""
echo "=== 모든 파일 생성 완료! ==="
echo ""
echo "생성된 파일:"
echo "- src/main/resources/templates/index.html"
echo "- src/main/resources/templates/pdf-merge.html"
echo "- src/main/resources/static/css/style.css"
echo "- src/main/resources/static/js/pdf-merge.js"
echo ""
echo "다음 단계:"
echo "1. Git 커밋:"
echo "   git add ."
echo '   git commit -m "Initial commit: Spring Boot PDF Merge Application"'
echo "   git push -u origin main"
echo ""
echo "2. 빌드 테스트:"
echo "   mvn clean package"