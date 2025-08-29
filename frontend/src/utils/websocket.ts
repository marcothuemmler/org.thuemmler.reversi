export function getWebSocketUrl(path: string) {
  const normalizedPath = `/ws/${path}`.replace(/\/+/g, '/')
  const protocol = window.location.protocol === 'https:' ? 'wss' : 'ws';
  return `${protocol}://${window.location.host}${normalizedPath}`;
}
