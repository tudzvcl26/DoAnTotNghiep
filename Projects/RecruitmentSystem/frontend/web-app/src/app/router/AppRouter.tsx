import { lazy, Suspense, type ComponentType } from 'react'
import { Navigate, Route, Routes } from 'react-router-dom'
import { LoadingScreen } from '../../components/feedback/LoadingScreen'
import { ProtectedRoute } from '../guards/ProtectedRoute'
import { RoleGuard } from '../guards/RoleGuard'
import { AdminLayout } from '../layouts/AdminLayout'
import { CandidateLayout } from '../layouts/CandidateLayout'
import { EmployerLayout } from '../layouts/EmployerLayout'
import { MainLayout } from '../layouts/MainLayout'
import { PublicLayout } from '../layouts/PublicLayout'

const lazyNamed = <T extends Record<string, unknown>>(loader: () => Promise<T>, name: keyof T) =>
  lazy(async () => ({ default: (await loader())[name] as ComponentType }))

const LoginPage = lazyNamed(() => import('../../features/auth/pages/LoginPage'), 'LoginPage')
const RegisterPage = lazyNamed(() => import('../../features/auth/pages/RegisterPage'), 'RegisterPage')
const ForgotPasswordPage = lazyNamed(() => import('../../features/auth/pages/ForgotPasswordPage'), 'ForgotPasswordPage')
const ResetPasswordPage = lazyNamed(() => import('../../features/auth/pages/ResetPasswordPage'), 'ResetPasswordPage')
const VerifyEmailPage = lazyNamed(() => import('../../features/auth/pages/VerifyEmailPage'), 'VerifyEmailPage')
const CompaniesPage = lazyNamed(() => import('../../features/companies/CompaniesPage'), 'CompaniesPage')
const CompanyDetailsPage = lazyNamed(() => import('../../features/companies/CompanyDetailsPage'), 'CompanyDetailsPage')
const HomePage = lazyNamed(() => import('../../features/home/HomePage'), 'HomePage')
const NotFoundPage = lazyNamed(() => import('../../features/home/NotFoundPage'), 'NotFoundPage')
const JobDetailsPage = lazyNamed(() => import('../../features/jobs/JobDetailsPage'), 'JobDetailsPage')
const JobsPage = lazyNamed(() => import('../../features/jobs/JobsPage'), 'JobsPage')
const CandidateDashboardPage = lazyNamed(() => import('../../features/candidate/CandidateDashboardPage'), 'CandidateDashboardPage')
const ApplicationDetailPage = lazyNamed(() => import('../../features/applications/ApplicationDetailPage'), 'ApplicationDetailPage')
const ApplicationListPage = lazyNamed(() => import('../../features/applications/ApplicationListPage'), 'ApplicationListPage')
const ProfilePage = lazyNamed(() => import('../../features/profile/ProfilePage'), 'ProfilePage')
const ResumePage = lazyNamed(() => import('../../features/resumes/ResumePage'), 'ResumePage')
const NotificationPage = lazyNamed(() => import('../../features/notifications/NotificationPage'), 'NotificationPage')
const AiCareerPage = lazyNamed(() => import('../../features/ai-career/AiCareerPage'), 'AiCareerPage')
const CvListPage = lazyNamed(() => import('../../features/cv-builder/CvListPage'), 'CvListPage')
const CvTemplatesPage = lazyNamed(() => import('../../features/cv-builder/CvTemplatesPage'), 'CvTemplatesPage')
const CvTemplatePreviewPage = lazyNamed(() => import('../../features/cv-builder/CvTemplatePreviewPage'), 'CvTemplatePreviewPage')
const CvEditorPage = lazyNamed(() => import('../../features/cv-builder/CvEditorPage'), 'CvEditorPage')
const CvPreviewPage = lazyNamed(() => import('../../features/cv-builder/CvPreviewPage'), 'CvPreviewPage')
const EmployerDashboardPage = lazyNamed(() => import('../../features/employer/EmployerDashboardPage'), 'EmployerDashboardPage')
const EmployerCompanyPage = lazyNamed(() => import('../../features/employer/EmployerCompanyPage'), 'EmployerCompanyPage')
const EmployerJobDetailPage = lazyNamed(() => import('../../features/employer/EmployerJobDetailPage'), 'EmployerJobDetailPage')
const EmployerJobFormPage = lazyNamed(() => import('../../features/employer/EmployerJobFormPage'), 'EmployerJobFormPage')
const EmployerJobsPage = lazyNamed(() => import('../../features/employer/EmployerJobsPage'), 'EmployerJobsPage')
const EmployerApplicationDetailPage = lazyNamed(() => import('../../features/employer/EmployerApplicationDetailPage'), 'EmployerApplicationDetailPage')
const EmployerApplicationsPage = lazyNamed(() => import('../../features/employer/EmployerApplicationsPage'), 'EmployerApplicationsPage')
const AdminDashboardPage = lazyNamed(() => import('../../features/admin/AdminDashboardPage'), 'AdminDashboardPage')
const AdminAiProviderPage = lazyNamed(() => import('../../features/admin/AdminAiProviderPage'), 'AdminAiProviderPage')
const AdminCatalogPage = lazy(() => import('../../features/admin/AdminCatalogPage').then((module) => ({ default: module.AdminCatalogPage })))
const AdminDeliveryLogsPage = lazyNamed(() => import('../../features/admin/AdminDeliveryLogsPage'), 'AdminDeliveryLogsPage')
const AdminNotificationsPage = lazyNamed(() => import('../../features/admin/AdminNotificationsPage'), 'AdminNotificationsPage')
const AdminNotificationTemplatesPage = lazyNamed(() => import('../../features/admin/AdminNotificationTemplatesPage'), 'AdminNotificationTemplatesPage')
const AdminUsersPage = lazyNamed(() => import('../../features/admin/AdminUsersPage'), 'AdminUsersPage')
const AdminCompaniesPage = lazyNamed(() => import('../../features/admin/AdminCompaniesPage'), 'AdminCompaniesPage')
const AdminApplicationsPage = lazyNamed(() => import('../../features/admin/AdminApplicationsPage'), 'AdminApplicationsPage')
const AdminApplicationDetailPage = lazyNamed(() => import('../../features/admin/AdminApplicationDetailPage'), 'AdminApplicationDetailPage')
const AdminJobsPage = lazyNamed(() => import('../../features/admin/AdminJobsPage'), 'AdminJobsPage')

