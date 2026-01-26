const CACHE_NAME = 'playground-v1';
const STATIC_CACHE = 'playground-static-v1';
const DYNAMIC_CACHE = 'playground-dynamic-v1';

// 캐시할 정적 리소스
const STATIC_ASSETS = [
  '/',
  '/css/common.css',
  '/css/home.css',
  '/css/tool-page.css',
  '/css/pdf-merge.css',
  '/js/pdf-merge.js',
  '/image/favicon.svg',
  '/manifest.json'
];

// 캐시 우선 전략을 사용할 리소스 패턴
const CACHE_FIRST_PATTERNS = [
  /\.css$/,
  /\.js$/,
  /\.png$/,
  /\.jpg$/,
  /\.jpeg$/,
  /\.svg$/,
  /\.woff2?$/
];

// 네트워크 우선 전략을 사용할 API 패턴
const NETWORK_FIRST_PATTERNS = [
  /\/api\//,
  /\/mngt\//
];

// Service Worker 설치
self.addEventListener('install', (event) => {
  console.log('[SW] Installing service worker...');
  
  event.waitUntil(
    caches.open(STATIC_CACHE)
      .then((cache) => {
        console.log('[SW] Caching static assets');
        return cache.addAll(STATIC_ASSETS);
      })
      .catch((error) => {
        console.error('[SW] Failed to cache static assets:', error);
      })
  );
  
  self.skipWaiting();
});

// Service Worker 활성화
self.addEventListener('activate', (event) => {
  console.log('[SW] Activating service worker...');
  
  event.waitUntil(
    caches.keys().then((cacheNames) => {
      return Promise.all(
        cacheNames.map((cacheName) => {
          if (cacheName !== STATIC_CACHE && cacheName !== DYNAMIC_CACHE) {
            console.log('[SW] Deleting old cache:', cacheName);
            return caches.delete(cacheName);
          }
        })
      );
    })
  );
  
  return self.clients.claim();
});

// Fetch 이벤트 처리
self.addEventListener('fetch', (event) => {
  const { request } = event;
  const url = new URL(request.url);
  
  // 외부 도메인은 무시
  if (url.origin !== location.origin) {
    return;
  }
  
  // POST, PUT, DELETE 등 수정 요청은 캐시하지 않음
  if (request.method !== 'GET') {
    return;
  }
  
  // 네트워크 우선 전략 (API 호출)
  if (NETWORK_FIRST_PATTERNS.some(pattern => pattern.test(url.pathname))) {
    event.respondWith(networkFirst(request));
    return;
  }
  
  // 캐시 우선 전략 (정적 리소스)
  if (CACHE_FIRST_PATTERNS.some(pattern => pattern.test(url.pathname))) {
    event.respondWith(cacheFirst(request));
    return;
  }
  
  // 기본: 네트워크 우선, 캐시 폴백
  event.respondWith(networkFirst(request));
});

// 캐시 우선 전략
async function cacheFirst(request) {
  const cache = await caches.open(STATIC_CACHE);
  const cached = await cache.match(request);
  
  if (cached) {
    console.log('[SW] Cache hit:', request.url);
    return cached;
  }
  
  try {
    const response = await fetch(request);
    
    if (response.ok) {
      const responseClone = response.clone();
      cache.put(request, responseClone);
    }
    
    return response;
  } catch (error) {
    console.error('[SW] Fetch failed:', error);
    return new Response('Offline', { status: 503, statusText: 'Service Unavailable' });
  }
}

// 네트워크 우선 전략
async function networkFirst(request) {
  const cache = await caches.open(DYNAMIC_CACHE);
  
  try {
    const response = await fetch(request);
    
    if (response.ok) {
      const responseClone = response.clone();
      cache.put(request, responseClone);
    }
    
    return response;
  } catch (error) {
    console.log('[SW] Network failed, trying cache:', request.url);
    const cached = await cache.match(request);
    
    if (cached) {
      return cached;
    }
    
    // HTML 페이지 요청이면 오프라인 페이지 반환
    if (request.headers.get('accept').includes('text/html')) {
      return caches.match('/');
    }
    
    return new Response('Offline', { status: 503, statusText: 'Service Unavailable' });
  }
}

// 백그라운드 동기화
self.addEventListener('sync', (event) => {
  console.log('[SW] Background sync:', event.tag);
  
  if (event.tag === 'sync-data') {
    event.waitUntil(syncData());
  }
});

async function syncData() {
  console.log('[SW] Syncing data...');
  // 필요한 동기화 로직 구현
}

// 푸시 알림
self.addEventListener('push', (event) => {
  console.log('[SW] Push notification received');
  
  const options = {
    body: event.data ? event.data.text() : 'New notification',
    icon: '/image/icon-192x192.png',
    badge: '/image/icon-72x72.png',
    vibrate: [200, 100, 200]
  };
  
  event.waitUntil(
    self.registration.showNotification('Playground', options)
  );
});

// 알림 클릭 처리
self.addEventListener('notificationclick', (event) => {
  console.log('[SW] Notification clicked');
  event.notification.close();
  
  event.waitUntil(
    clients.openWindow('/')
  );
});