function getCsrfToken() {
    const csrfToken = document.cookie.split('; ').find(row => row.startsWith('XSRF-TOKEN')).split('=')[1];
    return csrfToken;
}

self.addEventListener('fetch', event => {
    const csrfToken = getCsrfToken(); // Thay thế bằng token thực tế
    console.log('Fetching:', event.request.url, 'CSRF Token:', csrfToken);
    if (event.request.method === 'POST') {
      event.respondWith(
        (async () => {
          // Sao chép request gốc
          const modifiedHeaders = new Headers(event.request.headers);
          modifiedHeaders.append('X-CSRF-Token', csrfToken);
  
          const modifiedRequest = new Request(event.request, {
            headers: modifiedHeaders,
            mode: event.request.mode,
            credentials: event.request.credentials,
            redirect: event.request.redirect,
            referrer: event.request.referrer,
            body: event.request.body,
            method: event.request.method,
            cache: event.request.cache,
            integrity: event.request.integrity,
            keepalive: event.request.keepalive,
            signal: event.request.signal
          });
  
          return fetch(modifiedRequest);
        })()
      );
    } else {
      event.respondWith(fetch(event.request));
    }
});

