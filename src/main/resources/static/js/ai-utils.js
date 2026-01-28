/**
 * Transformers.js 기반 무료 AI 유틸리티
 * - 브라우저에서 직접 실행되는 AI 모델
 * - 완전 무료, API 키 불필요
 */

class AIUtils {
    constructor() {
        this.models = {
            summarizer: null,
            sentiment: null,
            translator: null,
            zeroShot: null
        };
        this.isLoading = false;
        this.loadedModels = new Set();
    }

    /**
     * 텍스트 요약
     */
    async summarize(text, maxLength = 200) {
        try {
            if (!this.models.summarizer) {
                this.showLoadingMessage('요약 모델 로딩 중... (최초 1회, 약 10초 소요)');
                
                // 동적 import (CDN에서 로드)
                const { pipeline } = await import('https://cdn.jsdelivr.net/npm/@xenova/transformers@2.17.2');
                
                this.models.summarizer = await pipeline(
                    'summarization',
                    'Xenova/distilbart-cnn-6-6'
                );
                
                this.loadedModels.add('summarizer');
                this.hideLoadingMessage();
            }

            const result = await this.models.summarizer(text, {
                max_length: maxLength,
                min_length: 30
            });

            return {
                success: true,
                result: result[0].summary_text
            };

        } catch (error) {
            console.error('요약 오류:', error);
            return {
                success: false,
                message: '요약 중 오류가 발생했습니다: ' + error.message
            };
        }
    }

    /**
     * 감정 분석
     */
    async analyzeSentiment(text) {
        try {
            if (!this.models.sentiment) {
                this.showLoadingMessage('감정 분석 모델 로딩 중... (최초 1회, 약 5초 소요)');
                
                const { pipeline } = await import('https://cdn.jsdelivr.net/npm/@xenova/transformers@2.17.2');
                
                this.models.sentiment = await pipeline(
                    'sentiment-analysis',
                    'Xenova/distilbert-base-uncased-finetuned-sst-2-english'
                );
                
                this.loadedModels.add('sentiment');
                this.hideLoadingMessage();
            }

            const result = await this.models.sentiment(text);
            
            // 한글로 변환
            const labelMap = {
                'POSITIVE': '긍정',
                'NEGATIVE': '부정',
                'NEUTRAL': '중립'
            };
            
            const sentiment = result[0];
            const label = labelMap[sentiment.label] || sentiment.label;
            const confidence = (sentiment.score * 100).toFixed(1);
            
            let analysis = `**감정**: ${label} (확신도: ${confidence}%)\n\n`;
            
            if (sentiment.label === 'POSITIVE') {
                analysis += '이 텍스트는 긍정적인 감정을 담고 있습니다. ';
                analysis += '만족, 기쁨, 희망 등의 긍정적 정서가 느껴집니다.';
            } else if (sentiment.label === 'NEGATIVE') {
                analysis += '이 텍스트는 부정적인 감정을 담고 있습니다. ';
                analysis += '불만, 실망, 우려 등의 부정적 정서가 느껴집니다.';
            } else {
                analysis += '이 텍스트는 중립적인 감정을 담고 있습니다. ';
                analysis += '객관적이거나 사실 전달 위주의 내용입니다.';
            }

            return {
                success: true,
                result: analysis
            };

        } catch (error) {
            console.error('감정 분석 오류:', error);
            return {
                success: false,
                message: '감정 분석 중 오류가 발생했습니다: ' + error.message
            };
        }
    }

