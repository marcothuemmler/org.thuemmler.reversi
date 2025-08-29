import { ref, onUnmounted } from 'vue'

export function useWebSocket<T = unknown>(
  url: string,
  onMessage: (event: MessageEvent<string>) => void,
  onOpen?: (context?: T) => void,
) {
  const socket = ref<WebSocket | null>(null)

  function connect(context?: T) {
    if (socket.value && socket.value.readyState !== WebSocket.CLOSED) return

    socket.value = new WebSocket(url)

    socket.value.onopen = () => {
      console.log('WebSocket connected')
      onOpen?.(context)
    }

    socket.value.onmessage = onMessage
    socket.value.onerror = (err) => console.error('WebSocket error', err)
    socket.value.onclose = (event) => {
      console.log('WebSocket closed', event.code, event.reason)
      socket.value = null
    }
  }

  function send(message: string) {
    if (socket.value?.readyState === WebSocket.OPEN) {
      socket.value.send(message)
    } else {
      console.warn('WebSocket not open, cannot send message')
    }
  }

  function close() {
    if (socket.value) {
      socket.value.close()
      socket.value = null
    }
  }

  onUnmounted(close)

  return { connect, send, close }
}
