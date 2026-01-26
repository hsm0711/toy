/**
 * PDF 병합 페이지 JavaScript
 */

let selectedFiles = [];

// DOM 요소
const uploadArea = document.getElementById('uploadArea');
const fileInput = document.getElementById('fileInput');
const fileList = document.getElementById('fileList');
const filesUl = document.getElementById('files');
const fileCount = document.getElementById('fileCount');
const actionButtons = document.getElementById('actionButtons');
const mergeBtn = document.getElementById('mergeBtn');
const clearBtn = document.getElementById('clearBtn');
const resultDiv = document.getElementById('result');
const loadingDiv = document.getElementById('loading');

// 이벤트 리스너 등록
uploadArea.addEventListener('click', () => fileInput.click());
uploadArea.addEventListener('dragover', handleDragOver);
uploadArea.addEventListener('dragleave', handleDragLeave);
uploadArea.addEventListener('drop', handleDrop);
fileInput.addEventListener('change', handleFileSelect);
mergeBtn.addEventListener('click', mergePDFs);
clearBtn.addEventListener('click', clearFiles);

/**
 * 드래그 오버 핸들러
 */
function handleDragOver(e) {
    e.preventDefault();
    uploadArea.classList.add('drag-over');
}

/**
 * 드래그 리브 핸들러
 */
function handleDragLeave(e) {
    e.preventDefault();
    uploadArea.classList.remove('drag-over');
}

/**
 * 드롭 핸들러
 */
function handleDrop(e) {
    e.preventDefault();
    uploadArea.classList.remove('drag-over');
    
    const files = Array.from(e.dataTransfer.files);
    const pdfFiles = files.filter(file => file.type === 'application/pdf');
    
    if (pdfFiles.length !== files.length) {
        alert('PDF 파일만 업로드 가능합니다.');
    }
    
    if (pdfFiles.length > 0) {
        addFiles(pdfFiles);
    }
}

/**
 * 파일 선택 핸들러
 */
function handleFileSelect(e) {
    const files = Array.from(e.target.files);
    addFiles(files);
    fileInput.value = ''; // 입력 초기화
}

/**
 * 파일 추가
 */
function addFiles(files) {
    selectedFiles = [...selectedFiles, ...files];
    updateFileList();
}

/**
 * 파일 목록 업데이트
 */
function updateFileList() {
    if (selectedFiles.length === 0) {
        fileList.style.display = 'none';
        actionButtons.style.display = 'none';
        return;
    }
    
    fileList.style.display = 'block';
    actionButtons.style.display = 'flex';
    fileCount.textContent = selectedFiles.length;
    
    filesUl.innerHTML = '';
    
    selectedFiles.forEach((file, index) => {
        const li = document.createElement('li');
        li.className = 'file-item';
        li.draggable = true;
        li.dataset.index = index;
        
        const fileInfo = document.createElement('div');
        fileInfo.className = 'file-info';
        
        const fileIcon = document.createElement('span');
        fileIcon.className = 'file-icon';
        fileIcon.textContent = '📄';
        
        const fileName = document.createElement('span');
        fileName.className = 'file-name';
        fileName.textContent = file.name;
        
        const fileSize = document.createElement('span');
        fileSize.className = 'file-size';
        fileSize.textContent = formatFileSize(file.size);
        
        fileInfo.appendChild(fileIcon);
        fileInfo.appendChild(fileName);
        fileInfo.appendChild(fileSize);
        
        const fileActions = document.createElement('div');
        fileActions.className = 'file-actions';
        
        const removeBtn = document.createElement('button');
        removeBtn.textContent = '삭제';
        removeBtn.onclick = () => removeFile(index);
        
        fileActions.appendChild(removeBtn);
        
        li.appendChild(fileInfo);
        li.appendChild(fileActions);
        
        // 드래그 앤 드롭 이벤트
        li.addEventListener('dragstart', handleFileDragStart);
        li.addEventListener('dragover', handleFileDragOver);
        li.addEventListener('drop', handleFileDrop);
        li.addEventListener('dragend', handleFileDragEnd);
        
        filesUl.appendChild(li);
    });
}