    /**
     * 키워드 추출 (간단 버전 - 통계 기반)
     */
    extractKeywords(text, count = 10) {
        try {
            // 불용어 제거
            const stopwords = new Set([
                '이', '그', '저', '것', '수', '등', '들', '및', '때', '등등',
                '하다', '있다', '되다', '않다', '없다', '아니다',
                'the', 'a', 'an', 'and', 'or', 'but', 'in', 'on', 'at', 'to', 'for',
                'of', 'is', 'are', 'was', 'were', 'be', 'been', 'being',
                'have', 'has', 'had', 'do', 'does', 'did', 'will', 'would',
                'can', 'could', 'should', 'may', 'might', 'must'
            ]);

            // 텍스트 정규화
            const words = text.toLowerCase()
                .replace(/[^\w\s가-힣]/g, ' ')
                .split(/\s+/)
                .filter(word => word.length > 2 && !stopwords.has(word));

            // 빈도 계산
            const frequency = {};
            words.forEach(word => {
                frequency[word] = (frequency[word] || 0) + 1;
            });

            // 상위 키워드 추출
            const keywords = Object.entries(frequency)
                .sort((a, b) => b[1] - a[1])
                .slice(0, count)
                .map(([word, freq]) => ({ word, frequency: freq }));

            let result = '**추출된 주요 키워드:**\n\n';
            keywords.forEach((kw, index) => {
                result += `${index + 1}. **${kw.word}** (${kw.frequency}회 등장)\n`;
            });

            return {
                success: true,
                result: result
            };

        } catch (error) {
            console.error('키워드 추출 오류:', error);
            return {
                success: false,
                message: '키워드 추출 중 오류가 발생했습니다: ' + error.message
            };
        }
    }

    /**
     * 번역 (간단 버전 - 영→한만 지원)
     */
    async translate(text) {
        try {
            if (!this.models.translator) {
                this.showLoadingMessage('번역 모델 로딩 중... (최초 1회, 약 15초 소요)');
                
                const { pipeline } = await import('https://cdn.jsdelivr.net/npm/@xenova/transformers@2.17.2');
                
                this.models.translator = await pipeline(
                    'translation',
                    'Xenova/opus-mt-en-ko'
                );
                
                this.loadedModels.add('translator');
                this.hideLoadingMessage();
            }

            const result = await this.models.translator(text);

            return {
                success: true,
                result: result[0].translation_text
            };

        } catch (error) {
            console.error('번역 오류:', error);
            return {
                success: false,
                message: '번역 중 오류가 발생했습니다. 영어→한국어만 지원됩니다.'
            };
        }
    }

    /**
     * 문장 개선 (통계 기반 제안)
     */
    improveSentence(text) {
        try {
            let improved = text;
            let suggestions = [];

            // 1. 반복 단어 제거
            const repeated = improved.match(/(\b\w+\b)(\s+\1)+/gi);
            if (repeated) {
                suggestions.push('- 반복된 단어 발견: ' + repeated.join(', '));
                improved = improved.replace(/(\b\w+\b)(\s+\1)+/gi, '$1');
            }

            // 2. 과도한 공백 정리
            improved = improved.replace(/\s+/g, ' ').trim();

            // 3. 문장 부호 정리
            improved = improved.replace(/\s+([.,!?])/g, '$1');
            improved = improved.replace(/([.,!?])([^\s])/g, '$1 $2');

            // 4. 연속된 특수문자 제거
            const specialChars = improved.match(/[!?.]{3,}/g);
            if (specialChars) {
                suggestions.push('- 과도한 특수문자 사용: ' + specialChars.join(', '));
                improved = improved.replace(/[!?.]{3,}/g, '!');
            }

            let result = '**개선된 텍스트:**\n\n' + improved + '\n\n';
            
            if (suggestions.length > 0) {
                result += '**개선 사항:**\n' + suggestions.join('\n') + '\n\n';
            } else {
                result += '**개선 사항:**\n문법적으로 양호합니다.\n\n';
            }

            // 추가 제안
            result += '**추가 제안:**\n';
            result += '- 문장 길이가 적절한지 확인하세요 (한 문장 20-30단어 권장)\n';
            result += '- 전문 용어는 일반인도 이해할 수 있게 설명을 추가하세요\n';
            result += '- 능동태 사용을 권장합니다';

            return {
                success: true,
                result: result
            };

        } catch (error) {
            console.error('문장 개선 오류:', error);
            return {
                success: false,
                message: '문장 개선 중 오류가 발생했습니다: ' + error.message
            };
        }
    }

