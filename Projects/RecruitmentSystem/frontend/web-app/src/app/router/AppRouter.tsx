import { Route, Routes } from 'react-router-dom'
import { AdminDashboardPage } from '../../features/admin/AdminDashboardPage'
import { AdminAiProviderPage } from '../../features/admin/AdminAiProviderPage'
import { AdminCatalogPage } from '../../features/admin/AdminCatalogPage'
import { AdminDeliveryLogsPage } from '../../features/admin/AdminDeliveryLogsPage'
import { AdminNotificationsPage } from '../../features/admin/AdminNotificationsPage'
import { AdminNotificationTemplatesPage } from '../../features/admin/AdminNotificationTemplatesPage'
import { AdminUsersPage } from '../../features/admin/AdminUsersPage'
import { AdminCompaniesPage } from '../../features/admin/AdminCompaniesPage'
import { AdminApplicationsPage } from '../../features/admin/AdminApplicationsPage'
import { AdminApplicationDetailPage } from '../../features/admin/AdminApplicationDetailPage'
import { LoginPage } from '../../features/auth/pages/LoginPage'
import { RegisterPage } from '../../features/auth/pages/RegisterPage'
import { ForgotPasswordPage } from '../../features/auth/pages/ForgotPasswordPage'
import { ResetPasswordPage } from '../../features/auth/pages/ResetPasswordPage'
import { VerifyEmailPage } from '../../features/auth/pages/VerifyEmailPage'
import { CompaniesPage } from '../../features/companies/CompaniesPage'
import { CompanyDetailsPage } from '../../features/companies/CompanyDetailsPage'
import { EmployerDashboardPage } from '../../features/employer/EmployerDashboardPage'
import { EmployerCompanyPage } from '../../features/employer/EmployerCompanyPage'
import { EmployerJobDetailPage } from '../../features/employer/EmployerJobDetailPage'
import { EmployerJobFormPage } from '../../features/employer/EmployerJobFormPage'
import { EmployerJobsPage } from '../../features/employer/EmployerJobsPage'
import { EmployerApplicationDetailPage } from '../../features/employer/EmployerApplicationDetailPage'
import { EmployerApplicationsPage } from '../../features/employer/EmployerApplicationsPage'
import { HomePage } from '../../features/home/HomePage'
import { NotFoundPage } from '../../features/home/NotFoundPage'
import { JobDetailsPage } from '../../features/jobs/JobDetailsPage'
import { JobsPage } from '../../features/jobs/JobsPage'
import { CandidateDashboardPage } from '../../features/candidate/CandidateDashboardPage'
import { ApplicationDetailPage } from '../../features/applications/ApplicationDetailPage'
import { ApplicationListPage } from '../../features/applications/ApplicationListPage'
import { ProfilePage } from '../../features/profile/ProfilePage'
import { ResumePage } from '../../features/resumes/ResumePage'
import { NotificationPage } from '../../features/notifications/NotificationPage'
import { AiCareerPage } from '../../features/ai-career/AiCareerPage'
import { ProtectedRoute } from '../guards/ProtectedRoute'
import { RoleGuard } from '../guards/RoleGuard'
import { AdminLayout } from '../layouts/AdminLayout'
import { CandidateLayout } from '../layouts/CandidateLayout'
import { EmployerLayout } from '../layouts/EmployerLayout'
import { MainLayout } from '../layouts/MainLayout'
import { PublicLayout } from '../layouts/PublicLayout'

export function AppRouter() {
  return (
    <Routes>
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
        <Route element={<ProtectedRoute><RoleGuard roles={['CANDIDATE']}><CandidateLayout /></RoleGuard></ProtectedRoute>}>
          <Route path="candidate" element={<CandidateDashboardPage />} />
          <Route path="candidate/profile" element={<ProfilePage />} />
          <Route path="candidate/resumes" element={<ResumePage />} />
          <Route path="candidate/applications" element={<ApplicationListPage />} />
          <Route path="candidate/applications/:applicationId" element={<ApplicationDetailPage />} />
          <Route path="candidate/notifications" element={<NotificationPage />} />
          <Route path="candidate/ai-career" element={<AiCareerPage />} />
        </Route>
        <Route element={<ProtectedRoute><RoleGuard roles={['EMPLOYER']}><EmployerLayout /></RoleGuard></ProtectedRoute>}>
          <Route path="employer" element={<EmployerDashboardPage />} />
          <Route path="employer/company" element={<EmployerCompanyPage />} />
          <Route path="employer/jobs" element={<EmployerJobsPage />} />
          <Route path="employer/jobs/new" element={<EmployerJobFormPage />} />
          <Route path="employer/jobs/:jobId" element={<EmployerJobDetailPage />} />
          <Route path="employer/jobs/:jobId/edit" element={<EmployerJobFormPage />} />
          <Route path="employer/applications" element={<EmployerApplicationsPage />} />
          <Route path="employer/applications/:applicationId" element={<EmployerApplicationDetailPage />} />
        </Route>
        <Route element={<ProtectedRoute><RoleGuard roles={['ADMIN']}><AdminLayout /></RoleGuard></ProtectedRoute>}>
          <Route path="admin" element={<AdminDashboardPage />} />
          <Route path="admin/users" element={<AdminUsersPage />} />
          <Route path="admin/companies" element={<AdminCompaniesPage />} />
          <Route path="admin/applications" element={<AdminApplicationsPage />} />
          <Route path="admin/applications/:applicationId" element={<AdminApplicationDetailPage />} />
          <Route path="admin/catalog/categories" element={<AdminCatalogPage kind="categories" />} />
          <Route path="admin/catalog/skills" element={<AdminCatalogPage kind="skills" />} />
          <Route path="admin/catalog/benefits" element={<AdminCatalogPage kind="benefits" />} />
          <Route path="admin/notifications" element={<AdminNotificationsPage />} />
          <Route path="admin/notification-templates" element={<AdminNotificationTemplatesPage />} />
          <Route path="admin/notification-delivery-logs" element={<AdminDeliveryLogsPage />} />
          <Route path="admin/ai-provider" element={<AdminAiProviderPage />} />
        </Route>
        <Route path="*" element={<NotFoundPage />} />
      </Route>
    </Routes>
  )
}