/**
 * 파일 크기 포맷팅
 */
function formatFileSize(bytes) {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i];
}

/**
 * 파일 제거
 */
function removeFile(index) {
    selectedFiles.splice(index, 1);
    updateFileList();
    hideResult();
}

/**
 * 모든 파일 초기화
 */
function clearFiles() {
    selectedFiles = [];
    updateFileList();
    hideResult();
}

/**
 * 결과 숨기기
 */
function hideResult() {
    resultDiv.style.display = 'none';
}

// 드래그 앤 드롭으로 순서 변경
let draggedItem = null;

function handleFileDragStart(e) {
    draggedItem = this;
    this.classList.add('dragging');
}

function handleFileDragOver(e) {
    e.preventDefault();
    const afterElement = getDragAfterElement(filesUl, e.clientY);
    if (afterElement == null) {
        filesUl.appendChild(draggedItem);
    } else {
        filesUl.insertBefore(draggedItem, afterElement);
    }
}

function handleFileDrop(e) {
    e.preventDefault();
}

function handleFileDragEnd(e) {
    this.classList.remove('dragging');
    
    // 새로운 순서로 파일 배열 재정렬
    const newOrder = Array.from(filesUl.children).map(li => 
        parseInt(li.dataset.index)
    );
    
    const newFiles = newOrder.map(index => selectedFiles[index]);
    selectedFiles = newFiles;
    updateFileList();
}

function getDragAfterElement(container, y) {
    const draggableElements = [...container.querySelectorAll('.file-item:not(.dragging)')];
    
    return draggableElements.reduce((closest, child) => {
        const box = child.getBoundingClientRect();
        const offset = y - box.top - box.height / 2;
        
        if (offset < 0 && offset > closest.offset) {
            return { offset: offset, element: child };
        } else {
            return closest;
        }
    }, { offset: Number.NEGATIVE_INFINITY }).element;
}

/**
 * PDF 병합 실행
 */
async function mergePDFs() {
    if (selectedFiles.length === 0) {
        alert('병합할 PDF 파일을 선택해주세요.');
        return;
    }
    
    const formData = new FormData();
    selectedFiles.forEach(file => {
        formData.append('files', file);
    });
    
    // 현재 순서 전달
    const order = selectedFiles.map((_, index) => index);
    order.forEach(index => {
        formData.append('order', index);
    });
    
    // 로딩 표시
    loadingDiv.style.display = 'block';
    resultDiv.style.display = 'none';
    actionButtons.style.display = 'none';
    
    try {
        const response = await fetch('/pdf-merge/merge', {
            method: 'POST',
            body: formData
        });
        
        const result = await response.json();
        
        loadingDiv.style.display = 'none';
        actionButtons.style.display = 'flex';
        
        if (result.success) {
            showResult(true, result.message, result.downloadUrl, result.fileName);
        } else {
            showResult(false, result.message);
        }
    } catch (error) {
        loadingDiv.style.display = 'none';
        actionButtons.style.display = 'flex';
        showResult(false, '서버 오류가 발생했습니다: ' + error.message);
    }
}

/**
 * 결과 표시
 */
function showResult(success, message, downloadUrl = null, fileName = null) {
    resultDiv.style.display = 'block';
    resultDiv.className = 'result ' + (success ? 'success' : 'error');
    
    let html = `<h3>${success ? '✅ 성공' : '❌ 실패'}</h3>`;
    html += `<p>${message}</p>`;
    
    if (success && downloadUrl) {
        html += `<a href="${downloadUrl}" class="btn btn-primary" download="${fileName}">다운로드</a>`;
    }
    
    resultDiv.innerHTML = html;
    
    // 성공 시 스크롤
    if (success) {
        resultDiv.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }
}