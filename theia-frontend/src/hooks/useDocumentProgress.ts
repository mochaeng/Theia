import { useState, useCallback, useRef } from 'react'
import { Client, type StompSubscription } from '@stomp/stompjs'
import { WEBSOCKET_ENDPOINT } from '@/lib/constants'
import { getOidc } from '@/oidc'

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
  onComplete: (document: DocumentProgress) => void,
) => {
  const [event, setEvent] = useState<DocumentProgress | null>(null)
  const [connectionStatus, setConnectionStatus] =
    useState<ConnectionStatus>('DISCONNECTED')

  const clientRef = useRef<Client | null>(null)
  const subscriptionRef = useRef<StompSubscription | null>(null)

  const disconnect = useCallback(() => {
    console.log('Disconnecting WebSocket')

    if (subscriptionRef.current) {
      subscriptionRef.current.unsubscribe()
      subscriptionRef.current = null
    }

    if (clientRef.current) {
      clientRef.current.deactivate()
      clientRef.current = null
    }

    setConnectionStatus('DISCONNECTED')
    setEvent(null)
  }, [])

  const connect = useCallback(
    async (documentId: string) => {
      console.log(`Connecting to WebSocket for document ${documentId}`)

      disconnect()

      try {
        setConnectionStatus('CONNECTING')

        const oidc = await getOidc()
        if (!oidc.isUserLoggedIn) {
          throw new Error('User is not authenticated')
        }
        const { accessToken } = await oidc.getTokens()
        console.log('Access token retrieved')

        const client = new Client({
          brokerURL: WEBSOCKET_ENDPOINT,
          reconnectDelay: 3000,
          heartbeatIncoming: 10000,
          heartbeatOutgoing: 10000,
          connectHeaders: {
            Authorization: `Bearer ${accessToken}`,
          },
          beforeConnect: () => {
            return Promise.resolve()
          },
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
                  console.log(
                    'Document processing reached terminal status:',
                    data.status,
                  )
                  onComplete?.(data)

                  setTimeout(() => {
                    disconnect()
                  }, 1000)
                }
              } catch (error) {
                console.error('Failed to parse progress message:', error)
              }
            },
          )

          subscriptionRef.current = subscription
        }

        client.onDisconnect = () => {
          console.log('WebSocket disconnected')
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

        client.activate()
      } catch (error) {
        console.error('Failed to connect:', error)
        setConnectionStatus('ERROR')
        disconnect()
      }
    },
    [disconnect, onComplete],
  )
  return {
    event,
    connectionStatus,
    connect,
    disconnect,
  }
}