    /**
     * 쉬운 설명 (통계 기반)
     */
    explainSimply(text) {
        try {
            // 어려운 단어 감지 (휴리스틱)
            const difficultWords = [];
            const words = text.split(/\s+/);
            
            words.forEach(word => {
                // 긴 단어 (10자 이상)
                if (word.length > 10) {
                    difficultWords.push(word);
                }
            });

            let result = '**쉬운 설명 제안:**\n\n';
            
            if (difficultWords.length > 0) {
                result += '다음 단어들을 더 쉬운 말로 바꾸면 좋습니다:\n\n';
                difficultWords.forEach(word => {
                    result += `- "${word}" → 쉬운 표현으로 바꾸기\n`;
                });
                result += '\n';
            }

            result += '**설명 가이드:**\n';
            result += '1. 문장을 짧게 나누세요 (한 문장 = 하나의 생각)\n';
            result += '2. 전문 용어 대신 일상 언어를 사용하세요\n';
            result += '3. 예시를 들어 설명하세요\n';
            result += '4. "무엇을", "왜", "어떻게"를 명확히 하세요\n\n';
            
            result += '**예시:**\n';
            result += '어렵게: "해당 시스템은 고효율 알고리즘을 활용하여 최적화를 수행합니다"\n';
            result += '쉽게: "이 프로그램은 빠르고 정확한 방법으로 작업을 개선합니다"';

            return {
                success: true,
                result: result
            };

        } catch (error) {
            console.error('쉬운 설명 오류:', error);
            return {
                success: false,
                message: '설명 생성 중 오류가 발생했습니다: ' + error.message
            };
        }
    }

