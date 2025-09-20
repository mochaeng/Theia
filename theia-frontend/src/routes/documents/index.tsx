import { Alert, AlertDescription } from '@/components/ui/alert'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Progress } from '@/components/ui/progress'
import {
  useDocumentProgress,
  type DocumentProgress,
} from '@/hooks/useDocumentProgress'
import { UPLOAD_ENDPOINT } from '@/lib/constants'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { createFileRoute } from '@tanstack/react-router'
import {
  CheckCircleIcon,
  ClockIcon,
  FileText,
  FileTextIcon,
  Loader2Icon,
  UploadIcon,
  XCircleIcon,
} from 'lucide-react'
import { useCallback, useState } from 'react'

export const Route = createFileRoute('/documents/')({
  component: DocumentUploadWithProgress,
})

function DocumentUploadWithProgress() {
  const [selectedFile, setSelectedFile] = useState<File | null>(null)
  const [documentId, setDocumentId] = useState('')
  const [isDragOver, setIsDragOver] = useState(false)
  const queryClient = useQueryClient()

  const handleProcessingComplete = useCallback(
    (data: DocumentProgress) => {
      queryClient.invalidateQueries({ queryKey: ['documents'] })
      console.log('Document processing completed:', data)
    },
    [queryClient],
  )

  const {
    event: progressEvent,
    connectionStatus,
    disconnect,
  } = useDocumentProgress(documentId, handleProcessingComplete)

  const uploadDocument = async (file: File) => {
    const formData = new FormData()
    formData.append('file', file)

    const response = await fetch(UPLOAD_ENDPOINT, {
      method: 'POST',
      body: formData,
    })

    if (!response.ok) {
      throw new Error(`Upload failed: ${response.statusText}`)
    }

    return response.json()
  }

  const uploadMutation = useMutation({
    mutationFn: uploadDocument,
    onSuccess: (data) => {
      setDocumentId(data.documentID)
      console.log('upload successful, document ID:', data.documentID)
    },
    onError: (error) => {
      console.error('upload failed:', error)
      setDocumentId('')
      setSelectedFile(null)
    },
  })

  const handleFileSelect = (file: File) => {
    if (file && file.type === 'application/pdf') {
      setSelectedFile(file)
    }
  }

  const handleFileChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0]
    if (file) {
      handleFileSelect(file)
    }
  }

  const handleDragOver = (event: React.DragEvent) => {
    event.preventDefault()
    setIsDragOver(true)
  }

  const handleDragLeave = (event: React.DragEvent) => {
    event.preventDefault()
    setIsDragOver(false)
  }

  const handleDrop = (event: React.DragEvent) => {
    event.preventDefault()
    setIsDragOver(false)
    const file = event.dataTransfer.files?.[0]
    if (file) handleFileSelect(file)
  }

  const handleUpload = () => {
    if (selectedFile && !uploadMutation.isPending) {
      uploadMutation.mutate(selectedFile)
    }
  }

  const handleReset = () => {
    disconnect()

    setSelectedFile(null)
    setDocumentId('')
    uploadMutation.reset()

    console.log('reset completedl, ready for new upload')
  }

  const getStatusIcon = () => {
    switch (progressEvent?.status) {
      case 'COMPLETED':
        return <CheckCircleIcon className="h-5 w-5 text-green-600" />
      case 'FAILED':
        return <XCircleIcon className="h-5 w-5 text-red-600" />
      case 'DOWNLOADING':
      case 'EMBEDDING':
      case 'EXTRACTING':
      case 'SAVING':
        return <Loader2Icon className="h-5 w-5 text-blue-600 animate-spin" />
      default:
        return <ClockIcon className="h-5 w-5 text-yellow-600" />
    }
  }

  const getStatusColor = () => {
    switch (progressEvent?.status) {
      case 'COMPLETED':
        return 'bg-green-100 text-green-800 border-green-200'
      case 'FAILED':
        return 'bg-red-100 text-red-800 border-red-200'
      case 'DOWNLOADING':
      case 'EMBEDDING':
      case 'EXTRACTING':
      case 'SAVING':
        return 'bg-blue-100 text-blue-800 border-blue-200'
      default:
        return 'bg-yellow-100 text-yellow-800 border-yellow-200'
    }
  }

  return (
    <div className="max-w-2xl mx-auto p-6 space-y-6">
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <FileTextIcon className="h-5 w-5" />
            Document Upload & Processing
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          {!selectedFile && (
            <div
              className={`border-2 border-dashed rounded-lg p-8 text-center transition-colors ${
                isDragOver
                  ? 'border-blue-400 bg-blue-50'
                  : 'border-gray-300 hover:border-gray-400'
              }`}
              onDragOver={handleDragOver}
              onDragLeave={handleDragLeave}
              onDrop={handleDrop}
            >
              <UploadIcon className="h-12 w-12 text-gray-400 mx-auto mb-4" />
              <p className="text-lg font-medium text-gray-700 mb-2">
                Drop your PDF here or click to browse
              </p>
              <p className="text-sm text-gray-500 mb-4">
                Only PDF files are supported
              </p>
              <input
                type="file"
                accept=".pdf"
                onChange={handleFileChange}
                className="hidden"
                id="file-upload"
              />
              <Button asChild variant="outline">
                <label htmlFor="file-upload" className="cursor-pointer">
                  Browse Files
                </label>
              </Button>
            </div>
          )}

          {selectedFile && !documentId && (
            <div className="space-y-4">
              <div className="flex items-center justify-between p-4 border rounded-lg bg-gray-50">
                <div className="flex items-center gap-3">
                  <FileText className="h-8 w-8 text-blue-600" />
                  <div>
                    <p className="font-medium">{selectedFile.name}</p>
                    <p className="text-sm text-gray-500">
                      {(selectedFile.size / 1024 / 1024).toFixed(2)} MB
                    </p>
                  </div>
                </div>
                <Button variant="ghost" size="sm" onClick={handleReset}>
                  Remove
                </Button>
              </div>

              <Button
                onClick={handleUpload}
                disabled={uploadMutation.isPending}
                className="w-full"
              >
                {uploadMutation.isPending ? (
                  <>
                    <Loader2Icon className="h-4 w-4 mr-2 animate-spin" />
                    Uploading...
                  </>
                ) : (
                  <>
                    <UploadIcon className="h-4 w-4 mr-2" />
                    Upload Document
                  </>
                )}
              </Button>
            </div>
          )}

          {uploadMutation.isError && (
            <Alert variant="destructive">
              <XCircleIcon className="h-4 w-4" />
              <AlertDescription>
                Upload failed:
                {uploadMutation.error?.message || 'Unknown error'}
              </AlertDescription>
            </Alert>
          )}
        </CardContent>
      </Card>

      {documentId && (
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center justify-between">
              <span>Processing Progress</span>
              <div className="flex items-center gap-2">
                {getStatusIcon()}
                <Badge className={getStatusColor()}>
                  {progressEvent?.status}
                </Badge>
              </div>
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <div className="flex justify-between text-sm">
                <span>Document ID:</span>
                <code className="font-mono text-xs bg-gray-100 px-2 py-1 rounded">
                  {documentId}
                </code>
              </div>
              <div className="flex justify-between text-sm">
                <span>Connection:</span>
                <Badge
                  variant={
                    connectionStatus === 'CONNECTED' ? 'default' : 'secondary'
                  }
                >
                  {connectionStatus}
                </Badge>
              </div>
            </div>

            {progressEvent && (
              <div className="space-y-3">
                <div className="flex justify-between items-center">
                  <span className="text-sm font-medium">Progress</span>
                  <span className="text-sm font-bold">
                    {progressEvent.percentage || 0}%
                  </span>
                </div>

                <Progress
                  value={progressEvent.percentage || 0}
                  className="h-2"
                />

                {progressEvent.message && (
                  <p className="text-sm text-gray-600 italic">
                    {progressEvent.message}
                  </p>
                )}
              </div>
            )}

            {connectionStatus === 'CONNECTED' && !progressEvent && (
              <div className="text-center py-4">
                <Loader2Icon className="h-6 w-6 animate-spin mx-auto mb-2 text-blue-600" />
                <p className="text-sm text-gray-500">
                  Waiting for processing to begin...
                </p>
              </div>
            )}

            {progressEvent?.status === 'COMPLETED' && (
              <div className="pt-4 border-t">
                <Button
                  onClick={handleReset}
                  variant="outline"
                  className="w-full"
                >
                  Process Another Document
                </Button>
              </div>
            )}
          </CardContent>
        </Card>
      )}
    </div>
  )
}
