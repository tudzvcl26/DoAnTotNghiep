import { apiClient } from '../../lib/api/client'
import type { ApiResponse, PageResponse } from '../../types/api/common'
import type {
  AiResume, AiTask, AssistantResponse, CandidateAssistantTask, CareerChatResponse, InterviewPreparation,
  MatchExplanation, MatchingResult, ResumeAnalysis,
  JobRecommendation,
} from './ai-career.types'

const AI = '/api/v1/ai'

export async function getAiResumes(): Promise<PageResponse<AiResume>> {
  const response = await apiClient.get<ApiResponse<PageResponse<AiResume>>>(`${AI}/resumes`, {
    params: { page: 0, size: 100, sort: 'createdAt,desc' },
  })
  return response.data.data
}

export async function uploadAiResume(file: File): Promise<AiResume> {
  const formData = new FormData()
  formData.append('file', file, file.name)
  const response = await apiClient.post<ApiResponse<AiResume>>(`${AI}/resumes/upload`, formData)
  return response.data.data
}

export async function deleteAiResume(resumeId: string): Promise<void> {
  await apiClient.delete(`${AI}/resumes/${resumeId}`)
}

export async function analyzeAiResume(resumeId: string): Promise<ResumeAnalysis> {
  const response = await apiClient.post<ApiResponse<ResumeAnalysis>>(`${AI}/resumes/${resumeId}/analyze`)
  return response.data.data
}

export async function getAiResumeAnalysis(resumeId: string): Promise<ResumeAnalysis> {
  const response = await apiClient.get<ApiResponse<ResumeAnalysis>>(`${AI}/resumes/${resumeId}/analysis`)
  return response.data.data
}

export async function runCandidateAssistant(task: CandidateAssistantTask, resumeId: string, matchId?: string): Promise<AssistantResponse> {
  const response = await apiClient.post<ApiResponse<AssistantResponse>>(`${AI}/assistant/candidate`, {
    task, resumeId, ...(matchId ? { matchId } : {}),
  })
  return response.data.data
}

export async function chatWithCareerCompanion(payload: {
  message: string
  resumeId?: string
  jobId?: string
}): Promise<CareerChatResponse> {
  const response = await apiClient.post<ApiResponse<CareerChatResponse>>(`${AI}/career/chat`, payload)
  return response.data.data
}

export async function matchJob(jobId: string, resumeId: string): Promise<MatchingResult> {
  const response = await apiClient.post<ApiResponse<MatchingResult>>(`${AI}/matching/jobs/${jobId}/resumes/${resumeId}`)
  return response.data.data
}

export async function getResumeMatches(resumeId: string): Promise<PageResponse<MatchingResult>> {
  const response = await apiClient.get<ApiResponse<PageResponse<MatchingResult>>>(`${AI}/matching/resume/${resumeId}`, {
    params: { page: 0, size: 20, sort: 'updatedAt,desc' },
  })
  return response.data.data
}

export async function generateMatchExplanation(matchId: string): Promise<MatchExplanation> {
  const response = await apiClient.post<ApiResponse<MatchExplanation>>(`${AI}/matching/${matchId}/explanation`)
  return response.data.data
}

export async function queueMatchExplanation(matchId: string): Promise<AiTask> {
  const response = await apiClient.post<ApiResponse<AiTask>>(`${AI}/matching/${matchId}/explanation/tasks`, null, { timeout: 30_000 })
  return response.data.data
}

export async function getLatestExplanationTask(matchId: string): Promise<AiTask | null> {
  const response = await apiClient.get<ApiResponse<AiTask | null>>(`${AI}/matching/${matchId}/explanation/tasks/latest`)
  return response.data.data
}

export async function getMatchExplanation(matchId: string): Promise<MatchExplanation> {
  const response = await apiClient.get<ApiResponse<MatchExplanation>>(`${AI}/matching/${matchId}/explanation`)
  return response.data.data
}

export async function generateInterviewPreparation(matchId: string): Promise<InterviewPreparation> {
  const response = await apiClient.post<ApiResponse<InterviewPreparation>>(`${AI}/matching/${matchId}/interview`)
  return response.data.data
}

export async function queueInterviewPreparation(matchId: string): Promise<AiTask> {
  const response = await apiClient.post<ApiResponse<AiTask>>(`${AI}/matching/${matchId}/interview/tasks`, null, { timeout: 30_000 })
  return response.data.data
}

export async function getLatestInterviewTask(matchId: string): Promise<AiTask | null> {
  const response = await apiClient.get<ApiResponse<AiTask | null>>(`${AI}/matching/${matchId}/interview/tasks/latest`)
  return response.data.data
}

export async function getInterviewPreparation(matchId: string): Promise<InterviewPreparation> {
  const response = await apiClient.get<ApiResponse<InterviewPreparation>>(`${AI}/matching/${matchId}/interview`)
  return response.data.data
}

export async function getAiTasks(): Promise<PageResponse<AiTask>> {
  const response = await apiClient.get<ApiResponse<PageResponse<AiTask>>>(`${AI}/tasks`, {
    params: { page: 0, size: 20, sort: 'createdAt,desc' },
  })
  return response.data.data
}

export async function getJobRecommendations(resumeId: string): Promise<PageResponse<JobRecommendation>> {
  const response = await apiClient.get<ApiResponse<PageResponse<JobRecommendation>>>(`${AI}/recommendations/jobs`, {
    params: { resumeId, page: 0, size: 10, sort: 'overallScore', direction: 'desc' },
  })
  return response.data.data
}

export async function refreshJobRecommendations(resumeId: string): Promise<AiTask> {
  const response = await apiClient.post<ApiResponse<AiTask>>(`${AI}/recommendations/jobs/refresh`, null, { params: { resumeId } })
  return response.data.data
}
