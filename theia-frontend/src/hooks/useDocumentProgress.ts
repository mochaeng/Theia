import { useEffect, useState, useCallback, useRef } from 'react'
import { Client, type StompSubscription } from '@stomp/stompjs'
import { WEBSOCKET_ENDPOINT } from '@/lib/constants'

type DocumentProgressStatus =
  | 'FAILED'
  | 'DOWNLOADING'
  | 'EXTRACTING'
  | 'EMBEDDING'
  | 'SAVING'
  | 'COMPLETED'

type ConnectionStatus = 'DISCONNECTED' | 'CONNECTING' | 'CONNECTED' | 'ERROR'

export type DocumentProgress = {
  documentID: string
  status: DocumentProgressStatus
  message: string
  percentage: number
  occurredAt: String
}

export const useDocumentProgress = (
  documentId: string,
  onComplete: (document: DocumentProgress) => void,
) => {
  const [event, setEvent] = useState<DocumentProgress | null>(null)
  const [connectionStatus, setConnectionStatus] =
    useState<ConnectionStatus>('DISCONNECTED')

  const clientRef = useRef<Client | null>(null)
  const subscriptionRef = useRef<StompSubscription | null>(null)
  const onCompleteRef = useRef(onComplete)

  useEffect(() => {
    onCompleteRef.current = onComplete
  }, [onComplete])

  const cleanup = useCallback(() => {
    if (subscriptionRef.current) {
      subscriptionRef.current.unsubscribe()
      subscriptionRef.current = null
    }
    if (clientRef.current && clientRef.current.connected) {
      clientRef.current.deactivate()
    }
    clientRef.current = null
    setConnectionStatus('DISCONNECTED')
  }, [])

  const handleTerminalStatus = useCallback(
    (data: DocumentProgress) => {
      console.log('Document processing reached terminal status:', data.status)

      onCompleteRef.current?.(data)

      setTimeout(() => {
        cleanup()
      }, 1000)
    },
    [cleanup],
  )

  useEffect(() => {
    setEvent(null)

    if (!documentId) {
      cleanup()
      return
    }

    cleanup()

    const client = new Client({
      brokerURL: WEBSOCKET_ENDPOINT,
      reconnectDelay: 3000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      debug: (str) => {
        console.log('STOMP Debug:', str)
      },
    })

    clientRef.current = client

    client.onConnect = () => {
      console.log('WebSocket connected for document:', documentId)
      setConnectionStatus('CONNECTED')

      const subscription = client.subscribe(
        `/topic/documents/${documentId}/progress`,
        (message) => {
          try {
            const data: DocumentProgress = JSON.parse(message.body)
            console.log('Received progress:', data)
            setEvent(data)

            if (data.status === 'COMPLETED' || data.status === 'FAILED') {
              handleTerminalStatus(data)
            }
          } catch (error) {
            console.error('Failed to parse progress message:', error)
          }
        },
      )

      subscriptionRef.current = subscription
    }

    client.onDisconnect = () => {
      console.log('WebSocket disconnected for document:', documentId)
      setConnectionStatus('DISCONNECTED')
    }

    client.onStompError = (frame) => {
      console.error('STOMP error:', frame)
      setConnectionStatus('ERROR')
    }

    client.onWebSocketError = (event) => {
      console.error('WebSocket error:', event)
      setConnectionStatus('ERROR')
    }

    console.log('Connecting to WebSocket for document:', documentId)
    setConnectionStatus('CONNECTING')
    client.activate()

    return cleanup
  }, [documentId, handleTerminalStatus, cleanup])

  return {
    event,
    connectionStatus,
    disconnect: cleanup,
  }
}
