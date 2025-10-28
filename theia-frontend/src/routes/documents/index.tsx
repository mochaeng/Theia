import { Alert, AlertDescription } from '@/components/ui/alert'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Progress } from '@/components/ui/progress'
import { Separator } from '@/components/ui/separator'
import {
  useDocumentProgress,
  type DocumentProgress,
} from '@/hooks/useDocumentProgress'
import { UPLOAD_ENDPOINT } from '@/lib/constants'
import { enforceLogin, useOidc } from '@/oidc'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { createFileRoute } from '@tanstack/react-router'
import {
  ActivityIcon,
  CheckCircle2Icon,
  CheckCircleIcon,
  ClockIcon,
  FileText,
  FileTextIcon,
  InfoIcon,
  Loader2Icon,
  TrendingUpIcon,
  UploadIcon,
  XCircleIcon,
} from 'lucide-react'
import { useCallback, useState } from 'react'

export const Route = createFileRoute('/documents/')({
  component: DocumentProcessingDashboard,
  beforeLoad: enforceLogin,
})

function DocumentUploadWithProgress() {
  const [selectedFile, setSelectedFile] = useState<File | null>(null)
  const [documentId, setDocumentId] = useState('')
  const [isDragOver, setIsDragOver] = useState(false)
  const queryClient = useQueryClient()
  const { decodedIdToken } = useOidc({
    assert: 'user logged in',
  })

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

function DocumentProcessingDashboard() {
  const [documents, setDocuments] = useState([])
  const [isDragOver, setIsDragOver] = useState(false)
  const [nextId, setNextId] = useState(1)
  const { decodedIdToken } = useOidc({ assert: 'user logged in' })

  const stages = [
    { id: 'uploading', label: 'Uploading', icon: UploadIcon },
    { id: 'extracting', label: 'Extracting', icon: FileTextIcon },
    { id: 'embedding', label: 'Embedding', icon: ActivityIcon },
    { id: 'saving', label: 'Saving', icon: CheckCircle2Icon },
  ]

  const handleFileSelect = (files) => {
    const pdfFiles = Array.from(files).filter(
      (file) => file.type === 'application/pdf',
    )
    const newDocuments = pdfFiles.map((file) => ({
      id: nextId + documents.length + pdfFiles.indexOf(file),
      file,
      status: 'queued', // queued, processing, completed, failed
      progress: 0,
      stage: 'uploading',
    }))

    setDocuments((prev) => [...prev, ...newDocuments])
    setNextId((prev) => prev + pdfFiles.length)
  }

  const handleDrop = (e) => {
    e.preventDefault()
    setIsDragOver(false)
    const files = e.dataTransfer.files
    if (files.length > 0) handleFileSelect(files)
  }

  const removeDocument = (id) => {
    setDocuments((prev) => prev.filter((doc) => doc.id !== id))
  }

  const simulateProcessing = (docId) => {
    setDocuments((prev) =>
      prev.map((doc) =>
        doc.id === docId ? { ...doc, status: 'processing', progress: 0 } : doc,
      ),
    )

    let currentProgress = 0
    const interval = setInterval(() => {
      currentProgress += Math.random() * 15

      if (currentProgress >= 100) {
        currentProgress = 100
        clearInterval(interval)

        setDocuments((prev) =>
          prev.map((doc) =>
            doc.id === docId
              ? { ...doc, status: 'completed', progress: 100 }
              : doc,
          ),
        )

        // Auto-remove after 3 seconds
        setTimeout(() => removeDocument(docId), 3000)
      } else {
        let stage = 'uploading'
        if (currentProgress >= 25 && currentProgress < 50) stage = 'extracting'
        else if (currentProgress >= 50 && currentProgress < 85)
          stage = 'embedding'
        else if (currentProgress >= 85) stage = 'saving'

        setDocuments((prev) =>
          prev.map((doc) =>
            doc.id === docId
              ? { ...doc, progress: Math.min(currentProgress, 100), stage }
              : doc,
          ),
        )
      }
    }, 500)
  }

  const processAll = () => {
    documents
      .filter((doc) => doc.status === 'queued')
      .forEach((doc) => simulateProcessing(doc.id))
  }

  const queuedCount = documents.filter((d) => d.status === 'queued').length
  const processingCount = documents.filter(
    (d) => d.status === 'processing',
  ).length
  const completedCount = documents.filter(
    (d) => d.status === 'completed',
  ).length

  return (
    <div className="min-h-screen p-6">
      <div className="mx-auto">
        {/* Header */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold tracking-tight mb-2">
            Research Repository - {decodedIdToken.name}
          </h1>
          <p className="text-muted-foreground">
            Upload white papers and scientific documents for semantic search
            indexing
          </p>
        </div>

        <div className="grid grid-cols-[1fr_320px] gap-6">
          {/* Main Content Area */}
          <div className="space-y-6">
            {/* Upload Card */}
            {/*<Card>
              <CardHeader>
                <CardTitle>Submit Research Documents</CardTitle>
                <CardDescription>
                  Upload white papers, research articles, or scientific
                  publications
                </CardDescription>
              </CardHeader>
              <CardContent>
                <div
                  className={`border-2 border-dashed rounded-lg p-8 text-center transition-colors cursor-pointer ${
                    isDragOver
                      ? 'border-primary bg-primary/5'
                      : 'border-muted-foreground/25 hover:border-muted-foreground/50'
                  }`}
                  onDragOver={(e) => {
                    e.preventDefault()
                    setIsDragOver(true)
                  }}
                  onDragLeave={(e) => {
                    e.preventDefault()
                    setIsDragOver(false)
                  }}
                  onDrop={handleDrop}
                  onClick={() => document.getElementById('file-input').click()}
                >
                  <input
                    type="file"
                    accept=".pdf"
                    multiple
                    onChange={(e) => handleFileSelect(e.target.files)}
                    className="hidden"
                    id="file-input"
                  />
                  <UploadIcon className="w-10 h-10 mx-auto mb-3 text-muted-foreground" />
                  <h3 className="text-base font-semibold mb-1">
                    {isDragOver
                      ? 'Drop your research papers here'
                      : 'Upload research documents'}
                  </h3>
                  <p className="text-sm text-muted-foreground mb-4">
                    PDF format only, up to 50MB per document
                  </p>
                  <Button variant="secondary" size="sm">
                    <PlusIcon className="w-4 h-4 mr-2" />
                    Select Files
                  </Button>
                </div>
              </CardContent>
            </Card>*/}
            <DocumentUploadWithProgress />

            {/* Queue Card */}
            {/*{documents.length > 0 && (
              <Card>
                <CardHeader>
                  <div className="flex items-center justify-between">
                    <div>
                      <CardTitle>Indexing Queue</CardTitle>
                      <CardDescription>
                        {documents.length} document
                        {documents.length !== 1 ? 's' : ''} in queue
                      </CardDescription>
                    </div>
                    {queuedCount > 0 && (
                      <Button onClick={processAll} size="sm">
                        <ActivityIcon className="w-4 h-4 mr-2" />
                        Index All ({queuedCount})
                      </Button>
                    )}
                  </div>
                </CardHeader>
                <CardContent>
                  <ScrollArea className="h-[400px] pr-4">
                    <div className="space-y-3">
                      {documents.map((doc) => (
                        <Card key={doc.id} className="relative">
                          <CardContent className="p-4">
                            <div className="flex items-start gap-3">
                              <div
                                className={`w-10 h-10 rounded-lg flex items-center justify-center flex-shrink-0 ${
                                  doc.status === 'completed'
                                    ? 'bg-green-100 dark:bg-green-950'
                                    : doc.status === 'processing'
                                      ? 'bg-blue-100 dark:bg-blue-950'
                                      : doc.status === 'failed'
                                        ? 'bg-red-100 dark:bg-red-950'
                                        : 'bg-muted'
                                }`}
                              >
                                {doc.status === 'completed' ? (
                                  <CheckCircle2Icon className="w-5 h-5 text-green-600 dark:text-green-400" />
                                ) : doc.status === 'processing' ? (
                                  <Loader2Icon className="w-5 h-5 text-blue-600 dark:text-blue-400 animate-spin" />
                                ) : doc.status === 'failed' ? (
                                  <XCircleIcon className="w-5 h-5 text-red-600 dark:text-red-400" />
                                ) : (
                                  <FileText className="w-5 h-5 text-muted-foreground" />
                                )}
                              </div>

                              <div className="flex-1 min-w-0 space-y-2">
                                <div className="flex items-start justify-between gap-2">
                                  <div className="min-w-0">
                                    <h4 className="font-medium text-sm truncate">
                                      {doc.file.name}
                                    </h4>
                                    <p className="text-xs text-muted-foreground">
                                      {(doc.file.size / 1024 / 1024).toFixed(2)}{' '}
                                      MB
                                    </p>
                                  </div>
                                  <div className="flex items-center gap-2 flex-shrink-0">
                                    <Badge
                                      variant={
                                        doc.status === 'completed'
                                          ? 'default'
                                          : doc.status === 'processing'
                                            ? 'secondary'
                                            : doc.status === 'failed'
                                              ? 'destructive'
                                              : 'outline'
                                      }
                                      className="text-xs"
                                    >
                                      {doc.status}
                                    </Badge>
                                    {doc.status === 'queued' && (
                                      <Button
                                        variant="ghost"
                                        size="icon"
                                        className="h-7 w-7"
                                        onClick={() => removeDocument(doc.id)}
                                      >
                                        <Trash2Icon className="w-3 h-3" />
                                      </Button>
                                    )}
                                  </div>
                                </div>

                                {doc.status === 'processing' && (
                                  <div className="space-y-2">
                                    <div className="flex justify-between text-xs">
                                      <span className="text-muted-foreground">
                                        {doc.stage}
                                      </span>
                                      <span className="font-medium">
                                        {Math.round(doc.progress)}%
                                      </span>
                                    </div>
                                    <Progress
                                      value={doc.progress}
                                      className="h-1.5"
                                    />
                                  </div>
                                )}

                                {doc.status === 'queued' && (
                                  <Button
                                    variant="outline"
                                    size="sm"
                                    className="w-full"
                                    onClick={() => simulateProcessing(doc.id)}
                                  >
                                    Index Now
                                  </Button>
                                )}
                              </div>
                            </div>
                          </CardContent>
                        </Card>
                      ))}
                    </div>
                  </ScrollArea>
                </CardContent>
              </Card>
            )}*/}
          </div>

          {/* Sidebar */}
          <div className="space-y-6">
            {/* Stats Card */}
            <Card>
              <CardHeader>
                <CardTitle className="text-base">Current Session</CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <ClockIcon className="w-4 h-4 text-muted-foreground" />
                    <span className="text-sm text-muted-foreground">
                      Queued
                    </span>
                  </div>
                  <span className="text-2xl font-bold">{queuedCount}</span>
                </div>
                <Separator />
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <ActivityIcon className="w-4 h-4 text-muted-foreground" />
                    <span className="text-sm text-muted-foreground">
                      Indexing
                    </span>
                  </div>
                  <span className="text-2xl font-bold text-blue-600">
                    {processingCount}
                  </span>
                </div>
                <Separator />
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <TrendingUpIcon className="w-4 h-4 text-muted-foreground" />
                    <span className="text-sm text-muted-foreground">
                      Indexed
                    </span>
                  </div>
                  <span className="text-2xl font-bold text-green-600">
                    {completedCount}
                  </span>
                </div>
              </CardContent>
            </Card>

            {/* Info Card */}
            <Card>
              <CardHeader>
                <CardTitle className="text-base flex items-center gap-2">
                  <InfoIcon className="w-4 h-4" />
                  How Indexing Works
                </CardTitle>
              </CardHeader>
              <CardContent className="space-y-3 text-sm text-muted-foreground">
                <p>
                  Your research documents are processed through several stages:
                </p>
                <Separator />
                <div className="space-y-2">
                  <div className="flex items-start gap-2">
                    <div className="w-5 h-5 rounded-full bg-primary/10 flex items-center justify-center flex-shrink-0 mt-0.5">
                      <span className="text-xs font-bold text-primary">1</span>
                    </div>
                    <div>
                      <p className="font-medium text-foreground">
                        Text Extraction
                      </p>
                      <p className="text-xs">Extract content from PDF</p>
                    </div>
                  </div>
                  <div className="flex items-start gap-2">
                    <div className="w-5 h-5 rounded-full bg-primary/10 flex items-center justify-center flex-shrink-0 mt-0.5">
                      <span className="text-xs font-bold text-primary">2</span>
                    </div>
                    <div>
                      <p className="font-medium text-foreground">
                        Semantic Embeddings
                      </p>
                      <p className="text-xs">Generate vector representations</p>
                    </div>
                  </div>
                  <div className="flex items-start gap-2">
                    <div className="w-5 h-5 rounded-full bg-primary/10 flex items-center justify-center flex-shrink-0 mt-0.5">
                      <span className="text-xs font-bold text-primary">3</span>
                    </div>
                    <div>
                      <p className="font-medium text-foreground">
                        Vector Database
                      </p>
                      <p className="text-xs">Index for semantic search</p>
                    </div>
                  </div>
                </div>
              </CardContent>
            </Card>

            {/* Tips Card */}
            <Card className="border-amber-200 bg-amber-50 dark:bg-amber-950 dark:border-amber-900">
              <CardHeader>
                <CardTitle className="text-base text-amber-900 dark:text-amber-100">
                  Submission Guidelines
                </CardTitle>
              </CardHeader>
              <CardContent className="text-sm text-amber-800 dark:text-amber-200 space-y-1">
                <p>• Submit text-based PDFs only</p>
                <p>• Scanned images won't index well</p>
                <p>• Maximum 50MB per document</p>
                <p>• Add metadata for better discovery</p>
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    </div>
  )
}
