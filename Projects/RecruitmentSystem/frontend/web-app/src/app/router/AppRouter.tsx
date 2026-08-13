import { Route, Routes } from 'react-router-dom'
import { AdminDashboardPage } from '../../features/admin/AdminDashboardPage'
import { LoginPage } from '../../features/auth/pages/LoginPage'
import { RegisterPage } from '../../features/auth/pages/RegisterPage'
import { CompaniesPage } from '../../features/companies/CompaniesPage'
import { CompanyDetailsPage } from '../../features/companies/CompanyDetailsPage'
import { EmployerDashboardPage } from '../../features/employer/EmployerDashboardPage'
import { HomePage } from '../../features/home/HomePage'
import { NotFoundPage } from '../../features/home/NotFoundPage'
import { JobDetailsPage } from '../../features/jobs/JobDetailsPage'
import { JobsPage } from '../../features/jobs/JobsPage'
import { CandidateDashboardPage } from '../../features/candidate/CandidateDashboardPage'
import { ApplicationDetailPage } from '../../features/applications/ApplicationDetailPage'
import { ApplicationListPage } from '../../features/applications/ApplicationListPage'
import { ProfilePage } from '../../features/profile/ProfilePage'
import { ResumePage } from '../../features/resumes/ResumePage'
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
        </Route>
        <Route element={<ProtectedRoute><RoleGuard roles={['CANDIDATE']}><CandidateLayout /></RoleGuard></ProtectedRoute>}>
          <Route path="candidate" element={<CandidateDashboardPage />} />
          <Route path="candidate/profile" element={<ProfilePage />} />
          <Route path="candidate/resumes" element={<ResumePage />} />
          <Route path="candidate/applications" element={<ApplicationListPage />} />
          <Route path="candidate/applications/:applicationId" element={<ApplicationDetailPage />} />
        </Route>
        <Route element={<ProtectedRoute><RoleGuard roles={['EMPLOYER', 'ADMIN']}><EmployerLayout /></RoleGuard></ProtectedRoute>}>
          <Route path="employer" element={<EmployerDashboardPage />} />
        </Route>
        <Route element={<ProtectedRoute><RoleGuard roles={['ADMIN']}><AdminLayout /></RoleGuard></ProtectedRoute>}>
          <Route path="admin" element={<AdminDashboardPage />} />
        </Route>
        <Route path="*" element={<NotFoundPage />} />
      </Route>
    </Routes>
  )
}