export function AppRouter() {
  return (
    <Suspense fallback={<LoadingScreen />}><Routes>
      <Route element={<MainLayout />}>
        <Route element={<PublicLayout />}>
          <Route index element={<HomePage />} />
          <Route path="jobs" element={<JobsPage />} />
          <Route path="jobs/:jobId" element={<JobDetailsPage />} />
          <Route path="companies" element={<CompaniesPage />} />
          <Route path="companies/:companyId" element={<CompanyDetailsPage />} />
          <Route path="login" element={<LoginPage />} />
          <Route path="register" element={<RegisterPage />} />
          <Route path="forgot-password" element={<ForgotPasswordPage />} />
          <Route path="reset-password" element={<ResetPasswordPage />} />
          <Route path="verify-email" element={<VerifyEmailPage />} />
        </Route>
        <Route path="*" element={<NotFoundPage />} />
      </Route>
      <Route element={<ProtectedRoute><RoleGuard roles={['CANDIDATE']}><CandidateLayout /></RoleGuard></ProtectedRoute>}>
        <Route path="candidate" element={<CandidateDashboardPage />} />
        <Route path="candidate/dashboard" element={<Navigate to="/candidate" replace />} />
        <Route path="candidate/profile" element={<ProfilePage />} />
        <Route path="candidate/resumes" element={<ResumePage />} />
        <Route path="candidate/applications" element={<ApplicationListPage />} />
        <Route path="candidate/applications/:applicationId" element={<ApplicationDetailPage />} />
        <Route path="candidate/notifications" element={<NotificationPage />} />
        <Route path="candidate/ai-career" element={<AiCareerPage />} />
        <Route path="cv" element={<CvListPage />} />
        <Route path="cv/templates" element={<CvTemplatesPage />} />
        <Route path="cv/templates/:templateId" element={<CvTemplatePreviewPage />} />
        <Route path="cv/new" element={<CvEditorPage />} />
        <Route path="cv/:id/edit" element={<CvEditorPage />} />
        <Route path="cv/:id/preview" element={<CvPreviewPage />} />
      </Route>
      <Route element={<ProtectedRoute><RoleGuard roles={['EMPLOYER']}><EmployerLayout /></RoleGuard></ProtectedRoute>}>
        <Route path="employer" element={<EmployerDashboardPage />} />
        <Route path="employer/dashboard" element={<Navigate to="/employer" replace />} />
        <Route path="employer/company" element={<EmployerCompanyPage />} />
        <Route path="employer/jobs" element={<EmployerJobsPage />} />
        <Route path="employer/jobs/new" element={<EmployerJobFormPage />} />
        <Route path="employer/jobs/:jobId" element={<EmployerJobDetailPage />} />
        <Route path="employer/jobs/:jobId/edit" element={<EmployerJobFormPage />} />
        <Route path="employer/applications" element={<EmployerApplicationsPage />} />
        <Route path="employer/applications/:applicationId" element={<EmployerApplicationDetailPage />} />
        <Route path="employer/notifications" element={<NotificationPage />} />
      </Route>
      <Route element={<ProtectedRoute><RoleGuard roles={['ADMIN']}><AdminLayout /></RoleGuard></ProtectedRoute>}>
        <Route path="admin" element={<AdminDashboardPage />} />
        <Route path="admin/dashboard" element={<Navigate to="/admin" replace />} />
        <Route path="admin/users" element={<AdminUsersPage />} />
        <Route path="admin/companies" element={<AdminCompaniesPage />} />
        <Route path="admin/jobs" element={<AdminJobsPage />} />
        <Route path="admin/applications" element={<AdminApplicationsPage />} />
        <Route path="admin/applications/:applicationId" element={<AdminApplicationDetailPage />} />
        <Route path="admin/catalog" element={<Navigate to="/admin/catalog/categories" replace />} />
        <Route path="admin/catalog/categories" element={<AdminCatalogPage kind="categories" />} />
        <Route path="admin/catalog/skills" element={<AdminCatalogPage kind="skills" />} />
        <Route path="admin/catalog/benefits" element={<AdminCatalogPage kind="benefits" />} />
        <Route path="admin/notifications" element={<AdminNotificationsPage />} />
        <Route path="admin/templates" element={<Navigate to="/admin/notification-templates" replace />} />
        <Route path="admin/notification-templates" element={<AdminNotificationTemplatesPage />} />
        <Route path="admin/delivery-logs" element={<Navigate to="/admin/notification-delivery-logs" replace />} />
        <Route path="admin/notification-delivery-logs" element={<AdminDeliveryLogsPage />} />
        <Route path="admin/ai" element={<Navigate to="/admin/ai-provider" replace />} />
        <Route path="admin/ai-provider" element={<AdminAiProviderPage />} />
      </Route>
    </Routes></Suspense>
  )
}
