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
