/**
 * 공통 JavaScript 유틸리티
 * 모든 페이지에서 재사용 가능한 함수 모음
 */

// API 호출 유틸리티
const ApiClient = {
    /**
     * POST 요청 (JSON)
     */
    async postJson(url, data) {
        try {
            const response = await fetch(url, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(data)
            });
            return await response.json();
        } catch (error) {
            console.error('API 호출 오류:', error);
            throw error;
        }
    },

    /**
     * POST 요청 (Form Data)
     */
    async postForm(url, formData) {
        try {
            const response = await fetch(url, {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: formData
            });
            return await response.json();
        } catch (error) {
            console.error('API 호출 오류:', error);
            throw error;
        }
    },

    /**
     * GET 요청
     */
    async get(url) {
        try {
            const response = await fetch(url);
            return await response.json();
        } catch (error) {
            console.error('API 호출 오류:', error);
            throw error;
        }
    }
};

// UI 유틸리티
const UiUtils = {
    /**
     * 요소 표시/숨김
     */
    show(elementId) {
        const element = document.getElementById(elementId);
        if (element) {
            element.style.display = 'block';
            element.classList.add('show');
        }
    },

    hide(elementId) {
        const element = document.getElementById(elementId);
        if (element) {
            element.style.display = 'none';
            element.classList.remove('show');
        }
    },

    /**
     * 로딩 표시
     */
    showLoading(elementId) {
        this.show(elementId);
    },

    hideLoading(elementId) {
        this.hide(elementId);
    },

    /**
     * 에러 메시지 표시
     */
    showError(message, elementId = 'errorMessage') {
        const errorDiv = document.getElementById(elementId);
        if (errorDiv) {
            errorDiv.textContent = message;
            errorDiv.classList.add('show');
        } else {
            alert(message);
        }
    },

    hideError(elementId = 'errorMessage') {
        const errorDiv = document.getElementById(elementId);
        if (errorDiv) {
            errorDiv.classList.remove('show');
        }
    },

    /**
     * 성공 메시지 표시 (Toast)
     */
    showSuccess(message, duration = 3000) {
        const toast = document.createElement('div');
        toast.className = 'toast success';
        toast.textContent = message;
        toast.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            background: #28a745;
            color: white;
            padding: 1rem 1.5rem;
            border-radius: 6px;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
            z-index: 2000;
            animation: slideIn 0.3s ease;
        `;
        document.body.appendChild(toast);

        setTimeout(() => {
            toast.style.animation = 'slideOut 0.3s ease';
            setTimeout(() => toast.remove(), 300);
        }, duration);
    }
};

// 클립보드 유틸리티
const ClipboardUtils = {
    /**
     * 클립보드에 복사
     */
    async copy(text) {
        try {
            if (navigator.clipboard && navigator.clipboard.writeText) {
                await navigator.clipboard.writeText(text);
                UiUtils.showSuccess('복사했습니다!');
                return true;
            } else {
                // Fallback for older browsers
                const textarea = document.createElement('textarea');
                textarea.value = text;
                textarea.style.position = 'fixed';
                textarea.style.opacity = '0';
                document.body.appendChild(textarea);
                textarea.select();
                const success = document.execCommand('copy');
                document.body.removeChild(textarea);
                
                if (success) {
                    UiUtils.showSuccess('복사했습니다!');
                }
                return success;
            }
        } catch (error) {
            console.error('복사 실패:', error);
            alert('복사에 실패했습니다.');
            return false;
        }
    },

    /**
     * 요소의 텍스트를 복사
     */
    async copyFromElement(elementId) {
        const element = document.getElementById(elementId);
        if (element) {
            const text = element.value || element.textContent;
            return await this.copy(text);
        }
        return false;
    }
};

// 입력 검증 유틸리티
const ValidationUtils = {
    /**
     * 빈 값 체크
     */
    isEmpty(value) {
        return value === null || value === undefined || value.trim() === '';
    },

    /**
     * 필수 입력 검증
     */
    validateRequired(elementId, errorMessage = '필수 입력 항목입니다.') {
        const element = document.getElementById(elementId);
        if (!element) return false;
        
        const value = element.value || '';
        if (this.isEmpty(value)) {
            UiUtils.showError(errorMessage);
            element.focus();
            return false;
        }
        return true;
    },

    /**
     * 숫자 범위 검증
     */
    validateRange(value, min, max) {
        const num = Number(value);
        return !isNaN(num) && num >= min && num <= max;
    }
};

// HTML 유틸리티
const HtmlUtils = {
    /**
     * HTML 이스케이프
     */
    escape(text) {
        if (!text) return '';
        const map = {
            '&': '&amp;',
            '<': '&lt;',
            '>': '&gt;',
            '"': '&quot;',
            "'": '&#039;'
        };
        return text.replace(/[&<>"']/g, m => map[m]);
    }
};

// 포맷팅 유틸리티
const FormatUtils = {
    /**
     * 파일 크기 포맷
     */
    formatFileSize(bytes) {
        if (bytes === 0) return '0 Bytes';
        const k = 1024;
        const sizes = ['Bytes', 'KB', 'MB', 'GB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i];
    },

    /**
     * 숫자 포맷 (천단위 콤마)
     */
    formatNumber(num) {
        return num.toLocaleString();
    }
};

// 전역으로 export
if (typeof window !== 'undefined') {
    window.ApiClient = ApiClient;
    window.UiUtils = UiUtils;
    window.ClipboardUtils = ClipboardUtils;
    window.ValidationUtils = ValidationUtils;
    window.HtmlUtils = HtmlUtils;
    window.FormatUtils = FormatUtils;
}