    /**
     * 코드 분석 (정적 분석)
     */
    analyzeCode(code, language) {
        try {
            let issues = [];
            let score = 100;

            // 1. 코드 길이 체크
            const lines = code.split('\n').length;
            if (lines > 100) {
                issues.push({
                    severity: 'medium',
                    message: `코드가 ${lines}줄로 너무 깁니다. 함수를 분리하는 것을 권장합니다.`
                });
                score -= 10;
            }

            // 2. 들여쓰기 체크
            const indentIssues = code.split('\n').filter(line => {
                return line.match(/^\s{1,3}\S/) || line.match(/^\t\S/);
            });
            if (indentIssues.length > lines * 0.3) {
                issues.push({
                    severity: 'low',
                    message: '일관되지 않은 들여쓰기가 발견되었습니다.'
                });
                score -= 5;
            }

            // 3. 주석 체크
            const commentLines = code.split('\n').filter(line => 
                line.trim().startsWith('//') || 
                line.trim().startsWith('/*') ||
                line.trim().startsWith('#')
            ).length;
            
            if (commentLines / lines < 0.1) {
                issues.push({
                    severity: 'low',
                    message: '주석이 부족합니다. 복잡한 로직에는 설명을 추가하세요.'
                });
                score -= 5;
            }

            // 4. 긴 함수 체크 (Java/JavaScript 기준)
            const functionMatches = code.match(/function\s+\w+\s*\([^)]*\)\s*{/g) || 
                                   code.match(/\w+\s*\([^)]*\)\s*{/g) || [];
            
            // 5. 하드코딩 체크
            const hardcodedStrings = code.match(/"[^"]{20,}"/g) || [];
            if (hardcodedStrings.length > 3) {
                issues.push({
                    severity: 'medium',
                    message: `긴 문자열 ${hardcodedStrings.length}개가 하드코딩되어 있습니다. 상수로 분리하세요.`
                });
                score -= 10;
            }

            // 6. 에러 처리 체크
            const hasErrorHandling = code.includes('try') || 
                                    code.includes('catch') ||
                                    code.includes('except') ||
                                    code.includes('error');
            
            if (!hasErrorHandling && lines > 20) {
                issues.push({
                    severity: 'high',
                    message: '에러 처리가 없습니다. try-catch 또는 에러 검사를 추가하세요.'
                });
                score -= 15;
            }

            // 7. 변수명 체크 (너무 짧은 이름)
            const shortVarNames = code.match(/\b[a-z]\b/g) || [];
            if (shortVarNames.length > 5) {
                issues.push({
                    severity: 'low',
                    message: '한 글자 변수명이 많습니다. 의미있는 이름을 사용하세요.'
                });
                score -= 5;
            }

            // 8. 중첩 깊이 체크
            const maxNesting = this.calculateMaxNesting(code);
            if (maxNesting > 4) {
                issues.push({
                    severity: 'high',
                    message: `중첩 깊이가 ${maxNesting}단계로 너무 깊습니다. 코드를 리팩토링하세요.`
                });
                score -= 15;
            }

            // 결과 포맷팅
            let result = `**코드 품질 점수**: ${Math.max(0, score)}/100\n\n`;
            
            if (issues.length === 0) {
                result += '✅ 발견된 문제가 없습니다!\n\n';
                result += '**긍정적인 부분:**\n';
                result += '- 코드가 깔끔하게 작성되었습니다.\n';
                result += '- 가독성이 좋습니다.\n';
            } else {
                result += '**발견된 문제점:**\n\n';
                
                const highIssues = issues.filter(i => i.severity === 'high');
                const mediumIssues = issues.filter(i => i.severity === 'medium');
                const lowIssues = issues.filter(i => i.severity === 'low');
                
                if (highIssues.length > 0) {
                    result += '🔴 **심각:**\n';
                    highIssues.forEach(issue => {
                        result += `  - ${issue.message}\n`;
                    });
                    result += '\n';
                }
                
                if (mediumIssues.length > 0) {
                    result += '🟡 **보통:**\n';
                    mediumIssues.forEach(issue => {
                        result += `  - ${issue.message}\n`;
                    });
                    result += '\n';
                }
                
                if (lowIssues.length > 0) {
                    result += '🟢 **경미:**\n';
                    lowIssues.forEach(issue => {
                        result += `  - ${issue.message}\n`;
                    });
                    result += '\n';
                }
            }

            result += '**개선 제안:**\n';
            result += '- 함수는 한 가지 일만 하도록 작성하세요\n';
            result += '- 변수와 함수 이름은 명확하고 의미있게 지으세요\n';
            result += '- 복잡한 로직은 주석으로 설명하세요\n';
            result += '- 에러 처리를 빠짐없이 추가하세요\n\n';

            result += '**전체 평가:**\n';
            if (score >= 80) {
                result += '우수한 코드입니다. 계속 이런 스타일을 유지하세요!';
            } else if (score >= 60) {
                result += '양호한 코드입니다. 몇 가지 개선이 필요합니다.';
            } else {
                result += '개선이 필요한 코드입니다. 위의 제안사항을 참고하세요.';
            }

            return {
                success: true,
                result: result
            };

        } catch (error) {
            console.error('코드 분석 오류:', error);
            return {
                success: false,
                message: '코드 분석 중 오류가 발생했습니다: ' + error.message
            };
        }
    }

    /**
     * 중첩 깊이 계산
     */
    calculateMaxNesting(code) {
        let maxDepth = 0;
        let currentDepth = 0;
        
        for (let char of code) {
            if (char === '{' || char === '(') {
                currentDepth++;
                maxDepth = Math.max(maxDepth, currentDepth);
            } else if (char === '}' || char === ')') {
                currentDepth--;
            }
        }
        
        return maxDepth;
    }

