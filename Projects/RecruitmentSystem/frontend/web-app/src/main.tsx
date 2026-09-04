import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import App from './App.tsx'
import { AppProviders } from './app/providers/AppProviders.tsx'
import { AppErrorBoundary } from './app/providers/AppErrorBoundary.tsx'
import './styles/tokens.css'
import './styles/global.css'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <AppErrorBoundary><AppProviders>
      <App />
    </AppProviders></AppErrorBoundary>
  </StrictMode>,
)
