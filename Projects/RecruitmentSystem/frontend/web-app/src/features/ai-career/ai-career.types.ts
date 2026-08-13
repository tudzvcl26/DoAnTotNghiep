export type JsonValue = string | number | boolean | null | JsonValue[] | { [key: string]: JsonValue }

export type AiResumeStatus = 'READY' | 'ANALYZED' | 'FAILED'

export type AiResume = {
  id: string
  ownerUserId: string
  originalFilename: string
  contentType: string
  fileSize: number
  status: AiResumeStatus
  extractionDurationMs: number
  uploadTime: string
  createdAt: string
  updatedAt: string
}

export type ScoreDimension = { score: number; maximum: number; rationale: string }
export type AnalysisSkill = { name: string; category: string }
export type AnalysisKeyword = { keyword: string; frequency: number }

export type ResumeAnalysis = {
  id: string
  resumeDocumentId: string
  aiTaskId: string
  providerName: string
  modelName: string
  structuredData: JsonValue
  qualityScore: number
  scoreBreakdown: Record<string, ScoreDimension>
  skills: AnalysisSkill[]
  keywords: AnalysisKeyword[]
  analysisDurationMs: number
  correlationId: string
  createdAt: string
  updatedAt: string
}

export const candidateAssistantTasks = [
  'CAREER_ROADMAP',
  'LEARNING_ROADMAP',
  'SKILL_ROADMAP',
  'CERTIFICATE_RECOMMENDATION',
  'PORTFOLIO_RECOMMENDATION',
  'JOB_SEARCH_ADVICE',
  'RESUME_IMPROVEMENT',
] as const

export type CandidateAssistantTask = typeof candidateAssistantTasks[number]

export type AssistantResponse = {
  sessionId: string
  responseId: string
  assistantType: string
  taskType: CandidateAssistantTask
  jobId: string | null
  resumeId: string
  matchId: string | null
  response: JsonValue
  providerName: string
  modelName: string
  generationDurationMs: number
  correlationId: string
  createdAt: string
}

export type MatchBreakdown = { dimension: string; maximumScore: number; actualScore: number; reason: string }

export type MatchingResult = {
  id: string
  jobId: string
  resumeId: string
  overallScore: number
  scoreBreakdown: MatchBreakdown[]
  matchedSkills: string[]
  missingSkills: string[]
  strengths: string[]
  weaknesses: string[]
  recommendations: string[]
  gapAnalysis: string[]
  matchingDurationMs: number
  correlationId: string
  createdAt: string
  updatedAt: string
}

export type MatchExplanation = {
  id: string
  matchId: string
  explanation: JsonValue
  providerName: string
  modelName: string
  generationDurationMs: number
  correlationId: string
  createdAt: string
}

export type InterviewPreparation = {
  id: string
  matchId: string
  questionSet: JsonValue
  providerName: string
  modelName: string
  generationDurationMs: number
  correlationId: string
  createdAt: string
}

export type AiTaskStatus = 'PENDING' | 'RUNNING' | 'COMPLETED' | 'PARTIAL' | 'FAILED' | 'CANCELLED'

export type AiTask = {
  id: string
  taskType: string
  status: AiTaskStatus
  subjectType: string | null
  subjectId: string | null
  progress: number | null
  providerName: string | null
  modelName: string | null
  errorCode: string | null
  errorMessage: string | null
  retryable: boolean | null
  startedAt: string | null
  completedAt: string | null
  createdAt: string
}