    /**
     * 데이터 분석 (통계 기반)
     */
    analyzeData(data) {
        try {
            if (!Array.isArray(data) || data.length === 0) {
                return {
                    success: false,
                    message: '유효한 데이터 배열이 아닙니다.'
                };
            }

            const sample = data[0];
            const columns = Object.keys(sample);
            
            let result = `**기본 통계:**\n\n`;
            result += `- 데이터 개수: ${data.length}개\n`;
            result += `- 컬럼 수: ${columns.length}개\n`;
            result += `- 주요 필드: ${columns.join(', ')}\n\n`;

            result += `**주요 발견사항:**\n\n`;

            // 각 컬럼 분석
            columns.forEach((col, index) => {
                const values = data.map(row => row[col]).filter(v => v != null);
                const uniqueValues = new Set(values);
                
                result += `${index + 1}. **${col}**\n`;
                result += `   - 고유값: ${uniqueValues.size}개\n`;
                
                // 숫자형 데이터 분석
                const numericValues = values.filter(v => !isNaN(parseFloat(v))).map(v => parseFloat(v));
                if (numericValues.length > 0) {
                    const sum = numericValues.reduce((a, b) => a + b, 0);
                    const avg = sum / numericValues.length;
                    const min = Math.min(...numericValues);
                    const max = Math.max(...numericValues);
                    
                    result += `   - 평균: ${avg.toFixed(2)}\n`;
                    result += `   - 최소: ${min}, 최대: ${max}\n`;
                } else {
                    // 문자형 데이터 - 최빈값
                    const frequency = {};
                    values.forEach(v => {
                        frequency[v] = (frequency[v] || 0) + 1;
                    });
                    const mostCommon = Object.entries(frequency)
                        .sort((a, b) => b[1] - a[1])[0];
                    if (mostCommon) {
                        result += `   - 최빈값: "${mostCommon[0]}" (${mostCommon[1]}회)\n`;
                    }
                }
                result += '\n';
            });

            result += `**추세 및 상관관계:**\n`;
            result += `- 데이터 분포가 ${data.length > 100 ? '충분합니다' : '더 필요할 수 있습니다'}\n`;
            result += `- ${columns.length}개 변수 간의 관계를 시각화하면 더 많은 인사이트를 얻을 수 있습니다\n\n`;

            result += `**제안사항:**\n`;
            result += `- 그래프를 그려 시각적으로 확인하세요\n`;
            result += `- 이상치(outlier)가 있는지 확인하세요\n`;
            result += `- 결측값(null)이 있다면 처리 방법을 결정하세요\n`;
            result += `- 시계열 데이터라면 트렌드를 분석하세요\n`;

            return {
                success: true,
                result: result
            };

        } catch (error) {
            console.error('데이터 분석 오류:', error);
            return {
                success: false,
                message: '데이터 분석 중 오류가 발생했습니다: ' + error.message
            };
        }
    }

    /**
     * 로딩 메시지 표시
     */
    showLoadingMessage(message) {
        const existingMsg = document.getElementById('ai-loading-message');
        if (existingMsg) {
            existingMsg.textContent = message;
            return;
        }

        const loadingDiv = document.createElement('div');
        loadingDiv.id = 'ai-loading-message';
        loadingDiv.style.cssText = `
            position: fixed;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 2rem 3rem;
            border-radius: 12px;
            box-shadow: 0 10px 40px rgba(0,0,0,0.3);
            z-index: 10000;
            text-align: center;
            font-weight: 500;
        `;
        loadingDiv.innerHTML = `
            <div style="font-size: 2rem; margin-bottom: 1rem;">🤖</div>
            <div>${message}</div>
            <div style="margin-top: 1rem; font-size: 0.9rem; opacity: 0.9;">
                잠시만 기다려주세요...
            </div>
        `;
        document.body.appendChild(loadingDiv);
    }

    /**
     * 로딩 메시지 숨기기
     */
    hideLoadingMessage() {
        const loadingDiv = document.getElementById('ai-loading-message');
        if (loadingDiv) {
            loadingDiv.remove();
        }
    }

    /**
     * 모든 모델 언로드 (메모리 절약)
     */
    unloadAllModels() {
        this.models = {
            summarizer: null,
            sentiment: null,
            translator: null,
            zeroShot: null
        };
        this.loadedModels.clear();
        console.log('모든 AI 모델이 언로드되었습니다.');
    }
}

// 전역 인스턴스
if (typeof window !== 'undefined') {
    window.aiUtils = new AIUtils();
}